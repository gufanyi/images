package org.I0Itec.zkclient.serialize;

import org.I0Itec.zkclient.exception.ZkMarshallingError;

public abstract interface ZkSerializer
{
  public abstract byte[] serialize(Object paramObject)
    throws ZkMarshallingError;

  public abstract Object deserialize(byte[] paramArrayOfByte)
    throws ZkMarshallingError;
}

/* Location:           D:\Workspaces\dubbox\taokeeper-master\lib\zkclient-0.2-20120215.025626-3.jar
 * Qualified Name:     org.I0Itec.zkclient.serialize.ZkSerializer
 * JD-Core Version:    0.6.2
 */