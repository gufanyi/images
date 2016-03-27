/*     */ package org.I0Itec.zkclient;
/*     */ 
/*     */ import java.io.IOException;
/*     */ import java.net.ConnectException;
/*     */ import java.net.InetAddress;
/*     */ import java.net.NetworkInterface;
/*     */ import java.net.Socket;
/*     */ import java.net.SocketException;
/*     */ import java.net.UnknownHostException;
/*     */ import java.util.Enumeration;
/*     */ import java.util.HashSet;
/*     */ import java.util.Set;
/*     */ 
/*     */ public class NetworkUtil
/*     */ {
/*     */   public static final String OVERWRITE_HOSTNAME_SYSTEM_PROPERTY = "zkclient.hostname.overwritten";
/*     */ 
/*     */   public static String[] getLocalHostNames()
/*     */   {
/*  34 */     Set hostNames = new HashSet();
/*     */ 
/*  39 */     hostNames.add("localhost");
/*     */     try {
/*  41 */       Enumeration networkInterfaces = NetworkInterface.getNetworkInterfaces();
/*  42 */       for (ifaces = networkInterfaces; ifaces.hasMoreElements(); ) {
/*  43 */         NetworkInterface iface = (NetworkInterface)ifaces.nextElement();
/*  44 */         ia = null;
/*  45 */         for (ips = iface.getInetAddresses(); ips.hasMoreElements(); ) {
/*  46 */           ia = (InetAddress)ips.nextElement();
/*  47 */           hostNames.add(ia.getCanonicalHostName());
/*  48 */           hostNames.add(ipToString(ia.getAddress()));
/*     */         }
/*     */       }
/*     */     }
/*     */     catch (SocketException e)
/*     */     {
/*     */       Enumeration ifaces;
/*     */       InetAddress ia;
/*     */       Enumeration ips;
/*  52 */       throw new RuntimeException("unable to retrieve host names of localhost");
/*     */     }
/*  54 */     return (String[])hostNames.toArray(new String[hostNames.size()]);
/*     */   }
/*     */ 
/*     */   private static String ipToString(byte[] bytes) {
/*  58 */     StringBuffer addrStr = new StringBuffer();
/*  59 */     for (int cnt = 0; cnt < bytes.length; cnt++) {
/*  60 */       int uByte = bytes[cnt] < 0 ? bytes[cnt] + 256 : bytes[cnt];
/*  61 */       addrStr.append(uByte);
/*  62 */       if (cnt < 3)
/*  63 */         addrStr.append('.');
/*     */     }
/*  65 */     return addrStr.toString();
/*     */   }
/*     */ 
/*     */   public static int hostNamesInList(String serverList, String[] hostNames) {
/*  69 */     String[] serverNames = serverList.split(",");
/*  70 */     for (int i = 0; i < hostNames.length; i++) {
/*  71 */       String hostname = hostNames[i];
/*  72 */       for (int j = 0; j < serverNames.length; j++) {
/*  73 */         String serverNameAndPort = serverNames[j];
/*  74 */         String serverName = serverNameAndPort.split(":")[0];
/*  75 */         if (serverName.equalsIgnoreCase(hostname)) {
/*  76 */           return j;
/*     */         }
/*     */       }
/*     */     }
/*  80 */     return -1;
/*     */   }
/*     */ 
/*     */   public static boolean hostNameInArray(String[] hostNames, String hostName) {
/*  84 */     for (String name : hostNames) {
/*  85 */       if (name.equalsIgnoreCase(hostName)) {
/*  86 */         return true;
/*     */       }
/*     */     }
/*  89 */     return false;
/*     */   }
/*     */ 
/*     */   public static boolean isPortFree(int port) {
/*     */     try {
/*  94 */       Socket socket = new Socket("localhost", port);
/*  95 */       socket.close();
/*  96 */       return false;
/*     */     } catch (ConnectException e) {
/*  98 */       return true;
/*     */     } catch (SocketException e) {
/* 100 */       if (e.getMessage().equals("Connection reset by peer")) {
/* 101 */         return true;
/*     */       }
/* 103 */       throw new RuntimeException(e);
/*     */     } catch (UnknownHostException e) {
/* 105 */       throw new RuntimeException(e);
/*     */     } catch (IOException e) {
/* 107 */       throw new RuntimeException(e);
/*     */     }
/*     */   }
/*     */ 
/*     */   public static String getLocalhostName() {
/* 112 */     String property = System.getProperty("zkclient.hostname.overwritten");
/* 113 */     if ((property != null) && (property.trim().length() > 0))
/* 114 */       return property;
/*     */     try
/*     */     {
/* 117 */       return InetAddress.getLocalHost().getHostName(); } catch (UnknownHostException e) {
/*     */     }
/* 119 */     throw new RuntimeException("unable to retrieve localhost name");
/*     */   }
/*     */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.NetworkUtil
 * JD-Core Version:    0.6.2
 */