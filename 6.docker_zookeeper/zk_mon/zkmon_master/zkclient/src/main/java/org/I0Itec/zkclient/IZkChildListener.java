package org.I0Itec.zkclient;

import java.util.List;

public abstract interface IZkChildListener
{
  public abstract void handleChildChange(String paramString, List<String> paramList)
    throws Exception;
}

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.IZkChildListener
 * JD-Core Version:    0.6.2
 */