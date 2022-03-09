package net.jplugin.db.mysql.svr.defaulthandler;

import net.jplugin.core.kernel.api.BindExtension;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.api.IServerGreetingHandler;
import net.jplugin.db.mysql.svr.resp.ServerGreetingResponse;

//默认实现优先级100
@BindExtension(pointTo = net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_GREETING_HANDLER, priority = 100)
public class DefaultServerGreetingHandler implements IServerGreetingHandler {

	@Override
	public void handleGreeting(ConnectionContext connCtx) {
		connCtx.setResponseObject(ServerGreetingResponse.create());
	}
}
