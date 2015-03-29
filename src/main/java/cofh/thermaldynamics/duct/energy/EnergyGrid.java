package cofh.thermaldynamics.duct.energy;

import cofh.api.energy.EnergyStorage;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import net.minecraft.world.World;

public class EnergyGrid extends MultiBlockGrid {

	public final EnergyStorage myStorage;
	private int currentEnergy = 0;
	private int extraEnergy = 0;

	private final int type;

	public static int NODE_STORAGE[] = { 1200, 4800, 48000, 192000, 0 };
	public static int NODE_TRANSFER[] = { 200, 800, 8000, 32000, 0 };

	public static void initialize() {

		String names[] = { "Basic", "Hardened", "Reinforced", "Resonant" };
		String category;
		String category2 = "Duct.Energy.";

		for (int i = 0; i < 4; i++) {
			category = category2 + names[i];
			NODE_TRANSFER[i] = MathHelper.clampI(ThermalDynamics.config.get(category, "Transfer", NODE_TRANSFER[i]), NODE_TRANSFER[i] / 10,
					NODE_TRANSFER[i] * 10);
			NODE_STORAGE[i] = NODE_TRANSFER[i] * 6;
		}
	}

	public EnergyGrid(World world, int type) {

		super(world);
		this.type = type;
		myStorage = new EnergyStorage(NODE_STORAGE[type], NODE_TRANSFER[type]);
	}

	@Override
	public void balanceGrid() {

		myStorage.setCapacity(nodeSet.size() * NODE_STORAGE[type]);
	}

	@Override
	public boolean canAddBlock(IMultiBlock aBlock) {

		return aBlock instanceof TileEnergyDuct && ((TileEnergyDuct) aBlock).getDuctType().type == this.type;
	}

	@Override
	public void tickGrid() {

		if (!nodeSet.isEmpty() && myStorage.getEnergyStored() > 0) {
			currentEnergy = myStorage.getEnergyStored() / nodeSet.size();
			extraEnergy = myStorage.getEnergyStored() % nodeSet.size();
			for (IMultiBlock m : nodeSet) {
				if (!m.tickPass(0) || m.getGrid() == null) {
					break;
				}
			}
		}
	}

	public int getSendableEnergy() {

		return Math.min(myStorage.getMaxExtract(), currentEnergy == 0 ? extraEnergy : currentEnergy);
	}

	public void useEnergy(int energyUsed) {

		myStorage.modifyEnergyStored(-energyUsed);
		if (energyUsed > currentEnergy) {
			extraEnergy -= (energyUsed - currentEnergy);
			extraEnergy = Math.max(0, extraEnergy);
		}
	}

	@Override
	public boolean canGridsMerge(MultiBlockGrid grid) {

		return super.canGridsMerge(grid) && ((EnergyGrid) grid).type == this.type;
	}

	@Override
	public void addNode(IMultiBlock aMultiBlock) {

		super.addNode(aMultiBlock);

		TileEnergyDuct theCondE = (TileEnergyDuct) aMultiBlock;
		if (theCondE.energyForGrid > 0) {
			myStorage.modifyEnergyStored(theCondE.energyForGrid);
		}
	}

	@Override
	public void removeBlock(IMultiBlock oldBlock) {

		if (oldBlock.isNode()) {
			((TileEnergyDuct) oldBlock).energyForGrid = getNodeShare(oldBlock);
		}
		super.removeBlock(oldBlock);
	}

	public int getNodeShare(IMultiBlock ductEnergy) {

		return nodeSet.size() == 1 ? myStorage.getEnergyStored() : isFirstMultiblock(ductEnergy) ? myStorage.getEnergyStored() / nodeSet.size()
				+ myStorage.getEnergyStored() % nodeSet.size() : myStorage.getEnergyStored() / nodeSet.size();
	}

}
