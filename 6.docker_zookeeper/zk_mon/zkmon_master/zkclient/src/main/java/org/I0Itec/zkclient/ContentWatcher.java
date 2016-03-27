/*    */ package org.I0Itec.zkclient;
/*    */ 
/*    */ import java.util.concurrent.locks.Condition;
/*    */ import java.util.concurrent.locks.Lock;
/*    */ import java.util.concurrent.locks.ReentrantLock;
/*    */ import org.I0Itec.zkclient.exception.ZkNoNodeException;
/*    */ import org.apache.log4j.Logger;
/*    */ 
/*    */ public final class ContentWatcher<T>
/*    */   implements IZkDataListener
/*    */ {
/* 31 */   private static final Logger LOG = Logger.getLogger(ContentWatcher.class);
/*    */ 
/* 33 */   private Lock _contentLock = new ReentrantLock(true);
/* 34 */   private Condition _contentAvailable = this._contentLock.newCondition();
/*    */   private Holder<T> _content;
/*    */   private String _fileName;
/*    */   private ZkClient _zkClient;
/*    */ 
/*    */   public ContentWatcher(ZkClient zkClient, String fileName)
/*    */   {
/* 41 */     this._fileName = fileName;
/* 42 */     this._zkClient = zkClient;
/*    */   }
/*    */ 
/*    */   public void start() {
/* 46 */     this._zkClient.subscribeDataChanges(this._fileName, this);
/* 47 */     readData();
/* 48 */     LOG.debug("Started ContentWatcher");
/*    */   }
/*    */ 
/*    */   private void readData()
/*    */   {
/*    */     try {
/* 54 */       setContent(this._zkClient.readData(this._fileName));
/*    */     }
/*    */     catch (ZkNoNodeException e) {
/*    */     }
/*    */   }
/*    */ 
/*    */   public void stop() {
/* 61 */     this._zkClient.unsubscribeDataChanges(this._fileName, this);
/*    */   }
/*    */ 
/*    */   public void setContent(T data) {
/* 65 */     LOG.debug("Received new data: " + data);
/* 66 */     this._contentLock.lock();
/*    */     try {
/* 68 */       this._content = new Holder(data);
/* 69 */       this._contentAvailable.signalAll();
/*    */     } finally {
/* 71 */       this._contentLock.unlock();
/*    */     }
/*    */   }
/*    */ 
/*    */   public void handleDataChange(String dataPath, Object data)
/*    */   {
/* 78 */     setContent(data);
/*    */   }
/*    */ 
/*    */   public void handleDataDeleted(String dataPath)
/*    */   {
/*    */   }
/*    */ 
/*    */   public T getContent() throws InterruptedException
/*    */   {
/* 87 */     this._contentLock.lock();
/*    */     try {
/* 89 */       while (this._content == null) {
/* 90 */         this._contentAvailable.await();
/*    */       }
/* 92 */       return this._content.get();
/*    */     } finally {
/* 94 */       this._contentLock.unlock();
/*    */     }
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.ContentWatcher
 * JD-Core Version:    0.6.2
 */