package net.jplugin.db.app.extensions;
import net.jplugin.common.kits.filter.FilterChain;
import net.jplugin.core.kernel.api.BindExtension;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.api.ICommandFilter;
import net.jplugin.db.mysql.svr.utils.Util;

@BindExtension(pointTo = net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_COMMAND_FILTER)
public class MyFilterTest2 implements ICommandFilter {

	@Override
	public Object filter(FilterChain fc, ConnectionContext ctx) throws Throwable {
		System.out.println("before command execute;" + Util.getCommandRequest(ctx));
		fc.next(ctx);
		System.out.println("after command execute;");
		return null;
	}

}
