package thermaldynamics.debughelper;

import cofh.lib.util.helpers.ServerHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.world.ChunkEvent;
import thermaldynamics.core.TickHandler;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

import java.util.HashSet;
import java.util.Random;

public class DebugTickHandler {

    public static DebugTickHandler INSTANCE = new DebugTickHandler();
    public static HashSet<EntityPlayer> debugPlayers;

    public final Random rand = new Random();
    public static boolean showParticles;

    public static boolean showLoading;

    @SubscribeEvent
    public void chunkLoad(ChunkEvent.Load event) {
        printChunkEvent(event);
    }

    @SubscribeEvent
    public void chunkLoad(ChunkEvent.Unload event) {
        printChunkEvent(event);
    }

    public void printChunkEvent(ChunkEvent event) {
        if (!showLoading) return;
        DebugHelper.info("[" + event.getChunk().xPosition + "," + event.getChunk().zPosition + "]_" + (event.getChunk().worldObj.isRemote ? "Client" : "Server"));
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void showParticles(TickEvent.WorldTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START || Minecraft.getMinecraft().theWorld == null
                || Minecraft.getMinecraft().theWorld.provider.dimensionId != evt.world.provider.dimensionId
                )
            return;

        if (ServerHelper.isClientWorld(evt.world))
            return;

        if (!showParticles)
            return;


        for (MultiBlockGrid grid : TickHandler.getTickHandler(evt.world).tickingGrids) {
            rand.setSeed(grid.hashCode());

            double r = rand.nextDouble(), g = rand.nextDouble(), b = rand.nextDouble();
            double m = 1 / (r > g ? (b > r ? b : r) : (b > g ? b : g));
            r *= m;
            g *= m;
            b *= m;


            for (IMultiBlock node : grid.nodeSet) {
                Minecraft.getMinecraft().theWorld.spawnParticle("reddust", node.x() + 0.5, node.y() + 0.75, node.z() + 0.5, r, g, b);
            }

            r *= 0.8;
            g *= 0.8;
            b *= 0.8;

            for (IMultiBlock node : grid.idleSet) {
                Minecraft.getMinecraft().theWorld.spawnParticle("reddust", node.x() + 0.5, node.y() + 0.75, node.z() + 0.5, r, g, b);
            }
        }

    }


    public enum DebugEvents {
        GRID_FORMED,
        GRID_BROKEN,
        NEIGHBOUR_CHANGE,
        NEIGHBOUR_WEAK_CHANGE,;

        static final int n = values().length;
    }

    int servertick = 0;

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        int j = (servertick + 1) % values.length;
        for (int i = 0; i < DebugEvents.n; i++) {
            displayValue[i] -= values[servertick][i];
            displayValue[i] += values[j][i];
        }

        servertick = j;
    }


    public int[] displayValue = new int[DebugEvents.values().length];

    public int[][] values = new int[100][DebugEvents.values().length];

    public void tickEvent(DebugEvents event) {
        values[servertick][event.ordinal()]++;
    }


}
