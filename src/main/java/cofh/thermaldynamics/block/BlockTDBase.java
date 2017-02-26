package cofh.thermaldynamics.block;

import codechicken.lib.block.property.PropertyInteger;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.RayTracer;
import cofh.api.energy.IEnergyHandler;
import cofh.api.tileentity.ITileInfo;
import cofh.core.block.BlockCoreTile;
import cofh.lib.util.helpers.ServerHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.lib.util.helpers.WrenchHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.TileTDBase.NeighborTypes;
import cofh.thermaldynamics.duct.entity.EntityTransport;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;

public abstract class BlockTDBase extends BlockCoreTile {

	public static PropertyInteger META = new PropertyInteger("meta", 15);

	protected BlockTDBase(Material material) {

		super(material);
		setSoundType(SoundType.METAL);
		setCreativeTab(ThermalDynamics.tabCommon);
	}

	@Override
	public int getMetaFromState(IBlockState state) {

		return state.getValue(META);
	}

	@Override
	public IBlockState getStateFromMeta(int meta) {

		return getDefaultState().withProperty(META, meta);
	}

	@Override
	protected BlockStateContainer createBlockState() {

		return new BlockStateContainer(this, META);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {

		return null;
	}

	/* BLOCK METHODS */
	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {

		TileTDBase theTile = (TileTDBase) world.getTileEntity(pos);
		return (theTile != null && (theTile.covers[side.ordinal()] != null || theTile.attachments[side.ordinal()] != null && theTile.attachments[side.ordinal()].makesSideSolid())) || super.isSideSolid(base_state, world, pos, side);
	}

	@Override
	public boolean isFullCube(IBlockState state) {

		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {

		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {

		return false;
	}

	public float getSize(IBlockState state) {

		return 0.3F;
	}

	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity) {

		if (entity instanceof EntityTransport) {
		    return;
		}

		float min = getSize(state);
		float max = 1 - min;

		AxisAlignedBB bb = new AxisAlignedBB(min, min, min, max, max, max);
		addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
		TileTDBase theTile = (TileTDBase) world.getTileEntity(pos);

		if (theTile != null) {
			for (byte i = 0; i < 6; i++) {
				if (theTile.attachments[i] != null) {
					theTile.attachments[i].addCollisionBoxesToList(entityBox, collidingBoxes, entity);
				}
				if (theTile.covers[i] != null) {
					theTile.covers[i].addCollisionBoxesToList(entityBox, collidingBoxes, entity);
				}
			}

			if (theTile.neighborTypes[0] != NeighborTypes.NONE) {
				bb = new AxisAlignedBB(min, 0.0F, min, max, max, max);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
			}
			if (theTile.neighborTypes[1] != NeighborTypes.NONE) {
				bb = new AxisAlignedBB(min, min, min, max, 1.0F, max);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
			}
			if (theTile.neighborTypes[2] != NeighborTypes.NONE) {
				bb = new AxisAlignedBB(min, min, 0.0F, max, max, max);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
			}
			if (theTile.neighborTypes[3] != NeighborTypes.NONE) {
				bb = new AxisAlignedBB(min, min, min, max, max, 1.0F);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
			}
			if (theTile.neighborTypes[4] != NeighborTypes.NONE) {
				bb = new AxisAlignedBB(0.0F, min, min, max, max, max);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
			}
			if (theTile.neighborTypes[5] != NeighborTypes.NONE) {
				bb = new AxisAlignedBB(min, min, min, 1.0F, max, max);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
			}
		}
	}

	@Override
	public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {

		float min = getSize(state);
		float max = 1 - min;
		return new AxisAlignedBB(min, min, min, max, max, max);
	}

	@Override
	public RayTraceResult collisionRayTrace(IBlockState blockState, World world, BlockPos pos, Vec3d start, Vec3d end) {

		TileTDBase theTile = (TileTDBase) world.getTileEntity(pos);
		if (theTile == null) {
			return null;
		}
		List<IndexedCuboid6> cuboids = new LinkedList<>();
		theTile.addTraceableCuboids(cuboids);
		return RayTracer.rayTraceCuboidsClosest(start, end, cuboids, pos);
	}

	@Override
	public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, @Nullable ItemStack heldItem, EnumFacing side, float hitX, float hitY, float hitZ) {

		RayTraceResult traceResult = RayTracer.retraceBlock(world, player, pos);
		if (traceResult == null || traceResult.getBlockPos() != pos) {
			return false;
		}
		PlayerInteractEvent event = new PlayerInteractEvent.RightClickBlock(player, hand, heldItem, pos, side, traceResult.hitVec);
		if (MinecraftForge.EVENT_BUS.post(event) || event.getResult() == Result.DENY) {
			return false;
		}
		if (player.isSneaking()) {
			if (WrenchHelper.isHoldingUsableWrench(player, traceResult)) {
				if (ServerHelper.isServerWorld(world)) {
					dismantleBlock(world, pos, state, player, false);
					WrenchHelper.usedWrench(player, traceResult);
				}
				return true;
			}
			return false;
		}
		TileTDBase tile = (TileTDBase) world.getTileEntity(pos);

		if (tile == null) {
			return false;
		}
		if (WrenchHelper.isHoldingUsableWrench(player, traceResult)) {
			if (ServerHelper.isServerWorld(world)) {
				tile.onWrench(player, side);
			}
			WrenchHelper.usedWrench(player, traceResult);
			return true;
		}

		return tile.openGui(player);
	}

	/* IBlockInfo */
	@Override
	public void getBlockInfo(List<ITextComponent> info, IBlockAccess world, BlockPos pos, EnumFacing side, EntityPlayer player, boolean debug) {

		TileEntity tile = world.getTileEntity(pos);

		if (tile instanceof ITileInfo) {
			((ITileInfo) tile).getTileInfo(info, side, player, debug);
		} else {
			if (tile instanceof IEnergyHandler) {
				IEnergyHandler eHandler = (IEnergyHandler) tile;
				if (eHandler.getMaxEnergyStored(side) <= 0) {
					return;
				}
				info.add(new TextComponentString(StringHelper.localize("info.cofh.energy") + ": " + eHandler.getEnergyStored(side) + "/" + eHandler.getMaxEnergyStored(side) + " RF."));
			}
		}
	}

}
