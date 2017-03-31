package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;

public class DuctUnitEnergyStorage  extends DuctUnitEnergy {
	public DuctUnitEnergyStorage(TileGrid parent, Duct duct) {
		super(parent, duct);
	}

	@Override
	public boolean sendEnergy() {
		return true;
	}
}
