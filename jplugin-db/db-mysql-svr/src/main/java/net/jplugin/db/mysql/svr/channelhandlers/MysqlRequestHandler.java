package net.jplugin.db.mysql.svr.channelhandlers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.DecoderException;
import io.netty.util.internal.StringUtil;
import net.jplugin.common.kits.filter.FilterChain;
import net.jplugin.common.kits.filter.FilterManager;
import net.jplugin.core.config.api.RefConfig;
import net.jplugin.core.kernel.api.BindBean;
import net.jplugin.core.kernel.api.Initializable;
import net.jplugin.core.kernel.api.PluginEnvirement;
import net.jplugin.core.kernel.api.PluginFilterManager;
import net.jplugin.core.kernel.api.RefExtension;
import net.jplugin.core.kernel.api.RefExtensionMap;
import net.jplugin.core.kernel.api.RefExtensions;
import net.jplugin.core.kernel.api.ctx.ThreadLocalContextManager;
import net.jplugin.core.log.api.Logger;
import net.jplugin.core.log.api.RefLogger;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.api.ICommandFilter;
import net.jplugin.db.mysql.svr.api.ICommandHandler;
import net.jplugin.db.mysql.svr.api.ILoginRequestHandler;
import net.jplugin.db.mysql.svr.api.IResponseObject;
import net.jplugin.db.mysql.svr.consts.ColumnType;
import net.jplugin.db.mysql.svr.consts.Command;
import net.jplugin.db.mysql.svr.req.AuthRequest;
import net.jplugin.db.mysql.svr.req.CommandRequest;
import net.jplugin.db.mysql.svr.req.IRequestObject;
import net.jplugin.db.mysql.svr.resp.ErrorResponse;
import net.jplugin.db.mysql.svr.resp.ResultSetResponse;
import net.jplugin.db.mysql.svr.resp.SuccessResponse;
import net.jplugin.db.mysql.svr.utils.PatternUtils;
import net.jplugin.db.mysql.svr.utils.Util;

@BindBean(id = "MysqlRequestHandler")
public class MysqlRequestHandler extends ChannelInboundHandlerAdapter implements ICommandFilter,Initializable{

    @RefExtensionMap(pointTo = net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_COMMAND_HANDLER)
    private Map<String, ICommandHandler> handlerMap;

    @RefExtension(pointTo = net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_AUTH_CHECK_HANDLER)
    private ILoginRequestHandler authCheckHandler;

    @RefLogger
    Logger logger;
    
	PluginFilterManager<ConnectionContext> commandFilterManager = new PluginFilterManager<ConnectionContext>(
			net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_COMMAND_FILTER, this);

    public MysqlRequestHandler() {
        PluginEnvirement.INSTANCE.resolveRefAnnotation(this);
    }
    

    @Override
    public void initialize() {
    	commandFilterManager.init();
    }

    @RefConfig(autoRefresh = true,defaultValue = "true",path = "mysql.log-sql-error")
    Boolean logSqlError;
    
