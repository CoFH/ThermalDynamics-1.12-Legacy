package cofh.thermaldynamics.item;

import cofh.core.render.IModelRegister;
import cofh.core.util.RayTracer;
import cofh.core.util.core.IInitializer;
import cofh.core.util.helpers.BlockHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ItemAttachment extends Item implements IInitializer, IModelRegister {

	public ItemAttachment() {

		super();
		setHasSubtypes(true);
		this.setCreativeTab(ThermalDynamics.tabCommon);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		ItemStack stack = player.getHeldItem(hand);
		Attachment attachment = getAttachment(stack, player, world, pos, facing);

		if (attachment != null && attachment.addToTile()) {
			if (!player.capabilities.isCreativeMode) {
				stack.shrink(1);
			}
			return EnumActionResult.SUCCESS;
		}
		return EnumActionResult.PASS;
	}

	public static Attachment getAttachment(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side) {

		Attachment attachment = null;

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileGrid) {
			int s;
			RayTraceResult movingObjectPosition = RayTracer.retraceBlock(world, player, pos);
			if (movingObjectPosition != null) {
				int subHit = movingObjectPosition.subHit;
				if (subHit < 6) {
					s = subHit;
				} else if (subHit < 12) {
					s = (subHit - 6);
				} else if (subHit == 13) {
					s = side.ordinal();
				} else {
					s = ((subHit - 14) % 6);
				}
				if (s != -1) {
					attachment = ((ItemAttachment) stack.getItem()).getAttachment(EnumFacing.VALUES[s ^ 1], stack, (TileGrid) tile);
				}
			}
		} else {
			tile = BlockHelper.getAdjacentTileEntity(world, pos, side);
			if (tile instanceof TileGrid) {
				attachment = ((ItemAttachment) stack.getItem()).getAttachment(side, stack, (TileGrid) tile);
			}
		}
		return attachment;
	}

	public abstract Attachment getAttachment(EnumFacing side, ItemStack stack, TileGrid tile);

	@Override
	public boolean register() {

		return true;
	}

	@Override
	@SideOnly (Side.CLIENT)
	public void registerModels() {

	}

}
