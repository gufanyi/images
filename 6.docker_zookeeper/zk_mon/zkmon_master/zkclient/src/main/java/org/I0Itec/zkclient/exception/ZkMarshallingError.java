/*    */ package org.I0Itec.zkclient.exception;
/*    */ 
/*    */ public class ZkMarshallingError extends ZkException
/*    */ {
/*    */   private static final long serialVersionUID = 1L;
/*    */ 
/*    */   public ZkMarshallingError()
/*    */   {
/*    */   }
/*    */ 
/*    */   public ZkMarshallingError(Throwable cause)
/*    */   {
/* 27 */     super(cause);
/*    */   }
/*    */ 
/*    */   public ZkMarshallingError(String message, Throwable cause) {
/* 31 */     super(message, cause);
/*    */   }
/*    */ 
/*    */   public ZkMarshallingError(String message) {
/* 35 */     super(message);
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.exception.ZkMarshallingError
 * JD-Core Version:    0.6.2
 */