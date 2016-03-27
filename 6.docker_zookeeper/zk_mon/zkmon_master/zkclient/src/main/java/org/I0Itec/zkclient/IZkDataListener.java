package org.I0Itec.zkclient;

public abstract interface IZkDataListener
{
  public abstract void handleDataChange(String paramString, Object paramObject)
    throws Exception;

  public abstract void handleDataDeleted(String paramString)
    throws Exception;
}

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.IZkDataListener
 * JD-Core Version:    0.6.2
 */