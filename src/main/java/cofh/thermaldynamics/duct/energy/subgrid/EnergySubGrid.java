package cofh.thermaldynamics.duct.energy.subgrid;

import cofh.api.energy.EnergyStorage;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.world.World;

import java.util.List;

public class EnergySubGrid<T extends SubTileEnergy> extends MultiBlockGrid<T> {

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

	public int getNodeShare(T ductEnergy) {

		return nodeSet.size() == 1 ? myStorage.getEnergyStored() : isFirstMultiblock(ductEnergy) ? myStorage.getEnergyStored() / nodeSet.size() + myStorage.getEnergyStored() % nodeSet.size() : myStorage.getEnergyStored() / nodeSet.size();
	}

	@Override
	public void addBlock(T aBlock) {

		super.addBlock(aBlock);
		SubTileEnergy theCondE = (SubTileEnergy) aBlock;
		if (theCondE.energyForGrid > 0) {
			myStorage.modifyEnergyStored(theCondE.energyForGrid);
		}
	}

	@Override
	public void mergeGrids(MultiBlockGrid<T> theGrid) {

		super.mergeGrids(theGrid);
		balanceGrid();
		myStorage.modifyEnergyStored(((EnergySubGrid) theGrid).myStorage.getEnergyStored());
	}

	@Override
	public void destroyNode(IMultiBlock node) {

		if (node.isNode()) {
			T subTileEnergy = (T) node;
			subTileEnergy.energyForGrid = getNodeShare(subTileEnergy);
		}
		super.destroyNode(node);
	}

	@Override
	public void addInfo(List<ITextComponent> info, EntityPlayer player, boolean debug) {

		super.addInfo(info, player, debug);
	}

}
