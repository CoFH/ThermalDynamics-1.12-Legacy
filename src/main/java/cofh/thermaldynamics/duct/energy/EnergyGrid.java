package cofh.thermaldynamics.duct.energy;

import cofh.api.energy.EnergyStorage;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.multiblock.MultiBlockGridTracking;
import net.minecraft.world.World;

public class EnergyGrid extends MultiBlockGridTracking<DuctUnitEnergy> {

	public static int NODE_STORAGE[] = {1000, 800 * 5, 8000 * 5, 16000 * 5, 32000 * 5, 0};
	public static int NODE_TRANSFER[] = {200, 800, 8000, 16000, 32000, 0};
	public final EnergyStorage myStorage;
	private final int transferLimit;

	private final int capacity;
	private int currentEnergy = 0;
	private int extraEnergy = 0;

	public EnergyGrid(World world, int transferLimit, int capacity) {

		super(world);
		this.transferLimit = transferLimit;
		this.capacity = capacity;
		myStorage = new EnergyStorage(EnergyGrid.this.capacity, EnergyGrid.this.transferLimit) {

			@Override
			public int receiveEnergy(int maxReceive, boolean simulate) {

				return trackIn(super.receiveEnergy(maxReceive, simulate), simulate);
			}

			@Override
			public int extractEnergy(int maxExtract, boolean simulate) {

				return trackOut(super.extractEnergy(maxExtract, simulate), simulate);
			}
		};
	}

	public static void initialize() {

		String names[] = {"Basic", "Hardened", "Reinforced", "Resonant"};
		String category;
		String category2 = "Duct.Energy.";

		for (int i = 0; i < 4; i++) {
			category = category2 + names[i];
			NODE_TRANSFER[i] = MathHelper.clamp(ThermalDynamics.CONFIG.get(category, "Transfer", NODE_TRANSFER[i]), NODE_TRANSFER[i] / 10, NODE_TRANSFER[i] * 10);
			NODE_STORAGE[i] = NODE_TRANSFER[i] * 6;
		}
	}

	@Override
	public void balanceGrid() {

		myStorage.setCapacity(nodeSet.size() * capacity);
	}

	@Override
	public boolean canAddBlock(IGridTile aBlock) {

		return aBlock instanceof DuctUnitEnergy && ((DuctUnitEnergy) aBlock).getTransferLimit() == transferLimit;
	}

	@Override
	public int getLevel() {

		return myStorage.getEnergyStored();
	}

	@Override
	public void tickGrid() {

		super.tickGrid();
		if (!nodeSet.isEmpty() && myStorage.getEnergyStored() > 0) {
			currentEnergy = myStorage.getEnergyStored() / nodeSet.size();
			extraEnergy = myStorage.getEnergyStored() % nodeSet.size();
			for (IGridTile m : nodeSet) {
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

		myStorage.extractEnergy(energyUsed, false);
		if (energyUsed > currentEnergy) {
			extraEnergy -= (energyUsed - currentEnergy);
			extraEnergy = Math.max(0, extraEnergy);
		}
	}

	@Override
	public boolean canGridsMerge(MultiBlockGrid grid) {

		return super.canGridsMerge(grid) && ((EnergyGrid) grid).transferLimit == this.transferLimit;
	}

	@Override
	public void addNode(DuctUnitEnergy aMultiBlock) {

		super.addNode(aMultiBlock);

		if (aMultiBlock.getEnergyForGrid() > 0) {
			myStorage.modifyEnergyStored(aMultiBlock.getEnergyForGrid());
		}
	}

	@Override
	public void removeBlock(DuctUnitEnergy oldBlock) {

		if (oldBlock.isNode()) {
			oldBlock.setEnergyForGrid(getNodeShare(oldBlock));
		}
		super.removeBlock(oldBlock);
	}

	//TODO:Rework
	public int getNodeShare(DuctUnitEnergy ductEnergy) {

		return nodeSet.size() == 1 ? myStorage.getEnergyStored() : isFirstMultiblock(ductEnergy) ? myStorage.getEnergyStored() / nodeSet.size() + myStorage.getEnergyStored() % nodeSet.size() : myStorage.getEnergyStored() / nodeSet.size();
	}

	@Override
	protected String getUnit() {

		return "RF";
	}

	public int receiveEnergy(int maxReceive, boolean simulate) {
		return myStorage.receiveEnergy(maxReceive, simulate);
	}

	public boolean isPowered() {
		return myStorage.getEnergyStored() > 0;
	}
}
