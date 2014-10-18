package thermaldynamics.debughelper;

import cpw.mods.fml.common.FMLCommonHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugHelper {
    public static boolean debug = true;

    public static final Logger log = LogManager.getLogger("ThermalDebug");

    public static void init() {
        if (!debug) return;
        FMLCommonHandler.instance().bus().register(DebugTickHandler.INSTANCE);
    }

    public static void log(String string) {
        if (debug) log.info(string);
    }
}
