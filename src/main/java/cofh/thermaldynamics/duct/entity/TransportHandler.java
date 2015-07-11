package cofh.thermaldynamics.duct.entity;

import cofh.core.render.ShaderHelper;
import cofh.lib.util.position.BlockPosition;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityClientPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import org.lwjgl.opengl.GL11;

public class TransportHandler {
    public static final TransportHandler INSTANCE = new TransportHandler();

    @SubscribeEvent
    public void cancelDamgage(LivingAttackEvent event) {
        EntityLivingBase entity = event.entityLiving;
        Entity ridingEntity = entity.ridingEntity;
        if (ridingEntity != null && ridingEntity.getClass() == EntityTransport.class) {
            EntityTransport ridingEntity1 = (EntityTransport) ridingEntity;
            BlockPosition p = ridingEntity1.pos;
            if(p == null) return;

            TileEntity tileEntity = event.entity.worldObj.getTileEntity(p.x, p.y, p.z);

            if (tileEntity != null && !tileEntity.isInvalid() && tileEntity instanceof TileTransportDuct)
                event.setCanceled(true);
        }
    }

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void cancelOverlay(RenderBlockOverlayEvent event) {
        EntityLivingBase entity = event.player;
        Entity ridingEntity = entity.ridingEntity;
        if (ridingEntity != null && ridingEntity.getClass() == EntityTransport.class) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    @SideOnly(Side.CLIENT)
    public void renderTravellers(RenderLivingEvent.Pre event) {

        EntityLivingBase entity = event.entity;
        Entity ridingEntity = entity.ridingEntity;
        if (ridingEntity != null && ridingEntity.getClass() == EntityTransport.class) {
            event.setCanceled(true);

            if (entity == Minecraft.getMinecraft().thePlayer)
                return;

            float f = ShaderHelper.midGameTick;
            ((EntityTransport) ridingEntity).setPosition(f);
            ridingEntity.updateRiderPosition();
            float rotation = entity.prevRotationYaw + (entity.rotationYaw - entity.prevRotationYaw) * f;


            entity.ridingEntity = null;
            GL11.glPushMatrix();
            float max = Math.max(entity.height, entity.width);
            GL11.glTranslated(event.x, event.y, event.z);


            double h = max == 0 ? 1 : 0.5 / max;
            GL11.glScaled(h, h, h);
            event.renderer.doRender((Entity) entity, 0, 0, 0, rotation, f);
            GL11.glPopMatrix();
            entity.ridingEntity = ridingEntity;

        }
    }

    Camera camera;

    @SubscribeEvent
    @SideOnly(Side.CLIENT)
    public void controlCamera(TickEvent.RenderTickEvent event) {
        Minecraft mc = Minecraft.getMinecraft();
        EntityClientPlayerMP thePlayer = mc.thePlayer;
        if (thePlayer == null) return;

        Entity ridingEntity = thePlayer.ridingEntity;
        if (ridingEntity == null) {
            if (mc.renderViewEntity != null && (mc.renderViewEntity == camera)) {
                mc.renderViewEntity = thePlayer;
            }
        } else if (ridingEntity.getClass() == EntityTransport.class) {
            EntityTransport ridingEntity1 = (EntityTransport) ridingEntity;

            if (camera == null) camera = new Camera();

            camera.copyFromEntityTransport(ridingEntity1, thePlayer);
            mc.renderViewEntity = camera;
        }
    }
}
