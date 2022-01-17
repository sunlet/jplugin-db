package net.jplugin.db.app.extensions;

import net.jplugin.core.kernel.api.BindExtension;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.api.ILoginRequestHandler;
import net.jplugin.db.mysql.svr.req.AuthRequest;
import net.jplugin.db.mysql.svr.resp.SuccessResponse;
/**
 * 本类注册一个Login处理的扩展
 * @author Administrator
 *
 */
@BindExtension(pointTo = net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_AUTH_CHECK_HANDLER)
public class LoginRequestHandler implements ILoginRequestHandler{

	@Override
	public void checkUserAuth(ConnectionContext connectionContext, AuthRequest request) {

		//直接返回成功
		SuccessResponse resp = SuccessResponse.create(0,0,2);
		
		connectionContext.setAuthizeSuccess();
		connectionContext.setCurrentDb(request.getDatabase());
		connectionContext.setResponseObject(resp);
		
	}

}
