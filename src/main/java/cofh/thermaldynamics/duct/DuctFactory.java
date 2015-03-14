package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.energy.TileEnergyDuct;
import cofh.thermaldynamics.duct.energy.TileEnergyDuctSuperConductor;
import cofh.thermaldynamics.duct.fluid.TileFluidDuct;
import cofh.thermaldynamics.duct.fluid.TileFluidDuctFlux;
import cofh.thermaldynamics.duct.fluid.TileFluidDuctFragile;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TileItemDuctEnder;
import cofh.thermaldynamics.duct.item.TileItemDuctFlux;
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

			return new TileEnergyDuctSuperConductor();
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

	public abstract TileTDBase createTileEntity(Duct duct, World worldObj);
}
