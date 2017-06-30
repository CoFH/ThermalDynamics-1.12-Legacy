package cofh.thermaldynamics.duct.tiles;

import cofh.redstoneflux.api.IEnergyReceiver;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergy;
import cofh.thermaldynamics.duct.fluid.DuctUnitFluid;
import cofh.thermaldynamics.duct.fluid.DuctUnitFluidSuper;
import cofh.thermaldynamics.duct.fluid.DuctUnitFluidTemperate;

public abstract class TileDuctFluid extends TileGridStructureBase {

	public static final int NODE_TRANSFER = 4000;

	public TileDuctFluid(Duct duct) {

		this(duct, true);
	}

	public TileDuctFluid(Duct duct, boolean addDefault) {

		if (addDefault) {
			addDuctUnits(DuctToken.FLUID, new DuctUnitFluid(this, duct));
		}
	}

	@Override
	protected DuctToken getPrimaryDuctToken() {

		return DuctToken.FLUID;
	}

	public static class Basic extends TileDuctFluid {

		public Basic(Duct duct) {

			super(duct, false);
			addDuctUnits(DuctToken.FLUID, new DuctUnitFluidTemperate(this, duct));
		}

		public static class Transparent extends Basic {

			public Transparent() {

				super(TDDucts.fluidBasic);
			}
		}

		public static class Opaque extends Basic {

			public Opaque() {

				super(TDDucts.fluidBasicOpaque);
			}
		}
	}

	public static class Hardened extends TileDuctFluid {

		public Hardened(Duct duct) {

			super(duct);
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

	public static class Energy extends TileDuctFluid implements IEnergyReceiver {

		public Energy(Duct duct) {

			super(duct);
			addDuctUnits(DuctToken.ENERGY, new DuctUnitEnergy(this, duct, NODE_TRANSFER, NODE_TRANSFER * 5));
		}

		public static class Transparent extends Energy {

			public Transparent() {

				super(TDDucts.fluidEnergy);
			}
		}

		public static class Opaque extends Energy {

			public Opaque() {

				super(TDDucts.fluidEnergyOpaque);
			}
		}
	}

	public static class Super extends TileDuctFluid {

		public Super(Duct duct) {

			super(duct, false);
			addDuctUnits(DuctToken.FLUID, new DuctUnitFluidSuper(this, duct));
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

}
