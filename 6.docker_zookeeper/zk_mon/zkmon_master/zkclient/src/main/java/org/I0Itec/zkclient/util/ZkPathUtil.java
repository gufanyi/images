/*    */ package org.I0Itec.zkclient.util;
/*    */ 
/*    */ import java.util.List;
/*    */ import org.I0Itec.zkclient.ZkClient;
/*    */ 
/*    */ public class ZkPathUtil
/*    */ {
/*    */   public static String leadingZeros(long number, int numberOfLeadingZeros)
/*    */   {
/* 25 */     return String.format("%0" + numberOfLeadingZeros + "d", new Object[] { Long.valueOf(number) });
/*    */   }
/*    */ 
/*    */   public static String toString(ZkClient zkClient) {
/* 29 */     return toString(zkClient, "/", PathFilter.ALL);
/*    */   }
/*    */ 
/*    */   public static String toString(ZkClient zkClient, String startPath, PathFilter pathFilter) {
/* 33 */     int level = 1;
/* 34 */     StringBuilder builder = new StringBuilder("+ (" + startPath + ")");
/* 35 */     builder.append("\n");
/* 36 */     addChildrenToStringBuilder(zkClient, pathFilter, 1, builder, startPath);
/* 37 */     return builder.toString();
/*    */   }
/*    */ 
/*    */   private static void addChildrenToStringBuilder(ZkClient zkClient, PathFilter pathFilter, int level, StringBuilder builder, String startPath) {
/* 41 */     List children = zkClient.getChildren(startPath);
/* 42 */     for (String node : children)
/*    */     {
/*    */       String nestedPath;
/*    */       String nestedPath;
/* 44 */       if (startPath.endsWith("/"))
/* 45 */         nestedPath = startPath + node;
/*    */       else {
/* 47 */         nestedPath = startPath + "/" + node;
/*    */       }
/* 49 */       if (pathFilter.showChilds(nestedPath)) {
/* 50 */         builder.append(getSpaces(level - 1) + "'-" + "+" + node + "\n");
/* 51 */         addChildrenToStringBuilder(zkClient, pathFilter, level + 1, builder, nestedPath);
/*    */       } else {
/* 53 */         builder.append(getSpaces(level - 1) + "'-" + "-" + node + " (contents hidden)\n");
/*    */       }
/*    */     }
/*    */   }
/*    */ 
/*    */   private static String getSpaces(int level) {
/* 59 */     String s = "";
/* 60 */     for (int i = 0; i < level; i++) {
/* 61 */       s = s + "  ";
/*    */     }
/* 63 */     return s;
/*    */   }
/*    */ 
/*    */   public static abstract interface PathFilter
/*    */   {
/* 68 */     public static final PathFilter ALL = new PathFilter()
/*    */     {
/*    */       public boolean showChilds(String path)
/*    */       {
/* 72 */         return true;
/*    */       }
/* 68 */     };
/*    */ 
/*    */     public abstract boolean showChilds(String paramString);
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.util.ZkPathUtil
 * JD-Core Version:    0.6.2
 */