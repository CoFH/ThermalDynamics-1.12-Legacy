package cofh.thermaldynamics.duct.energy;

import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;

public class DuctUnitEnergyStorage extends DuctUnitEnergy {
	public DuctUnitEnergyStorage(TileGrid parent, Duct duct, int transferLimit, int capacity) {
		super(parent, duct, transferLimit, capacity);
	}

	@Override
	public DuctToken<DuctUnitEnergy, EnergyGrid, IEnergyReceiver> getToken() {
		return DuctToken.ENERGY_STORAGE;
	}

	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitEnergy, EnergyGrid, IEnergyReceiver> adjDuct, byte side) {
		return super.canConnectToOtherDuct(adjDuct, side) && adjDuct.cast() instanceof DuctUnitEnergyStorage;
	}

	@Override
	public boolean sendEnergy() {
		return true;
	}
}
