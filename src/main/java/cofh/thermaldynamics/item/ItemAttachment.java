package cofh.thermaldynamics.item;

import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.util.ItemUtils;
import codechicken.lib.vec.Cuboid6;
import cofh.api.core.IInitializer;
import cofh.api.core.IModelRegister;
import cofh.core.render.hitbox.CustomHitBox;
import cofh.core.render.hitbox.RenderHitbox;
import cofh.lib.util.helpers.BlockHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.TileDuctBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.World;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ItemAttachment extends Item implements IInitializer, IModelRegister {

	public ItemAttachment() {

		super();
		setHasSubtypes(true);
		this.setCreativeTab(ThermalDynamics.tabCommon);
	}

	@Override
	public EnumActionResult onItemUse(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {

		Attachment attachment = getAttachment(stack, player, world, pos, facing);

		if (attachment != null && attachment.addToTile()) {
			if (!player.capabilities.isCreativeMode) {
				stack.stackSize--;
			}
			return EnumActionResult.SUCCESS;
		}

		return super.onItemUse(stack, player, world, pos, hand, facing, hitX, hitY, hitZ);
	}

	public Attachment getAttachment(ItemStack stack, EntityPlayer player, World world, BlockPos pos, EnumFacing side) {

		Attachment attachment = null;

		TileEntity tile = world.getTileEntity(pos);
		if (tile instanceof TileDuctBase) {
			int s = -1;
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
					attachment = getAttachment(EnumFacing.VALUES[s ^ 1], stack, (TileDuctBase) tile);
				}
			}
		} else {
			tile = BlockHelper.getAdjacentTileEntity(world, pos, side);
			if (tile instanceof TileDuctBase) {
				attachment = getAttachment(side, stack, (TileDuctBase) tile);
			}
		}
		return attachment;
	}

	public abstract Attachment getAttachment(EnumFacing side, ItemStack stack, TileDuctBase tile);

	@Override
	public boolean initialize() {

		MinecraftForge.EVENT_BUS.register(this);
		return true;
	}

	@Override
	public boolean postInit() {

		return true;
	}

	@Override
	@SideOnly (Side.CLIENT)
	public void registerModels() {

	}

	@SideOnly (Side.CLIENT)
	@SubscribeEvent (priority = EventPriority.HIGHEST)
	public void onBlockHighlight(DrawBlockHighlightEvent event) {

		RayTraceResult target = event.getTarget();
		if (target.typeOfHit != Type.BLOCK || !ItemUtils.isPlayerHoldingSomething(event.getPlayer()) || ItemUtils.getHeldStack(event.getPlayer()).getItem() != this) {
			return;
		}
		RayTracer.retraceBlock(event.getPlayer().worldObj, event.getPlayer(), target.getBlockPos());
		ItemStack stack = ItemUtils.getHeldStack(event.getPlayer());
		Attachment attachment = getAttachment(stack, event.getPlayer(), event.getPlayer().getEntityWorld(), target.getBlockPos(), target.sideHit);

		if (attachment == null || !attachment.canAddToTile(attachment.tile)) {
			return;
		}
		Cuboid6 c = attachment.getCuboid();
		c.max.subtract(c.min);

		RenderHitbox.drawSelectionBox(event.getPlayer(), target, event.getPartialTicks(), new CustomHitBox(c.max.y, c.max.z, c.max.x, attachment.tile.x() + c.min.x, attachment.tile.y() + c.min.y, attachment.tile.z() + c.min.z));

		attachment.drawSelectionExtra(event.getPlayer(), target, event.getPartialTicks());

		event.setCanceled(true);
	}

}
