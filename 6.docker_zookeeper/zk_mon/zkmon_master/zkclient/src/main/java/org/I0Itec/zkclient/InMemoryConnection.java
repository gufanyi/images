/*     */ package org.I0Itec.zkclient;
/*     */ 
/*     */ import java.util.ArrayList;
/*     */ import java.util.HashMap;
/*     */ import java.util.HashSet;
/*     */ import java.util.List;
/*     */ import java.util.Map;
/*     */ import java.util.Set;
/*     */ import java.util.concurrent.BlockingQueue;
/*     */ import java.util.concurrent.LinkedBlockingDeque;
/*     */ import java.util.concurrent.atomic.AtomicInteger;
/*     */ import java.util.concurrent.locks.Lock;
/*     */ import java.util.concurrent.locks.ReentrantLock;
/*     */ import org.I0Itec.zkclient.exception.ZkException;
/*     */ import org.I0Itec.zkclient.exception.ZkInterruptedException;
/*     */ import org.I0Itec.zkclient.exception.ZkNoNodeException;
/*     */ import org.I0Itec.zkclient.util.ZkPathUtil;
/*     */ import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
/*     */ import org.apache.zookeeper.AsyncCallback.StringCallback;
/*     */ import org.apache.zookeeper.AsyncCallback.VoidCallback;
/*     */ import org.apache.zookeeper.CreateMode;
/*     */ import org.apache.zookeeper.KeeperException;
/*     */ import org.apache.zookeeper.KeeperException.Code;
/*     */ import org.apache.zookeeper.KeeperException.NoNodeException;
/*     */ import org.apache.zookeeper.KeeperException.NodeExistsException;
/*     */ import org.apache.zookeeper.KeeperException.UnimplementedException;
/*     */ import org.apache.zookeeper.WatchedEvent;
/*     */ import org.apache.zookeeper.Watcher;
/*     */ import org.apache.zookeeper.Watcher.Event.EventType;
/*     */ import org.apache.zookeeper.Watcher.Event.KeeperState;
/*     */ import org.apache.zookeeper.ZooKeeper.States;
/*     */ import org.apache.zookeeper.data.ACL;
/*     */ import org.apache.zookeeper.data.Stat;
/*     */ 
/*     */ public class InMemoryConnection
/*     */   implements IZkConnection
/*     */ {
/*  44 */   private Lock _lock = new ReentrantLock(true);
/*  45 */   private Map<String, byte[]> _data = new HashMap();
/*  46 */   private Map<String, Long> _creationTime = new HashMap();
/*  47 */   private final AtomicInteger sequence = new AtomicInteger(0);
/*     */ 
/*  49 */   private Set<String> _dataWatches = new HashSet();
/*  50 */   private Set<String> _nodeWatches = new HashSet();
/*     */   private EventThread _eventThread;
/*     */ 
/*     */   public InMemoryConnection()
/*     */   {
/*     */     try
/*     */     {
/*  80 */       create("/", null, CreateMode.PERSISTENT);
/*     */     } catch (KeeperException e) {
/*  82 */       throw ZkException.create(e);
/*     */     } catch (InterruptedException e) {
/*  84 */       Thread.currentThread().interrupt();
/*  85 */       throw new ZkInterruptedException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public void close() throws InterruptedException
/*     */   {
/*  91 */     this._lock.lockInterruptibly();
/*     */     try {
/*  93 */       if (this._eventThread != null) {
/*  94 */         this._eventThread.interrupt();
/*  95 */         this._eventThread.join();
/*  96 */         this._eventThread = null;
/*     */       }
/*     */     } finally {
/*  99 */       this._lock.unlock();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void connect(Watcher watcher)
/*     */   {
/* 105 */     this._lock.lock();
/*     */     try {
/* 107 */       if (this._eventThread != null) {
/* 108 */         throw new IllegalStateException("Already connected.");
/*     */       }
/* 110 */       this._eventThread = new EventThread(watcher);
/* 111 */       this._eventThread.start();
/* 112 */       this._eventThread.send(new WatchedEvent(null, Watcher.Event.KeeperState.SyncConnected, null));
/*     */     } finally {
/* 114 */       this._lock.unlock();
/*     */     }
/*     */   }
/*     */ 
/*     */   public String create(String path, byte[] data, CreateMode mode) throws KeeperException, InterruptedException
/*     */   {
/* 120 */     this._lock.lock();
/*     */     try
/*     */     {
/* 123 */       if (mode.isSequential()) {
/* 124 */         int newSequence = this.sequence.getAndIncrement();
/* 125 */         path = path + ZkPathUtil.leadingZeros(newSequence, 10);
/*     */       }
/*     */ 
/* 128 */       if (exists(path, false)) {
/* 129 */         throw new KeeperException.NodeExistsException();
/*     */       }
/* 131 */       this._data.put(path, data);
/* 132 */       this._creationTime.put(path, Long.valueOf(System.currentTimeMillis()));
/* 133 */       checkWatch(this._nodeWatches, path, Watcher.Event.EventType.NodeCreated);
/*     */ 
/* 135 */       String parentPath = getParentPath(path);
/* 136 */       if (parentPath != null) {
/* 137 */         checkWatch(this._nodeWatches, parentPath, Watcher.Event.EventType.NodeChildrenChanged);
/*     */       }
/* 139 */       return path;
/*     */     } finally {
/* 141 */       this._lock.unlock();
/*     */     }
/*     */   }
/*     */ 
/*     */   public String create(String path, byte[] data, List<ACL> acl, CreateMode mode)
/*     */     throws KeeperException, InterruptedException
/*     */   {
/* 148 */     throw new RuntimeException("InMemoryConnection not support the acl control at present.");
/*     */   }
/*     */ 
/*     */   private String getParentPath(String path)
/*     */   {
/* 153 */     int lastIndexOf = path.lastIndexOf("/");
/* 154 */     if ((lastIndexOf == -1) || (lastIndexOf == 0)) {
/* 155 */       return null;
/*     */     }
/* 157 */     return path.substring(0, lastIndexOf);
/*     */   }
/*     */ 
/*     */   public void delete(String path) throws InterruptedException, KeeperException
/*     */   {
/* 162 */     this._lock.lock();
/*     */     try {
/* 164 */       if (!exists(path, false)) {
/* 165 */         throw new KeeperException.NoNodeException();
/*     */       }
/* 167 */       this._data.remove(path);
/* 168 */       this._creationTime.remove(path);
/* 169 */       checkWatch(this._nodeWatches, path, Watcher.Event.EventType.NodeDeleted);
/* 170 */       String parentPath = getParentPath(path);
/* 171 */       if (parentPath != null)
/* 172 */         checkWatch(this._nodeWatches, parentPath, Watcher.Event.EventType.NodeChildrenChanged);
/*     */     }
/*     */     finally {
/* 175 */       this._lock.unlock();
/*     */     }
/*     */   }
/*     */ 
/*     */   public boolean exists(String path, boolean watch) throws KeeperException, InterruptedException
/*     */   {
/* 181 */     this._lock.lock();
/*     */     try {
/* 183 */       if (watch) {
/* 184 */         installWatch(this._nodeWatches, path);
/*     */       }
/* 186 */       return this._data.containsKey(path);
/*     */     } finally {
/* 188 */       this._lock.unlock();
/*     */     }
/*     */   }
/*     */ 
/*     */   private void installWatch(Set<String> watches, String path) {
/* 193 */     watches.add(path);
/*     */   }
/*     */ 
/*     */   public List<String> getChildren(String path, boolean watch) throws KeeperException, InterruptedException
/*     */   {
/* 198 */     if (!exists(path, false)) {
/* 199 */       throw KeeperException.create(KeeperException.Code.NONODE, path);
/*     */     }
/* 201 */     if ((exists(path, false)) && (watch)) {
/* 202 */       installWatch(this._nodeWatches, path);
/*     */     }
/*     */ 
/* 205 */     ArrayList children = new ArrayList();
/* 206 */     String[] directoryStack = path.split("/");
/* 207 */     Set keySet = this._data.keySet();
/*     */ 
/* 209 */     for (String string : keySet) {
/* 210 */       if (string.startsWith(path)) {
/* 211 */         String[] stack = string.split("/");
/*     */ 
/* 214 */         if (stack.length == directoryStack.length + 1) {
/* 215 */           children.add(stack[(stack.length - 1)]);
/*     */         }
/*     */       }
/*     */     }
/*     */ 
/* 220 */     return children;
/*     */   }
/*     */ 
/*     */   public ZooKeeper.States getZookeeperState()
/*     */   {
/* 225 */     this._lock.lock();
/*     */     try
/*     */     {
/*     */       ZooKeeper.States localStates;
/* 227 */       if (this._eventThread == null) {
/* 228 */         return ZooKeeper.States.CLOSED;
/*     */       }
/* 230 */       return ZooKeeper.States.CONNECTED;
/*     */     } finally {
/* 232 */       this._lock.unlock();
/*     */     }
/*     */   }
/*     */ 
/*     */   public byte[] readData(String path, Stat stat, boolean watch) throws KeeperException, InterruptedException
/*     */   {
/* 238 */     if (watch) {
/* 239 */       installWatch(this._dataWatches, path);
/*     */     }
/* 241 */     this._lock.lock();
/*     */     try {
/* 243 */       byte[] bs = (byte[])this._data.get(path);
/* 244 */       if (bs == null) {
/* 245 */         throw new ZkNoNodeException(new KeeperException.NoNodeException());
/*     */       }
/* 247 */       return bs;
/*     */     } finally {
/* 249 */       this._lock.unlock();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void writeData(String path, byte[] data, int expectedVersion) throws KeeperException, InterruptedException
/*     */   {
/* 255 */     this._lock.lock();
/*     */     try {
/* 257 */       checkWatch(this._dataWatches, path, Watcher.Event.EventType.NodeDataChanged);
/* 258 */       if (!exists(path, false)) {
/* 259 */         throw new KeeperException.NoNodeException();
/*     */       }
/* 261 */       this._data.put(path, data);
/* 262 */       String parentPath = getParentPath(path);
/* 263 */       if (parentPath != null)
/* 264 */         checkWatch(this._nodeWatches, parentPath, Watcher.Event.EventType.NodeChildrenChanged);
/*     */     }
/*     */     finally {
/* 267 */       this._lock.unlock();
/*     */     }
/*     */   }
/*     */ 
/*     */   private void checkWatch(Set<String> watches, String path, Watcher.Event.EventType eventType) {
/* 272 */     if (watches.contains(path)) {
/* 273 */       watches.remove(path);
/* 274 */       this._eventThread.send(new WatchedEvent(eventType, Watcher.Event.KeeperState.SyncConnected, path));
/*     */     }
/*     */   }
/*     */ 
/*     */   public long getCreateTime(String path)
/*     */   {
/* 280 */     Long time = (Long)this._creationTime.get(path);
/* 281 */     if (time == null) {
/* 282 */       return -1L;
/*     */     }
/* 284 */     return time.longValue();
/*     */   }
/*     */ 
/*     */   public String getServers()
/*     */   {
/* 289 */     return "mem";
/*     */   }
/*     */ 
/*     */   public void create(String path, byte[] data, CreateMode mode, AsyncCallback.StringCallback callback, Object context) throws KeeperException, InterruptedException
/*     */   {
/* 294 */     throw new KeeperException.UnimplementedException();
/*     */   }
/*     */ 
/*     */   public void delete(String path, AsyncCallback.VoidCallback callback, Object context) throws InterruptedException, KeeperException
/*     */   {
/* 299 */     throw new KeeperException.UnimplementedException();
/*     */   }
/*     */ 
/*     */   public void getChildren(String path, boolean watch, AsyncCallback.ChildrenCallback callback, Object context) throws KeeperException, InterruptedException
/*     */   {
/* 304 */     throw new KeeperException.UnimplementedException();
/*     */   }
/*     */ 
/*     */   public void addAuthInfo(String scheme, byte[] auth)
/*     */   {
/* 309 */     throw new RuntimeException("InMemoryConnection not support the auth control at present.");
/*     */   }
/*     */ 
/*     */   private class EventThread extends Thread
/*     */   {
/*     */     private Watcher _watcher;
/*  56 */     private BlockingQueue<WatchedEvent> _blockingQueue = new LinkedBlockingDeque();
/*     */ 
/*     */     public EventThread(Watcher watcher) {
/*  59 */       this._watcher = watcher;
/*     */     }
/*     */ 
/*     */     public void run()
/*     */     {
/*     */       try {
/*     */         while (true)
/*  66 */           this._watcher.process((WatchedEvent)this._blockingQueue.take());
/*     */       }
/*     */       catch (InterruptedException e)
/*     */       {
/*     */       }
/*     */     }
/*     */ 
/*     */     public void send(WatchedEvent event) {
/*  74 */       this._blockingQueue.add(event);
/*     */     }
/*     */   }
/*     */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.InMemoryConnection
 * JD-Core Version:    0.6.2
 */