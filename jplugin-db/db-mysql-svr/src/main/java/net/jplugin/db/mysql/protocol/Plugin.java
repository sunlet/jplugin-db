package net.jplugin.db.mysql.protocol;

import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;
//import net.jplugin.db.basic.DBPriority;

@PluginAnnotation
public class Plugin extends AbstractPlugin {

	@Override
	public void init() {

	}

	@Override
	public int getPrivority() {
//		return DBPriority.MYSQL_PROTOCOL;
		return 101;
	}

}
