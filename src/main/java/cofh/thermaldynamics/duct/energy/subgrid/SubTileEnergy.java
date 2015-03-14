package cofh.thermaldynamics.duct.energy.subgrid;

import cofh.thermaldynamics.block.SubTileMultiBlock;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import net.minecraft.nbt.NBTTagCompound;

public class SubTileEnergy extends SubTileMultiBlock {

	public int energyForGrid;
	public EnergySubGrid energyGrid;
	public int lastStoredValue;

	public SubTileEnergy(TileTDBase parent) {

		super(parent);
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		return new EnergySubGridDistribute(world(), 2400, 400);
	}

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		energyGrid = ((EnergySubGrid) newGrid);
	}

	@Override
	public boolean isNode() {

		return parent.isSubNode();
	}

    @Override
    public void onNeighborBlockChange() {
        parent.onNeighborBlockChange();
    }

    @Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);
		energyForGrid = nbt.getInteger("Energy");
	}

	@Override
	public void tileUnloading() {

		if (isNode()) {
			energyGrid.myStorage.extractEnergy(lastStoredValue, false);
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		if (energyGrid != null) {
			if (isNode()) {
				lastStoredValue = energyGrid.getNodeShare(this);
				nbt.setInteger("Energy", lastStoredValue);
			}
		} else if (energyForGrid > 0) {
			nbt.setInteger("Energy", energyForGrid);
		} else {
			energyForGrid = 0;
		}
	}

}
