package thermaldynamics.ducts;

import net.minecraft.world.World;

import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.energy.TileEnergyDuct;
import thermaldynamics.ducts.energy.TileEnergyDuctSuperConductor;
import thermaldynamics.ducts.fluid.TileFluidDuct;
import thermaldynamics.ducts.fluid.TileFluidDuctFlux;
import thermaldynamics.ducts.fluid.TileFluidDuctFragile;
import thermaldynamics.ducts.item.TileItemDuct;
import thermaldynamics.ducts.item.TileItemDuctEnder;
import thermaldynamics.ducts.item.TileItemDuctFlux;

public abstract class DuctFactory {

	public static DuctFactory structural = new DuctFactory() {

		@Override
		public TileMultiBlock createTileEntity(Duct duct, World worldObj) {

			return new TileStructuralDuct();
		}
	};

	public static DuctFactory item = new DuctFactory() {

		@Override
		public TileMultiBlock createTileEntity(Duct duct, World worldObj) {

			return new TileItemDuct();
		}
	};

	public static DuctFactory item_ender = new DuctFactory() {

		@Override
		public TileMultiBlock createTileEntity(Duct duct, World worldObj) {

			return new TileItemDuctEnder();
		}
	};

	public static DuctFactory item_flux = new DuctFactory() {

		@Override
		public TileMultiBlock createTileEntity(Duct duct, World worldObj) {

			return new TileItemDuctFlux();
		}
	};

	public static DuctFactory energy = new DuctFactory() {

		@Override
		public TileMultiBlock createTileEntity(Duct duct, World worldObj) {

			return new TileEnergyDuct();
		}
	};

	public static DuctFactory energy_super = new DuctFactory() {

		@Override
		public TileMultiBlock createTileEntity(Duct duct, World worldObj) {

			return new TileEnergyDuctSuperConductor();
		}
	};

	public static DuctFactory fluid = new DuctFactory() {

		@Override
		public TileMultiBlock createTileEntity(Duct duct, World worldObj) {

			return new TileFluidDuct();
		}
	};

	public static DuctFactory fluid_fragile = new DuctFactory() {

		@Override
		public TileMultiBlock createTileEntity(Duct duct, World worldObj) {

			return new TileFluidDuctFragile();
		}
	};

	public static DuctFactory fluid_flux = new DuctFactory() {

		@Override
		public TileMultiBlock createTileEntity(Duct duct, World worldObj) {

			return new TileFluidDuctFlux();
		}
	};

	public abstract TileMultiBlock createTileEntity(Duct duct, World worldObj);
}
