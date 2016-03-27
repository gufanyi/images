/*    */ package org.I0Itec.zkclient.serialize;
/*    */ 
/*    */ import java.io.ByteArrayInputStream;
/*    */ import java.io.ByteArrayOutputStream;
/*    */ import java.io.IOException;
/*    */ import java.io.ObjectInputStream;
/*    */ import java.io.ObjectOutputStream;
/*    */ import org.I0Itec.zkclient.exception.ZkMarshallingError;
/*    */ 
/*    */ public class SerializableSerializer
/*    */   implements ZkSerializer
/*    */ {
/*    */   public Object deserialize(byte[] bytes)
/*    */     throws ZkMarshallingError
/*    */   {
/*    */     try
/*    */     {
/* 31 */       ObjectInputStream inputStream = new ObjectInputStream(new ByteArrayInputStream(bytes));
/* 32 */       return inputStream.readObject();
/*    */     }
/*    */     catch (ClassNotFoundException e) {
/* 35 */       throw new ZkMarshallingError("Unable to find object class.", e);
/*    */     } catch (IOException e) {
/* 37 */       throw new ZkMarshallingError(e);
/*    */     }
/*    */   }
/*    */ 
/*    */   public byte[] serialize(Object serializable) throws ZkMarshallingError
/*    */   {
/*    */     try {
/* 44 */       ByteArrayOutputStream byteArrayOS = new ByteArrayOutputStream();
/* 45 */       ObjectOutputStream stream = new ObjectOutputStream(byteArrayOS);
/* 46 */       stream.writeObject(serializable);
/* 47 */       stream.close();
/* 48 */       return byteArrayOS.toByteArray();
/*    */     } catch (IOException e) {
/* 50 */       throw new ZkMarshallingError(e);
/*    */     }
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.serialize.SerializableSerializer
 * JD-Core Version:    0.6.2
 */