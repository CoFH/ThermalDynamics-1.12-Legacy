package cofh.thermaldynamics.duct.energy.subgrid;

import cofh.api.energy.EnergyStorage;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.world.World;

public class EnergySubGrid extends MultiBlockGrid {

	public EnergyStorage myStorage = new EnergyStorage(1000);
	public final int perStorage;

	public EnergySubGrid(World worldObj, int perStorage, int maxTransfer) {

		super(worldObj);
		this.perStorage = perStorage;
		myStorage.setMaxTransfer(maxTransfer);
	}

	@Override
	public void balanceGrid() {

		myStorage.setCapacity(nodeSet.size() * perStorage);
	}

	@Override
	public boolean canAddBlock(IMultiBlock aBlock) {

		return aBlock instanceof SubTileEnergy;
	}

	@Override
	public boolean canGridsMerge(MultiBlockGrid grid) {

		return super.canGridsMerge(grid);
	}

	public int getNodeShare(IMultiBlock ductEnergy) {

		return nodeSet.size() == 1 ? myStorage.getEnergyStored() : isFirstMultiblock(ductEnergy) ? myStorage.getEnergyStored() / nodeSet.size()
				+ myStorage.getEnergyStored() % nodeSet.size() : myStorage.getEnergyStored() / nodeSet.size();
	}

	@Override
	public void addBlock(IMultiBlock aBlock) {

		super.addBlock(aBlock);
		SubTileEnergy theCondE = (SubTileEnergy) aBlock;
		if (theCondE.energyForGrid > 0) {
			myStorage.modifyEnergyStored(theCondE.energyForGrid);
		}
	}

	@Override
	public void mergeGrids(MultiBlockGrid theGrid) {

		super.mergeGrids(theGrid);
		balanceGrid();
		myStorage.modifyEnergyStored(((EnergySubGrid) theGrid).myStorage.getEnergyStored());
	}

	@Override
	public void destroyNode(IMultiBlock node) {

		if (node.isNode()) {
			((SubTileEnergy) node).energyForGrid = getNodeShare(node);
		}
		super.destroyNode(node);
	}

}
