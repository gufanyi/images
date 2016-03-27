/*     */ package org.I0Itec.zkclient;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.util.List;
/*     */ import java.util.concurrent.locks.Lock;
/*     */ import java.util.concurrent.locks.ReentrantLock;
/*     */ import org.I0Itec.zkclient.exception.ZkException;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
/*     */ import org.apache.zookeeper.AsyncCallback.StringCallback;
/*     */ import org.apache.zookeeper.AsyncCallback.VoidCallback;
/*     */ import org.apache.zookeeper.CreateMode;
/*     */ import org.apache.zookeeper.KeeperException;
/*     */ import org.apache.zookeeper.Watcher;
/*     */ import org.apache.zookeeper.ZooDefs.Ids;
/*     */ import org.apache.zookeeper.ZooKeeper;
/*     */ import org.apache.zookeeper.ZooKeeper.States;
/*     */ import org.apache.zookeeper.data.ACL;
/*     */ import org.apache.zookeeper.data.Stat;
/*     */ 
/*     */ public class ZkConnection
/*     */   implements IZkConnection
/*     */ {
/*  33 */   private static final Logger LOG = Logger.getLogger(ZkConnection.class);
/*     */   private static final int DEFAULT_SESSION_TIMEOUT = 30000;
/*  38 */   private ZooKeeper _zk = null;
/*  39 */   private Lock _zookeeperLock = new ReentrantLock();
/*     */   private final String _servers;
/*     */   private final int _sessionTimeOut;
/*     */ 
/*     */   public ZkConnection(String zkServers)
/*     */   {
/*  45 */     this(zkServers, 30000);
/*     */   }
/*     */ 
/*     */   public ZkConnection(String zkServers, int sessionTimeOut) {
/*  49 */     this._servers = zkServers;
/*  50 */     this._sessionTimeOut = sessionTimeOut;
/*     */   }
/*     */ 
/*     */   public void connect(Watcher watcher)
/*     */   {
/*  55 */     this._zookeeperLock.lock();
/*     */     try {
/*  57 */       if (this._zk != null)
/*  58 */         throw new IllegalStateException("zk client has already been started");
/*     */       try
/*     */       {
/*  61 */         LOG.debug("Creating new ZookKeeper instance to connect to " + this._servers + ".");
/*  62 */         this._zk = new ZooKeeper(this._servers, this._sessionTimeOut, watcher);
/*     */       } catch (IOException e) {
/*  64 */         throw new ZkException("Unable to connect to " + this._servers, e);
/*     */       }
/*     */     } finally {
/*  67 */       this._zookeeperLock.unlock();
/*     */     }
/*     */   }
/*     */ 
/*     */   public void close() throws InterruptedException {
/*  72 */     this._zookeeperLock.lock();
/*     */     try {
/*  74 */       if (this._zk != null) {
/*  75 */         LOG.debug("Closing ZooKeeper connected to " + this._servers);
/*  76 */         this._zk.close();
/*  77 */         this._zk = null;
/*     */       }
/*     */     } finally {
/*  80 */       this._zookeeperLock.unlock();
/*     */     }
/*     */   }
/*     */ 
/*     */   public String create(String path, byte[] data, CreateMode mode) throws KeeperException, InterruptedException {
/*  85 */     return this._zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode);
/*     */   }
/*     */ 
/*     */   public String create(String path, byte[] data, List<ACL> acl, CreateMode mode) throws KeeperException, InterruptedException {
/*  89 */     if (null == acl)
/*  90 */       acl = ZooDefs.Ids.OPEN_ACL_UNSAFE;
/*  91 */     return this._zk.create(path, data, acl, mode);
/*     */   }
/*     */ 
/*     */   public void delete(String path) throws InterruptedException, KeeperException
/*     */   {
/*  96 */     this._zk.delete(path, -1);
/*     */   }
/*     */ 
/*     */   public void create(String path, byte[] data, CreateMode mode, AsyncCallback.StringCallback callback, Object context) throws KeeperException, InterruptedException {
/* 100 */     this._zk.create(path, data, ZooDefs.Ids.OPEN_ACL_UNSAFE, mode, callback, context);
/*     */   }
/*     */ 
/*     */   public void delete(String path, AsyncCallback.VoidCallback callback, Object context) throws InterruptedException, KeeperException {
/* 104 */     this._zk.delete(path, -1, callback, context);
/*     */   }
/*     */ 
/*     */   public boolean exists(String path, boolean watch) throws KeeperException, InterruptedException {
/* 108 */     return this._zk.exists(path, watch) != null;
/*     */   }
/*     */ 
/*     */   public List<String> getChildren(String path, boolean watch) throws KeeperException, InterruptedException {
/* 112 */     return this._zk.getChildren(path, watch);
/*     */   }
/*     */ 
/*     */   public void getChildren(String path, boolean watch, AsyncCallback.ChildrenCallback callback, Object context) throws KeeperException, InterruptedException {
/* 116 */     this._zk.getChildren(path, watch, callback, context);
/*     */   }
/*     */ 
/*     */   public byte[] readData(String path, Stat stat, boolean watch) throws KeeperException, InterruptedException {
/* 120 */     return this._zk.getData(path, watch, stat);
/*     */   }
/*     */ 
/*     */   public void writeData(String path, byte[] data) throws KeeperException, InterruptedException {
/* 124 */     writeData(path, data, -1);
/*     */   }
/*     */ 
/*     */   public void writeData(String path, byte[] data, int version) throws KeeperException, InterruptedException {
/* 128 */     this._zk.setData(path, data, version);
/*     */   }
/*     */ 
/*     */   public ZooKeeper.States getZookeeperState() {
/* 132 */     return this._zk != null ? this._zk.getState() : null;
/*     */   }
/*     */ 
/*     */   public ZooKeeper getZookeeper() {
/* 136 */     return this._zk;
/*     */   }
/*     */ 
/*     */   public long getCreateTime(String path) throws KeeperException, InterruptedException
/*     */   {
/* 141 */     Stat stat = this._zk.exists(path, false);
/* 142 */     if (stat != null) {
/* 143 */       return stat.getCtime();
/*     */     }
/* 145 */     return -1L;
/*     */   }
/*     */ 
/*     */   public String getServers()
/*     */   {
/* 150 */     return this._servers;
/*     */   }
/*     */ 
/*     */   public void addAuthInfo(String scheme, byte[] auth)
/*     */   {
/* 155 */     this._zk.addAuthInfo(scheme, auth);
/*     */   }
/*     */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.ZkConnection
 * JD-Core Version:    0.6.2
 */