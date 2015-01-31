package thermaldynamics.ducts.fluid;

import cofh.api.energy.IEnergyConnection;
import cofh.api.energy.IEnergyHandler;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thermaldynamics.block.BlockDuct;
import thermaldynamics.block.SubTileMultiBlock;
import thermaldynamics.ducts.energy.subgrid.SubTileEnergy;
import thermaldynamics.multiblock.MultiBlockGrid;

public abstract class TileFluidDuctPowered extends TileFluidDuct implements IEnergyHandler {


    SubTileEnergy energy;

    public TileFluidDuctPowered() {
        super();
    }

    public void setSubEnergy(SubTileEnergy energy) {
        this.energy = energy;
        this.subTiles = new SubTileMultiBlock[]{energy};
    }


    @Override
    public BlockDuct.ConnectionTypes getConnectionType(int side) {
        if (neighborTypes[side] == NeighborTypes.STRUCTURE)
            return connectionTypes[side] != ConnectionTypes.BLOCKED ? BlockDuct.ConnectionTypes.STRUCTURE : BlockDuct.ConnectionTypes.NONE;
        else
            return super.getConnectionType(side);
    }

    @Override
    public void setGrid(MultiBlockGrid newGrid) {
        super.setGrid(newGrid);
        if (newGrid != null) ((FluidGrid) newGrid).doesPassiveTicking = true;
    }

    @Override
    public boolean isStructureTile(TileEntity theTile, int side) {
        return theTile instanceof IEnergyConnection && ((IEnergyConnection) theTile).canConnectEnergy(ForgeDirection.getOrientation(side ^ 1));
    }

    @Override
    public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {
        return energy.energyGrid != null && canConnectEnergy(from) ? energy.energyGrid.myStorage.receiveEnergy(maxReceive, simulate) : 0;
    }

    @Override
    public int extractEnergy(ForgeDirection from, int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored(ForgeDirection from) {
        return energy.energyGrid != null ? energy.energyGrid.myStorage.getEnergyStored() : 0;
    }

    @Override
    public int getMaxEnergyStored(ForgeDirection from) {
        return energy.energyGrid != null ? energy.energyGrid.myStorage.getMaxEnergyStored() : 0;
    }

    @Override
    public boolean canConnectEnergy(ForgeDirection from) {
        return connectionTypes[from.ordinal()] != ConnectionTypes.BLOCKED ;
                //(neighborTypes[from.ordinal()] == NeighborTypes.OUTPUT || neighborTypes[from.ordinal()] == NeighborTypes.STRUCTURE);
    }



}
