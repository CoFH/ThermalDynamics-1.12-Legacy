package cofh.thermaldynamics.duct.attachments.cover;

import cofh.api.block.IBlockAppearance;
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

	IBlockAccess world;

	public static ThreadLocal<CoverBlockAccess> instances = ThreadLocal.withInitial(CoverBlockAccess::new);

	public static CoverBlockAccess getInstance(IBlockAccess world, BlockPos pos, EnumFacing side, IBlockState state) {

		CoverBlockAccess instance = instances.get();
		instance.world = world;
		instance.pos = pos;
		instance.side = side;
		instance.state = state;
		return instance;
	}

	public BlockPos pos;
	public EnumFacing side;

	IBlockState state;

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

		if (((side == DOWN && pos.getY() > this.pos.getY()) || (side == UP && pos.getY() < this.pos.getY()) || (side == NORTH && pos.getZ() > this.pos.getZ()) || (side == SOUTH && pos.getZ() < this.pos.getZ()) || (side == WEST && pos.getX() > this.pos.getX()) || (side == EAST && pos.getX() < this.pos.getX()))) {
			return AIR;
		}

		IBlockState worldState = world.getBlockState(pos);
		Block worldBlock = worldState.getBlock();

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
			if (worldState.equals(state)) {
				return state.isNormalCube() ? BEDROCK : AIR;
			} else {
				return ORIGINAL;
			}
		}
	}

	@Override
	public IBlockState getBlockState(BlockPos pos) {

		Result action = getAction(pos);
		return action == ORIGINAL ? world.getBlockState(pos) : action == AIR ? Blocks.AIR.getDefaultState() : action == BEDROCK ? Blocks.BEDROCK.getDefaultState() : action == COVER ? ((IBlockAppearance) world.getBlockState(pos).getBlock()).getVisualState(world, pos, side) : state;
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
