package net.jplugin.db.mysql.svr.channelhandlers;

import java.util.List;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.core.kernel.api.RefExtension;
import net.jplugin.core.kernel.api.RefExtensions;
import net.jplugin.core.log.api.Logger;
import net.jplugin.core.log.api.RefLogger;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.api.IResponseObject;
import net.jplugin.db.mysql.svr.api.IServerGreetingHandler;
import net.jplugin.db.mysql.svr.req.AuthRequest;
import net.jplugin.db.mysql.svr.resp.ServerGreetingResponse;
import net.jplugin.db.mysql.svr.resp.SuccessResponse;
import net.jplugin.db.mysql.svr.utils.Util;

public class TcpConnectionHandler extends ChannelInboundHandlerAdapter {

    public static final TcpConnectionHandler INSTANCE = new TcpConnectionHandler();
    
    @RefLogger
    Logger logger;
    
    public TcpConnectionHandler() {
    	PluginEnvirement.INSTANCE.resolveRefAnnotation(this);
    }
    
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        
        //init ConnectionContext
        ConnectionContext connCtx = Util.createConnContext(ctx);
        
        if (logger.isInfoEnabled()) 
        	logger.info("Channel Active:"+ctx.channel().id());
        
        //todo, do some log
//        Channel channel = ctx.channel();
//        sendAuthencationPackage(channel);
        handleGreeting(connCtx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
    	if (logger.isInfoEnabled()) 
    		logger.info("Channel InActive:"+ctx.channel().id());
    }
    
  
    
    @RefExtension(pointTo = net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_GREETING_HANDLER)
    IServerGreetingHandler greetingHandler;    
    private void handleGreeting(ConnectionContext connCtx) {
        if (logger.isInfoEnabled()) {
            logger.info("Now handler Server Greeting.");
        }

        IResponseObject response;
        try {
        	//查找优先级最高的一个执行
        	greetingHandler.handleGreeting(connCtx);
	        response = connCtx.getResponseObject();
        }catch(Throwable th) {
        	response = null;
        }
        
        Channel channel = connCtx.getChannelHandlerContext().channel();
        
        //如果response是空，或者channel非活动，断开连接
        if (response==null || !channel.isActive()) {
        	channel.disconnect();
        }else {
        	ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(128);
        	response.write(byteBuf);
            channel.writeAndFlush(byteBuf);
        }
    }


}
