package cofh.thermaldynamics.duct.energy;

import cofh.redstoneflux.impl.EnergyStorage;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.multiblock.MultiBlockGridTracking;
import net.minecraft.world.World;

public class GridEnergy extends MultiBlockGridTracking<DuctUnitEnergy> {

	public static final int XFER_BASE = 1000;

	public static int CAPACITY[] = { 1 * 5, 4 * 5, 9 * 5, 16 * 5, 25 * 5, 0 };
	public static int XFER[] = { 1, 4, 9, 16, 25, 0 };
	public final EnergyStorage myStorage;
	private final int transferLimit;

	private final int capacity;
	private int currentEnergy = 0;
	private int extraEnergy = 0;

	public static void initialize() {

		String category = "Duct.Energy";

		int xfer = XFER_BASE;
		String comment = "Adjust this value to change the amount of Energy (in RF/t) that can be received by a Leadstone Fluxduct. This base value will scale with duct level.";
		xfer = ThermalDynamics.CONFIG.getConfiguration().getInt("BaseTransfer", category, xfer, xfer / 10, xfer * 10, comment);

		for (int i = 0; i < CAPACITY.length; i++) {
			CAPACITY[i] *= xfer;
			XFER[i] *= xfer;
		}
	}

	public GridEnergy(World world, int transferLimit, int capacity) {

		super(world);

		this.transferLimit = transferLimit;
		this.capacity = capacity;
		myStorage = new EnergyStorage(GridEnergy.this.capacity, GridEnergy.this.transferLimit) {

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

	@Override
	public void addNode(DuctUnitEnergy aMultiBlock) {

		super.addNode(aMultiBlock);

		if (aMultiBlock.getEnergyForGrid() > 0) {
			myStorage.modifyEnergyStored(aMultiBlock.getEnergyForGrid());
		}
	}

	@Override
	public void balanceGrid() {

		myStorage.setCapacity(nodeSet.size() * capacity);
	}

	@Override
	public void destroyNode(IGridTile node) {

		if (node.isNode()) {
			((DuctUnitEnergy) node).setEnergyForGrid(getNodeShare((DuctUnitEnergy) node));
		}
		super.destroyNode(node);
	}

	@Override
	public void removeBlock(DuctUnitEnergy oldBlock) {

		if (oldBlock.isNode()) {
			oldBlock.setEnergyForGrid(getNodeShare(oldBlock));
		}
		super.removeBlock(oldBlock);
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

	@Override
	public boolean canAddBlock(IGridTile aBlock) {

		return aBlock instanceof DuctUnitEnergy && ((DuctUnitEnergy) aBlock).getTransferLimit() == transferLimit;
	}

	@Override
	public boolean canGridsMerge(MultiBlockGrid grid) {

		return super.canGridsMerge(grid) && ((GridEnergy) grid).transferLimit == this.transferLimit;
	}

	@Override
	protected int getLevel() {

		return myStorage.getEnergyStored();
	}

	@Override
	protected String getUnit() {

		return "RF";
	}

	/* HELPERS */
	public void useEnergy(int energyUsed) {

		myStorage.extractEnergy(energyUsed, false);

		if (energyUsed > currentEnergy) {
			extraEnergy -= (energyUsed - currentEnergy);
			extraEnergy = Math.max(0, extraEnergy);
		}
	}

	public boolean isPowered() {

		return myStorage.getEnergyStored() > 0;
	}

	public int getNodeShare(DuctUnitEnergy ductEnergy) {

		return nodeSet.size() == 1 ? myStorage.getEnergyStored() : isFirstMultiblock(ductEnergy) ? myStorage.getEnergyStored() / nodeSet.size() + myStorage.getEnergyStored() % nodeSet.size() : myStorage.getEnergyStored() / nodeSet.size();
	}

	public int getSendableEnergy() {

		return Math.min(myStorage.getMaxExtract(), currentEnergy == 0 ? extraEnergy : currentEnergy);
	}

	public int receiveEnergy(int maxReceive, boolean simulate) {

		return myStorage.receiveEnergy(maxReceive, simulate);
	}

}
