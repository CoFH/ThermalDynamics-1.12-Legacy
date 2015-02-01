package thermaldynamics.ducts.energy.subgrid;

import net.minecraft.world.World;

public class EnergySubGridDistribute extends EnergySubGrid {

	public EnergySubGridDistribute(World worldObj, int perStorage, int maxTransfer) {

		super(worldObj, perStorage, maxTransfer);
	}

	public int toDistribute = 0;

	@Override
	public void tickGrid() {

		super.tickGrid();
		toDistribute = nodeSet.size() != 0 ? myStorage.getEnergyStored() / nodeSet.size() : 0;
	}

}
