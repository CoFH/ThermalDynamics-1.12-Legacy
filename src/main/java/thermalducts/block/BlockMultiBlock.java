package thermalducts.block;

import cofh.block.BlockCoFHBase;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public abstract class BlockMultiBlock extends BlockCoFHBase implements ITileEntityProvider {

	protected BlockMultiBlock(Material p_i45394_1_) {

		super(p_i45394_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {

		return new TileMultiBlock();
	}

}