    /**
     * ????????????
     */
    @Override
    public boolean isSharable() {
    	return true;
    }
    
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            ThreadLocalContextManager.instance.createContext();
            channelReadInner(ctx, msg);
        } finally {
            ThreadLocalContextManager.instance.releaseContext();
        }
    }

    private void channelReadInner(ChannelHandlerContext ctx, Object msg) throws Exception {
        ConnectionContext connCtx = Util.getConnContext(ctx);

        //Get response
        IRequestObject pkg = (IRequestObject) msg;
        IResponseObject resultPacket;
        try {
            //??????????????????
            handleRequest(connCtx, pkg);
            resultPacket = connCtx.getResponseObject();
            if (resultPacket == null) {
                String errMsg = "No result packet after handled";
                resultPacket = ErrorResponse.create(11, errMsg);
                logger.error(errMsg);
            }

            //??????????????????
            connCtx.setResponseObject(null);

        } catch (Throwable th) {
//        	logger.error(th.getMessage(),th);
            //??????????????????
            connCtx.setResponseObject(null);

            String errMessage="Error Caught:" + th.getMessage();
            if (logSqlError) {
            	logError(connCtx,errMessage,th);
            }
            
            //????????????????????????
//			  logger.error(th.getMessage(),th);
            resultPacket = ErrorResponse.create(11, errMessage);
        }finally {
        	Util.cleanRequestInfo(connCtx);
        }

        //Send the message
        if (ctx.channel().isActive()) {
            ByteBuf byteBuf = PooledByteBufAllocator.DEFAULT.buffer(128);
            resultPacket.write(byteBuf);
            ctx.channel().writeAndFlush(byteBuf);
        }
    }

    private void logError(ConnectionContext connCtx, String errMessage, Throwable th) {
    	String query = Util.getCommandQuery(connCtx);
		logger.error(errMessage + "  Query="+query ,th);
	}

	private void handleRequest(ConnectionContext connCtx, IRequestObject msg) {
        // Do check
        if (msg instanceof AuthRequest) {
            handleLogin(connCtx, (AuthRequest) msg);
        } else if (msg instanceof CommandRequest) {
			if (logger.isInfoEnabled()) {
			     logger.info("Now handler Command." + msg + " DB=" + connCtx.getCurrentDb());
			}
			
        	//??????Command???ConnectionContext??????
            Util.setCommand(connCtx, (CommandRequest) msg);
            
            //?????????????????????command
            commandFilterManager.filter(connCtx);
        } else {
            throw new RuntimeException("Command should not be  auth command now!");
        }
    }
    
    /**
     * ???????????????Handler?????????Command
     */
	@Override
	public Object filter(FilterChain fc, ConnectionContext connCtx) throws Throwable {
    	CommandRequest commandPackage = Util.getCommandRequest(connCtx);
		final byte type = commandPackage.getCommandType();
        
        ICommandHandler handler = handlerMap.get(type + "");
        handler.handleCommand(connCtx, commandPackage);
        
        //???????????????????????????????????????????????????null
		return null;
	}
    

    private void handleLogin(ConnectionContext connCtx, AuthRequest lr) {
        if (logger.isInfoEnabled()) {
            logger.info("Now handler Login Request." + lr);
        }

        if (authCheckHandler!=null) {
        	authCheckHandler.checkUserAuth(connCtx, lr);
        } else {
            //??????????????????????????????
            SuccessResponse resp = SuccessResponse.create(0, 0, 2);
            connCtx.setAuthizeSuccess();
            connCtx.setCurrentDb(lr.getDatabase());
            connCtx.setResponseObject(resp);
        }

    }


    


//    //????????????????????????
//    Optional<Command> command = Command.findByCommandCode(type);
//
//    if (!command.isPresent()) {
//        throw new DecoderException("Unknown command type:" + type);
//    }

//  switch (command.get()) {
//  case COM_QUERY:
//      handleQuery(connCtx, commandPackage);
//      break;
//  case COM_FIELD_LIST:
//      handleFieldList(connCtx, commandPackage);
//      break;
//  case COM_INIT_DB:
//      handleUseDB(connCtx, commandPackage);
//      break;
//  case COM_QUIT:
//      handleQuit(connCtx, commandPackage);
//      break;
//  case COM_PING:
//      handlePing(connCtx, commandPackage);
//      break;
//  default:
//      AbstractCommandHandler handler = handlerMap.get(type + "");
//      handler.handleCommand(connCtx, commandPackage);
//}

