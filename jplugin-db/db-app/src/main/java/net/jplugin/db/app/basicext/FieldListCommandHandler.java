package net.jplugin.db.app.basicext;

import net.jplugin.core.kernel.api.BindExtension;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.api.ICommandHandler;
import net.jplugin.db.mysql.svr.consts.ColumnType;
import net.jplugin.db.mysql.svr.consts.Constants;
import net.jplugin.db.mysql.svr.req.CommandRequest;
import net.jplugin.db.mysql.svr.resp.ResultSetResponse;
import net.jplugin.db.mysql.svr.resp.SuccessResponse;

import java.util.ArrayList;
import java.util.List;


@BindExtension(pointTo = net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_COMMAND_HANDLER, name = Constants.COMMAND_FIELD_LIST)
public class FieldListCommandHandler implements ICommandHandler {

    @Override
    public void handleCommand(ConnectionContext connCtx, CommandRequest commandPackage) {
        connCtx.setCurrentDb(commandPackage.getCommand());

        List<Integer> columnTypes = new ArrayList<>();
        columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
        List<List<String>> datas = new ArrayList<>();
        List<String> rowDatas = new ArrayList<>();
        rowDatas.add("jplugin");
        datas.add(rowDatas);
        List<String> columnNames = new ArrayList<>();
        columnNames.add("comment");
        String schema = "def";
        String table = "system";

        connCtx.setResponseObject(ResultSetResponse.create(columnTypes, datas, columnNames, schema, table));

    }


}
