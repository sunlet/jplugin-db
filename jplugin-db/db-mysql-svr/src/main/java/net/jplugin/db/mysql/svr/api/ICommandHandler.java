package net.jplugin.db.mysql.svr.api;

import net.jplugin.db.mysql.svr.req.CommandRequest;
import net.jplugin.db.mysql.svr.resp.ResultSetResponse;


public interface ICommandHandler {
	/**
	 *  <PRE>
	   *   本方法实现中，调用 connCtx.setResponseObject(resObject) 来设置返回消息。
	 * resObject参数的构造方法：
	 *     ResultSetResponse.create(....)
	 *     SuccessResponse.create(...)
	 *     ErrorResponse.create(...)
	 * </PRE>
	 * 
	 * @param connCtx
	 * @param commandPackage
	 */
	public void handleCommand(net.jplugin.db.mysql.svr.api.ConnectionContext connCtx,CommandRequest commandPackage);


}
