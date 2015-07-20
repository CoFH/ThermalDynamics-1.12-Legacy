package cofh.thermaldynamics.duct.entity;

import cofh.core.render.ShaderHelper;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderLivingEvent;
import org.lwjgl.opengl.GL11;

public class TransportHandler {

	public static final TransportHandler INSTANCE = new TransportHandler();

//	@SubscribeEvent
//	public void cancelDamgage(LivingAttackEvent event) {
//
//		EntityLivingBase entity = event.entityLiving;
//		Entity ridingEntity = entity.ridingEntity;
//		if (ridingEntity != null && ridingEntity.getClass() == EntityTransport.class) {
//			EntityTransport ridingEntity1 = (EntityTransport) ridingEntity;
//			BlockPosition p = ridingEntity1.pos;
//			if (p == null) {
//				return;
//			}
//
//			TileEntity tileEntity = event.entity.worldObj.getTileEntity(p.x, p.y, p.z);
//
//			if (tileEntity != null && !tileEntity.isInvalid() && tileEntity instanceof TileTransportDuctBase) {
//				event.setCanceled(true);
//			}
//		}
//	}

	@SubscribeEvent(priority = EventPriority.HIGHEST)
	@SideOnly(Side.CLIENT)
	public void renderTravellers(RenderLivingEvent.Pre event) {

		EntityLivingBase entity = event.entity;
        Entity ridingEntity = entity.ridingEntity;
		if (ridingEntity != null && ridingEntity.getClass() == EntityTransport.class) {
			event.setCanceled(true);

			if (entity instanceof EntityPlayer) {
				return;
			}

			float f = ShaderHelper.midGameTick;
            EntityTransport transport = (EntityTransport) ridingEntity;
            transport.setPosition(0);
			ridingEntity.updateRiderPosition();
			float rotation = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * f;


            GL11.glPushMatrix();
            float max = Math.max(Math.max(Math.max(entity.height, entity.width), transport.originalWidth), transport.originalHeight);

            GL11.glTranslated(event.x, event.y, event.z);

            if (max > 0.4) {
                double h = 0.4 / max;
                GL11.glTranslated(0, -h / 2, 0);
                GL11.glScaled(h, h, h);
            } else
                GL11.glTranslated(0, -1 / 2, 0);

            try {
                entity.ridingEntity = null;
                event.renderer.doRender(entity, 0, 0, 0, rotation, f);
            }finally {
                entity.ridingEntity = transport;
            }
            GL11.glPopMatrix();

		}
	}

    @SideOnly(Side.CLIENT)
	Camera camera;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void controlPlayer(TickEvent.ClientTickEvent event){

        if(event.phase == TickEvent.Phase.END)
            return;

        Minecraft mc = Minecraft.getMinecraft();

        EntityClientPlayerMP thePlayer = mc.thePlayer;
        if (thePlayer == null) {
            return;
        }

        Entity ridingEntity = thePlayer.ridingEntity;
        if (ridingEntity != null && ridingEntity.getClass() == EntityTransport.class) {
            EntityTransport transport = (EntityTransport) ridingEntity;
            transport.updateRider(thePlayer);

            if(mc.gameSettings.thirdPersonView != 0) return;
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
            if (rotationPitch == 0)
                thePlayer.rotationYaw += Math.sin((rotationYaw - thePlayer.rotationYaw) / 180 * Math.PI) * 30;
        }
    }


	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void controlCamera(TickEvent.RenderTickEvent event) {

		Minecraft mc = Minecraft.getMinecraft();
		EntityClientPlayerMP thePlayer = mc.thePlayer;
		if (thePlayer == null) {
			return;
		}

		Entity ridingEntity = thePlayer.ridingEntity;
		if (ridingEntity == null) {
			if (mc.renderViewEntity != null && (mc.renderViewEntity == camera)) {
				mc.renderViewEntity = thePlayer;
                camera.worldObj = null;
			}
		} else if (ridingEntity.getClass() == EntityTransport.class) {
			EntityTransport ridingEntity1 = (EntityTransport) ridingEntity;

			if (camera == null) {
				camera = new Camera();
			}

			camera.copyFromEntityTransport(ridingEntity1, thePlayer);
			mc.renderViewEntity = camera;
		}
	}
}
