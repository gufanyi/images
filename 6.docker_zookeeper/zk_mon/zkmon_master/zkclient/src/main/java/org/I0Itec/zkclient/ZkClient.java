/*      */ package org.I0Itec.zkclient;
/*      */ 
/*      */ import java.io.IOException;
/*      */ import java.io.OutputStream;
/*      */ import java.util.Date;
/*      */ import java.util.List;
/*      */ import java.util.Map;
/*      */ import java.util.Map.Entry;
/*      */ import java.util.Set;
/*      */ import java.util.concurrent.Callable;
/*      */ import java.util.concurrent.ConcurrentHashMap;
/*      */ import java.util.concurrent.CopyOnWriteArraySet;
/*      */ import java.util.concurrent.TimeUnit;
/*      */ import java.util.concurrent.locks.Condition;
/*      */ import org.I0Itec.zkclient.exception.ZkBadVersionException;
/*      */ import org.I0Itec.zkclient.exception.ZkException;
/*      */ import org.I0Itec.zkclient.exception.ZkInterruptedException;
/*      */ import org.I0Itec.zkclient.exception.ZkNoNodeException;
/*      */ import org.I0Itec.zkclient.exception.ZkNodeExistsException;
/*      */ import org.I0Itec.zkclient.exception.ZkTimeoutException;
/*      */ import org.I0Itec.zkclient.serialize.SerializableSerializer;
/*      */ import org.I0Itec.zkclient.serialize.ZkSerializer;
/*      */ import org.I0Itec.zkclient.util.ZkPathUtil;
/*      */ import org.apache.log4j.Logger;
/*      */ import org.apache.zookeeper.AsyncCallback.StringCallback;
/*      */ import org.apache.zookeeper.AsyncCallback.VoidCallback;
/*      */ import org.apache.zookeeper.CreateMode;
/*      */ import org.apache.zookeeper.KeeperException;
/*      */ import org.apache.zookeeper.KeeperException.ConnectionLossException;
/*      */ import org.apache.zookeeper.KeeperException.SessionExpiredException;
/*      */ import org.apache.zookeeper.WatchedEvent;
/*      */ import org.apache.zookeeper.Watcher;
/*      */ import org.apache.zookeeper.Watcher.Event.EventType;
/*      */ import org.apache.zookeeper.Watcher.Event.KeeperState;
/*      */ import org.apache.zookeeper.ZooDefs.Ids;
/*      */ import org.apache.zookeeper.ZooKeeper.States;
/*      */ import org.apache.zookeeper.data.ACL;
/*      */ import org.apache.zookeeper.data.Stat;
/*      */ 
/*      */ public class ZkClient
/*      */   implements Watcher
/*      */ {
/*   51 */   private static final Logger LOG = Logger.getLogger(ZkClient.class);
/*      */   protected IZkConnection _connection;
/*   54 */   private final Map<String, Set<IZkChildListener>> _childListener = new ConcurrentHashMap();
/*   55 */   private final ConcurrentHashMap<String, Set<IZkDataListener>> _dataListener = new ConcurrentHashMap();
/*   56 */   private final Set<IZkStateListener> _stateListener = new CopyOnWriteArraySet();
/*      */   private Watcher.Event.KeeperState _currentState;
/*   58 */   private final ZkLock _zkEventLock = new ZkLock();
/*      */   private boolean _shutdownTriggered;
/*      */   private ZkEventThread _eventThread;
/*      */   private Thread _zookeeperEventThread;
/*      */   private ZkSerializer _zkSerializer;
/*      */ 
/*      */   public ZkClient(String serverstring)
/*      */   {
/*   66 */     this(serverstring, 2147483647);
/*      */   }
/*      */ 
/*      */   public ZkClient(String zkServers, int connectionTimeout) {
/*   70 */     this(new ZkConnection(zkServers), connectionTimeout);
/*      */   }
/*      */ 
/*      */   public ZkClient(String zkServers, int sessionTimeout, int connectionTimeout) {
/*   74 */     this(new ZkConnection(zkServers, sessionTimeout), connectionTimeout);
/*      */   }
/*      */ 
/*      */   public ZkClient(String zkServers, int sessionTimeout, int connectionTimeout, ZkSerializer zkSerializer) {
/*   78 */     this(new ZkConnection(zkServers, sessionTimeout), connectionTimeout, zkSerializer);
/*      */   }
/*      */ 
/*      */   public ZkClient(IZkConnection connection) {
/*   82 */     this(connection, 2147483647);
/*      */   }
/*      */ 
/*      */   public ZkClient(IZkConnection connection, int connectionTimeout) {
/*   86 */     this(connection, connectionTimeout, new SerializableSerializer());
/*      */   }
/*      */ 
/*      */   public ZkClient(IZkConnection zkConnection, int connectionTimeout, ZkSerializer zkSerializer) {
/*   90 */     this._connection = zkConnection;
/*   91 */     this._zkSerializer = zkSerializer;
/*   92 */     connect(connectionTimeout, this);
/*      */   }
/*      */ 
/*      */   public void setZkSerializer(ZkSerializer zkSerializer) {
/*   96 */     this._zkSerializer = zkSerializer;
/*      */   }
/*      */ 
/*      */   public List<String> subscribeChildChanges(String path, IZkChildListener listener) {
/*  100 */     synchronized (this._childListener) {
/*  101 */       Set listeners = (Set)this._childListener.get(path);
/*  102 */       if (listeners == null) {
/*  103 */         listeners = new CopyOnWriteArraySet();
/*  104 */         this._childListener.put(path, listeners);
/*      */       }
/*  106 */       listeners.add(listener);
/*      */     }
/*  108 */     return watchForChilds(path);
/*      */   }
/*      */ 
/*      */   public void unsubscribeChildChanges(String path, IZkChildListener childListener) {
/*  112 */     synchronized (this._childListener) {
/*  113 */       Set listeners = (Set)this._childListener.get(path);
/*  114 */       if (listeners != null)
/*  115 */         listeners.remove(childListener);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void subscribeDataChanges(String path, IZkDataListener listener)
/*      */   {
/*  122 */     synchronized (this._dataListener) {
/*  123 */       Set listeners = (Set)this._dataListener.get(path);
/*  124 */       if (listeners == null) {
/*  125 */         listeners = new CopyOnWriteArraySet();
/*  126 */         this._dataListener.put(path, listeners);
/*      */       }
/*  128 */       listeners.add(listener);
/*      */     }
/*  130 */     watchForData(path);
/*  131 */     LOG.debug("Subscribed data changes for " + path);
/*      */   }
/*      */ 
/*      */   public void unsubscribeDataChanges(String path, IZkDataListener dataListener) {
/*  135 */     synchronized (this._dataListener) {
/*  136 */       Set listeners = (Set)this._dataListener.get(path);
/*  137 */       if (listeners != null) {
/*  138 */         listeners.remove(dataListener);
/*      */       }
/*  140 */       if ((listeners == null) || (listeners.isEmpty()))
/*  141 */         this._dataListener.remove(path);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void subscribeStateChanges(IZkStateListener listener)
/*      */   {
/*  147 */     synchronized (this._stateListener) {
/*  148 */       this._stateListener.add(listener);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void unsubscribeStateChanges(IZkStateListener stateListener) {
/*  153 */     synchronized (this._stateListener) {
/*  154 */       this._stateListener.remove(stateListener);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void unsubscribeAll() {
/*  159 */     synchronized (this._childListener) {
/*  160 */       this._childListener.clear();
/*      */     }
/*  162 */     synchronized (this._dataListener) {
/*  163 */       this._dataListener.clear();
/*      */     }
/*  165 */     synchronized (this._stateListener) {
/*  166 */       this._stateListener.clear();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void createPersistent(String path)
/*      */     throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException
/*      */   {
/*  182 */     createPersistent(path, false);
/*      */   }
/*      */ 
/*      */   public void createPersistent(String path, boolean createParents)
/*      */     throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException
/*      */   {
/*      */     try
/*      */     {
/*  198 */       create(path, null, CreateMode.PERSISTENT);
/*      */     } catch (ZkNodeExistsException e) {
/*  200 */       if (!createParents)
/*  201 */         throw e;
/*      */     }
/*      */     catch (ZkNoNodeException e) {
/*  204 */       if (!createParents) {
/*  205 */         throw e;
/*      */       }
/*  207 */       String parentDir = path.substring(0, path.lastIndexOf(47));
/*  208 */       createPersistent(parentDir, createParents);
/*  209 */       createPersistent(path, createParents);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void createPersistent(String path, Object data)
/*      */     throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException
/*      */   {
/*  224 */     create(path, data, CreateMode.PERSISTENT);
/*      */   }
/*      */ 
/*      */   public void createPersistent(String path, List<ACL> acl, Object data)
/*      */     throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException
/*      */   {
/*  239 */     create(path, data, acl, CreateMode.PERSISTENT);
/*      */   }
/*      */ 
/*      */   public String createPersistentSequential(String path, Object data)
/*      */     throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException
/*      */   {
/*  255 */     return create(path, data, CreateMode.PERSISTENT_SEQUENTIAL);
/*      */   }
/*      */ 
/*      */   public void createEphemeral(String path)
/*      */     throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException
/*      */   {
/*  268 */     create(path, null, CreateMode.EPHEMERAL);
/*      */   }
/*      */ 
/*      */   public String create(String path, Object data, CreateMode mode)
/*      */     throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException
/*      */   {
/*  285 */     if (path == null) {
/*  286 */       throw new NullPointerException("path must not be null.");
/*      */     }
/*  288 */     return create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
/*      */   }
/*      */ 
/*      */   public String create(final String path, Object data, final List<ACL> acl, final CreateMode mode)
/*      */     throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException
/*      */   {
/*  308 */     if (path == null) {
/*  309 */       throw new NullPointerException("path must not be null.");
/*      */     }
/*  311 */     if (null == acl) {
/*  312 */       throw new NullPointerException("acl must not be null.");
/*      */     }
/*      */ 
/*  315 */     final byte[] bytes = data == null ? null : serialize(data);
/*      */ 
/*  317 */     return (String)retryUntilConnected(new Callable()
/*      */     {
/*      */       public String call() throws Exception
/*      */       {
/*  321 */         return ZkClient.this._connection.create(path, bytes, acl, mode);
/*      */       }
/*      */     });
/*      */   }
/*      */ 
/*      */   public void create(final String path, Object data, final CreateMode mode, final AsyncCallback.StringCallback callback, final Object context)
/*      */     throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException
/*      */   {
/*  347 */     if (path == null) {
/*  348 */       throw new NullPointerException("path must not be null.");
/*      */     }
/*  350 */     final byte[] bytes = data == null ? null : serialize(data);
/*      */ 
/*  352 */     retryUntilConnected(new Callable()
/*      */     {
/*      */       public Boolean call() throws Exception
/*      */       {
/*  356 */         ZkClient.this._connection.create(path, bytes, mode, callback, context);
/*  357 */         return Boolean.TRUE;
/*      */       }
/*      */     });
/*      */   }
/*      */ 
/*      */   public void createEphemeral(String path, Object data)
/*      */     throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException
/*      */   {
/*  373 */     create(path, data, CreateMode.EPHEMERAL);
/*      */   }
/*      */ 
/*      */   public String createEphemeralSequential(String path, Object data)
/*      */     throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException
/*      */   {
/*  388 */     return create(path, data, CreateMode.EPHEMERAL_SEQUENTIAL);
/*      */   }
/*      */ 
/*      */   public void process(WatchedEvent event) {
/*  392 */     LOG.debug("Received event: " + event);
/*  393 */     this._zookeeperEventThread = Thread.currentThread();
/*      */ 
/*  395 */     boolean stateChanged = event.getPath() == null;
/*  396 */     boolean znodeChanged = event.getPath() != null;
/*  397 */     boolean dataChanged = (event.getType() == Watcher.Event.EventType.NodeDataChanged) || (event.getType() == Watcher.Event.EventType.NodeDeleted) || (event.getType() == Watcher.Event.EventType.NodeCreated) || (event.getType() == Watcher.Event.EventType.NodeChildrenChanged);
/*      */ 
/*  400 */     getEventLock().lock();
/*      */     try
/*      */     {
/*  404 */       if (getShutdownTrigger()) {
/*  405 */         LOG.debug("ignoring event '{" + event.getType() + " | " + event.getPath() + "}' since shutdown triggered");
/*      */       }
/*      */       else {
/*  408 */         if (stateChanged) {
/*  409 */           processStateChanged(event);
/*      */         }
/*  411 */         if (dataChanged)
/*  412 */           processDataOrChildChange(event);
/*      */       }
/*      */     } finally {
/*  415 */       if (stateChanged) {
/*  416 */         getEventLock().getStateChangedCondition().signalAll();
/*      */ 
/*  422 */         if (event.getState() == Watcher.Event.KeeperState.Expired) {
/*  423 */           getEventLock().getZNodeEventCondition().signalAll();
/*  424 */           getEventLock().getDataChangedCondition().signalAll();
/*      */ 
/*  426 */           fireAllEvents();
/*      */         }
/*      */       }
/*  429 */       if (znodeChanged) {
/*  430 */         getEventLock().getZNodeEventCondition().signalAll();
/*      */       }
/*  432 */       if (dataChanged) {
/*  433 */         getEventLock().getDataChangedCondition().signalAll();
/*      */       }
/*  435 */       getEventLock().unlock();
/*  436 */       LOG.debug("Leaving process event");
/*      */     }
/*      */   }
/*      */ 
/*      */   private void fireAllEvents() {
/*  441 */     for (Map.Entry entry : this._childListener.entrySet()) {
/*  442 */       fireChildChangedEvents((String)entry.getKey(), (Set)entry.getValue());
/*      */     }
/*  444 */     for (Map.Entry entry : this._dataListener.entrySet())
/*  445 */       fireDataChangedEvents((String)entry.getKey(), (Set)entry.getValue());
/*      */   }
/*      */ 
/*      */   public List<String> getChildren(String path)
/*      */   {
/*  450 */     return getChildren(path, hasListeners(path));
/*      */   }
/*      */ 
/*      */   protected List<String> getChildren(final String path, final boolean watch) {
/*  454 */     return (List)retryUntilConnected(new Callable()
/*      */     {
/*      */       public List<String> call() throws Exception {
/*  457 */         return ZkClient.this._connection.getChildren(path, watch);
/*      */       }
/*      */     });
/*      */   }
/*      */ 
/*      */   public int countChildren(String path)
/*      */   {
/*      */     try
/*      */     {
/*  470 */       return getChildren(path).size(); } catch (ZkNoNodeException e) {
/*      */     }
/*  472 */     return 0;
/*      */   }
/*      */ 
/*      */   protected boolean exists(final String path, final boolean watch)
/*      */   {
/*  477 */     return ((Boolean)retryUntilConnected(new Callable()
/*      */     {
/*      */       public Boolean call() throws Exception {
/*  480 */         return Boolean.valueOf(ZkClient.this._connection.exists(path, watch));
/*      */       }
/*      */     })).booleanValue();
/*      */   }
/*      */ 
/*      */   public boolean exists(String path)
/*      */   {
/*  486 */     return exists(path, hasListeners(path));
/*      */   }
/*      */ 
/*      */   private void processStateChanged(WatchedEvent event) {
/*  490 */     LOG.info("zookeeper state changed (" + event.getState() + ")");
/*  491 */     setCurrentState(event.getState());
/*  492 */     if (getShutdownTrigger())
/*  493 */       return;
/*      */     try
/*      */     {
/*  496 */       fireStateChangedEvent(event.getState());
/*      */ 
/*  498 */       if (event.getState() == Watcher.Event.KeeperState.Expired) {
/*  499 */         reconnect();
/*  500 */         fireNewSessionEvents();
/*      */       }
/*      */     } catch (Exception e) {
/*  503 */       throw new RuntimeException("Exception while restarting zk client", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   private void fireNewSessionEvents() {
/*  508 */     for (final IZkStateListener stateListener : this._stateListener)
/*  509 */       this._eventThread.send(new ZkEventThread.ZkEvent("New session event sent to " + stateListener)
/*      */       {
/*      */         public void run() throws Exception
/*      */         {
/*  513 */           stateListener.handleNewSession();
/*      */         }
/*      */       });
/*      */   }
/*      */ 
/*      */   private void fireStateChangedEvent(final Watcher.Event.KeeperState state)
/*      */   {
/*  520 */     for (final IZkStateListener stateListener : this._stateListener)
/*  521 */       this._eventThread.send(new ZkEventThread.ZkEvent("State changed to " + state + " sent to " + stateListener)
/*      */       {
/*      */         public void run() throws Exception
/*      */         {
/*  525 */           stateListener.handleStateChanged(state);
/*      */         }
/*      */       });
/*      */   }
/*      */ 
/*      */   private boolean hasListeners(String path)
/*      */   {
/*  532 */     Set dataListeners = (Set)this._dataListener.get(path);
/*  533 */     if ((dataListeners != null) && (dataListeners.size() > 0)) {
/*  534 */       return true;
/*      */     }
/*  536 */     Set childListeners = (Set)this._childListener.get(path);
/*  537 */     if ((childListeners != null) && (childListeners.size() > 0)) {
/*  538 */       return true;
/*      */     }
/*  540 */     return false;
/*      */   }
/*      */ 
/*      */   public boolean deleteRecursive(String path) {
/*      */     List children;
/*      */     try {
/*  546 */       children = getChildren(path, false);
/*      */     } catch (ZkNoNodeException e) {
/*  548 */       return true;
/*      */     }
/*      */ 
/*  551 */     for (String subPath : children) {
/*  552 */       if (!deleteRecursive(path + "/" + subPath)) {
/*  553 */         return false;
/*      */       }
/*      */     }
/*      */ 
/*  557 */     return delete(path);
/*      */   }
/*      */ 
/*      */   private void processDataOrChildChange(WatchedEvent event) {
/*  561 */     String path = event.getPath();
/*      */ 
/*  563 */     if ((event.getType() == Watcher.Event.EventType.NodeChildrenChanged) || (event.getType() == Watcher.Event.EventType.NodeCreated) || (event.getType() == Watcher.Event.EventType.NodeDeleted)) {
/*  564 */       Set childListeners = (Set)this._childListener.get(path);
/*  565 */       if ((childListeners != null) && (!childListeners.isEmpty())) {
/*  566 */         fireChildChangedEvents(path, childListeners);
/*      */       }
/*      */     }
/*      */ 
/*  570 */     if ((event.getType() == Watcher.Event.EventType.NodeDataChanged) || (event.getType() == Watcher.Event.EventType.NodeDeleted) || (event.getType() == Watcher.Event.EventType.NodeCreated)) {
/*  571 */       Set listeners = (Set)this._dataListener.get(path);
/*  572 */       if ((listeners != null) && (!listeners.isEmpty()))
/*  573 */         fireDataChangedEvents(event.getPath(), listeners);
/*      */     }
/*      */   }
/*      */ 
/*      */   private void fireDataChangedEvents(final String path, Set<IZkDataListener> listeners)
/*      */   {
/*  579 */     for (final IZkDataListener listener : listeners)
/*  580 */       this._eventThread.send(new ZkEventThread.ZkEvent("Data of " + path + " changed sent to " + listener)
/*      */       {
/*      */         public void run()
/*      */           throws Exception
/*      */         {
/*  585 */           ZkClient.this.exists(path, true);
/*      */           try {
/*  587 */             Object data = ZkClient.this.readData(path, null, true);
/*  588 */             listener.handleDataChange(path, data);
/*      */           } catch (ZkNoNodeException e) {
/*  590 */             listener.handleDataDeleted(path);
/*      */           }
/*      */         }
/*      */       });
/*      */   }
/*      */ 
/*      */   private void fireChildChangedEvents(final String path, Set<IZkChildListener> childListeners)
/*      */   {
/*      */     try
/*      */     {
/*  600 */       for (final IZkChildListener listener : childListeners)
/*  601 */         this._eventThread.send(new ZkEventThread.ZkEvent("Children of " + path + " changed sent to " + listener)
/*      */         {
/*      */           public void run() throws Exception
/*      */           {
/*      */             try
/*      */             {
/*  607 */               ZkClient.this.exists(path);
/*  608 */               List children = ZkClient.this.getChildren(path);
/*  609 */               listener.handleChildChange(path, children);
/*      */             } catch (ZkNoNodeException e) {
/*  611 */               listener.handleChildChange(path, null);
/*      */             }
/*      */           }
/*      */         });
/*      */     }
/*      */     catch (Exception e) {
/*  617 */       LOG.error("Failed to fire child changed event. Unable to getChildren.  ", e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public boolean waitUntilExists(String path, TimeUnit timeUnit, long time) throws ZkInterruptedException {
/*  622 */     Date timeout = new Date(System.currentTimeMillis() + timeUnit.toMillis(time));
/*  623 */     LOG.debug("Waiting until znode '" + path + "' becomes available.");
/*  624 */     if (exists(path)) {
/*  625 */       return true;
/*      */     }
/*  627 */     acquireEventLock();
/*      */     try
/*      */     {
/*      */       boolean gotSignal;
/*  629 */       while (!exists(path, true)) {
/*  630 */         gotSignal = getEventLock().getZNodeEventCondition().awaitUntil(timeout);
/*  631 */         if (!gotSignal) {
/*  632 */           return false;
/*      */         }
/*      */       }
/*  635 */       return true;
/*      */     } catch (InterruptedException e) {
/*  637 */       throw new ZkInterruptedException(e);
/*      */     } finally {
/*  639 */       getEventLock().unlock();
/*      */     }
/*      */   }
/*      */ 
/*      */   protected Set<IZkDataListener> getDataListener(String path) {
/*  644 */     return (Set)this._dataListener.get(path);
/*      */   }
/*      */ 
/*      */   public void showFolders(OutputStream output) {
/*      */     try {
/*  649 */       output.write(ZkPathUtil.toString(this).getBytes());
/*      */     } catch (IOException e) {
/*  651 */       e.printStackTrace();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void waitUntilConnected() throws ZkInterruptedException {
/*  656 */     waitUntilConnected(2147483647L, TimeUnit.MILLISECONDS);
/*      */   }
/*      */ 
/*      */   public boolean waitUntilConnected(long time, TimeUnit timeUnit) throws ZkInterruptedException {
/*  660 */     return waitForKeeperState(Watcher.Event.KeeperState.SyncConnected, time, timeUnit);
/*      */   }
/*      */ 
/*      */   public boolean waitForKeeperState(Watcher.Event.KeeperState keeperState, long time, TimeUnit timeUnit) throws ZkInterruptedException {
/*  664 */     if ((this._zookeeperEventThread != null) && (Thread.currentThread() == this._zookeeperEventThread)) {
/*  665 */       throw new IllegalArgumentException("Must not be done in the zookeeper event thread.");
/*      */     }
/*  667 */     Date timeout = new Date(System.currentTimeMillis() + timeUnit.toMillis(time));
/*      */ 
/*  669 */     LOG.debug("Waiting for keeper state " + keeperState);
/*  670 */     acquireEventLock();
/*      */     try {
/*  672 */       boolean stillWaiting = true;
/*      */       boolean bool1;
/*  673 */       while (this._currentState != keeperState) {
/*  674 */         if (!stillWaiting) {
/*  675 */           return false;
/*      */         }
/*  677 */         stillWaiting = getEventLock().getStateChangedCondition().awaitUntil(timeout);
/*      */       }
/*  679 */       LOG.debug("State is " + this._currentState);
/*  680 */       return true;
/*      */     } catch (InterruptedException e) {
/*  682 */       throw new ZkInterruptedException(e);
/*      */     } finally {
/*  684 */       getEventLock().unlock();
/*      */     }
/*      */   }
/*      */ 
/*      */   private void acquireEventLock() {
/*      */     try {
/*  690 */       getEventLock().lockInterruptibly();
/*      */     } catch (InterruptedException e) {
/*  692 */       throw new ZkInterruptedException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public <T> T retryUntilConnected(Callable<T> callable)
/*      */     throws ZkInterruptedException, IllegalArgumentException, ZkException, RuntimeException
/*      */   {
/*  706 */     if ((this._zookeeperEventThread != null) && (Thread.currentThread() == this._zookeeperEventThread)) {
/*  707 */       throw new IllegalArgumentException("Must not be done in the zookeeper event thread.");
/*      */     }
/*      */     try
/*      */     {
/*  711 */       return callable.call();
/*      */     } catch (KeeperException.ConnectionLossException e) {
/*      */       while (true) {
/*  714 */         Thread.yield();
/*  715 */         waitUntilConnected();
/*      */       }
/*      */     } catch (KeeperException.SessionExpiredException e) {
/*      */       while (true) { Thread.yield();
/*  719 */         waitUntilConnected(); }
/*      */     } catch (KeeperException e) {
/*  721 */       throw ZkException.create(e);
/*      */     } catch (InterruptedException e) {
/*  723 */       throw new ZkInterruptedException(e);
/*      */     } catch (Exception e) {
/*  725 */       throw ExceptionUtil.convertToRuntimeException(e);
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setCurrentState(Watcher.Event.KeeperState currentState)
/*      */   {
/*  731 */     getEventLock().lock();
/*      */     try {
/*  733 */       this._currentState = currentState;
/*      */     } finally {
/*  735 */       getEventLock().unlock();
/*      */     }
/*      */   }
/*      */ 
/*      */   public ZkLock getEventLock()
/*      */   {
/*  747 */     return this._zkEventLock;
/*      */   }
/*      */ 
/*      */   public boolean delete(final String path) {
/*      */     try {
/*  752 */       retryUntilConnected(new Callable()
/*      */       {
/*      */         public Object call() throws Exception
/*      */         {
/*  756 */           ZkClient.this._connection.delete(path);
/*  757 */           return null;
/*      */         }
/*      */       });
/*  761 */       return true; } catch (ZkNoNodeException e) {
/*      */     }
/*  763 */     return false;
/*      */   }
/*      */ 
/*      */   public void delete(final String path, final AsyncCallback.VoidCallback callback, final Object context)
/*      */   {
/*      */     try
/*      */     {
/*  775 */       retryUntilConnected(new Callable()
/*      */       {
/*      */         public Boolean call() throws Exception
/*      */         {
/*  779 */           ZkClient.this._connection.delete(path, callback, context);
/*  780 */           return Boolean.TRUE;
/*      */         }
/*      */       });
/*      */     }
/*      */     catch (ZkNoNodeException e)
/*      */     {
/*      */     }
/*      */   }
/*      */ 
/*      */   private byte[] serialize(Object data) {
/*  790 */     return this._zkSerializer.serialize(data);
/*      */   }
/*      */ 
/*      */   private <T> T derializable(byte[] data)
/*      */   {
/*  795 */     if (data == null) {
/*  796 */       return null;
/*      */     }
/*  798 */     return this._zkSerializer.deserialize(data);
/*      */   }
/*      */ 
/*      */   public <T> T readData(String path) {
/*  802 */     return readData(path, false);
/*      */   }
/*      */ 
/*      */   public <T> T readData(String path, boolean returnNullIfPathNotExists) {
/*  806 */     Object data = null;
/*      */     try {
/*  808 */       data = readData(path, null);
/*      */     } catch (ZkNoNodeException e) {
/*  810 */       if (!returnNullIfPathNotExists) {
/*  811 */         throw e;
/*      */       }
/*      */     }
/*  814 */     return data;
/*      */   }
/*      */ 
/*      */   public <T> T readData(String path, Stat stat)
/*      */   {
/*  819 */     return readData(path, stat, hasListeners(path));
/*      */   }
/*      */ 
/*      */   protected <T> T readData(final String path, final Stat stat, final boolean watch)
/*      */   {
/*  824 */     byte[] data = (byte[])retryUntilConnected(new Callable()
/*      */     {
/*      */       public byte[] call() throws Exception
/*      */       {
/*  828 */         return ZkClient.this._connection.readData(path, stat, watch);
/*      */       }
/*      */     });
/*  831 */     return derializable(data);
/*      */   }
/*      */ 
/*      */   public void writeData(String path, Object object) {
/*  835 */     writeData(path, object, -1);
/*      */   }
/*      */ 
/*      */   public <T> void updateDataSerialized(String path, DataUpdater<T> updater)
/*      */   {
/*  849 */     Stat stat = new Stat();
/*      */     boolean retry;
/*      */     do
/*      */     {
/*  852 */       retry = false;
/*      */       try {
/*  854 */         Object oldData = readData(path, stat);
/*  855 */         Object newData = updater.update(oldData);
/*  856 */         writeData(path, newData, stat.getVersion());
/*      */       } catch (ZkBadVersionException e) {
/*  858 */         retry = true;
/*      */       }
/*      */     }
/*  860 */     while (retry);
/*      */   }
/*      */ 
/*      */   public void writeData(final String path, Object datat, final int expectedVersion) {
/*  864 */     final byte[] data = serialize(datat);
/*  865 */     retryUntilConnected(new Callable()
/*      */     {
/*      */       public Object call() throws Exception
/*      */       {
/*  869 */         ZkClient.this._connection.writeData(path, data, expectedVersion);
/*  870 */         return null;
/*      */       }
/*      */     });
/*      */   }
/*      */ 
/*      */   public void addAuthInfo(final String scheme, final byte[] auth)
/*      */   {
/*  877 */     retryUntilConnected(new Callable()
/*      */     {
/*      */       public Object call() throws Exception {
/*  880 */         ZkClient.this._connection.addAuthInfo(scheme, auth);
/*  881 */         return null;
/*      */       }
/*      */     });
/*      */   }
/*      */ 
/*      */   public void watchForData(final String path)
/*      */   {
/*  888 */     retryUntilConnected(new Callable()
/*      */     {
/*      */       public Object call() throws Exception {
/*  891 */         ZkClient.this._connection.exists(path, true);
/*  892 */         return null;
/*      */       }
/*      */     });
/*      */   }
/*      */ 
/*      */   public List<String> watchForChilds(final String path)
/*      */   {
/*  904 */     if ((this._zookeeperEventThread != null) && (Thread.currentThread() == this._zookeeperEventThread)) {
/*  905 */       throw new IllegalArgumentException("Must not be done in the zookeeper event thread.");
/*      */     }
/*  907 */     return (List)retryUntilConnected(new Callable()
/*      */     {
/*      */       public List<String> call() throws Exception {
/*  910 */         ZkClient.this.exists(path, true);
/*      */         try {
/*  912 */           return ZkClient.this.getChildren(path, true);
/*      */         }
/*      */         catch (ZkNoNodeException e) {
/*      */         }
/*  916 */         return null;
/*      */       }
/*      */     });
/*      */   }
/*      */ 
/*      */   public void connect(long maxMsToWaitUntilConnected, Watcher watcher)
/*      */     throws ZkInterruptedException, ZkTimeoutException, IllegalStateException
/*      */   {
/*  931 */     boolean started = false;
/*      */     try {
/*  933 */       getEventLock().lockInterruptibly();
/*  934 */       setShutdownTrigger(false);
/*  935 */       this._eventThread = new ZkEventThread(this._connection.getServers());
/*  936 */       this._eventThread.start();
/*  937 */       this._connection.connect(watcher);
/*      */ 
/*  939 */       LOG.debug("Awaiting connection to Zookeeper server");
/*  940 */       if (!waitUntilConnected(maxMsToWaitUntilConnected, TimeUnit.MILLISECONDS)) {
/*  941 */         throw new ZkTimeoutException("Unable to connect to zookeeper server within timeout: " + maxMsToWaitUntilConnected);
/*      */       }
/*  943 */       started = true;
/*      */     } catch (InterruptedException e) {
/*  945 */       ZooKeeper.States state = this._connection.getZookeeperState();
/*  946 */       throw new IllegalStateException("Not connected with zookeeper server yet. Current state is " + state);
/*      */     } finally {
/*  948 */       getEventLock().unlock();
/*      */ 
/*  952 */       if (!started)
/*  953 */         close();
/*      */     }
/*      */   }
/*      */ 
/*      */   public long getCreationTime(String path)
/*      */   {
/*      */     try {
/*  960 */       getEventLock().lockInterruptibly();
/*  961 */       return this._connection.getCreateTime(path);
/*      */     } catch (KeeperException e) {
/*  963 */       throw ZkException.create(e);
/*      */     } catch (InterruptedException e) {
/*  965 */       throw new ZkInterruptedException(e);
/*      */     } finally {
/*  967 */       getEventLock().unlock();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void close()
/*      */     throws ZkInterruptedException
/*      */   {
/*  977 */     if (this._connection == null) {
/*  978 */       return;
/*      */     }
/*  980 */     LOG.debug("Closing ZkClient...");
/*  981 */     getEventLock().lock();
/*      */     try {
/*  983 */       setShutdownTrigger(true);
/*  984 */       this._eventThread.interrupt();
/*  985 */       this._eventThread.join(2000L);
/*  986 */       this._connection.close();
/*  987 */       this._connection = null;
/*      */     } catch (InterruptedException e) {
/*  989 */       throw new ZkInterruptedException(e);
/*      */     } finally {
/*  991 */       getEventLock().unlock();
/*      */     }
/*  993 */     LOG.debug("Closing ZkClient...done");
/*      */   }
/*      */ 
/*      */   private void reconnect() {
/*  997 */     getEventLock().lock();
/*      */     try {
/*  999 */       this._connection.close();
/* 1000 */       this._connection.connect(this);
/*      */     } catch (InterruptedException e) {
/* 1002 */       throw new ZkInterruptedException(e);
/*      */     } finally {
/* 1004 */       getEventLock().unlock();
/*      */     }
/*      */   }
/*      */ 
/*      */   public void setShutdownTrigger(boolean triggerState) {
/* 1009 */     this._shutdownTriggered = triggerState;
/*      */   }
/*      */ 
/*      */   public boolean getShutdownTrigger() {
/* 1013 */     return this._shutdownTriggered;
/*      */   }
/*      */ 
/*      */   public int numberOfListeners() {
/* 1017 */     int listeners = 0;
/* 1018 */     for (Set childListeners : this._childListener.values()) {
/* 1019 */       listeners += childListeners.size();
/*      */     }
/* 1021 */     for (Set dataListeners : this._dataListener.values()) {
/* 1022 */       listeners += dataListeners.size();
/*      */     }
/* 1024 */     listeners += this._stateListener.size();
/*      */ 
/* 1026 */     return listeners;
/*      */   }
/*      */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.ZkClient
 * JD-Core Version:    0.6.2
 */