/*    */ package org.I0Itec.zkclient.exception;
/*    */ 
/*    */ import org.apache.zookeeper.KeeperException;
/*    */ 
/*    */ public class ZkException extends RuntimeException
/*    */ {
/*    */   private static final long serialVersionUID = 1L;
/*    */ 
/*    */   public ZkException()
/*    */   {
/*    */   }
/*    */ 
/*    */   public ZkException(String message, Throwable cause)
/*    */   {
/* 29 */     super(message, cause);
/*    */   }
/*    */ 
/*    */   public ZkException(String message) {
/* 33 */     super(message);
/*    */   }
/*    */ 
/*    */   public ZkException(Throwable cause) {
/* 37 */     super(cause);
/*    */   }
/*    */ 
/*    */   public static ZkException create(KeeperException e) {
/* 41 */     switch (1.$SwitchMap$org$apache$zookeeper$KeeperException$Code[e.code().ordinal()])
/*    */     {
/*    */     case 1:
/* 47 */       return new ZkNoNodeException(e);
/*    */     case 2:
/* 51 */       return new ZkBadVersionException(e);
/*    */     case 3:
/* 55 */       return new ZkNodeExistsException(e);
/*    */     }
/*    */ 
/* 68 */     return new ZkException(e);
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.exception.ZkException
 * JD-Core Version:    0.6.2
 */