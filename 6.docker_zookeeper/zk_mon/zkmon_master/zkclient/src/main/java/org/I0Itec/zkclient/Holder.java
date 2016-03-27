/*    */ package org.I0Itec.zkclient;
/*    */ 
/*    */ public class Holder<T>
/*    */ {
/*    */   private T _value;
/*    */ 
/*    */   public Holder()
/*    */   {
/*    */   }
/*    */ 
/*    */   public Holder(T value)
/*    */   {
/* 27 */     this._value = value;
/*    */   }
/*    */ 
/*    */   public T get() {
/* 31 */     return this._value;
/*    */   }
/*    */ 
/*    */   public void set(T value) {
/* 35 */     this._value = value;
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.Holder
 * JD-Core Version:    0.6.2
 */