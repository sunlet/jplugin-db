package net.jplugin.db.mysql.svr.channelhandlers;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.Attribute;
import net.jplugin.common.kits.AssertKit;
import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.core.log.api.Logger;
import net.jplugin.core.log.api.RefLogger;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.req.AuthRequest;
import net.jplugin.db.mysql.svr.req.CommandRequest;
import net.jplugin.db.mysql.svr.req.IRequestObject;

import java.util.List;

public class ByteBufToRequestDecoder extends MessageToMessageDecoder<ByteBuf> {

	@RefLogger
	Logger logger;
	
	public ByteBufToRequestDecoder() {
		PluginEnvirement.INSTANCE.resolveRefAnnotation(this);
	}
    @Override
    
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        final IRequestObject result;
        
        ConnectionContext connCtx = channelHandlerContext.channel().attr(net.jplugin.db.mysql.svr.consts.Constants.ATTR_KEY_CONNECTION_CTX).get();

        if (connCtx.isAuthrized()) {
        	result = new CommandRequest();
        } else {
        	result = new AuthRequest();
        }

        result.read(byteBuf);
        list.add(result);
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
//    	logger.error("Exception ."+ctx.channel().id(),cause);
    }
}
