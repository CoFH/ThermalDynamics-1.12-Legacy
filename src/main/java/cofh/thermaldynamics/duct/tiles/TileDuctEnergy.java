package cofh.thermaldynamics.duct.tiles;

import cofh.api.energy.IEnergyProvider;
import cofh.api.energy.IEnergyReceiver;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergy;

public class TileDuctEnergy extends TileGridStructureBase implements IEnergyReceiver, IEnergyProvider {

	public TileDuctEnergy(Duct duct) {

		addDuctUnits(DuctToken.ENERGY, new DuctUnitEnergy(this, duct));
	}

	@Override
	protected DuctToken getPrimaryDuctToken() {

		return DuctToken.ENERGY;
	}

	public static class Basic extends TileDuctEnergy {

		public Basic() {

			super(TDDucts.energyBasic);
		}
	}

	public static class Reinforced extends TileDuctEnergy {

		public Reinforced() {

			super(TDDucts.energyReinforced);
		}
	}

	public static class Hardened extends TileDuctEnergy {

		public Hardened() {

			super(TDDucts.energyHardened);
		}
	}

	public static class Signalum extends TileDuctEnergy {

		public Signalum() {

			super(TDDucts.energySignalum);
		}
	}

	public static class Resonant extends TileDuctEnergy {

		public Resonant() {

			super(TDDucts.energyResonant);
		}
	}

}
