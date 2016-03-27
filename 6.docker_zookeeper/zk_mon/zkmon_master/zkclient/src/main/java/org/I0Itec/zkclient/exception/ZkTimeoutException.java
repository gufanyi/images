/*    */ package org.I0Itec.zkclient.exception;
/*    */ 
/*    */ public class ZkTimeoutException extends ZkException
/*    */ {
/*    */   private static final long serialVersionUID = 1L;
/*    */ 
/*    */   public ZkTimeoutException()
/*    */   {
/*    */   }
/*    */ 
/*    */   public ZkTimeoutException(String message, Throwable cause)
/*    */   {
/* 27 */     super(message, cause);
/*    */   }
/*    */ 
/*    */   public ZkTimeoutException(String message) {
/* 31 */     super(message);
/*    */   }
/*    */ 
/*    */   public ZkTimeoutException(Throwable cause) {
/* 35 */     super(cause);
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.exception.ZkTimeoutException
 * JD-Core Version:    0.6.2
 */