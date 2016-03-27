/*     */ package org.I0Itec.zkclient;
/*     */ 
/*     */ import java.io.File;
/*     */ import java.io.IOException;
/*     */ import java.net.InetSocketAddress;
/*     */ import java.util.Arrays;
/*     */ import javax.annotation.PostConstruct;
/*     */ import javax.annotation.PreDestroy;
/*     */ import org.I0Itec.zkclient.exception.ZkException;
/*     */ import org.I0Itec.zkclient.exception.ZkInterruptedException;
/*     */ import org.apache.log4j.Logger;
/*     */ import org.apache.zookeeper.server.NIOServerCnxn.Factory;
/*     */ import org.apache.zookeeper.server.ZooKeeperServer;
/*     */ 
/*     */ public class ZkServer
/*     */ {
/*  35 */   private static final Logger LOG = Logger.getLogger(ZkServer.class);
/*     */   public static final int DEFAULT_PORT = 2181;
/*     */   public static final int DEFAULT_TICK_TIME = 5000;
/*     */   public static final int DEFAULT_MIN_SESSION_TIMEOUT = 10000;
/*     */   private String _dataDir;
/*     */   private String _logDir;
/*     */   private IDefaultNameSpace _defaultNameSpace;
/*     */   private ZooKeeperServer _zk;
/*     */   private NIOServerCnxn.Factory _nioFactory;
/*     */   private ZkClient _zkClient;
/*     */   private int _port;
/*     */   private int _tickTime;
/*     */   private int _minSessionTimeout;
/*     */ 
/*     */   public ZkServer(String dataDir, String logDir, IDefaultNameSpace defaultNameSpace)
/*     */   {
/*  54 */     this(dataDir, logDir, defaultNameSpace, 2181);
/*     */   }
/*     */ 
/*     */   public ZkServer(String dataDir, String logDir, IDefaultNameSpace defaultNameSpace, int port) {
/*  58 */     this(dataDir, logDir, defaultNameSpace, port, 5000);
/*     */   }
/*     */   public ZkServer(String dataDir, String logDir, IDefaultNameSpace defaultNameSpace, int port, int tickTime) {
/*  61 */     this(dataDir, logDir, defaultNameSpace, port, tickTime, 10000);
/*     */   }
/*     */ 
/*     */   public ZkServer(String dataDir, String logDir, IDefaultNameSpace defaultNameSpace, int port, int tickTime, int minSessionTimeout) {
/*  65 */     this._dataDir = dataDir;
/*  66 */     this._logDir = logDir;
/*  67 */     this._defaultNameSpace = defaultNameSpace;
/*  68 */     this._port = port;
/*  69 */     this._tickTime = tickTime;
/*  70 */     this._minSessionTimeout = minSessionTimeout;
/*     */   }
/*     */ 
/*     */   public int getPort() {
/*  74 */     return this._port;
/*     */   }
/*     */ 
/*     */   @PostConstruct
/*     */   public void start() {
/*  79 */     String[] localHostNames = NetworkUtil.getLocalHostNames();
/*  80 */     String names = "";
/*  81 */     for (int i = 0; i < localHostNames.length; i++) {
/*  82 */       String name = localHostNames[i];
/*  83 */       names = names + " " + name;
/*  84 */       if (i + 1 != localHostNames.length) {
/*  85 */         names = names + ",";
/*     */       }
/*     */     }
/*  88 */     LOG.info("Starting ZkServer on: [" + names + "] port " + this._port + "...");
/*  89 */     startZooKeeperServer();
/*  90 */     this._zkClient = new ZkClient("localhost:" + this._port, 10000);
/*  91 */     this._defaultNameSpace.createDefaultNameSpace(this._zkClient);
/*     */   }
/*     */ 
/*     */   private void startZooKeeperServer() {
/*  95 */     String[] localhostHostNames = NetworkUtil.getLocalHostNames();
/*  96 */     String servers = "localhost:" + this._port;
/*     */ 
/*  98 */     int pos = -1;
/*  99 */     LOG.debug("check if hostNames " + servers + " is in list: " + Arrays.asList(localhostHostNames));
/* 100 */     if ((pos = NetworkUtil.hostNamesInList(servers, localhostHostNames)) != -1)
/*     */     {
/* 102 */       String[] hosts = servers.split(",");
/* 103 */       String[] hostSplitted = hosts[pos].split(":");
/* 104 */       int port = this._port;
/* 105 */       if (hostSplitted.length > 1) {
/* 106 */         port = Integer.parseInt(hostSplitted[1]);
/*     */       }
/*     */ 
/* 109 */       if (NetworkUtil.isPortFree(port)) {
/* 110 */         File dataDir = new File(this._dataDir);
/* 111 */         File dataLogDir = new File(this._logDir);
/* 112 */         dataDir.mkdirs();
/* 113 */         dataLogDir.mkdirs();
/*     */ 
/* 115 */         if (hosts.length > 1)
/*     */         {
/* 117 */           LOG.info("Start distributed zookeeper server...");
/* 118 */           throw new IllegalArgumentException("Unable to start distributed zookeeper server");
/*     */         }
/*     */ 
/* 121 */         LOG.info("Start single zookeeper server...");
/* 122 */         LOG.info("data dir: " + dataDir.getAbsolutePath());
/* 123 */         LOG.info("data log dir: " + dataLogDir.getAbsolutePath());
/* 124 */         startSingleZkServer(this._tickTime, dataDir, dataLogDir, port);
/*     */       } else {
/* 126 */         throw new IllegalStateException("Zookeeper port " + port + " was already in use. Running in single machine mode?");
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   private void startSingleZkServer(int tickTime, File dataDir, File dataLogDir, int port) {
/*     */     try {
/* 133 */       this._zk = new ZooKeeperServer(dataDir, dataLogDir, tickTime);
/* 134 */       this._zk.setMinSessionTimeout(this._minSessionTimeout);
/* 135 */       this._nioFactory = new NIOServerCnxn.Factory(new InetSocketAddress(port));
/* 136 */       this._nioFactory.startup(this._zk);
/*     */     } catch (IOException e) {
/* 138 */       throw new ZkException("Unable to start single ZooKeeper server.", e);
/*     */     } catch (InterruptedException e) {
/* 140 */       throw new ZkInterruptedException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   @PreDestroy
/*     */   public void shutdown() {
/* 146 */     LOG.info("Shutting down ZkServer...");
/*     */     try {
/* 148 */       this._zkClient.close();
/*     */     } catch (ZkException e) {
/* 150 */       LOG.warn("Error on closing zkclient: " + e.getClass().getName());
/*     */     }
/* 152 */     if (this._nioFactory != null) {
/* 153 */       this._nioFactory.shutdown();
/*     */       try {
/* 155 */         this._nioFactory.join();
/*     */       } catch (InterruptedException e) {
/* 157 */         Thread.currentThread().interrupt();
/*     */       }
/* 159 */       this._nioFactory = null;
/*     */     }
/* 161 */     if (this._zk != null) {
/* 162 */       this._zk.shutdown();
/* 163 */       this._zk = null;
/*     */     }
/* 165 */     LOG.info("Shutting down ZkServer...done");
/*     */   }
/*     */ 
/*     */   public ZkClient getZkClient() {
/* 169 */     return this._zkClient;
/*     */   }
/*     */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.ZkServer
 * JD-Core Version:    0.6.2
 */