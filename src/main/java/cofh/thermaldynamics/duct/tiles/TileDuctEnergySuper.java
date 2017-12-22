package cofh.thermaldynamics.duct.tiles;

import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergySuper;

public class TileDuctEnergySuper extends TileGridSingle implements IEnergyProvider, IEnergyReceiver {

	public TileDuctEnergySuper() {

		super(DuctToken.ENERGY, TDDucts.energySuperCond);
	}

	@Override
	public DuctUnit createDuctUnit(DuctToken token, Duct ductType) {

		return new DuctUnitEnergySuper(this, ductType, 100, 100);
	}

}
