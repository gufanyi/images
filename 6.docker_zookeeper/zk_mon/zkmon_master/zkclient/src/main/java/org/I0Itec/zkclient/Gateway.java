/*    */ package org.I0Itec.zkclient;
/*    */ 
/*    */ public class Gateway
/*    */ {
/*    */   private GatewayThread _thread;
/*    */   private final int _port;
/*    */   private final int _destinationPort;
/*    */ 
/*    */   public Gateway(int port, int destinationPort)
/*    */   {
/* 25 */     this._port = port;
/* 26 */     this._destinationPort = destinationPort;
/*    */   }
/*    */ 
/*    */   public synchronized void start() {
/* 30 */     if (this._thread != null) {
/* 31 */       throw new IllegalStateException("Gateway already running");
/*    */     }
/* 33 */     this._thread = new GatewayThread(this._port, this._destinationPort);
/* 34 */     this._thread.start();
/* 35 */     this._thread.awaitUp();
/*    */   }
/*    */ 
/*    */   public synchronized void stop() {
/* 39 */     if (this._thread != null) {
/*    */       try {
/* 41 */         this._thread.interruptAndJoin();
/*    */       } catch (InterruptedException e) {
/* 43 */         Thread.currentThread().interrupt();
/*    */       }
/* 45 */       this._thread = null;
/*    */     }
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.Gateway
 * JD-Core Version:    0.6.2
 */