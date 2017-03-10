package cofh.thermaldynamics.duct;

import codechicken.lib.block.property.PropertyInteger;
import codechicken.lib.model.DummyBakedModel;
import codechicken.lib.model.ModelRegistryHelper;
import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.RayTracer;
import cofh.core.render.IBlockAppearance;
import cofh.api.block.IBlockConfigGui;
import cofh.core.render.IModelRegister;
import cofh.core.init.CoreProps;
import cofh.core.network.PacketHandler;
import cofh.core.render.hitbox.ICustomHitBox;
import cofh.core.render.hitbox.RenderHitbox;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.BlockTDBase;
import cofh.thermaldynamics.duct.TileDuctBase.NeighborTypes;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.energy.EnergyGrid;
import cofh.thermaldynamics.duct.energy.TileEnergyDuct;
import cofh.thermaldynamics.duct.energy.TileEnergyDuctSuper;
import cofh.thermaldynamics.duct.energy.subgrid.SubTileEnergyRedstone;
import cofh.thermaldynamics.duct.entity.*;
import cofh.thermaldynamics.duct.fluid.*;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TileItemDuctEnder;
import cofh.thermaldynamics.duct.item.TileItemDuctFlux;
import cofh.thermaldynamics.proxy.ProxyClient;
import cofh.thermaldynamics.render.RenderDuct;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.renderer.block.statemap.StateMap;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumBlockRenderType;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.registry.EntityRegistry;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class BlockDuct extends BlockTDBase implements IBlockAppearance, IBlockConfigGui, IModelRegister {

	public int offset;

	public static PropertyInteger META = new PropertyInteger("meta", 15);

	public BlockDuct(int offset) {

		super(Material.GLASS);
		setHardness(1.0F);
		setResistance(10.0F);
		setUnlocalizedName("thermaldynamics.duct");
		setDefaultState(getBlockState().getBaseState().withProperty(META, 0));
		this.offset = offset * 16;
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
	public void getSubBlocks(Item item, CreativeTabs tab, List<ItemStack> list) {

		for (int i = 0; i < 16; i++) {
			if (TDDucts.isValid(i + offset)) {
				list.add(TDDucts.getDuct(i + offset).itemStack.copy());
			}
		}
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {

		TileDuctBase theTile = (TileDuctBase) world.getTileEntity(pos);
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

	@Override
	public boolean hasTileEntity(IBlockState state) {

		return TDDucts.isValid(getMetaFromState(state) + offset);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {

		Duct duct = TDDucts.getType(metadata + offset);
		return duct.factory.createTileEntity(duct, world);
	}

	@Override
	public int damageDropped(IBlockState state) {

		return getMetaFromState(state);
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
		TileDuctBase theTile = (TileDuctBase) world.getTileEntity(pos);

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

		TileDuctBase theTile = (TileDuctBase) world.getTileEntity(pos);
		if (theTile == null) {
			return null;
		}
		List<IndexedCuboid6> cuboids = new LinkedList<>();
		theTile.addTraceableCuboids(cuboids);
		return RayTracer.rayTraceCuboidsClosest(start, end, cuboids, pos);
	}

	@SideOnly (Side.CLIENT)
	@SubscribeEvent (priority = EventPriority.HIGH)
	public void onBlockHighlight(DrawBlockHighlightEvent event) {

		RayTraceResult target = event.getTarget();
		EntityPlayer player = event.getPlayer();
		if (target.typeOfHit == Type.BLOCK && player.worldObj.getBlockState(event.getTarget().getBlockPos()).getBlock().getUnlocalizedName().equals(getUnlocalizedName())) {
			RayTracer.retraceBlock(player.worldObj, player, target.getBlockPos());

			ICustomHitBox theTile = ((ICustomHitBox) player.worldObj.getTileEntity(target.getBlockPos()));
			if (theTile.shouldRenderCustomHitBox(target.subHit, player)) {
				event.setCanceled(true);
				RenderHitbox.drawSelectionBox(player, target, event.getPartialTicks(), theTile.getCustomHitBox(target.subHit, player));
			}
		}
	}

	@Override
	public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {

		if (target.subHit >= 14 && target.subHit < 20) {
			TileDuctBase tileEntity = (TileDuctBase) world.getTileEntity(pos);
			ItemStack pickBlock = tileEntity.attachments[target.subHit - 14].getPickBlock();
			if (pickBlock != null) {
				return pickBlock;
			}
		}
		if (target.subHit >= 20 && target.subHit < 26) {
			TileDuctBase tileEntity = (TileDuctBase) world.getTileEntity(pos);
			ItemStack pickBlock = tileEntity.covers[target.subHit - 20].getPickBlock();
			if (pickBlock != null) {
				return pickBlock;
			}
		}
		return super.getPickBlock(state, target, world, pos, player);
	}

	@Override
	@SideOnly (Side.CLIENT)
	public EnumBlockRenderType getRenderType(IBlockState state) {

		return ProxyClient.renderType;
	}

	@Override
	@SideOnly (Side.CLIENT)
	public BlockRenderLayer getBlockLayer() {

		return BlockRenderLayer.CUTOUT;
	}

	@Override
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {

		return true;
	}

	@Override
	public IBlockState getVisualState(IBlockAccess world, BlockPos pos, EnumFacing side) {

		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileDuctBase) {
			Cover cover = ((TileDuctBase) tileEntity).covers[side.ordinal()];
			if (cover != null) {
				return cover.state;
			}
		}
		return world.getBlockState(pos);
	}

	@Override
	public boolean supportsVisualConnections() {

		return true;
	}

	@Override
	public boolean openConfigGui(IBlockAccess world, BlockPos pos, EnumFacing side, EntityPlayer player) {

		TileDuctBase tile = (TileDuctBase) world.getTileEntity(pos);
		if (tile instanceof IBlockConfigGui) {
			return ((IBlockConfigGui) tile).openConfigGui(world, pos, side, player);
		} else {
			int subHit = side.ordinal();
			if (world instanceof World) {
				RayTraceResult rayTrace = RayTracer.retraceBlock((World) world, player, pos);
				if (rayTrace == null) {
					return false;
				}

				if (subHit > 13 && subHit < 20) {
					subHit = rayTrace.subHit - 14;
				}
			}

			if (subHit > 13 && subHit < 20) {
				Attachment attachment = tile.attachments[subHit - 14];
				if (attachment instanceof IBlockConfigGui) {
					return ((IBlockConfigGui) attachment).openConfigGui(world, pos, side, player);
				}
			}

		}

		return false;
	}

	public enum ConnectionTypes {
		NONE(false), DUCT, TILECONNECTION, STRUCTURE, CLEANDUCT;

		private final boolean renderDuct;

		ConnectionTypes() {

			renderDuct = true;
		}

		ConnectionTypes(boolean renderDuct) {

			this.renderDuct = renderDuct;
		}

		public boolean renderDuct() {

			return renderDuct;
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase living, ItemStack stack) {

		super.onBlockPlacedBy(world, pos, state, living, stack);
		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileDuctBase) {
			((TileDuctBase) tile).onPlacedBy(living, stack);
		}
	}

	@Override
	@SideOnly (Side.CLIENT)
	public void randomDisplayTick(IBlockState state, World world, BlockPos pos, Random rand) {

		super.randomDisplayTick(state, world, pos, rand);

		TileEntity tileEntity = world.getTileEntity(pos);
		if (tileEntity instanceof TileDuctBase) {
			((TileDuctBase) tileEntity).randomDisplayTick();
		}
	}

	public float getSize(IBlockState state) {

		return TDDucts.getDuct(offset + getMetaFromState(state)).isLargeTube() ? 0.05F : 0.3F;
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		GameRegistry.register(this.setRegistryName("ThermalDynamics_" + offset));
		GameRegistry.register(new ItemBlockDuct(this).setRegistryName("ThermalDynamics_" + offset));
		ThermalDynamics.proxy.addIModelRegister(this);

		for (int i = 0; i < 16; i++) {
			if (TDDucts.isValid(offset + i)) {
				TDDucts.getType(offset + i).itemStack = new ItemStack(this, 1, i);
			}
		}

		return true;
	}

	@Override
	public boolean initialize() {

		MinecraftForge.EVENT_BUS.register(this);

		if (offset != 0) {
			return true;
		}
		GameRegistry.registerTileEntity(TileEnergyDuct.class, "thermaldynamics.FluxDuct");
		GameRegistry.registerTileEntity(TileEnergyDuctSuper.class, "thermaldynamics.FluxDuctSuperConductor");

		EnergyGrid.initialize();
		SubTileEnergyRedstone.initialize();

		PacketHandler.instance.registerPacket(PacketFluid.class);
		GameRegistry.registerTileEntity(TileFluidDuct.class, "thermaldynamics.FluidDuct");
		GameRegistry.registerTileEntity(TileFluidDuctFragile.class, "thermaldynamics.FluidDuctFragile");
		GameRegistry.registerTileEntity(TileFluidDuctFlux.class, "thermaldynamics.FluidDuctFlux");
		GameRegistry.registerTileEntity(TileFluidDuctSuper.class, "thermaldynamics.FluidDuctSuper");

		GameRegistry.registerTileEntity(TileItemDuct.class, "thermaldynamics.ItemDuct");
		GameRegistry.registerTileEntity(TileItemDuctEnder.class, "thermaldynamics.ItemDuctEnder");
		GameRegistry.registerTileEntity(TileItemDuctFlux.class, "thermaldynamics.ItemDuctFlux");

		GameRegistry.registerTileEntity(TileStructuralDuct.class, "thermaldynamics.StructuralDuct");

		GameRegistry.registerTileEntity(TileTransportDuct.class, "thermaldynamics.TransportDuct");
		GameRegistry.registerTileEntity(TileTransportDuctLongRange.class, "thermaldynamics.TransportDuctLongRange");
		GameRegistry.registerTileEntity(TileTransportDuctCrossover.class, "thermaldynamics.TransportDuctCrossover");
		EntityRegistry.registerModEntity(EntityTransport.class, "Transport", 0, ThermalDynamics.instance, CoreProps.ENTITY_TRACKING_DISTANCE, 1, true);
		MinecraftForge.EVENT_BUS.register(TransportHandler.INSTANCE);
		FMLCommonHandler.instance().bus().register(TransportHandler.INSTANCE);

		return true;
	}

	@Override
	public boolean postInit() {

		return true;
	}

	/* IModelRegister */
	@Override
	@SideOnly (Side.CLIENT)
	public void registerModels() {

		ModelLoader.setCustomStateMapper(this, new StateMap.Builder().ignore(META).build());
		ModelResourceLocation normalLocation = new ModelResourceLocation(getRegistryName(), "normal");
		ModelRegistryHelper.register(normalLocation, new DummyBakedModel());
		ModelRegistryHelper.registerItemRenderer(Item.getItemFromBlock(this), RenderDuct.instance);
	}

	@Override
	public int getWeakPower(IBlockState blockState, IBlockAccess world, BlockPos pos, EnumFacing side) {

		TileDuctBase theTile = (TileDuctBase) world.getTileEntity(pos);
		if (theTile != null && theTile.attachments[side.ordinal() ^ 1] != null) {
			return theTile.attachments[side.ordinal() ^ 1].getRSOutput();
		}
		return 0;
	}

	@Override
	public int getStrongPower(IBlockState blockState, IBlockAccess blockAccess, BlockPos pos, EnumFacing side) {

		return 0;
	}

	@Override
	public boolean canConnectRedstone(IBlockState state, IBlockAccess world, BlockPos pos, EnumFacing side) {

		if (side == null) {
			return false;
		}

		int s;
		if (side == EnumFacing.DOWN) {
			s = 2;
		} else if (side == EnumFacing.UP) {
			s = 5;
		} else if (side == EnumFacing.NORTH) {
			s = 3;
		} else {
			s = 4;
		}

		TileDuctBase theTile = (TileDuctBase) world.getTileEntity(pos);
		return theTile != null && theTile.attachments[s] != null && theTile.attachments[s].shouldRSConnect();
	}
}
