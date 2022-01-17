package net.jplugin.db.mysql.svr.api;

import io.netty.buffer.ByteBuf;

public interface IResponseObject {
	
    void write(ByteBuf byteBuf); 
}
