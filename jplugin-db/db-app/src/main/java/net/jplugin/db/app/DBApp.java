package net.jplugin.db.app;

import net.jplugin.core.kernel.PluginApp;
import net.jplugin.core.kernel.api.PluginAutoDetect;

/**
 * Hello world!
 *
 */
public class DBApp 
{
    public static void main( String[] args )
    {
        PluginAutoDetect.addAutoDetectPackage("net.jplugin.db");
        PluginApp.main(null);
    }
}
