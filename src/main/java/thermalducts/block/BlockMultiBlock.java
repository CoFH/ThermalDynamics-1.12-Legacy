package thermalducts.block;

import net.minecraft.block.Block;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public abstract class BlockMultiBlock extends Block implements ITileEntityProvider {

	protected BlockMultiBlock(Material p_i45394_1_) {

		super(p_i45394_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {

		return new TileMultiBlock();
	}

	@Override
	public void onNeighborBlockChange(World worldObj, int xCoord, int yCoord, int zCoord, Block neighborBlock) {

		((TileMultiBlock) worldObj.getTileEntity(xCoord, yCoord, zCoord)).neighborChanged();
	}

	@Override
	public void onNeighborChange(IBlockAccess worldObj, int xCoord, int yCoord, int zCoord, int tileX, int tileY, int tileZ) {

		((TileMultiBlock) worldObj.getTileEntity(xCoord, yCoord, zCoord)).neighborChanged(tileX, tileY, tileZ);
	}

}
