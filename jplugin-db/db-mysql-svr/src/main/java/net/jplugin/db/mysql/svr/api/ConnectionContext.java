package net.jplugin.db.mysql.svr.api;

import com.google.common.collect.Maps;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.ReferenceCountUtil;

import java.util.Map;

public class ConnectionContext {

    /**
     * 结果package
     */
    IResponseObject responseObject;

    /**
     * Connection
     */
    private ChannelHandlerContext channelHandlerContext;

    /**
     * if db is not null, this context may use db;
     */
    private String currentDb;
    
    /**
     * 	当前登录用户
     */
    private String currentUser;

    /**
              *   存储连接相关的属性，生命周期和Connection同步
     */
    private Map<String, Object> connectionProperties = Maps.newHashMap();
    
    /**
             * 存储和每次命令执行相关的属性，一次命令执行完毕，数据返回客户端后系统会清理这里的数据。
     */

    private Map<String, Object> requestProperties = Maps.newHashMap();
    
    boolean authed = false;

    public ConnectionContext(ChannelHandlerContext channelHandlerContext) {
        this.channelHandlerContext = channelHandlerContext;
    }

    public String getCurrentDb() {
        return currentDb;
    }

    public void setCurrentDb(String db) {
        this.currentDb = db;
    }

    public Map<String, Object> getConnectionAttributes() {
        return connectionProperties;
    }
    
    public Map<String, Object> getRequestAttributes() {
        return requestProperties;
    }

    public ChannelHandlerContext getChannelHandlerContext() {
        return channelHandlerContext;
    }

    public void write(ByteBuf byteBuf) {
        channelHandlerContext.writeAndFlush(byteBuf);
        if (ReferenceCountUtil.refCnt(byteBuf) > 0) {
            ReferenceCountUtil.release(byteBuf);
        }
    }

	public boolean isAuthrized() {
		return authed;
	}
	
	public void setAuthizeSuccess() {
		this.authed = true;
	}
	
    public String getCurrentUser() {
		return currentUser;
	}

	public void setCurrentUser(String currentUser) {
		this.currentUser = currentUser;
	}

	public IResponseObject getResponseObject() {
        return responseObject;
    }

    public void setResponseObject(IResponseObject resultObject) {
        this.responseObject = resultObject;
    }
}
