package net.jplugin.db.app.extensions;

import net.jplugin.core.kernel.api.BindExtension;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.api.ICommandHandler;
import net.jplugin.db.mysql.svr.consts.Constants;
import net.jplugin.db.mysql.svr.req.CommandRequest;
import net.jplugin.db.mysql.svr.resp.SuccessResponse;



/**
 * 执行Use命令
 * @author Administrator
 *
 */

@BindExtension(pointTo = net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_COMMAND_HANDLER, name = Constants.COMMAND_USE_DB)
public class UseHandler implements ICommandHandler {

	@Override
	public void handleCommand(ConnectionContext connCtx, CommandRequest commandPackage) {
		SuccessResponse resp = SuccessResponse.create(0, 0);
		connCtx.setCurrentDb(commandPackage.getCommand());
		connCtx.setResponseObject(resp);
	}

}
