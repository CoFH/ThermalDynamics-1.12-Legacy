package thermaldynamics.ducts.item;

import cofh.api.energy.IEnergyHandler;
import cofh.api.energy.IEnergyProvider;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import thermaldynamics.block.BlockDuct;
import thermaldynamics.block.SubTileMultiBlock;
import thermaldynamics.ducts.energy.subgrid.SubTileEnergy;

public class TileItemDuctPower extends TileItemDuct implements IEnergyHandler {
    SubTileEnergy energy;

    public TileItemDuctPower() {
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
    public boolean isStructureTile(TileEntity theTile, byte side) {
        return theTile instanceof IEnergyProvider && ((IEnergyProvider) theTile).canConnectEnergy(ForgeDirection.getOrientation(side ^ 1));
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
        return connectionTypes[from.ordinal()] != ConnectionTypes.BLOCKED &&
                (neighborTypes[from.ordinal()] == NeighborTypes.OUTPUT || neighborTypes[from.ordinal()] == NeighborTypes.STRUCTURE);
    }
}
