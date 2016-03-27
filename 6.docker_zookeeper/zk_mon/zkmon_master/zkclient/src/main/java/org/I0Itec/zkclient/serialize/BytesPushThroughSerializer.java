/*    */ package org.I0Itec.zkclient.serialize;
/*    */ 
/*    */ import org.I0Itec.zkclient.exception.ZkMarshallingError;
/*    */ 
/*    */ public class BytesPushThroughSerializer
/*    */   implements ZkSerializer
/*    */ {
/*    */   public Object deserialize(byte[] bytes)
/*    */     throws ZkMarshallingError
/*    */   {
/* 27 */     return bytes;
/*    */   }
/*    */ 
/*    */   public byte[] serialize(Object bytes) throws ZkMarshallingError
/*    */   {
/* 32 */     return (byte[])bytes;
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.serialize.BytesPushThroughSerializer
 * JD-Core Version:    0.6.2
 */