package cofh.thermaldynamics.duct.tiles;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergyEnder;
import cofh.thermaldynamics.duct.fluid.DuctUnitFluidEnder;
import cofh.thermaldynamics.duct.item.DuctUnitItemEnder;

public class TileDuctEnder extends TileGridStructureBase {

	public static final int NODE_TRANSFER = 4000;

	public TileDuctEnder(Duct duct) {

		addDuctUnits(DuctToken.ITEMS, new DuctUnitItemEnder(this, duct));
		addDuctUnits(DuctToken.FLUID, new DuctUnitFluidEnder(this, duct));
		addDuctUnits(DuctToken.ENERGY, new DuctUnitEnergyEnder(this, duct, NODE_TRANSFER, NODE_TRANSFER * 5));
	}

	@Override
	protected DuctToken getPrimaryDuctToken() {

		return DuctToken.ITEMS;
	}

	public static class Transparent extends TileDuctEnder {

		public Transparent() {

			super(TDDucts.ender);
		}
	}

	public static class Opaque extends TileDuctEnder {

		public Opaque() {

			super(TDDucts.enderOpaque);
		}
	}

}
