package thermaldynamics.debughelper;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Random;

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
        MinecraftForge.EVENT_BUS.register(DebugTickHandler.INSTANCE);
    }

    public static void info(Object string) {
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

    private static Random rand = new Random();

    @SideOnly(Side.CLIENT)
    public static void showParticle(World world, double x, double y, double z, int seed) {
        rand.setSeed(seed);
        double r = rand.nextDouble(), g = rand.nextDouble(), b = rand.nextDouble();
        double m = 1 / (r > g ? (b > r ? b : r) : (b > g ? b : g));

        r *= m;
        g *= m;
        b *= m;

        if (world == null) world = Minecraft.getMinecraft().theWorld;
        world.spawnParticle("reddust", x, y, z, r, g, b);
    }

}
