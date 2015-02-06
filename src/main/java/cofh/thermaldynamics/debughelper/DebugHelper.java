package cofh.thermaldynamics.debughelper;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.Random;
import net.minecraft.client.Minecraft;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.GL11;

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

    public static <T> T logObject(T o) {
        info(o);
        return o;
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

    public static void quit() {
        FMLCommonHandler.instance().exitJava(0, true);
    }


    static String[] glStates = {"GL_ALPHA_TEST",
            "GL_AUTO_NORMAL",
            "GL_BLEND",
            "GL_CLIP_PLANE i",
            "GL_COLOR_ARRAY",
            "GL_COLOR_LOGIC_OP",
            "GL_COLOR_MATERIAL",
            "GL_CULL_FACE",
            "GL_DEPTH_TEST",
            "GL_DITHER",
            "GL_FOG",
            "GL_INDEX_ARRAY",
            "GL_INDEX_LOGIC_OP",
            "GL_LIGHT i",
            "GL_LIGHTING",
            "GL_LINE_SMOOTH",
            "GL_LINE_STIPPLE",
            "GL_MAP1_COLOR_4",
            "GL_MAP1_INDEX",
            "GL_MAP1_NORMAL",
            "GL_MAP1_TEXTURE_COORD_1",
            "GL_MAP1_TEXTURE_COORD_2",
            "GL_MAP1_TEXTURE_COORD_3",
            "GL_MAP1_TEXTURE_COORD_4",
            "GL_MAP1_VERTEX_3",
            "GL_MAP1_VERTEX_4",
            "GL_MAP2_COLOR_4",
            "GL_MAP2_INDEX",
            "GL_MAP2_NORMAL",
            "GL_MAP2_TEXTURE_COORD_1",
            "GL_MAP2_TEXTURE_COORD_2",
            "GL_MAP2_TEXTURE_COORD_3",
            "GL_MAP2_TEXTURE_COORD_4",
            "GL_MAP2_VERTEX_3",
            "GL_MAP2_VERTEX_4",
            "GL_NORMAL_ARRAY",
            "GL_NORMALIZE",
            "GL_POINT_SMOOTH",
            "GL_POLYGON_OFFSET_FILL",
            "GL_POLYGON_OFFSET_LINE",
            "GL_POLYGON_OFFSET_POINT",
            "GL_POLYGON_SMOOTH",
            "GL_POLYGON_STIPPLE",
            "GL_SCISSOR_TEST",
            "GL_STENCIL_TEST",
            "GL_TEXTURE_1D",
            "GL_TEXTURE_2D",
            "GL_TEXTURE_COORD_ARRAY",
            "GL_TEXTURE_GEN_Q",
            "GL_TEXTURE_GEN_R",
            "GL_TEXTURE_GEN_S",
            "GL_TEXTURE_GEN_T",
            "GL_VERTEX_ARRAY"};

    @SideOnly(Side.CLIENT)
    public static int[] glCaps;

    @SideOnly(Side.CLIENT)
    private static void initGLStates() {
        glCaps = new int[glStates.length];

        for (int i = 0; i < glStates.length; i++) {
            glCaps[i] = -1;
            try {
                glCaps[i] = GL11.class.getField(glStates[i]).getInt(null);
            } catch (NoSuchFieldException ignored) {

            } catch (IllegalAccessException ignored) {

            }
        }

    }

    @SideOnly(Side.CLIENT)
    public static void logGLStates() {
        if (glCaps == null)
            initGLStates();

        for (int i = 0; i < glCaps.length; i++) {
            if (glCaps[i] != -1 && GL11.glIsEnabled(glCaps[i])) {
                info(glStates[i]);
            }
        }
    }
}
