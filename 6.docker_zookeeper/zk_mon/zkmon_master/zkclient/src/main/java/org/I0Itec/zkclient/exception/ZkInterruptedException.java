/*    */ package org.I0Itec.zkclient.exception;
/*    */ 
/*    */ public class ZkInterruptedException extends ZkException
/*    */ {
/*    */   private static final long serialVersionUID = 1L;
/*    */ 
/*    */   public ZkInterruptedException(InterruptedException e)
/*    */   {
/* 23 */     super(e);
/* 24 */     Thread.currentThread().interrupt();
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.exception.ZkInterruptedException
 * JD-Core Version:    0.6.2
 */