package net.jplugin.db.app.extensions;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.google.common.collect.Lists;

import net.jplugin.common.kits.tuple.Tuple2;
import net.jplugin.core.das.api.DataSourceFactory;
import net.jplugin.core.kernel.api.BindExtension;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.api.ICommandHandler;
import net.jplugin.db.mysql.svr.api.IResponseObject;
import net.jplugin.db.mysql.svr.consts.Constants;
import net.jplugin.db.mysql.svr.req.CommandRequest;
import net.jplugin.db.mysql.svr.resp.ResultSetResponse;

/**
 * 本类注册一个Command处理的扩展
 * @author LiuHang
 *
 */

@BindExtension(pointTo = net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_COMMAND_HANDLER, name = Constants.COMMAND_QUERY)
public class QueryHandler implements ICommandHandler {

	@Override
	public void handleCommand(ConnectionContext connCtx, CommandRequest commandPackage) {
		
		System.out.println("sql:"+commandPackage.getCommand());

		Statement stmt = null;
		ResultSet rs = null;
		try {
			//查询数据
			Connection conn = DataSourceFactory.getDataSource("ddd").getConnection();
			stmt = conn.createStatement();
			rs = stmt.executeQuery(commandPackage.getCommand());

			//组织返回结果
			connCtx.setResponseObject(ResultSetResponse.create(rs));

		} catch (SQLException e) {
			if (rs != null) 
				try {rs.close();} catch (Exception ex) {}
			
			if (stmt != null) 
				try {stmt.close();} catch (Exception ex) {}
			
			throw new RuntimeException (e);
		}
		
	}
}
