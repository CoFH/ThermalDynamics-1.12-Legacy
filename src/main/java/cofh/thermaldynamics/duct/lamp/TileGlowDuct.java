package cofh.thermaldynamics.duct.lamp;

import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.util.ForgeDirection;

public class TileGlowDuct extends TileTDBase {
    public LampGrid lampGrid = null;

    @Override
    public MultiBlockGrid getNewGrid() {

        return new LampGrid(worldObj);
    }

    @Override
    public void setGrid(MultiBlockGrid newGrid) {

        super.setGrid(newGrid);
        lampGrid = (LampGrid) newGrid;
    }

    @Override
    public boolean cachesExist() {

        return true;
    }

    @Override
    public void createCaches() {

    }

    @Override
    public void cacheImportant(TileEntity tile, int side) {

    }

    @Override
    public void clearCache(int side) {

    }

    boolean lit = false;

    @Override
    public int getLightValue() {

        return isLit() ? 15 : 0;
    }

    public boolean isLit() {

        return ServerHelper.isClientWorld(worldObj) || lampGrid == null ? lit : lampGrid.lit;
    }

    @Override
    public void blockPlaced() {

        super.blockPlaced();
        if (ServerHelper.isServerWorld(worldObj))
            lit = worldObj.isBlockIndirectlyGettingPowered(x(), y(), z());
        else {
            for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
                TileEntity tile = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
                if (tile instanceof TileGlowDuct) {
                    if (((TileGlowDuct) tile).lit) {
                        lit = true;
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onNeighborBlockChange() {

        super.onNeighborBlockChange();
        if (ServerHelper.isClientWorld(worldObj))
            return;

        lit = worldObj.isBlockIndirectlyGettingPowered(x(), y(), z());

        if (lampGrid != null && lampGrid.lit != lit)
            lampGrid.upToDate = false;
    }

    @Override
    public PacketCoFHBase getPacket() {

        PacketCoFHBase packet = super.getPacket();
        packet.addBool(lit || (lampGrid != null && lampGrid.lit));
        return packet;
    }

    @Override
    public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {
        super.handleTilePacket(payload, isServer);
        boolean b = payload.getBool();

        if (b != lit) {
            lit = b;
            checkLight();
        }
    }

    @Override
    public IIcon getBaseIcon() {
        return super.getBaseIcon();
    }

    public void checkLight() {
//        worldObj.func_147451_t(xCoord, yCoord, zCoord);
        worldObj.updateLightByType(EnumSkyBlock.Block, xCoord, yCoord, zCoord);
    }

    @Override
    public boolean isConnectable(TileEntity theTile, int side) {
        return theTile instanceof TileGlowDuct;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("isLit", lit);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        lit = nbt.getBoolean("isLit");
    }
}
