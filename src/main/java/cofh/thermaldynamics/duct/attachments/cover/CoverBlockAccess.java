package cofh.thermaldynamics.duct.attachments.cover;

import cofh.core.render.IBlockAppearance;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import static cofh.thermaldynamics.duct.attachments.cover.CoverBlockAccess.Result.*;
import static net.minecraft.util.EnumFacing.*;

public class CoverBlockAccess implements IBlockAccess {

	public final IBlockAccess world;
	public final BlockPos pos;
	public final EnumFacing side;
	public final IBlockState state;

	public CoverBlockAccess(IBlockAccess world, BlockPos pos, EnumFacing side, IBlockState state) {

		this.world = world;
		this.pos = pos;
		this.side = side;
		this.state = state;
	}

	public enum Result {
		ORIGINAL, AIR, BASE, BEDROCK, COVER
	}

	public Result getAction(BlockPos pos) {

		if (this.pos == pos) {
			return BASE;
		}
		if (pos == this.pos.offset(side)) {
			return ORIGINAL;
		}
		IBlockState worldState = world.getBlockState(pos);
		Block worldBlock = worldState.getBlock();

		//This really isn't needed..
		if (worldBlock instanceof IBlockAppearance) {
			IBlockAppearance blockAppearance = (IBlockAppearance) worldBlock;
			if (blockAppearance.supportsVisualConnections()) {
				return COVER;
			}

			if (blockAppearance.getVisualState(world, pos, side).equals(state)) {
				return state.isNormalCube() ? BEDROCK : AIR;
			} else {
				return COVER;
			}
		} else {
			return ORIGINAL;
		}
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {

		IBlockState ret;
		Result action = getAction(pos);
		if (action == ORIGINAL) {
			ret = world.getBlockState(pos);
		} else if (action == AIR) {
			ret = Blocks.AIR.getDefaultState();
		} else if (action == BEDROCK) {
			ret = Blocks.BEDROCK.getDefaultState();
		} else if (action == COVER) {
			ret = ((IBlockAppearance) world.getBlockState(pos).getBlock()).getVisualState(world, pos, side);
		} else {
			ret = state;
		}
		return ret;
	}

	@Override
	public TileEntity getTileEntity(BlockPos pos) {

		return getAction(pos) == ORIGINAL ? world.getTileEntity(pos) : null;
	}

	@Override
	@SideOnly (Side.CLIENT)
	public int getCombinedLight(BlockPos pos, int t) {

		if (((side == DOWN && pos.getY() > this.pos.getY()) || (side == UP && pos.getY() < this.pos.getY()) || (side == NORTH && pos.getZ() > this.pos.getZ()) || (side == SOUTH && pos.getZ() < this.pos.getZ()) || (side == WEST && pos.getX() > this.pos.getX()) || (side == EAST && pos.getX() < this.pos.getX()))) {
			return world.getCombinedLight(this.pos, t);
		}
		return world.getCombinedLight(pos, t);
	}

	@Override
	public int getStrongPower(BlockPos pos, EnumFacing side) {

		return world.getStrongPower(pos, side);
	}

	@Override
	public WorldType getWorldType() {

		return world.getWorldType();
	}

	@Override
	public boolean isAirBlock(BlockPos pos) {

		Result action = getAction(pos);
		return action == AIR || (action == ORIGINAL && world.isAirBlock(pos)) || (action == COVER && getBlockState(pos).getBlock().isAir(getBlockState(pos), this, pos));
	}

	@Override
	@SideOnly (Side.CLIENT)
	public Biome getBiome(BlockPos pos) {

		return world.getBiome(pos);
	}

	@Override
	public boolean isSideSolid(BlockPos pos, EnumFacing side, boolean _default) {

		if (pos.getX() < -30000000 || pos.getZ() < -30000000 || pos.getX() >= 30000000 || pos.getZ() >= 30000000) {
			return _default;
		} else {
			return getBlockState(pos).isSideSolid(this, pos, side);
		}
	}
}
