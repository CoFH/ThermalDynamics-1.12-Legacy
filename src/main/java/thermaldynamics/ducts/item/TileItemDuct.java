package thermaldynamics.ducts.item;

import cofh.lib.util.helpers.ServerHelper;
import cofh.lib.util.position.BlockPosition;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.util.ForgeDirection;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.IMultiBlockRoute;
import thermaldynamics.multiblock.MultiBlockGrid;
import thermaldynamics.multiblock.Route;

import java.util.LinkedList;

public class TileItemDuct extends TileMultiBlock implements IMultiBlockRoute {
    final ItemDuct internalDuct;
    ItemGrid internalGrid;
    private boolean wasOutputFound;

    public TileItemDuct() {
        internalDuct = new ItemDuct(this);
    }

    /*
     * Should return true if theTile is significant to this multiblock
     *
     * IE: Inventory's to ItemDuct's
     */
    @Override
    public boolean isSignificantTile(TileEntity theTile, int side) {
        return internalDuct.isSignificantTile(theTile, side);
    }

    @Override
    public void setGrid(MultiBlockGrid newGrid) {
        super.setGrid(newGrid);
        internalGrid = (ItemGrid) newGrid;
    }

    @Override
    public MultiBlockGrid getNewGrid() {
        return new ItemGrid(worldObj);
    }

    @Override
    public void tickPass(int pass) {
        internalDuct.tickPass(pass);
    }

    @Override
    public int getWeight() {
        return 1;
    }

    @Override
    public boolean canStuffItem() {
        return false;
    }

    boolean wasVisited = false;

    @Override
    public boolean wasVisited() {
        return wasVisited;
    }

    @Override
    public void setVisited(boolean wasVisited) {
        this.wasVisited = wasVisited;
    }

    @Override
    public boolean isOutput() {
        return isNode();
    }

    @Override
    public boolean wasOutputFound() {
        return wasOutputFound;
    }

    @Override
    public void setOutputFound(boolean outputFound) {
        wasOutputFound = outputFound;
    }

    @Override
    public int getMaxRange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public NeighborTypes getCachedSideType(byte side) {
        return neighborTypes[side];
    }

    @Override
    public IMultiBlock getCachedTile(byte side) {
        return neighborMultiBlocks[side];
    }

    @Override
    public boolean openGui(EntityPlayer player) {
        if (ServerHelper.isClientWorld(worldObj) || !isOutput())
            return true;


        LinkedList<Route> routes = internalGrid.getRoutesFromOutput(this);
        for (Route route : routes) {
            double r = player.worldObj.rand.nextDouble(),
                    g = player.worldObj.rand.nextDouble(),
                    b = player.worldObj.rand.nextDouble();

            double m = r > g ? b > r ? b : r : b > g ? b : g;
            r = r / m;
            g = g / m;
            b = b / m;

            double dx = player.worldObj.rand.nextDouble() * 0.5 + 0.25,
                    dy = player.worldObj.rand.nextDouble() * 0.5 + 0.25,
                    dz = player.worldObj.rand.nextDouble() * 0.5 + 0.25;

            route = route.copy();
            BlockPosition pos = new BlockPosition(xCoord, yCoord, zCoord);

            while (route.hasNextDirection()) {
                ForgeDirection dir = ForgeDirection.getOrientation(route.getNextDirection());
                for (float f = 0; f < 1; f += 0.2)
                    Minecraft.getMinecraft().theWorld.spawnParticle("reddust", pos.x + dx + dir.offsetX * f, pos.y + dy + dir.offsetY * f, pos.z + dz + dir.offsetZ * f, r, g, b);
                pos.step(dir);

            }
        }
        player.addChatComponentMessage(new ChatComponentText("Routes: " + routes.size()));

        return true;
    }
}
