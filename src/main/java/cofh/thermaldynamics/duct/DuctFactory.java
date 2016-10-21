package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.energy.TileEnergyDuct;
import cofh.thermaldynamics.duct.energy.TileEnergyDuctSuper;
//import cofh.thermaldynamics.duct.entity.TileTransportDuct;
//import cofh.thermaldynamics.duct.entity.TileTransportDuctCrossover;
//import cofh.thermaldynamics.duct.entity.TileTransportDuctLongRange;
import cofh.thermaldynamics.duct.fluid.TileFluidDuct;
import cofh.thermaldynamics.duct.fluid.TileFluidDuctFlux;
import cofh.thermaldynamics.duct.fluid.TileFluidDuctFragile;
import cofh.thermaldynamics.duct.fluid.TileFluidDuctSuper;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TileItemDuctEnder;
import cofh.thermaldynamics.duct.item.TileItemDuctFlux;
import cofh.thermaldynamics.duct.light.TileLightDuct;

import net.minecraft.world.World;

public abstract class DuctFactory {

	public static DuctFactory structural = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileStructuralDuct();
		}
	};

	public static DuctFactory item = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileItemDuct();
		}
	};

	public static DuctFactory item_ender = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileItemDuctEnder();
		}
	};

	public static DuctFactory item_flux = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileItemDuctFlux();
		}
	};

	public static DuctFactory energy = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileEnergyDuct();
		}
	};

	public static DuctFactory energy_super = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileEnergyDuctSuper();
		}
	};

	public static DuctFactory fluid = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileFluidDuct();
		}
	};

	public static DuctFactory fluid_fragile = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileFluidDuctFragile();
		}
	};

	public static DuctFactory fluid_flux = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileFluidDuctFlux();
		}
	};

	public static DuctFactory glow = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileLightDuct();
		}
	};

	public static DuctFactory fluid_super = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileFluidDuctSuper();
		}
	};

	/*public static DuctFactory transport = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileTransportDuct();
		}
	};

	public static DuctFactory transport_longrange = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileTransportDuctLongRange();
		}
	};

	public static DuctFactory transport_crossover = new DuctFactory() {

		@Override
		public TileTDBase createTileEntity(Duct duct, World worldObj) {

			return new TileTransportDuctCrossover();
		}
	};*/

	public abstract TileTDBase createTileEntity(Duct duct, World worldObj);
}
