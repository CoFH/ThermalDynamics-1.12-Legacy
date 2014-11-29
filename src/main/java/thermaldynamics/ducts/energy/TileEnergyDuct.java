package thermaldynamics.ducts.energy;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class TileEnergyDuct extends TileMultiBlock implements IEnergyHandler {

    public int energyForGrid = 0;
    public int lastStoredValue = 0;
    EnergyGrid internalGrid;



    public TileEnergyDuct() {


    }

    @Override
    public void setGrid(MultiBlockGrid newGrid) {

        super.setGrid(newGrid);
        internalGrid = (EnergyGrid) newGrid;
    }



    @Override
    public MultiBlockGrid getNewGrid() {

        return new EnergyGrid(worldObj, getDuctType().type);
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return //neighborTypes[from.ordinal()] == NeighborTypes.TILE &&
                connectionTypes[from.ordinal()] != ConnectionTypes.BLOCKED;
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        return (this.internalGrid != null && canConnectEnergy(from)) ? this.internalGrid.myStorage.receiveEnergy(maxReceive, simulate) : 0;
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
        return (this.internalGrid != null && canConnectEnergy(from)) ? this.internalGrid.myStorage.extractEnergy(maxExtract, simulate) : 0;
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
        return theTile instanceof TileEnergyDuct && ((TileEnergyDuct) theTile).getDuctType().type == getDuctType().type;
    }

    public boolean isSignificantTile(TileEntity theTile, int side) {
        return theTile instanceof IEnergyConnection && ((IEnergyConnection) theTile).canConnectEnergy(ForgeDirection.VALID_DIRECTIONS[side ^ 1])
        ;
    }

    @Override
    public void tileUnloading() {
        if (isNode) {
            internalGrid.myStorage.extractEnergy(lastStoredValue, false);
        }
    }

    public boolean tickPass(int pass) {
        int power = this.internalGrid.getSendableEnergy();

        int usedPower = transmitEnergy(power);

        this.internalGrid.useEnergy(usedPower);
        return super.tickPass(pass);
    }

    public int transmitEnergy(int power) {
        int usedPower = 0;

        for (byte i = this.internalSideCounter; i < this.neighborTypes.length && usedPower < power; i++) {
            if (this.neighborTypes[i] == NeighborTypes.OUTPUT && this.connectionTypes[i] == ConnectionTypes.NORMAL) {

                if (cache[i] != null) {

                    if (cache[i].canConnectEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1])) {
                        usedPower += cache[i].receiveEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1], power - usedPower, false);
                    }
                    if (usedPower >= power) {
                        this.tickInternalSideCounter(i + 1);
                        break;
                    }
                }
            }
        }

        for (byte i = 0; i < this.internalSideCounter && usedPower < power; i++) {
            if (this.neighborTypes[i] == NeighborTypes.OUTPUT && this.connectionTypes[i] == ConnectionTypes.NORMAL) {
                if (cache[i] != null) {
                    if (cache[i].canConnectEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1])) {
                        usedPower += cache[i].receiveEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1], power - usedPower, false);
                    }
                    if (usedPower >= power) {
                        this.tickInternalSideCounter(i + 1);
                        break;
                    }
                }
            }
        }
        return usedPower;
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        energyForGrid = nbt.getInteger("Energy");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        if (internalGrid != null) {
            if (isNode) {
                lastStoredValue = internalGrid.getNodeShare(this);
                nbt.setInteger("Energy", lastStoredValue);
            }
        } else if (energyForGrid > 0) {
            nbt.setInteger("Energy", energyForGrid);
        } else {
            energyForGrid = 0;
        }
    }

    IEnergyHandler[] cache = new IEnergyHandler[6];

    @Override
    public void cacheImportant(TileEntity tile, int side) {
        cache[side] = (IEnergyHandler) tile;
    }

    @Override
    public void clearCache(int side) {
        cache[side] = null;
    }


}
