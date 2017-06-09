package cofh.thermaldynamics.block;

import cofh.core.block.BlockCoreTile;
import cofh.core.block.TileCore;
import cofh.core.util.CoreUtils;
import cofh.lib.util.RayTracer;
import cofh.lib.util.helpers.ServerHelper;
import cofh.lib.util.helpers.WrenchHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.Explosion;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.fml.common.eventhandler.Event.Result;

import javax.annotation.Nullable;
import java.util.ArrayList;

public abstract class BlockTDBase extends BlockCoreTile {

	protected BlockTDBase(Material material) {

		super(material);
		setSoundType(SoundType.STONE);
		setCreativeTab(ThermalDynamics.tabCommon);
	}

	/* BLOCK METHODS */
	//	@Override
	//	public void onBlockPlacedBy(World world, BlockPos pos, IBlockState state, EntityLivingBase living, ItemStack stack) {
	//
	//	}

	@Override
	public boolean addLandingEffects(IBlockState state, WorldServer worldObj, BlockPos blockPosition, IBlockState iblockstate, EntityLivingBase entity, int numberOfParticles) {

		return true;
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
		}
		TileGrid tile = (TileGrid) world.getTileEntity(pos);

		if (tile == null || tile.isInvalid()) {
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

	@Override
	public float getBlockHardness(IBlockState state, World world, BlockPos pos) {

		// TileGrid tile = (TileGrid) world.getTileEntity(pos);
		//
		// TODO
		//		if (tile instanceof TileDuct) {
		//
		//		}
		return blockHardness;
	}

	@Override
	public float getExplosionResistance(World world, BlockPos pos, Entity exploder, Explosion explosion) {

		// TileGrid tile = (TileGrid) world.getTileEntity(pos);
		//
		// TODO
		//		if (tile instanceof TileDuct) {
		//
		//		}
		return blockResistance / 5.0F;
	}

	/* RENDERING METHODS */
	// TODO
	//	@Override
	//	@SideOnly (Side.CLIENT)
	//	public boolean addDestroyEffects(World world, BlockPos pos, ParticleManager manager) {
	//
	//		if (this instanceof IWorldBlockTextureProvider) {
	//			CustomParticleHandler.addDestroyEffects(world, pos, manager, (IWorldBlockTextureProvider) this);
	//			return true;
	//		}
	//		return false;
	//	}
	//
	//	@Override
	//	@SideOnly (Side.CLIENT) // Because vanilla removed state and side based particle textures in 1.8..
	//	public boolean addHitEffects(IBlockState state, World world, RayTraceResult trace, ParticleManager manager) {
	//
	//		if (this instanceof IWorldBlockTextureProvider) {
	//			CustomParticleHandler.addHitEffects(state, world, trace, manager, ((IWorldBlockTextureProvider) this));
	//			return true;
	//		}
	//		return false;
	//	}

	/* HELPERS */
	// TODO
	//	@Override
	//	public NBTTagCompound getItemStackTag(IBlockAccess world, BlockPos pos) {
	//
	//	}

	@Override
	public ArrayList<ItemStack> dropDelegate(NBTTagCompound nbt, IBlockAccess world, BlockPos pos, int fortune) {

		return dismantleDelegate(nbt, (World) world, pos, null, false, true);
	}

	@Override
	public ArrayList<ItemStack> dismantleDelegate(NBTTagCompound nbt, World world, BlockPos pos, EntityPlayer player, boolean returnDrops, boolean simulate) {

		TileEntity tile = world.getTileEntity(pos);
		IBlockState state = world.getBlockState(pos);
		int meta = getMetaFromState(state);

		ItemStack dropBlock = tile instanceof TileGrid ? ((TileGrid) tile).getDrop() : new ItemStack(this, 1, meta);

		if (nbt != null) {
			dropBlock.setTagCompound(nbt);
		}
		ArrayList<ItemStack> ret = new ArrayList<>();
		ret.add(dropBlock);

		if (tile instanceof TileGrid) {
			TileGrid ductBase = (TileGrid) tile;
			TileGrid.AttachmentData attachmentData = ductBase.attachmentData;
			if (attachmentData != null) {
				for (Attachment attachment : attachmentData.attachments) {
					if (attachment != null) {
						ret.addAll(attachment.getDrops());
					}
				}
				for (Cover cover : attachmentData.covers) {
					if (cover != null) {
						ret.addAll(cover.getDrops());
					}
				}
			}
			ductBase.dropAdditional(ret);
		}
		if (nbt != null) {
			dropBlock.setTagCompound(nbt);
		}
		if (!simulate) {
			if (tile instanceof TileCore) {
				((TileCore) tile).blockDismantled();
			}
			world.setBlockToAir(pos);

			if (!returnDrops) {
				for (ItemStack stack : ret) {
					float f = 0.3F;
					double x2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
					double y2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
					double z2 = world.rand.nextFloat() * f + (1.0F - f) * 0.5D;
					EntityItem dropEntity = new EntityItem(world, pos.getX() + x2, pos.getY() + y2, pos.getZ() + z2, stack);
					dropEntity.setPickupDelay(10);
					world.spawnEntityInWorld(dropEntity);
				}
				if (player != null) {
					CoreUtils.dismantleLog(player.getName(), this, meta, pos);
				}
			}
		}
		return ret;
	}

}
