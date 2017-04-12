package cofh.thermaldynamics.duct.tiles;

import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.duct.DuctItem;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergy;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergyStorage;
import cofh.thermaldynamics.duct.energy.EnergyGrid;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import cofh.thermaldynamics.duct.item.DuctUnitItemEnder;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGridStructureBase;

public class TileItemDuct extends TileGridStructureBase {
	@Override
	protected DuctToken getPrimaryDuctToken() {
		return DuctToken.ITEMS;
	}

	public static class Basic extends TileItemDuct {
		public Basic() {
			addDuctUnits(DuctToken.ITEMS, new DuctUnitItem(this, TDDucts.itemBasic));
		}
	}

	public static class Opaque extends TileItemDuct {
		public Opaque() {
			addDuctUnits(DuctToken.ITEMS, new DuctUnitItem(this, TDDucts.itemBasicOpaque));
		}
	}

	public static class Fast extends TileItemDuct {
		public Fast() {
			addDuctUnits(DuctToken.ITEMS, new DuctUnitItem(this, TDDucts.itemFast));
		}
	}

	public static class FastOpaque extends TileItemDuct {
		public FastOpaque() {
			addDuctUnits(DuctToken.ITEMS, new DuctUnitItem(this, TDDucts.itemFastOpaque));
		}
	}

	public static class Flux extends TileItemDuct implements IEnergyReceiver {
		public Flux(DuctItem ductType) {
			super();
			addDuctUnits(DuctToken.ENERGY, new DuctUnitEnergy(this, ductType, 400, 1000));
			addDuctUnits(DuctToken.ITEMS, new DuctUnitItem(this, ductType));
		}

		public static class Transparent extends TileItemDuct {

			public Transparent() {
				super();
			}
		}

		public static class Opaque extends TileItemDuct {

			public Opaque() {
				super();
			}
		}
	}


	public static class Warp extends TileItemDuct implements IEnergyReceiver {
		public Warp(DuctItem ductType) {
			DuctUnitEnergyStorage energyStorage = new DuctUnitEnergyStorage(this, ductType, 400, 1000){
				@Override
				public boolean canConnectToOtherDuct(DuctUnit<DuctUnitEnergy, EnergyGrid, IEnergyReceiver> adjDuct, byte side, byte oppositeSide) {
					return super.canConnectToOtherDuct(adjDuct, side, oppositeSide);
				}
			};
			addDuctUnits(DuctToken.ENERGY, energyStorage);
			addDuctUnits(DuctToken.ITEMS, new DuctUnitItemEnder(this, ductType, energyStorage));
		}

		public static class Transparent extends Warp {

			public Transparent() {
				super(TDDucts.itemEnder);
			}
		}

		public static class Opaque extends Warp {

			public Opaque() {
				super(TDDucts.itemEnder);
			}
		}
	}
}
