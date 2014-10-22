package thermaldynamics.debughelper;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.client.Minecraft;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.core.TickHandler;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

import java.util.Random;

public class DebugTickHandler {

    public static DebugTickHandler INSTANCE = new DebugTickHandler();

    public final Random rand = new Random();
    public static boolean showParticles;

    @SubscribeEvent
    public void showParticles(TickEvent.WorldTickEvent evt) {
        if (evt.phase == TickEvent.Phase.START || Minecraft.getMinecraft().theWorld == null
                || Minecraft.getMinecraft().theWorld.provider.dimensionId != evt.world.provider.dimensionId)
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
                TileMultiBlock tile = ((TileMultiBlock) node);
                Minecraft.getMinecraft().theWorld.spawnParticle("reddust", tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5, r, g, b);
            }

            r *= 0.8;
            g *= 0.8;
            b *= 0.8;

            for (IMultiBlock node : grid.idleSet) {
                TileMultiBlock tile = ((TileMultiBlock) node);
                Minecraft.getMinecraft().theWorld.spawnParticle("reddust", tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5, r, g, b);
            }
        }

    }


}
