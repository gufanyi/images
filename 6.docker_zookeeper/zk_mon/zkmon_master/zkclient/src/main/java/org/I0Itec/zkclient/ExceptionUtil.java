/*    */ package org.I0Itec.zkclient;
/*    */ 
/*    */ import org.I0Itec.zkclient.exception.ZkInterruptedException;
/*    */ 
/*    */ public class ExceptionUtil
/*    */ {
/*    */   public static RuntimeException convertToRuntimeException(Throwable e)
/*    */   {
/* 23 */     if ((e instanceof RuntimeException)) {
/* 24 */       return (RuntimeException)e;
/*    */     }
/* 26 */     retainInterruptFlag(e);
/* 27 */     return new RuntimeException(e);
/*    */   }
/*    */ 
/*    */   public static void retainInterruptFlag(Throwable catchedException)
/*    */   {
/* 38 */     if ((catchedException instanceof InterruptedException))
/* 39 */       Thread.currentThread().interrupt();
/*    */   }
/*    */ 
/*    */   public static void rethrowInterruptedException(Throwable e) throws InterruptedException
/*    */   {
/* 44 */     if ((e instanceof InterruptedException)) {
/* 45 */       throw ((InterruptedException)e);
/*    */     }
/* 47 */     if ((e instanceof ZkInterruptedException))
/* 48 */       throw ((ZkInterruptedException)e);
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.ExceptionUtil
 * JD-Core Version:    0.6.2
 */