//
//    /**
//     *
//     * @param connCtx
//     * @param command
//     */
//    private void handleQuery(ConnectionContext connCtx, CommandRequest command) {
//        connCtx.setQueryString(command.getCommand());
//        if (isSettingsQuery(connCtx)) {
//            handleSetting(connCtx);
//            return;
//        }
//
//        if (isServerSettingsQuery(connCtx)) {
//            handleServerSettings(connCtx);
//            return;
//        }
//
//        if (isOthersQuery(connCtx)) {
//            handleOthersQuery(connCtx);
//            return;
//        }
//
//        AbstractCommandHandler handler = handlerMap.get(command.getCommandType() + "");
//        handler.handleCommand(connCtx, command);
//    }
//
//    private boolean isSettingsQuery(ConnectionContext connCtx) {
//        String query = connCtx.getQueryString().toLowerCase();
//        Matcher matcher = PatternUtils.SETTINGS_PATTERN.matcher(query);
//        return matcher.find();
//    }
//
//    private void handleSetting(ConnectionContext context) {
//        String query = context.getQueryString().toLowerCase();
//        Matcher matcher = PatternUtils.SETTINGS_PATTERN.matcher(query);
//        while (matcher.find()) {
//            if ("null".equalsIgnoreCase(matcher.group(7))) {
//                continue;
//            }
//            context.setSetting(matcher.group(3), matcher.group(7));
//        }
//
//        SuccessResponse resp = SuccessResponse.create(0, 0);
//        context.setResponseObject(resp);
//    }
//
//    private boolean isServerSettingsQuery(ConnectionContext connCtx) {
//        String query = connCtx.getQueryString().toLowerCase();
//        return query.contains("select") && !query.contains("from") && query.contains("@@");
//    }
//
//
//    private void handleServerSettings(ConnectionContext connCtx) {
//
//        final Matcher matcher = PatternUtils.SELECT_SETTINGS_PATTERN.matcher(connCtx.getQueryString());
//
//        List<Integer> columnTypes = new ArrayList<>();
//        List<List<String>> datas = new ArrayList<>();
//        List<String> rowDatas = new ArrayList<>();
//        List<String> columnNames = new ArrayList<>();
//        String schema = "def";
//        String table = "system";
//
//        while (matcher.find()) {
//            String systemVariable = matcher.group(1);
//            String fieldName = matcher.group(8);
//            if (fieldName == null) {
//                fieldName = "@@" + systemVariable;
//            }
//            switch (systemVariable) {
//                case "character_set_client":
//                case "character_set_connection":
//                case "character_set_results":
//                case "character_set_server":
//                case "collation_connection":
//                case "performance_schema":
//                case "character_set_database":
//                case "collation_database":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
//                    rowDatas.add("utf-8");
//                    columnNames.add(fieldName);
//                    break;
//                case "collation_server":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
//                    rowDatas.add("utf8_general_ci");
//                    columnNames.add(fieldName);
//                    break;
//                case "init_connect":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
//                    rowDatas.add("SET NAMES utf8");
//                    columnNames.add(fieldName);
//                    break;
//                case "interactive_timeout":
//                case "wait_timeout":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_INT24.getValue());
//                    rowDatas.add("28800");
//                    columnNames.add(fieldName);
//                    break;
//                case "language":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
//                    rowDatas.add("CN");
//                    columnNames.add(fieldName);
//                    break;
//                case "license":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
//                    rowDatas.add("ASLv2");
//                    columnNames.add(fieldName);
//                    break;
//                case "lower_case_table_names":
//                case "query_cache_size":
//                case "query_cache_type":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_TINY.getValue());
//                    rowDatas.add("0");
//                    columnNames.add(fieldName);
//                    break;
//                case "max_allowed_packet":
//                case "net_buffer_length":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_INT24.getValue());
//                    rowDatas.add("4194304");
//                    columnNames.add(fieldName);
//                    break;
//                case "net_write_timeout":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_INT24.getValue());
//                    rowDatas.add("60");
//                    columnNames.add(fieldName);
//                    break;
//                case "have_query_cache":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
//                    rowDatas.add("YES");
//                    columnNames.add(fieldName);
//                    break;
//                case "sql_mode":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_SET.getValue());
//                    rowDatas.add("ONLY_FULL_GROUP_BY,NO_AUTO_VALUE_ON_ZERO,STRICT_TRANS_TABLES,NO_AUTO_CREATE_USER,NO_ENGINE_SUBSTITUTION");
//                    columnNames.add(fieldName);
//                    break;
//                case "system_time_zone":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
//                    rowDatas.add("UTC");
//                    columnNames.add(fieldName);
//                    break;
//                case "time_zone":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
//                    rowDatas.add("SYSTEM");
//                    columnNames.add(fieldName);
//                    break;
//                case "tx_isolation":
//                case "transaction_isolation":
//                case "session.transaction_isolation":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_ENUM.getValue());
//                    rowDatas.add("READ-COMMITTED");
//                    columnNames.add(fieldName);
//                    break;
//                case "SESSION.auto_increment_increment":
//                case "session.auto_increment_increment":
//                case "auto_increment_increment":
//                case "session.autocommit":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_LONG.getValue());
//                    rowDatas.add("1");
//                    columnNames.add(fieldName);
//                    break;
//                case "version_comment":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
//                    rowDatas.add("jplugin");
//                    columnNames.add(fieldName);
//                    break;
//                case "transaction_read_only":
//                case "session.transaction_read_only":
//                    columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
//                    rowDatas.add("0");
//                    columnNames.add(fieldName);
//                    break;
//                default:
//                    throw new Error("Unknown system variable " + systemVariable);
//            }
//        }
//        datas.add(rowDatas);
//        connCtx.setResponseObject(ResultSetResponse.create(columnTypes, datas, columnNames, schema, table));
//    }
//
//    private boolean isOthersQuery(ConnectionContext context) {
//        String query = context.getQueryString().toLowerCase();
//        return "SELECT DATABASE()".equalsIgnoreCase(query) ||
//                query.toLowerCase().startsWith("show variables like")
//                || "SHOW ENGINES".equalsIgnoreCase(query)
//                || "SHOW COLLATION".equalsIgnoreCase(query)
//                || "SHOW CHARACTER SET".equalsIgnoreCase(query)
//                || "SHOW STATUS".equalsIgnoreCase(query)
//                || PatternUtils.DESC_TABLE_PATTERN.matcher(query).find();
//    }
//
//    private void handleOthersQuery(ConnectionContext context) {
//
//        int size = 3;
//
//        List<Integer> columnTypes = new ArrayList<>();
//        for (int i = 0; i < size; i++) {
//            columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
//        }
//        List<List<String>> datas = new ArrayList<>();
//        List<String> rowDatas = new ArrayList<>();
//        String currentDb = context.getCurrentDb();
//        if (StringUtil.isNullOrEmpty(currentDb)) {
//            currentDb = "";
//        }
//        rowDatas.add(currentDb);
//        rowDatas.add("jplugin");
//        rowDatas.add("jplugin engines from mysql");
//
//        datas.add(rowDatas);
//
//        List<String> columnNames = new ArrayList<>();
//        columnNames.add("DATABASE()");
//        columnNames.add("Engine");
//        columnNames.add("Comment");
//
//        String schema = "def";
//        String table = "system";
//
//        context.setResponseObject(ResultSetResponse.create(columnTypes, datas, columnNames, schema, table));
//    }
//
//    private void handleUseDB(ConnectionContext connCtx, CommandRequest commandPackage) {
//        connCtx.setCurrentDb(commandPackage.getCommand());
//        okResponse(connCtx);
//    }
//
//    private void handleQuit(ConnectionContext connCtx, CommandRequest commandPackage) {
//        //TODO ??????????????????
//        AbstractCommandHandler handler = handlerMap.get(commandPackage.getCommandType() + "");
//        okResponse(connCtx);
//        if (handler != null) {
//            handler.postHandleCommand(connCtx, commandPackage);
//        }
//    }
//
//    private void handlePing(ConnectionContext connCtx, CommandRequest commandPackage) {
//        okResponse(connCtx);
//    }
//
//    private void okResponse(ConnectionContext connCtx) {
//        SuccessResponse resp = SuccessResponse.create(0, 0);
//        connCtx.setResponseObject(resp);
//    }
//
//    private void handleFieldList(ConnectionContext context, CommandRequest commandPackage) {
//        context.setCurrentDb(commandPackage.getCommand());
//
//        List<Integer> columnTypes = new ArrayList<>();
//        columnTypes.add(ColumnType.MYSQL_TYPE_VAR_STRING.getValue());
//        List<List<String>> datas = new ArrayList<>();
//        List<String> rowDatas = new ArrayList<>();
//        rowDatas.add("jplugin");
//        datas.add(rowDatas);
//        List<String> columnNames = new ArrayList<>();
//        columnNames.add("comment");
//        String schema = "def";
//        String table = "system";
//
//        context.setResponseObject(ResultSetResponse.create(columnTypes, datas, columnNames, schema, table));
//    }

}
