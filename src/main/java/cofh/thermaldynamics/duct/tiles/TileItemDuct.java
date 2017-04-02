package cofh.thermaldynamics.duct.tiles;

import cofh.thermaldynamics.duct.DuctItem;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergy;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergyStorage;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import cofh.thermaldynamics.duct.item.DuctUnitItemEnder;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.TileGridStructureBase;

public class TileItemDuct extends TileGridStructureBase {
	public TileItemDuct(DuctItem ductType) {
		addDuctUnits(DuctToken.ITEMS, new DuctUnitItem(this, ductType));
	}

	@Override
	protected DuctToken getPrimaryDuctToken() {
		return DuctToken.ITEMS;
	}

	public static class Basic extends TileItemDuct {
		public Basic() {
			super(TDDucts.itemBasic);
		}
	}

	public static class Opaque extends TileItemDuct {
		public Opaque() {
			super(TDDucts.itemBasicOpaque);
		}
	}

	public static class Fast extends TileItemDuct {
		public Fast() {
			super(TDDucts.itemFast);
		}
	}

	public static class FastOpaque extends TileItemDuct {
		public FastOpaque() {
			super(TDDucts.itemFastOpaque);
		}
	}

	public static class Flux extends TileItemDuct {
		public Flux(DuctItem ductType) {
			super(ductType);
			addDuctUnits(DuctToken.ENERGY, new DuctUnitEnergy(this, ductType, 400, 1000));
		}

		public static class Transparent extends TileItemDuct {

			public Transparent() {
				super(TDDucts.itemEnergy);
			}
		}

		public static class Opaque extends TileItemDuct {

			public Opaque() {
				super(TDDucts.itemEnergyOpaque);
			}
		}
	}

	public static class Ender extends TileItemDuct {
		public Ender(DuctItem ductType) {
			super(ductType);
			DuctUnitEnergyStorage energyStorage = new DuctUnitEnergyStorage(this, ductType, 1000, 1000);
			addDuctUnits(DuctToken.ENERGY_STORAGE, energyStorage);
			addDuctUnits(DuctToken.ITEMS, new DuctUnitItemEnder(this, ductType, energyStorage));
		}

		public static class Transparent extends Ender {

			public Transparent() {
				super(TDDucts.itemEnder);
			}
		}

		public static class Opaque extends Ender {
			public Opaque() {
				super(TDDucts.itemEnderOpaque);
			}
		}
	}
}
