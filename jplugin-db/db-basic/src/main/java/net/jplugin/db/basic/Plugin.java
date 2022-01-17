package net.jplugin.db.basic;

import net.jplugin.core.kernel.api.AbstractPlugin;
import net.jplugin.core.kernel.api.PluginAnnotation;

@PluginAnnotation
public class Plugin extends AbstractPlugin {

	@Override
	public void init() {
	}

	@Override
	public int getPrivority() {
		return DBPriority.BASIC;
	}

}
