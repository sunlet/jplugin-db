package net.jplugin.db.mysql.svr.utils;

import io.netty.channel.ChannelHandlerContext;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.consts.Constants;
import net.jplugin.db.mysql.svr.req.CommandRequest;

public class Util {

	private static final String COMMAND = "QUERY_STRING";
	public static ConnectionContext getConnContext(ChannelHandlerContext ctx) {
		//get
		ConnectionContext connCtx = ctx.channel().attr(Constants.ATTR_KEY_CONNECTION_CTX).get();

		//assert
		if (connCtx == null)
			throw new RuntimeException("Conn ctx can't be null");
		return connCtx;
	}

	public static ConnectionContext createConnContext(ChannelHandlerContext ctx) {
		//assert
		if (ctx.channel().attr(Constants.ATTR_KEY_CONNECTION_CTX).get()!=null)
			throw new RuntimeException("Conn ctx must be null");
		
		//create and set
		ConnectionContext connCtx = new net.jplugin.db.mysql.svr.api.ConnectionContext(ctx);
        ctx.channel().attr(Constants.ATTR_KEY_CONNECTION_CTX).set(connCtx);
        return connCtx;
	}
	
	public static CommandRequest getCommandRequest(ConnectionContext ctx) {
		return (CommandRequest)ctx.getRequestAttributes().get(COMMAND);
	}
	public static void setCommand(ConnectionContext ctx,CommandRequest c) {
		ctx.getRequestAttributes().put(COMMAND,c);
	}
	
	public static String getCommandQuery(ConnectionContext ctx) {
		return getCommandRequest(ctx).getCommand();
	}


}
