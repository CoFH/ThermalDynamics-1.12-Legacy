package thermaldynamics.ducts.energy;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import cofh.lib.util.helpers.BlockHelper;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thermaldynamics.block.BlockDuct;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class TileEnergyDuct extends TileMultiBlock implements IEnergyHandler {


    EnergyGrid internalGrid;
    public int type = 0;

    public TileEnergyDuct() {


    }

    static BlockDuct.ConnectionTypes[] types = {BlockDuct.ConnectionTypes.ENERGY_BASIC, BlockDuct.ConnectionTypes.ENERGY_HARDENED, BlockDuct.ConnectionTypes.ENERGY_REINFORCED};

    protected BlockDuct.ConnectionTypes getDefaultConnection() {
        return types[type];
    }

    public TileEnergyDuct(int type) {
        this.type = type;
    }


    @Override
    public void setGrid(MultiBlockGrid newGrid) {

        super.setGrid(newGrid);
        internalGrid = (EnergyGrid) newGrid;
    }

    @Override
    public MultiBlockGrid getNewGrid() {

        return new EnergyGrid(worldObj, type);
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return true;
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        return this.internalGrid != null ? this.internalGrid.myStorage.receiveEnergy(maxReceive, simulate) : 0;
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
        return this.internalGrid != null ? this.internalGrid.myStorage.extractEnergy(maxExtract, simulate) : 0;
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {

        return this.internalGrid != null ? this.internalGrid.myStorage.getEnergyStored() : 0;
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {

        return this.internalGrid != null ? this.internalGrid.myStorage.getMaxEnergyStored() : 0;
    }

    public boolean isConnectable(TileEntity theTile, int side) {
        return theTile instanceof TileEnergyDuct && ((TileEnergyDuct) theTile).type == type;
    }

    public boolean isSignificantTile(TileEntity theTile, int side) {

        return theTile instanceof IEnergyConnection && ((IEnergyConnection) theTile).canConnectEnergy(ForgeDirection.VALID_DIRECTIONS[side]);
    }

    public void tickPass(int pass) {

        int power = this.internalGrid.getSendableEnergy();
        int usedPower = 0;

        for (int i = this.internalSideCounter; i < this.neighborTypes.length && usedPower < power; i++) {
            if (this.neighborTypes[i] == NeighborTypes.TILE && this.connectionTypes[i] == ConnectionTypes.NORMAL) {
                TileEntity theTile = BlockHelper.getAdjacentTileEntity(this, i);
                if (theTile instanceof IEnergyHandler) {
                    IEnergyHandler theTileE = (IEnergyHandler) theTile;
                    if (theTileE.canConnectEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1])) {
                        usedPower += theTileE.receiveEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1], power - usedPower, false);
                    }
                    if (usedPower >= power) {
                        this.tickInternalSideCounter(i + 1);
                        break;
                    }
                }
            }
        }

        for (int i = 0; i < this.internalSideCounter && usedPower < power; i++) {
            if (this.neighborTypes[i] == NeighborTypes.TILE && this.connectionTypes[i] == ConnectionTypes.NORMAL) {
                TileEntity theTile = BlockHelper.getAdjacentTileEntity(this, i);
                if (theTile instanceof IEnergyHandler) {
                    IEnergyHandler theTileE = (IEnergyHandler) theTile;
                    if (theTileE.canConnectEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1])) {
                        usedPower += theTileE.receiveEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1], power - usedPower, false);
                    }
                    if (usedPower >= power) {
                        this.tickInternalSideCounter(i + 1);
                        break;
                    }
                }
            }
        }

        this.internalGrid.useEnergy(usedPower);
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        type = nbt.getByte("type");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("type", (byte) type);
    }
}
