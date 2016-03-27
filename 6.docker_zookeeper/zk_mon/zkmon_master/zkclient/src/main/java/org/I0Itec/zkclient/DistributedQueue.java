/*     */ package org.I0Itec.zkclient;
/*     */ 
/*     */ import java.io.Serializable;
/*     */ import java.util.List;
/*     */ import org.I0Itec.zkclient.exception.ZkNoNodeException;
/*     */ 
/*     */ public class DistributedQueue<T extends Serializable>
/*     */ {
/*     */   private ZkClient _zkClient;
/*     */   private String _root;
/*     */   private static final String ELEMENT_NAME = "element";
/*     */ 
/*     */   public DistributedQueue(ZkClient zkClient, String root)
/*     */   {
/*  49 */     this._zkClient = zkClient;
/*  50 */     this._root = root;
/*     */   }
/*     */ 
/*     */   public boolean offer(T element) {
/*     */     try {
/*  55 */       this._zkClient.createPersistentSequential(this._root + "/" + "element" + "-", element);
/*     */     } catch (Exception e) {
/*  57 */       throw ExceptionUtil.convertToRuntimeException(e);
/*     */     }
/*  59 */     return true;
/*     */   }
/*     */ 
/*     */   public T poll() {
/*     */     while (true) {
/*  64 */       Element element = getFirstElement();
/*  65 */       if (element == null) {
/*  66 */         return null;
/*     */       }
/*     */       try
/*     */       {
/*  70 */         this._zkClient.delete(element.getName());
/*  71 */         return (Serializable)element.getData();
/*     */       }
/*     */       catch (ZkNoNodeException e) {
/*     */       }
/*     */       catch (Exception e) {
/*  76 */         throw ExceptionUtil.convertToRuntimeException(e);
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   private String getSmallestElement(List<String> list) {
/*  82 */     String smallestElement = (String)list.get(0);
/*  83 */     for (String element : list) {
/*  84 */       if (element.compareTo(smallestElement) < 0) {
/*  85 */         smallestElement = element;
/*     */       }
/*     */     }
/*     */ 
/*  89 */     return smallestElement;
/*     */   }
/*     */ 
/*     */   public boolean isEmpty() {
/*  93 */     return this._zkClient.getChildren(this._root).size() == 0;
/*     */   }
/*     */ 
/*     */   private Element<T> getFirstElement()
/*     */   {
/*     */     try {
/*     */       while (true) {
/* 100 */         List list = this._zkClient.getChildren(this._root);
/* 101 */         if (list.size() == 0) {
/* 102 */           return null;
/*     */         }
/* 104 */         String elementName = getSmallestElement(list);
/*     */         try
/*     */         {
/* 107 */           return new Element(this._root + "/" + elementName, (Serializable)this._zkClient.readData(this._root + "/" + elementName));
/*     */         }
/*     */         catch (ZkNoNodeException e) {
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (Exception e) {
/* 114 */       throw ExceptionUtil.convertToRuntimeException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public T peek() {
/* 119 */     Element element = getFirstElement();
/* 120 */     if (element == null) {
/* 121 */       return null;
/*     */     }
/* 123 */     return (Serializable)element.getData();
/*     */   }
/*     */ 
/*     */   private static class Element<T>
/*     */   {
/*     */     private String _name;
/*     */     private T _data;
/*     */ 
/*     */     public Element(String name, T data)
/*     */     {
/*  30 */       this._name = name;
/*  31 */       this._data = data;
/*     */     }
/*     */ 
/*     */     public String getName() {
/*  35 */       return this._name;
/*     */     }
/*     */ 
/*     */     public T getData() {
/*  39 */       return this._data;
/*     */     }
/*     */   }
/*     */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.DistributedQueue
 * JD-Core Version:    0.6.2
 */