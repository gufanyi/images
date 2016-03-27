/*    */ package org.I0Itec.zkclient;
/*    */ 
/*    */ import java.util.concurrent.BlockingQueue;
/*    */ import java.util.concurrent.LinkedBlockingQueue;
/*    */ import java.util.concurrent.atomic.AtomicInteger;
/*    */ import org.I0Itec.zkclient.exception.ZkInterruptedException;
/*    */ import org.apache.log4j.Logger;
/*    */ 
/*    */ class ZkEventThread extends Thread
/*    */ {
/* 35 */   private static final Logger LOG = Logger.getLogger(ZkEventThread.class);
/*    */ 
/* 37 */   private BlockingQueue<ZkEvent> _events = new LinkedBlockingQueue();
/*    */ 
/* 39 */   private static AtomicInteger _eventId = new AtomicInteger(0);
/*    */ 
/*    */   ZkEventThread(String name)
/*    */   {
/* 58 */     setDaemon(true);
/* 59 */     setName("ZkClient-EventThread-" + getId() + "-" + name);
/*    */   }
/*    */ 
/*    */   public void run()
/*    */   {
/* 64 */     LOG.info("Starting ZkClient event thread.");
/*    */     try {
/* 66 */       while (!isInterrupted()) {
/* 67 */         ZkEvent zkEvent = (ZkEvent)this._events.take();
/* 68 */         int eventId = _eventId.incrementAndGet();
/* 69 */         LOG.debug("Delivering event #" + eventId + " " + zkEvent);
/*    */         try {
/* 71 */           zkEvent.run();
/*    */         } catch (InterruptedException e) {
/* 73 */           interrupt();
/*    */         } catch (ZkInterruptedException e) {
/* 75 */           interrupt();
/*    */         } catch (Throwable e) {
/* 77 */           LOG.error("Error handling event " + zkEvent, e);
/*    */         }
/* 79 */         LOG.debug("Delivering event #" + eventId + " done");
/*    */       }
/*    */     } catch (InterruptedException e) {
/* 82 */       LOG.info("Terminate ZkClient event thread.");
/*    */     }
/*    */   }
/*    */ 
/*    */   public void send(ZkEvent event) {
/* 87 */     if (!isInterrupted()) {
/* 88 */       LOG.debug("New event: " + event);
/* 89 */       this._events.add(event);
/*    */     }
/*    */   }
/*    */ 
/*    */   static abstract class ZkEvent
/*    */   {
/*    */     private String _description;
/*    */ 
/*    */     public ZkEvent(String description)
/*    */     {
/* 46 */       this._description = description;
/*    */     }
/*    */ 
/*    */     public abstract void run() throws Exception;
/*    */ 
/*    */     public String toString()
/*    */     {
/* 53 */       return "ZkEvent[" + this._description + "]";
/*    */     }
/*    */   }
/*    */ }

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.ZkEventThread
 * JD-Core Version:    0.6.2
 */