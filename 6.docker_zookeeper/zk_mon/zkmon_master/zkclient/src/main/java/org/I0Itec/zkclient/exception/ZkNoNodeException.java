/*    */ package org.I0Itec.zkclient.exception;
/*    */ 
/*    */ import org.apache.zookeeper.KeeperException;
/*    */ 
/*    */ public class ZkNoNodeException extends ZkException
/*    */ {
/*    */   private static final long serialVersionUID = 1L;
/*    */ 
/*    */   public ZkNoNodeException()
/*    */   {
/*    */   }
/*    */ 
/*    */   public ZkNoNodeException(KeeperException cause)
/*    */   {
/* 29 */     super(cause);
/*    */   }
/*    */ 
/*    */   public ZkNoNodeException(String message, KeeperException cause) {
/* 33 */     super(message, cause);
/*    */   }
/*    */ 
/*    */   public ZkNoNodeException(String message) {
/* 37 */     super(message);
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.exception.ZkNoNodeException
 * JD-Core Version:    0.6.2
 */