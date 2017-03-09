package cofh.thermaldynamics.block;

import cofh.api.core.IModelRegister;
import cofh.thermaldynamics.duct.TileDuctBase;
import cofh.thermaldynamics.duct.TileDuctBase.NeighborTypes;
import cofh.thermaldynamics.duct.entity.EntityTransport;
import net.minecraft.block.material.Material;
import net.minecraft.block.properties.PropertyEnum;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.IStringSerializable;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.List;

public class BlockDuct extends BlockTDBase implements IModelRegister {

	public static final PropertyEnum<BlockDuct.Type> VARIANT = PropertyEnum.create("type", Type.class);

	public BlockDuct() {

		super(Material.GLASS);

		setUnlocalizedName("duct");

		setHardness(1.0F);
		setResistance(10.0F);
	}

	@Override
	protected BlockStateContainer createBlockState() {

		BlockStateContainer.Builder builder = new BlockStateContainer.Builder(this);
		// Listed
		builder.add(VARIANT);

		return builder.build();
	}

	/* TYPE METHODS */
	@Override
	public IBlockState getStateFromMeta(int meta) {

		return this.getDefaultState().withProperty(VARIANT, Type.byMetadata(meta));
	}

	@Override
	public int getMetaFromState(IBlockState state) {

		return state.getValue(VARIANT).getMetadata();
	}

	@Override
	public int damageDropped(IBlockState state) {

		return state.getValue(VARIANT).getMetadata();
	}

	/* ITileEntityProvider */
	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {

		return null;
	}

	/* BLOCK METHODS */
	@Override
	public void addCollisionBoxToList(IBlockState state, World world, BlockPos pos, AxisAlignedBB entityBox, List<AxisAlignedBB> collidingBoxes, @Nullable Entity entity) {

		if (entity instanceof EntityTransport) {
			return;
		}
		// TODO
		float min = 0.3F;
		float max = 1 - min;

		AxisAlignedBB bb = new AxisAlignedBB(min, min, min, max, max, max);
		addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
		TileDuctBase tile = (TileDuctBase) world.getTileEntity(pos);

		if (tile != null) {
			for (int i = 0; i < 6; i++) {
				if (tile.attachments[i] != null) {
					tile.attachments[i].addCollisionBoxesToList(entityBox, collidingBoxes, entity);
				}
				if (tile.covers[i] != null) {
					tile.covers[i].addCollisionBoxesToList(entityBox, collidingBoxes, entity);
				}
			}
			if (tile.neighborTypes[0] != NeighborTypes.NONE) {
				bb = new AxisAlignedBB(min, 0.0F, min, max, max, max);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
			}
			if (tile.neighborTypes[1] != NeighborTypes.NONE) {
				bb = new AxisAlignedBB(min, min, min, max, 1.0F, max);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
			}
			if (tile.neighborTypes[2] != NeighborTypes.NONE) {
				bb = new AxisAlignedBB(min, min, 0.0F, max, max, max);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
			}
			if (tile.neighborTypes[3] != NeighborTypes.NONE) {
				bb = new AxisAlignedBB(min, min, min, max, max, 1.0F);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
			}
			if (tile.neighborTypes[4] != NeighborTypes.NONE) {
				bb = new AxisAlignedBB(0.0F, min, min, max, max, max);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
			}
			if (tile.neighborTypes[5] != NeighborTypes.NONE) {
				bb = new AxisAlignedBB(min, min, min, 1.0F, max, max);
				addCollisionBoxToList(pos, entityBox, collidingBoxes, bb);
			}
		}
	}

	@Override
	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase living, ItemStack stack) {

	}

	@Override
	public boolean isFullCube(IBlockState state) {

		return false;
	}

	@Override
	public boolean isNormalCube(IBlockState state, IBlockAccess world, BlockPos pos) {

		return false;
	}

	@Override
	public boolean isOpaqueCube(IBlockState state) {

		return false;
	}

	@Override
	public boolean isSideSolid(IBlockState base_state, IBlockAccess world, BlockPos pos, EnumFacing side) {

		return true;
	}

	/* RENDERING METHODS */
	@Override
	@SideOnly (Side.CLIENT)
	public boolean canRenderInLayer(IBlockState state, BlockRenderLayer layer) {

		return layer == BlockRenderLayer.SOLID || layer == BlockRenderLayer.CUTOUT;
	}

	/* IModelRegister */
	@Override
	@SideOnly (Side.CLIENT)
	public void registerModels() {

		//		ModelLoader.setCustomStateMapper(this, new StateMap.Builder().ignore(META).build());
		//		ModelResourceLocation normalLocation = new ModelResourceLocation(getRegistryName(), "normal");
		//		ModelRegistryHelper.register(normalLocation, new DummyBakedModel());
		//		ModelRegistryHelper.registerItemRenderer(Item.getItemFromBlock(this), RenderDuct.instance);
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		return true;
	}

	@Override
	public boolean initialize() {

		return true;
	}

	@Override
	public boolean postInit() {

		return true;
	}

	/* REFERENCES */

	/* TYPE */
	public enum Type implements IStringSerializable {

		// @formatter:off
		ENERGY(0, "energy"),
		FLUID(1, "fluid"),
		ITEM(2, "item"),
		TRANSPORT(3, "transport");
		// @formatter:on

		private static final BlockDuct.Type[] METADATA_LOOKUP = new BlockDuct.Type[values().length];
		private final int metadata;
		private final String name;

		Type(int metadata, String name) {

			this.metadata = metadata;
			this.name = name;
		}

		public int getMetadata() {

			return this.metadata;
		}

		@Override
		public String getName() {

			return this.name;
		}

		public static Type byMetadata(int metadata) {

			if (metadata < 0 || metadata >= METADATA_LOOKUP.length) {
				metadata = 0;
			}
			return METADATA_LOOKUP[metadata];
		}

		static {
			for (Type type : values()) {
				METADATA_LOOKUP[type.getMetadata()] = type;
			}
		}
	}

}
