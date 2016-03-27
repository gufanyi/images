package org.I0Itec.zkclient;

import org.apache.zookeeper.Watcher.Event.KeeperState;

public abstract interface IZkStateListener
{
  public abstract void handleStateChanged(Watcher.Event.KeeperState paramKeeperState)
    throws Exception;

  public abstract void handleNewSession()
    throws Exception;
}

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.IZkStateListener
 * JD-Core Version:    0.6.2
 */