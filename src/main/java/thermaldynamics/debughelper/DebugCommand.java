package thermaldynamics.debughelper;

import cofh.lib.util.position.BlockPosition;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thermaldynamics.ThermalDynamics;
import thermaldynamics.block.TileMultiBlock;

import java.util.LinkedList;
import java.util.Random;

public class DebugCommand extends CommandBase {
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

    @Override
    public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {
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

        for (int i = 0; i < 10000; i++) {
            if (rand.nextInt(20) == 0)
                positions.add(pos.copy());

            world.setBlock(pos.x, pos.y, pos.z, ThermalDynamics.blockDuct, 0, 3);

            pos.getTileEntity(world, TileMultiBlock.class).tilePlaced();

            if (rand.nextInt(4) == 0) {
                pos.setOrientation(pos.orientation.getRotation(rand.nextBoolean() ? ForgeDirection.UP : ForgeDirection.DOWN));
            }
            pos.moveForwards(1);
        }

        for (BlockPosition p : positions) {
            world.setBlock(p.x, p.y + 1, p.z, Blocks.chest, 0, 3);
        }
    }
}
