package thermaldynamics.debughelper;

import cpw.mods.fml.common.FMLCommonHandler;
import net.minecraft.world.World;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DebugHelper {
    public static boolean debug;

    static {
        try {
            World.class.getMethod("getBlock", int.class, int.class, int.class);
            debug = true;
        } catch (NoSuchMethodException e) {
            debug = false;
        }
    }

    public static final Logger log = LogManager.getLogger("ThermalDebug");

    public static void init() {
        if (!debug) return;
        FMLCommonHandler.instance().bus().register(DebugTickHandler.INSTANCE);
    }

    public static void log(Object string) {
        if (debug) log.info(string);
    }

    public static long time = 0;

    public static void startTimer() {
        time = System.nanoTime();
    }

    public static void stopTimer(String cause) {
        if (debug) {
            double v = (System.nanoTime() - time) * (1.0E-6);
            log.info(cause + ": " + v + " ms");
        }
    }

}
