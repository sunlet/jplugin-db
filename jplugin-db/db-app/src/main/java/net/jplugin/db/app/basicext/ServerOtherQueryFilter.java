package net.jplugin.db.app.basicext;

import net.jplugin.common.kits.StringKit;
import net.jplugin.common.kits.filter.FilterChain;
import net.jplugin.core.kernel.api.BindExtension;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.api.ICommandFilter;
import net.jplugin.db.mysql.svr.consts.ColumnType;
import net.jplugin.db.mysql.svr.resp.ResultSetResponse;
import net.jplugin.db.mysql.svr.utils.PatternUtils;
import net.jplugin.db.mysql.svr.utils.Util;


import java.util.ArrayList;
import java.util.List;

@BindExtension(pointTo = net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_COMMAND_FILTER)
public class ServerOtherQueryFilter implements ICommandFilter {

    @Override
    public Object filter(FilterChain fc, ConnectionContext ctx) throws Throwable {

        if (isOthersQuery(ctx)) {
            handleOthersQuery(ctx);
            return null;
        }
        return fc.next(ctx);
    }

    private boolean isOthersQuery(ConnectionContext context) {
        String query = Util.getCommandQuery(context).toLowerCase();
        return "SELECT DATABASE()".equalsIgnoreCase(query) ||
                query.toLowerCase().startsWith("show variables like")
                || "SHOW ENGINES".equalsIgnoreCase(query)
                || "SHOW COLLATION".equalsIgnoreCase(query)
                || "SHOW CHARACTER SET".equalsIgnoreCase(query)
                || "SHOW STATUS".equalsIgnoreCase(query)
                || PatternUtils.DESC_TABLE_PATTERN.matcher(query).find()
                || PatternUtils.SHOW_PROCEDURE_PATTERN.matcher(query).find();
    }

    private void handleOthersQuery(ConnectionContext context) {

        int size = 5;

        List<Integer> columnTypes = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
        }
        List<List<String>> datas = new ArrayList<>();
        List<String> rowDatas = new ArrayList<>();
        String currentDb = context.getCurrentDb();
        if (StringKit.isNull(currentDb)) {
            currentDb = "";
        }
        rowDatas.add(currentDb);
        rowDatas.add("haiziwang");
        rowDatas.add("haiziwang engines from mysql");
        rowDatas.add("utf8");
        rowDatas.add("utf8");

        datas.add(rowDatas);

        List<String> columnNames = new ArrayList<>();
        columnNames.add("DATABASE()");
        columnNames.add("Engine");
        columnNames.add("Comment");
        columnNames.add("character_set_client");
        columnNames.add("Charset");

        String schema = "def";
        String table = "system";

        context.setResponseObject(ResultSetResponse.create(columnTypes, datas, columnNames, schema, table));
    }

}
