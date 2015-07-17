package cofh.thermaldynamics.duct.entity;

import cofh.core.render.ShaderHelper;
import java.util.List;
import java.util.WeakHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class RenderTransport extends RenderEntity {
    RenderPlayerRiding renderPlayer = new RenderPlayerRiding();

    WeakHashMap<EntityPlayer, EntityOtherPlayerMP> dolls = new WeakHashMap<EntityPlayer, EntityOtherPlayerMP>();

    @Override
    public void doRender(Entity entity, double p_76986_2_, double p_76986_4_, double p_76986_6_, float p_76986_8_, float p_76986_9_) {

        if (entity.riddenByEntity == null)
            return;

        EntityPlayer player = null;

        if (entity.riddenByEntity instanceof EntityPlayer) {
            player = (EntityPlayer) entity.riddenByEntity;
        }

        if (player == Minecraft.getMinecraft().thePlayer) {
            if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0)
                return;
        }

        if (player == null) return;

        EntityTransport transport = (EntityTransport) entity;

        transport.setPosition(ShaderHelper.midGameTick);

        transport.updateRiderPosition();

        EntityOtherPlayerMP doll = dolls.get(player);
        if (doll == null) {
            doll = new EntityOtherPlayerMP(player.worldObj, player.getGameProfile());
            dolls.put(player, doll);
        }

        List allWatched = player.getDataWatcher().getAllWatched();
        if (allWatched != null)
            doll.getDataWatcher().updateWatchedObjectsFromList(allWatched);

        for (int i = 1; i < 5; i++) {
            doll.setCurrentItemOrArmor(i, player.getEquipmentInSlot(i));
        }

        renderPlayer.setRenderManager(renderManager);

        transport.setPosition(0);

        GL11.glPushMatrix();
        RenderPlayerRiding.transport = transport;

        double dy = player.yOffset - 1.62F;
        renderPlayer.doRender(doll, p_76986_2_, p_76986_4_ + dy, p_76986_6_, p_76986_8_, p_76986_9_);
        RenderPlayerRiding.transport = null;
        GL11.glPopMatrix();
    }

    @Override
    protected ResourceLocation getEntityTexture(Entity p_110775_1_) {
        return null;
    }


    public void copyFromEntityTransport(Entity doll, EntityTransport other, EntityPlayer player) {

        if (other.pos != null) {
            other.setPosition(0);
        }

        doll.worldObj = Minecraft.getMinecraft().theWorld;

        double dx = 0, dy = -(player.posY - (player.boundingBox.maxY + player.boundingBox.minY)), dz = 0;

        doll.posX = other.posX + dx;
        doll.posY = other.posY + dy;
        doll.posZ = other.posZ + dz;

        doll.lastTickPosX = other.lastTickPosX + dx;
        doll.lastTickPosY = other.lastTickPosY + dy;
        doll.lastTickPosZ = other.lastTickPosZ + dz;

        doll.prevPosX = other.prevPosX + dx;
        doll.prevPosY = other.prevPosY + dy;
        doll.prevPosZ = other.prevPosZ + dz;

        doll.rotationYaw = player.rotationYaw;
        doll.rotationPitch = player.rotationPitch;

        doll.prevRotationYaw = player.prevRotationYaw;
        doll.prevRotationPitch = player.prevRotationPitch;
    }



}
