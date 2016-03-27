package org.I0Itec.zkclient;

import java.util.List;
import org.apache.zookeeper.AsyncCallback.ChildrenCallback;
import org.apache.zookeeper.AsyncCallback.StringCallback;
import org.apache.zookeeper.AsyncCallback.VoidCallback;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper.States;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Stat;

public abstract interface IZkConnection
{
  public abstract void connect(Watcher paramWatcher);

  public abstract void close()
    throws InterruptedException;

  public abstract void delete(String paramString)
    throws InterruptedException, KeeperException;

  public abstract String create(String paramString, byte[] paramArrayOfByte, List<ACL> paramList, CreateMode paramCreateMode)
    throws KeeperException, InterruptedException;

  public abstract String create(String paramString, byte[] paramArrayOfByte, CreateMode paramCreateMode)
    throws KeeperException, InterruptedException;

  public abstract void create(String paramString, byte[] paramArrayOfByte, CreateMode paramCreateMode, AsyncCallback.StringCallback paramStringCallback, Object paramObject)
    throws KeeperException, InterruptedException;

  public abstract void delete(String paramString, AsyncCallback.VoidCallback paramVoidCallback, Object paramObject)
    throws InterruptedException, KeeperException;

  public abstract boolean exists(String paramString, boolean paramBoolean)
    throws KeeperException, InterruptedException;

  public abstract List<String> getChildren(String paramString, boolean paramBoolean)
    throws KeeperException, InterruptedException;

  public abstract void getChildren(String paramString, boolean paramBoolean, AsyncCallback.ChildrenCallback paramChildrenCallback, Object paramObject)
    throws KeeperException, InterruptedException;

  public abstract byte[] readData(String paramString, Stat paramStat, boolean paramBoolean)
    throws KeeperException, InterruptedException;

  public abstract void writeData(String paramString, byte[] paramArrayOfByte, int paramInt)
    throws KeeperException, InterruptedException;

  public abstract ZooKeeper.States getZookeeperState();

  public abstract long getCreateTime(String paramString)
    throws KeeperException, InterruptedException;

  public abstract String getServers();

  public abstract void addAuthInfo(String paramString, byte[] paramArrayOfByte);
}

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.IZkConnection
 * JD-Core Version:    0.6.2
 */