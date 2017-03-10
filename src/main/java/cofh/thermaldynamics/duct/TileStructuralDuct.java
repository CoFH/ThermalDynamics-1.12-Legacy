package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.tileentity.TileEntity;

public class TileStructuralDuct extends TileDuctBase {

	@Override
	public boolean isConnectable(TileEntity theTile, int side) {

		return theTile != null && theTile.getClass() == this.getClass() && theTile.getBlockType() == this.getBlockType() && theTile.getBlockMetadata() == this.getBlockMetadata();
	}

	@Override
	public boolean cachesExist() {

		return true;
	}

	@Override
	public void createCaches() {

	}

	@Override
	public void cacheImportant(TileEntity tile, int side) {

	}

	@Override
	public void clearCache(int side) {

	}

	@Override
	public MultiBlockGrid createGrid() {

		return new GridStructural(worldObj);
	}

	@Override
	public boolean isStructureTile(TileEntity tile, int side) {

		return false;
	}

}
