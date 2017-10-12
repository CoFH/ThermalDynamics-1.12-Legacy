package cofh.thermaldynamics.proxy;

import codechicken.lib.vec.Cuboid6;
import cofh.core.render.hitbox.CustomHitBox;
import cofh.core.render.hitbox.ICustomHitBox;
import cofh.core.render.hitbox.RenderHitbox;
import cofh.core.util.RayTracer;
import cofh.core.util.helpers.ItemHelper;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.init.TDTextures;
import cofh.thermaldynamics.item.ItemAttachment;
import cofh.thermaldynamics.render.RenderDuct;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraftforge.client.event.DrawBlockHighlightEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class EventHandlerClient {

	public static final EventHandlerClient INSTANCE = new EventHandlerClient();

	@SubscribeEvent
	public void handleTextureStitchEventPre(TextureStitchEvent.Pre event) {

		TDTextures.registerTextures(event.getMap());

		for (int i = 0; i < TDDucts.ductList.size(); i++) {
			if (TDDucts.isValid(i)) {
				TDDucts.ductList.get(i).registerIcons(event.getMap());
			}
		}
		TDDucts.structureInvis.registerIcons(event.getMap());
	}

	@SubscribeEvent
	public void handleTextureStitchEventPost(TextureStitchEvent.Post event) {

		RenderDuct.initialize();
	}

	@SubscribeEvent (priority = EventPriority.HIGHEST)
	public void onBlockHighlight(DrawBlockHighlightEvent event) {

		RayTraceResult target = event.getTarget();
		EntityPlayer player = event.getPlayer();
		float partialTicks = event.getPartialTicks();

		if (doAttachmentHighlight(target, player, partialTicks)) {
			event.setCanceled(true);
		} else if (doDuctHighlight(target, player, partialTicks)) {
			event.setCanceled(true);
		}
	}

	/* HELPERS */
	private boolean doAttachmentHighlight(RayTraceResult target, EntityPlayer player, float partialTicks) {

		if (!(ItemHelper.getHeldStack(player).getItem() instanceof ItemAttachment) || target.typeOfHit != Type.BLOCK) {
			return false;
		}
		RayTracer.retraceBlock(player.world, player, target.getBlockPos());
		ItemStack stack = ItemHelper.getHeldStack(player);
		Attachment attachment = ItemAttachment.getAttachment(stack, player, player.world, target.getBlockPos(), target.sideHit);

		if (attachment == null || !attachment.canAddToTile(attachment.baseTile)) {
			return false;
		}
		Cuboid6 c = attachment.getCuboid();
		c.max.subtract(c.min);

		RenderHitbox.drawSelectionBox(player, target, partialTicks, new CustomHitBox(c.max.y, c.max.z, c.max.x, attachment.baseTile.x() + c.min.x, attachment.baseTile.y() + c.min.y, attachment.baseTile.z() + c.min.z));
		attachment.drawSelectionExtra(player, target, partialTicks);
		return true;
	}

	private boolean doDuctHighlight(RayTraceResult target, EntityPlayer player, float partialTicks) {

		if (target.typeOfHit != Type.BLOCK) {
			return false;
		}
		RayTracer.retraceBlock(player.world, player, target.getBlockPos());
		TileEntity tile = player.world.getTileEntity(target.getBlockPos());

		if (tile instanceof ICustomHitBox) {
			ICustomHitBox hitbox = (ICustomHitBox) tile;

			if (hitbox.shouldRenderCustomHitBox(target.subHit, player)) {
				RenderHitbox.drawSelectionBox(player, target, partialTicks, hitbox.getCustomHitBox(target.subHit, player));
				return true;
			}
		}
		return false;
	}

}
