package net.jplugin.db.app.basicext;

import net.jplugin.common.kits.StringKit;
import net.jplugin.common.kits.filter.FilterChain;
import net.jplugin.core.kernel.api.BindExtension;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.api.ICommandFilter;
import net.jplugin.db.mysql.svr.consts.ColumnType;
import net.jplugin.db.mysql.svr.req.CommandRequest;
import net.jplugin.db.mysql.svr.resp.ResultSetResponse;
import net.jplugin.db.mysql.svr.resp.SuccessResponse;
import net.jplugin.db.mysql.svr.utils.PatternUtils;
import net.jplugin.db.mysql.svr.utils.Util;


import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

@BindExtension(pointTo = net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_COMMAND_FILTER)
public class ServerInfoQueryFilter implements ICommandFilter {

    @Override
    public Object filter(FilterChain fc, ConnectionContext ctx) throws Throwable {
        if (isServerSettingsQuery(ctx)) {
            handleServerSettings(ctx);
            return null;
        }
        return fc.next(ctx);
    }

    private boolean isServerSettingsQuery(ConnectionContext connCtx) {
        String query = Util.getCommandQuery(connCtx).toLowerCase();
        return query.contains("select") && !query.contains("from") && query.contains("@@");
    }

    private void handleServerSettings(ConnectionContext connCtx) {

        final Matcher matcher = PatternUtils.SELECT_SETTINGS_PATTERN.matcher(Util.getCommandQuery(connCtx).toLowerCase());

        List<Integer> columnTypes = new ArrayList<>();
        List<List<String>> datas = new ArrayList<>();
        List<String> rowDatas = new ArrayList<>();
        List<String> columnNames = new ArrayList<>();
        String schema = "def";
        String table = "system";

        while (matcher.find()) {
            String systemVariable = matcher.group(1);
            String fieldName = matcher.group(8);
            if (fieldName == null) {
                fieldName = "@@" + systemVariable;
            }
            switch (systemVariable) {
                case "character_set_client":
                case "character_set_connection":
                case "character_set_results":
                case "character_set_server":
                case "collation_connection":
                case "performance_schema":
                case "character_set_database":
                case "collation_database":
                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
                    rowDatas.add("utf-8");
                    columnNames.add(fieldName);
                    break;
                case "collation_server":
                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
                    rowDatas.add("utf8_general_ci");
                    columnNames.add(fieldName);
                    break;
                case "init_connect":
                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
                    rowDatas.add("SET NAMES utf8");
                    columnNames.add(fieldName);
                    break;
                case "interactive_timeout":
                case "wait_timeout":
                    columnTypes.add(ColumnType.MYSQL_TYPE_INT24.getValue());
                    rowDatas.add("28800");
                    columnNames.add(fieldName);
                    break;
                case "language":
                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
                    rowDatas.add("CN");
                    columnNames.add(fieldName);
                    break;
                case "license":
                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
                    rowDatas.add("ASLv2");
                    columnNames.add(fieldName);
                    break;
                case "lower_case_table_names":
                case "query_cache_size":
                case "query_cache_type":
                    columnTypes.add(ColumnType.MYSQL_TYPE_TINY.getValue());
                    rowDatas.add("0");
                    columnNames.add(fieldName);
                    break;
                case "max_allowed_packet":
                case "net_buffer_length":
                    columnTypes.add(ColumnType.MYSQL_TYPE_INT24.getValue());
                    rowDatas.add("4194304");
                    columnNames.add(fieldName);
                    break;
                case "net_write_timeout":
                    columnTypes.add(ColumnType.MYSQL_TYPE_INT24.getValue());
                    rowDatas.add("60");
                    columnNames.add(fieldName);
                    break;
                case "have_query_cache":
                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
                    rowDatas.add("YES");
                    columnNames.add(fieldName);
                    break;
                case "sql_mode":
                    columnTypes.add(ColumnType.MYSQL_TYPE_SET.getValue());
                    rowDatas.add("ONLY_FULL_GROUP_BY,NO_AUTO_VALUE_ON_ZERO,STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION");
                    columnNames.add(fieldName);
                    break;
                case "system_time_zone":
                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
                    rowDatas.add("UTC");
                    columnNames.add(fieldName);
                    break;
                case "time_zone":
                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
                    rowDatas.add("SYSTEM");
                    columnNames.add(fieldName);
                    break;
                case "tx_isolation":
                case "transaction_isolation":
                case "session.transaction_isolation":
                    columnTypes.add(ColumnType.MYSQL_TYPE_ENUM.getValue());
                    rowDatas.add("READ-COMMITTED");
                    columnNames.add(fieldName);
                    break;
                case "SESSION.auto_increment_increment":
                case "session.auto_increment_increment":
                case "auto_increment_increment":
                case "session.autocommit":
                    columnTypes.add(ColumnType.MYSQL_TYPE_LONG.getValue());
                    rowDatas.add("1");
                    columnNames.add(fieldName);
                    break;
                case "version_comment":
                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
                    rowDatas.add("haiziwang");
                    columnNames.add(fieldName);
                    break;
                case "transaction_read_only":
                case "session.transaction_read_only":
                case "session.tx_read_only":
                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
                    rowDatas.add("0");
                    columnNames.add(fieldName);
                    break;
                default:
                    throw new Error("Unknown system variable " + systemVariable);
            }
        }
        datas.add(rowDatas);
        connCtx.setResponseObject(ResultSetResponse.create(columnTypes, datas, columnNames, schema, table));
    }

    private boolean isOthersQuery(ConnectionContext context) {
        String query = Util.getCommandQuery(context).toLowerCase();
        return "SELECT DATABASE()".equalsIgnoreCase(query) ||
                query.toLowerCase().startsWith("show variables like")
                || "SHOW ENGINES".equalsIgnoreCase(query)
                || "SHOW COLLATION".equalsIgnoreCase(query)
                || "SHOW CHARACTER SET".equalsIgnoreCase(query)
                || "SHOW STATUS".equalsIgnoreCase(query)
                || PatternUtils.DESC_TABLE_PATTERN.matcher(query).find();
    }

    private void handleOthersQuery(ConnectionContext context) {

        int size = 3;

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

        datas.add(rowDatas);

        List<String> columnNames = new ArrayList<>();
        columnNames.add("DATABASE()");
        columnNames.add("Engine");
        columnNames.add("Comment");

        String schema = "def";
        String table = "system";

        context.setResponseObject(ResultSetResponse.create(columnTypes, datas, columnNames, schema, table));
    }

    private void handleUseDB(ConnectionContext connCtx, CommandRequest commandPackage) {
        connCtx.setCurrentDb(commandPackage.getCommand());
        okResponse(connCtx);
    }

    private void okResponse(ConnectionContext connCtx) {
        SuccessResponse resp = SuccessResponse.create(0, 0);
        connCtx.setResponseObject(resp);
    }

    private void handleFieldList(ConnectionContext context, CommandRequest commandPackage) {
        context.setCurrentDb(commandPackage.getCommand());

        List<Integer> columnTypes = new ArrayList<>();
        columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
        List<List<String>> datas = new ArrayList<>();
        List<String> rowDatas = new ArrayList<>();
        rowDatas.add("haiziwang");
        datas.add(rowDatas);
        List<String> columnNames = new ArrayList<>();
        columnNames.add("comment");
        String schema = "def";
        String table = "system";

        context.setResponseObject(ResultSetResponse.create(columnTypes, datas, columnNames, schema, table));
    }
}
