package cofh.thermaldynamics.duct.entity;

import cofh.core.render.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.Phase;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class TransportHandler {

	public static final TransportHandler INSTANCE = new TransportHandler();

	@SuppressWarnings ("unchecked")
	@SubscribeEvent (priority = EventPriority.HIGHEST)
	@SideOnly (Side.CLIENT)
	public void renderTravellers(RenderLivingEvent.Pre event) {

		EntityLivingBase entity = event.getEntity();
		Entity ridingEntity = entity.getRidingEntity();
		if (ridingEntity != null && ridingEntity.getClass() == EntityTransport.class) {
			event.setCanceled(true);

			if (entity instanceof EntityPlayer) {
				return;
			}
			float f = ShaderHelper.midGameTick;
			EntityTransport transport = (EntityTransport) ridingEntity;
			transport.setPosition(f);
			ridingEntity.updatePassenger(entity);
			float rotation = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * f;

			GlStateManager.pushMatrix();
			float max = Math.max(Math.max(Math.max(entity.height, entity.width), transport.originalWidth), transport.originalHeight);

			GlStateManager.translate(event.getX(), event.getY(), event.getZ());

			if (max > 0.4) {
				double h = 0.4 / max;
				GlStateManager.translate(0, -h / 2, 0);
				GlStateManager.scale(h, h, h);
			} else {
				GlStateManager.translate(0, -1 / 2, 0);
			}
			try {
				entity.dismountRidingEntity();
				event.getRenderer().doRender(entity, 0, 0, 0, rotation, f);
			} finally {
				entity.startRiding(transport);
			}
			GlStateManager.popMatrix();
		}
	}

	@SideOnly (Side.CLIENT)
	Camera camera;

	@SubscribeEvent
	@SideOnly (Side.CLIENT)
	public void controlPlayer(TickEvent.ClientTickEvent event) {

		if (event.phase == Phase.END) {
			return;
		}
		Minecraft mc = Minecraft.getMinecraft();

		EntityPlayerSP thePlayer = mc.thePlayer;
		if (thePlayer == null) {
			return;
		}
		Entity ridingEntity = thePlayer.getRidingEntity();
		if (ridingEntity != null && ridingEntity.getClass() == EntityTransport.class) {
			EntityTransport transport = (EntityTransport) ridingEntity;
			transport.updateRider(thePlayer);

			if (mc.gameSettings.thirdPersonView != 0) {
				return;
			}
			double rotationYaw = 0, rotationPitch = 0;

			byte d = transport.direction;
			switch (d) {
				case 0:
					rotationPitch = 90;
					break;
				case 1:
					rotationPitch = -90;
					break;
				case 2:
					rotationYaw = 180;
					break;
				case 3:
					rotationYaw = 0;
					break;
				case 4:
					rotationYaw = 90;
					break;
				case 5:
					rotationYaw = 270;
					break;

				default:
					return;
			}
			thePlayer.rotationPitch += Math.sin((rotationPitch - thePlayer.rotationPitch) / 180 * Math.PI) * 30;
			if (rotationPitch == 0) {
				thePlayer.rotationYaw += Math.sin((rotationYaw - thePlayer.rotationYaw) / 180 * Math.PI) * 30;
			}
		}
	}

	@SubscribeEvent
	@SideOnly (Side.CLIENT)
	public void controlCamera(TickEvent.RenderTickEvent event) {

		Minecraft mc = Minecraft.getMinecraft();
		EntityPlayerSP thePlayer = mc.thePlayer;

		if (thePlayer == null) {
			return;
		}
		Entity ridingEntity = thePlayer.getRidingEntity();
		if (ridingEntity == null) {
			if (mc.getRenderViewEntity() != null && (mc.getRenderViewEntity() == camera)) {
				mc.setRenderViewEntity(thePlayer);
				camera.worldObj = null;
			}
		} else if (ridingEntity.getClass() == EntityTransport.class) {
			EntityTransport transport = (EntityTransport) ridingEntity;

			if (camera == null) {
				camera = new Camera();
			}
			camera.copyFromEntityTransport(transport, thePlayer);
			mc.setRenderViewEntity(camera);
		}
	}

}
