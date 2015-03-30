package cofh.thermaldynamics.duct;

import cofh.api.block.IBlockAppearance;
import cofh.api.core.IInitializer;
import cofh.core.block.TileCoFHBase;
import cofh.core.render.IconRegistry;
import cofh.core.render.hitbox.ICustomHitBox;
import cofh.core.render.hitbox.RenderHitbox;
import cofh.core.util.CoreUtils;
import cofh.repack.codechicken.lib.raytracer.RayTracer;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.Attachment;
import cofh.thermaldynamics.block.BlockTDBase;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.core.TDProps;
import cofh.thermaldynamics.duct.attachments.facades.Cover;
import cofh.thermaldynamics.duct.energy.EnergyGrid;
import cofh.thermaldynamics.duct.energy.TileEnergyDuct;
import cofh.thermaldynamics.duct.energy.TileEnergyDuctSuperConductor;
import cofh.thermaldynamics.duct.energy.subgrid.SubTileEnergyRedstone;
import cofh.thermaldynamics.duct.fluid.TileFluidDuct;
import cofh.thermaldynamics.duct.fluid.TileFluidDuctFlux;
import cofh.thermaldynamics.duct.fluid.TileFluidDuctFragile;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TileItemDuctEnder;
import cofh.thermaldynamics.duct.item.TileItemDuctFlux;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;

public class BlockDuct extends BlockTDBase implements IInitializer, IBlockAppearance {

	public int offset;

	public BlockDuct(int offset) {

		super(Material.glass);
		setHardness(1.0F);
		setResistance(10.0F);
		setStepSound(soundTypeMetal);
		setBlockName("thermaldynamics.duct");
		setCreativeTab(ThermalDynamics.tab);
		this.offset = offset * 16;
	}

	@Override
	public void getSubBlocks(Item item, CreativeTabs tab, List list) {

		for (int i = 0; i < 16; i++) {
			if (TDDucts.isValid(i + offset)) {
				list.add(TDDucts.getDuct(i + offset).itemStack.copy());
			}
		}
	}

	@Override
	public boolean hasTileEntity(int metadata) {

		return TDDucts.isValid(metadata + offset);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {

		Duct duct = TDDucts.getType(metadata + offset);
		return duct.factory.createTileEntity(duct, world);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int side, int metadata) {

		return TDDucts.getType(metadata + offset).iconBaseTexture;
	}

	@Override
	public int damageDropped(int i) {

		return i;
	}

	@Override
	public boolean canCreatureSpawn(EnumCreatureType type, IBlockAccess world, int x, int y, int z) {

		return false;
	}

	@SideOnly(Side.CLIENT)
	@SubscribeEvent
	public void onBlockHighlight(DrawBlockHighlightEvent event) {

		if (event.target.typeOfHit == MovingObjectType.BLOCK
				&& event.player.worldObj.getBlock(event.target.blockX, event.target.blockY, event.target.blockZ).getUnlocalizedName()
						.equals(getUnlocalizedName())) {
			RayTracer.retraceBlock(event.player.worldObj, event.player, event.target.blockX, event.target.blockY, event.target.blockZ);

			ICustomHitBox theTile = ((ICustomHitBox) event.player.worldObj.getTileEntity(event.target.blockX, event.target.blockY, event.target.blockZ));
			if (theTile.shouldRenderCustomHitBox(event.target.subHit, event.player)) {
				event.setCanceled(true);
				RenderHitbox.drawSelectionBox(event.player, event.target, event.partialTicks, theTile.getCustomHitBox(event.target.subHit, event.player));
			}
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister ir) {

		if (offset != 0) {
			return;
		}
		for (int i = 0; i < 5; i++) {
			for (int j = 0; j < 2; j++) {
				IconRegistry.addIcon("ServoBase" + (i * 2 + j), "thermaldynamics:duct/attachment/servo/ServoBase" + i + "" + j, ir);
				IconRegistry.addIcon("RetrieverBase" + (i * 2 + j), "thermaldynamics:duct/attachment/retriever/RetrieverBase" + i + "" + j, ir);
			}
		}

		IconRegistry.addIcon("CoverBase", "thermaldynamics:duct/attachment/cover/support", ir);

		for (int i = 0; i < 5; i++) {
			IconRegistry.addIcon("FilterBase" + i, "thermaldynamics:duct/attachment/filter/Filter" + i + "0", ir);
		}
		IconRegistry.addIcon("SideDucts", "thermaldynamics:duct/sideDucts", ir);

		for (int i = 0; i < TDDucts.ductList.size(); i++) {
			if (TDDucts.isValid(i)) {
				TDDucts.ductList.get(i).registerIcons(ir);
			}
		}
		TDDucts.structureInvis.registerIcons(ir);
	}

	@Override
	public ItemStack getPickBlock(MovingObjectPosition target, World world, int x, int y, int z) {

		if (target.subHit >= 14 && target.subHit < 20) {
			TileTDBase tileEntity = (TileTDBase) world.getTileEntity(x, y, z);
			ItemStack pickBlock = tileEntity.attachments[target.subHit - 14].getPickBlock();
			if (pickBlock != null) {
				return pickBlock;
			}
		}
		if (target.subHit >= 20 && target.subHit < 26) {
			TileTDBase tileEntity = (TileTDBase) world.getTileEntity(x, y, z);
			ItemStack pickBlock = tileEntity.covers[target.subHit - 20].getPickBlock();
			if (pickBlock != null) {
				return pickBlock;
			}
		}
		return super.getPickBlock(target, world, x, y, z);
	}

	@Override
	public int getRenderType() {

		return TDProps.renderDuctId;
	}

	@Override
	public boolean canRenderInPass(int pass) {

		renderPass = pass;
		return pass < 2;
	}

	@Override
	public int getRenderBlockPass() {

		return 1;
	}

	@Override
	public Block getVisualBlock(IBlockAccess world, int x, int y, int z, ForgeDirection side) {

		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileTDBase) {
			Cover cover = ((TileTDBase) tileEntity).covers[side.ordinal()];
			if (cover != null) {
				return cover.block;
			}

		}
		return this;
	}

	@Override
	public int getVisualMeta(IBlockAccess world, int x, int y, int z, ForgeDirection side) {

		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileTDBase) {
			Cover cover = ((TileTDBase) tileEntity).covers[side.ordinal()];
			if (cover != null) {
				return cover.meta;
			}
		}
		return world.getBlockMetadata(x, y, z);
	}

