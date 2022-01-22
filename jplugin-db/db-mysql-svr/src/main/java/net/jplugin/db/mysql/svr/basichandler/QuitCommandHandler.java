package net.jplugin.db.mysql.svr.basichandler;

import net.jplugin.core.kernel.api.BindExtension;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.api.ICommandHandler;
import net.jplugin.db.mysql.svr.consts.Constants;
import net.jplugin.db.mysql.svr.req.CommandRequest;
import net.jplugin.db.mysql.svr.resp.SuccessResponse;


@BindExtension(pointTo = net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_COMMAND_HANDLER, name = Constants.COMMAND_QUIT)
public class QuitCommandHandler implements ICommandHandler {

    @Override
    public void handleCommand(ConnectionContext connCtx, CommandRequest commandPackage) {
        SuccessResponse resp = SuccessResponse.create(0, 0);
        connCtx.setResponseObject(resp);
    }


}
