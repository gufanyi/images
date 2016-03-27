/*     */ package org.I0Itec.zkclient;
/*     */ 
/*     */ import java.io.Closeable;
/*     */ import java.io.IOException;
/*     */ import java.io.InputStream;
/*     */ import java.io.OutputStream;
/*     */ import java.net.ServerSocket;
/*     */ import java.net.Socket;
/*     */ import java.net.SocketException;
/*     */ import java.util.ArrayList;
/*     */ import java.util.List;
/*     */ import java.util.Vector;
/*     */ import java.util.concurrent.locks.Condition;
/*     */ import java.util.concurrent.locks.Lock;
/*     */ import java.util.concurrent.locks.ReentrantLock;
/*     */ import org.apache.log4j.Logger;
/*     */ 
/*     */ public class GatewayThread extends Thread
/*     */ {
/*  36 */   protected static final Logger LOG = Logger.getLogger(GatewayThread.class);
/*     */   private final int _port;
/*     */   private final int _destinationPort;
/*     */   private ServerSocket _serverSocket;
/*  41 */   private Lock _lock = new ReentrantLock();
/*  42 */   private Condition _runningCondition = this._lock.newCondition();
/*  43 */   private boolean _running = false;
/*     */ 
/*     */   public GatewayThread(int port, int destinationPort) {
/*  46 */     this._port = port;
/*  47 */     this._destinationPort = destinationPort;
/*  48 */     setDaemon(true);
/*     */   }
/*     */ 
/*     */   public void run()
/*     */   {
/*  53 */     final List runningThreads = new Vector();
/*     */     try {
/*  55 */       LOG.info("Starting gateway on port " + this._port + " pointing to port " + this._destinationPort);
/*  56 */       this._serverSocket = new ServerSocket(this._port);
/*  57 */       this._lock.lock();
/*     */       try {
/*  59 */         this._running = true;
/*  60 */         this._runningCondition.signalAll();
/*     */       } finally {
/*  62 */         this._lock.unlock();
/*     */       }while (true) { final Socket socket = this._serverSocket.accept();
/*  66 */         LOG.info("new client is connected " + socket.getInetAddress());
/*  67 */         final InputStream incomingInputStream = socket.getInputStream();
/*  68 */         final OutputStream incomingOutputStream = socket.getOutputStream();
/*     */         final Socket outgoingSocket;
/*     */         try { outgoingSocket = new Socket("localhost", this._destinationPort);
/*     */         } catch (Exception e) {
/*  74 */           LOG.warn("could not connect to " + this._destinationPort);
/*  75 */         }continue;
/*     */ 
/*  77 */         final InputStream outgoingInputStream = outgoingSocket.getInputStream();
/*  78 */         final OutputStream outgoingOutputStream = outgoingSocket.getOutputStream();
/*     */ 
/*  80 */         Thread writeThread = new Thread()
/*     */         {
/*     */           public void run() {
/*  83 */             runningThreads.add(this);
/*     */             try {
/*  85 */               int read = -1;
/*  86 */               while ((read = incomingInputStream.read()) != -1)
/*  87 */                 outgoingOutputStream.write(read);
/*     */             }
/*     */             catch (IOException e) {
/*     */             }
/*     */             finally {
/*  92 */               GatewayThread.this.closeQuietly(outgoingOutputStream);
/*  93 */               runningThreads.remove(this);
/*     */             }
/*     */           }
/*     */ 
/*     */           public void interrupt()
/*     */           {
/*     */             try {
/* 100 */               socket.close();
/* 101 */               outgoingSocket.close();
/*     */             } catch (IOException e) {
/* 103 */               GatewayThread.LOG.error("error on stopping closing sockets", e);
/*     */             }
/*     */ 
/* 106 */             super.interrupt();
/*     */           }
/*     */         };
/* 110 */         Thread readThread = new Thread()
/*     */         {
/*     */           public void run() {
/* 113 */             runningThreads.add(this);
/*     */             try {
/* 115 */               int read = -1;
/* 116 */               while ((read = outgoingInputStream.read()) != -1)
/* 117 */                 incomingOutputStream.write(read);
/*     */             }
/*     */             catch (IOException e) {
/*     */             }
/*     */             finally {
/* 122 */               GatewayThread.this.closeQuietly(incomingOutputStream);
/* 123 */               runningThreads.remove(this);
/*     */             }
/*     */           }
/*     */         };
/* 128 */         writeThread.setDaemon(true);
/* 129 */         readThread.setDaemon(true);
/*     */ 
/* 131 */         writeThread.start();
/* 132 */         readThread.start(); }
/*     */     }
/*     */     catch (SocketException e) {
/* 135 */       if (!this._running) {
/* 136 */         throw ExceptionUtil.convertToRuntimeException(e);
/*     */       }
/* 138 */       LOG.info("Stopping gateway");
/*     */     } catch (Exception e) {
/* 140 */       LOG.error("error on gateway execution", e);
/*     */     }
/*     */ 
/* 143 */     for (Thread thread : new ArrayList(runningThreads)) {
/* 144 */       thread.interrupt();
/*     */       try {
/* 146 */         thread.join();
/*     */       }
/*     */       catch (InterruptedException e) {
/*     */       }
/*     */     }
/*     */   }
/*     */ 
/*     */   protected void closeQuietly(Closeable closable) {
/*     */     try {
/* 155 */       closable.close();
/*     */     }
/*     */     catch (IOException e)
/*     */     {
/*     */     }
/*     */   }
/*     */ 
/*     */   public void interrupt() {
/*     */     try {
/* 164 */       this._serverSocket.close();
/*     */     } catch (Exception cE) {
/* 166 */       LOG.error("error on stopping gateway", cE);
/*     */     }
/* 168 */     super.interrupt();
/*     */   }
/*     */ 
/*     */   public void interruptAndJoin() throws InterruptedException {
/* 172 */     interrupt();
/* 173 */     join();
/*     */   }
/*     */ 
/*     */   public void awaitUp() {
/* 177 */     this._lock.lock();
/*     */     try {
/* 179 */       while (!this._running)
/* 180 */         this._runningCondition.await();
/*     */     }
/*     */     catch (InterruptedException e) {
/* 183 */       Thread.currentThread().interrupt();
/*     */     } finally {
/* 185 */       this._lock.unlock();
/*     */     }
/*     */   }
/*     */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.GatewayThread
 * JD-Core Version:    0.6.2
 */