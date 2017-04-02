package cofh.thermaldynamics.duct.tiles;

import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergy;
import cofh.thermaldynamics.duct.fluid.DuctUnitFluid;
import cofh.thermaldynamics.duct.fluid.DuctUnitFluidSuper;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.TileGridStructureBase;

public abstract class TileFluidDuct extends TileGridStructureBase {
	public TileFluidDuct(Duct ductType) {
		addDuctUnits(DuctToken.FLUID, new DuctUnitFluid(this, ductType));
	}

	@Override
	protected DuctToken getPrimaryDuctToken() {
		return DuctToken.FLUID;
	}

	public static class Fragile extends TileFluidDuct {

		public Fragile(Duct ductType) {
			super(ductType);
		}

		public static class Transparent extends Fragile {

			public Transparent() {
				super(TDDucts.fluidBasic);
			}
		}

		public static class Opaque extends Fragile {

			public Opaque() {
				super(TDDucts.fluidBasicOpaque);
			}
		}
	}

	public static class Super extends TileFluidDuct {

		public Super(Duct ductType) {
			super(ductType);
			addDuctUnits(DuctToken.FLUID, new DuctUnitFluidSuper(this, ductType));
		}

		public static class Transparent extends Super {

			public Transparent() {
				super(TDDucts.fluidSuper);
			}
		}

		public static class Opaque extends Super {

			public Opaque() {
				super(TDDucts.fluidSuperOpaque);
			}
		}
	}

	public static class Hardened extends TileFluidDuct {

		public Hardened(Duct ductType) {
			super(ductType);
		}

		public static class Transparent extends Hardened {

			public Transparent() {
				super(TDDucts.fluidHardened);
			}
		}

		public static class Opaque extends Hardened {

			public Opaque() {
				super(TDDucts.fluidHardenedOpaque);
			}
		}
	}


	public static class Flux extends TileFluidDuct implements IEnergyReceiver {

		public Flux(Duct ductType) {
			super(ductType);
			addDuctUnits(DuctToken.ENERGY, new DuctUnitEnergy(this, ductType, 1000, 1000));
		}

		public static class Transparent extends Flux {

			public Transparent() {
				super(TDDucts.fluidFlux);
			}
		}

		public static class Opaque extends Flux {

			public Opaque() {
				super(TDDucts.fluidFluxOpaque);
			}
		}
	}
}
