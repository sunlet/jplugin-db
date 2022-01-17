package net.jplugin.db.mysql.svr.req;


import io.netty.buffer.ByteBuf;

public interface IRequestObject {
  public void read(ByteBuf byteBuf);
}
