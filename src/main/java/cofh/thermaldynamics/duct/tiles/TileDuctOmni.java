package cofh.thermaldynamics.duct.tiles;

import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.DuctItem;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergy;
import cofh.thermaldynamics.duct.energy.EnergyGrid;
import cofh.thermaldynamics.duct.fluid.DuctUnitFluidOmni;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGridStructureBase;

import javax.annotation.Nonnull;

public class TileDuctOmni extends TileGridStructureBase implements IEnergyReceiver {
	public TileDuctOmni(DuctItem ductType) {
		super();
		DuctUnitEnergy energy = new DuctUnitEnergy(this, ductType, 1000, 1000) {
			@Override
			public boolean canConnectToOtherDuct(DuctUnit<DuctUnitEnergy, EnergyGrid, IEnergyReceiver> adjDuct, byte side, byte oppositeSide) {
				return super.canConnectToOtherDuct(adjDuct, side, oppositeSide) && (adjDuct.getDuctType() == TDDucts.itemOmni || adjDuct.getDuctType() == TDDucts.itemOmniOpaque);
			}

			@Nonnull
			@Override
			protected BlockDuct.ConnectionType getConnectionTypeDuct(DuctUnitEnergy duct, int side) {
				return BlockDuct.ConnectionType.CLEAN_DUCT;
			}
		};
		addDuctUnits(DuctToken.ENERGY, energy);
		addDuctUnits(DuctToken.ITEMS, new DuctUnitItem(this, ductType));
		addDuctUnits(DuctToken.FLUID, new DuctUnitFluidOmni(this, ductType));
	}

	@Override
	protected DuctToken getPrimaryDuctToken() {
		return DuctToken.ITEMS;
	}

	public static class Transparent extends TileDuctOmni {

		public Transparent() {
			super(TDDucts.itemOmni);
		}
	}

	public static class Opaque extends TileDuctOmni {
		public Opaque() {
			super(TDDucts.itemOmniOpaque);
		}
	}
}
