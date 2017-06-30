package cofh.thermaldynamics.duct.energy;

import cofh.redstoneflux.api.IEnergyReceiver;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.tiles.DuctUnit;
import cofh.thermaldynamics.duct.tiles.TileGrid;

public class DuctUnitEnergyStorage extends DuctUnitEnergy {

	public DuctUnitEnergyStorage(TileGrid parent, Duct duct, int transferLimit, int capacity) {

		super(parent, duct, transferLimit, capacity);
	}

	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitEnergy, GridEnergy, IEnergyReceiver> adjDuct, byte side, byte oppositeSide) {

		return super.canConnectToOtherDuct(adjDuct, side, oppositeSide) && adjDuct.cast() instanceof DuctUnitEnergyStorage;
	}

	@Override
	public boolean sendEnergy() {

		return true;
	}

}
