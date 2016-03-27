/*    */ package org.I0Itec.zkclient;
/*    */ 
/*    */ import java.util.concurrent.locks.Condition;
/*    */ import java.util.concurrent.locks.ReentrantLock;
/*    */ 
/*    */ public class ZkLock extends ReentrantLock
/*    */ {
/*    */   private static final long serialVersionUID = 1L;
/* 25 */   private Condition _dataChangedCondition = newCondition();
/* 26 */   private Condition _stateChangedCondition = newCondition();
/* 27 */   private Condition _zNodeEventCondition = newCondition();
/*    */ 
/*    */   public Condition getDataChangedCondition()
/*    */   {
/* 35 */     return this._dataChangedCondition;
/*    */   }
/*    */ 
/*    */   public Condition getStateChangedCondition()
/*    */   {
/* 45 */     return this._stateChangedCondition;
/*    */   }
/*    */ 
/*    */   public Condition getZNodeEventCondition()
/*    */   {
/* 54 */     return this._zNodeEventCondition;
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.ZkLock
 * JD-Core Version:    0.6.2
 */