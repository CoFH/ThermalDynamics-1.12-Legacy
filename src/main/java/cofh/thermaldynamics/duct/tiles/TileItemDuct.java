package cofh.thermaldynamics.duct.tiles;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.DuctItem;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergy;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergyStorage;
import cofh.thermaldynamics.duct.energy.EnergyGrid;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import cofh.thermaldynamics.duct.item.DuctUnitItemEnder;

public class TileItemDuct extends TileGridStructureBase {

	public TileItemDuct() {
		// TODO: Temporary
	}

	public TileItemDuct(Duct duct) {

		addDuctUnits(DuctToken.ITEMS, new DuctUnitItem(this, duct));
	}

	@Override
	protected DuctToken getPrimaryDuctToken() {

		return DuctToken.ITEMS;
	}

	public static class Basic extends TileItemDuct {

		public Basic(Duct duct) {

			super(duct);
		}

		public static class Transparent extends Basic {

			public Transparent() {

				super(TDDucts.itemBasic);
			}
		}

		public static class Opaque extends Basic {

			public Opaque() {

				super(TDDucts.itemBasicOpaque);
			}
		}
	}

	public static class Fast extends TileItemDuct {

		public Fast(Duct duct) {

			super(duct);
		}

		public static class Transparent extends Fast {

			public Transparent() {

				super(TDDucts.itemFast);
			}
		}

		public static class Opaque extends Fast {

			public Opaque() {

				super(TDDucts.itemFastOpaque);
			}
		}
	}

	public static class Energy extends TileItemDuct implements IEnergyReceiver, IEnergyProvider {

		public Energy(Duct duct) {

			super(duct);
			addDuctUnits(DuctToken.ENERGY, new DuctUnitEnergy(this, duct, 2000, 2000));
		}

		public static class Transparent extends Energy {

			public Transparent() {

				super(TDDucts.itemEnergy);
			}
		}

		public static class Opaque extends Energy {

			public Opaque() {

				super(TDDucts.itemEnergyOpaque);
			}
		}
	}

	public static class EnergyFast extends TileItemDuct implements IEnergyReceiver, IEnergyProvider {

		public EnergyFast(Duct duct) {

			super(duct);
			addDuctUnits(DuctToken.ENERGY, new DuctUnitEnergy(this, duct, 2000, 2000));
		}

		public static class Transparent extends EnergyFast {

			public Transparent() {

				super(TDDucts.itemEnergyFast);
			}
		}

		public static class Opaque extends EnergyFast {

			public Opaque() {

				super(TDDucts.itemEnergyFastOpaque);
			}
		}
	}

	public static class Warp extends TileItemDuct implements IEnergyReceiver {

		public Warp(DuctItem duct) {

			DuctUnitEnergyStorage energyStorage = new DuctUnitEnergyStorage(this, duct, 400, 1000) {
				@Override
				public boolean canConnectToOtherDuct(DuctUnit<DuctUnitEnergy, EnergyGrid, IEnergyReceiver> adjDuct, byte side, byte oppositeSide) {

					return super.canConnectToOtherDuct(adjDuct, side, oppositeSide);
				}
			};
			addDuctUnits(DuctToken.ENERGY, energyStorage);
			addDuctUnits(DuctToken.ITEMS, new DuctUnitItemEnder(this, duct, energyStorage));
		}

		public static class Transparent extends Warp {

			public Transparent() {

				super(TDDucts.itemEnder);
			}
		}

		public static class Opaque extends Warp {

			public Opaque() {

				super(TDDucts.itemEnderOpaque);
			}
		}
	}
}
