package thermaldynamics.debughelper;

import cofh.lib.util.position.BlockPosition;
import cofh.repack.codechicken.lib.raytracer.RayTracer;
import com.google.common.base.Throwables;
import cpw.mods.fml.relauncher.ReflectionHelper;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.ChunkCoordIntPair;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.gen.ChunkProviderServer;
import net.minecraftforge.common.util.ForgeDirection;
import thermaldynamics.ThermalDynamics;
import thermaldynamics.block.TileMultiBlock;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

public class CommandThermalDebug extends CommandBase {
    @Override
    public String getCommandName() {
        return "debug";
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {
        return true;
    }

    @Override
    public String getCommandUsage(ICommandSender p_71518_1_) {
        return "debug";
    }

    Random rand = new Random();

    Field chunksToUnload;

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] args) {
        if (args.length == 0)
            return;

        if ("showLoading".equals(args[0])) {
            DebugTickHandler.showLoading = !DebugTickHandler.showLoading;
        } else if ("unload".equals(args[0])) {
            if (!(p_71515_1_ instanceof EntityPlayerMP))
                return;

            if (chunksToUnload == null) {
                chunksToUnload = ReflectionHelper.findField(ChunkProviderServer.class, "chunksToUnload");
            }

            EntityPlayerMP player = (EntityPlayerMP) p_71515_1_;
            MovingObjectPosition trace = RayTracer.reTrace(player.worldObj, player, 100);
            Chunk chunk = player.worldObj.getChunkFromBlockCoords(trace.blockX, trace.blockZ);
            Set<Long> o;
            try {
                //noinspection unchecked
                o = (Set<Long>) chunksToUnload.get(player.getServerForPlayer().theChunkProviderServer);
            } catch (IllegalAccessException e) {
                throw Throwables.propagate(e);
            }

            o.add(ChunkCoordIntPair.chunkXZ2Int(chunk.xPosition, chunk.zPosition));
        } else if ("grids".equals(args[0])) {
            DebugTickHandler.showParticles = !DebugTickHandler.showParticles;
        } else if ("generate".equals(args[0]) && args.length == 2) {
            if (!(p_71515_1_ instanceof EntityPlayerMP))
                return;

            EntityPlayerMP playerMP = (EntityPlayerMP) p_71515_1_;
            BlockPosition pos = new BlockPosition((int) Math.floor(playerMP.posX),
                    (int) Math.floor(playerMP.posY) - 5,
                    (int) Math.floor(playerMP.posZ));

            final World world = playerMP.getEntityWorld();
            if (pos.getBlock(world) != Blocks.air)
                return;

            pos.setOrientation(ForgeDirection.NORTH);

            LinkedList<BlockPosition> positions = new LinkedList<BlockPosition>();

            int n = Integer.valueOf(args[1]);
            ;

            for (int i = 0; i < n; i++) {
                if (rand.nextInt(20) == 0)
                    positions.add(pos.copy());

                world.setBlock(pos.x, pos.y, pos.z, ThermalDynamics.blockDuct[0], 0, 3);

                pos.getTileEntity(world, TileMultiBlock.class).tilePlaced();

                if (rand.nextInt(4) == 0) {
                    pos.setOrientation(pos.orientation.getRotation(rand.nextBoolean() ? ForgeDirection.UP : ForgeDirection.DOWN));
                }
                pos.moveForwards(1);
            }

            for (BlockPosition p : positions) {
//                world.setBlock(p.x, p.y + 1, p.z, Blocks.chest, 0, 3);
            }
        }
    }
}
