package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;

public class DuctUnitEnergySuper extends DuctUnitEnergy {

	public DuctUnitEnergySuper(TileGrid parent, Duct duct) {
		super(parent, duct);
	}

	@Override
	public EnergyGridSuper createGrid() {

		return new EnergyGridSuper(parent.world(), getTransferLimit(), getCapacity());
	}
}