	@Override
	public boolean supportsVisualConnections() {

		return true;
	}

	public static enum ConnectionTypes {
		NONE(false), DUCT, TILECONNECTION, STRUCTURE, CLEANDUCT;

		private final boolean renderDuct;

		private ConnectionTypes() {

			renderDuct = true;
		}

		private ConnectionTypes(boolean renderDuct) {

			this.renderDuct = renderDuct;
		}

		public boolean renderDuct() {

			return renderDuct;
		}
	}

	@Override
	public void onBlockPlacedBy(World world, int x, int y, int z, EntityLivingBase living, ItemStack stack) {

		super.onBlockPlacedBy(world, x, y, z, living, stack);
		TileEntity tile = world.getTileEntity(x, y, z);
		if (tile instanceof TileTDBase) {
			((TileTDBase) tile).onPlacedBy(living, stack);
		}
	}

	@Override
	public ArrayList<ItemStack> dismantleBlock(EntityPlayer player, NBTTagCompound nbt, World world, int x, int y, int z, boolean returnDrops, boolean simulate) {

		TileEntity tile = world.getTileEntity(x, y, z);
		int bMeta = world.getBlockMetadata(x, y, z);

		ItemStack dropBlock;
		if (tile instanceof TileTDBase) {
			dropBlock = ((TileTDBase) tile).getDrop();
		} else {
			dropBlock = new ItemStack(this, 1, bMeta);
		}

		ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
		ret.add(dropBlock);

		if (tile instanceof TileTDBase) {
			TileTDBase multiBlock = (TileTDBase) tile;
			for (Attachment a : multiBlock.attachments) {
				if (a != null) {
					ret.addAll(a.getDrops());
				}
			}
			for (Cover cover : multiBlock.covers) {
				if (cover != null) {
					ret.addAll(cover.getDrops());
				}
			}
			multiBlock.dropAdditonal(ret);
		}

		if (nbt != null) {
			dropBlock.setTagCompound(nbt);
		}
		if (!simulate) {
			if (tile instanceof TileCoFHBase) {
				((TileCoFHBase) tile).blockDismantled();
			}
			world.setBlockToAir(x, y, z);

			if (!returnDrops) {
				float f = 0.3F;
				for (ItemStack stack : ret) {
					double x2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
					double y2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
					double z2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
					EntityItem item = new EntityItem(world, x + x2, y + y2, z + z2, stack);
					item.delayBeforeCanPickup = 10;
					world.spawnEntityInWorld(item);
				}

				if (player != null) {
					CoreUtils.dismantleLog(player.getCommandSenderName(), this, bMeta, x, y, z);
				}
			}
		}

		return ret;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random rand) {

		super.randomDisplayTick(world, x, y, z, rand);

		TileEntity tileEntity = world.getTileEntity(x, y, z);
		if (tileEntity instanceof TileTDBase) {
			((TileTDBase) tileEntity).randomDisplayTick();
		}
	}

	@Override
	public float getSize(World world, int x, int y, int z) {

		return TDDucts.getDuct(offset + world.getBlockMetadata(x, y, z)).isLargeTube() ? 0.05F : super.getSize(world, x, y, z);
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		GameRegistry.registerBlock(this, ItemBlockDuct.class, "ThermalDynamics_" + offset);

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
		GameRegistry.registerTileEntity(TileEnergyDuctSuperConductor.class, "thermaldynamics.FluxDuctSuperConductor");

		EnergyGrid.initialize();
		SubTileEnergyRedstone.initialize();

		GameRegistry.registerTileEntity(TileFluidDuct.class, "thermaldynamics.FluidDuct");
		GameRegistry.registerTileEntity(TileFluidDuctFragile.class, "thermaldynamics.FluidDuctFragile");
		GameRegistry.registerTileEntity(TileFluidDuctFlux.class, "thermaldynamics.FluidDuctFlux");

		GameRegistry.registerTileEntity(TileItemDuct.class, "thermaldynamics.ItemDuct");
		GameRegistry.registerTileEntity(TileItemDuctEnder.class, "thermaldynamics.ItemDuctEnder");
		GameRegistry.registerTileEntity(TileItemDuctFlux.class, "thermaldynamics.ItemDuctFlux");

		GameRegistry.registerTileEntity(TileStructuralDuct.class, "thermaldynamics.StructuralDuct");
		return true;
	}

	@Override
	public boolean postInit() {

		return true;
	}

}
