package cofh.thermaldynamics.duct.energy.subgrid;

import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.TileDuctBase;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

public class SubTileEnergyRedstone extends SubTileEnergy {

	public static int NODE_TRANSFER = 2000;
	public static int NODE_STORAGE = 12000;

	public static void initialize() {

		String category = "Duct.Energy.Hybrid";
		NODE_TRANSFER = MathHelper.clamp(ThermalDynamics.CONFIG.get(category, "Transfer", NODE_TRANSFER), NODE_TRANSFER / 10, NODE_TRANSFER * 10);
		NODE_STORAGE = NODE_TRANSFER * 6;
	}

	public EnergySubGridDistribute internalGrid;

	public SubTileEnergyRedstone(TileDuctBase parent) {

		super(parent);
	}

	@Override
	public MultiBlockGrid createGrid() {

		return new EnergySubGridDistribute(parent.world(), NODE_STORAGE, NODE_TRANSFER);
	}

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		internalGrid = (EnergySubGridDistribute) newGrid;
	}

}
