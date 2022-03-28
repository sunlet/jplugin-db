package net.jplugin.db.app.basicext;

import net.jplugin.common.kits.filter.FilterChain;
import net.jplugin.core.kernel.api.BindExtension;
import net.jplugin.db.mysql.svr.api.ConnectionContext;
import net.jplugin.db.mysql.svr.api.ICommandFilter;
import net.jplugin.db.mysql.svr.resp.SuccessResponse;
import net.jplugin.db.mysql.svr.utils.PatternUtils;
import net.jplugin.db.mysql.svr.utils.Util;


import java.util.regex.Matcher;

@BindExtension(pointTo = net.jplugin.db.mysql.svr.Plugin.EP_MYSQL_COMMAND_FILTER)
public class SettingQueryFilter implements ICommandFilter {

    @Override
    public Object filter(FilterChain fc, ConnectionContext ctx) throws Throwable {
        if (isSettingsQuery(ctx)) {
            handleSetting(ctx);
            return null;
        }
        return fc.next(ctx);
    }

    private boolean isSettingsQuery(ConnectionContext connCtx) {
        String query = Util.getCommandQuery(connCtx).toLowerCase();
        Matcher matcher = PatternUtils.SETTINGS_PATTERN.matcher(query);
        return matcher.find();
    }

    private void handleSetting(ConnectionContext context) {
        String query = Util.getCommandQuery(context).toLowerCase();
        Matcher matcher = PatternUtils.SETTINGS_PATTERN.matcher(query);
        while (matcher.find()) {
            if ("null".equalsIgnoreCase(matcher.group(7))) {
                continue;
            }
            context.getRequestAttributes().put(matcher.group(3), matcher.group(7));
        }

        SuccessResponse resp = SuccessResponse.create(0, 0);
        context.setResponseObject(resp);
    }
}
