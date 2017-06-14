package cofh.thermaldynamics.duct.entity;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.client.renderer.entity.RenderEntity;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import java.util.List;
import java.util.WeakHashMap;

public class RenderTransport extends RenderEntity {

	RenderPlayerRiding renderPlayer;

	WeakHashMap<EntityPlayer, EntityOtherPlayerMP> dolls = new WeakHashMap<>();

	public RenderTransport(RenderManager renderManager) {

		super(renderManager);
		renderPlayer = new RenderPlayerRiding(renderManager);
	}

	@Override
	public void doRender(Entity entity, double x, double y, double z, float entityYaw, float partialTicks) {

		if (!entity.isBeingRidden()) {
			return;
		}
		EntityPlayer player = null;

		if (entity.getPassengers().get(0) instanceof EntityPlayer) {
			player = (EntityPlayer) entity.getPassengers().get(0);
		}
		if (player == Minecraft.getMinecraft().thePlayer) {
			if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 0) {
				return;
			}
		}
		if (player == null) {
			return;
		}
		EntityTransport transport = (EntityTransport) entity;

		transport.setPosition(partialTicks);
		transport.updatePassenger(player);

		EntityOtherPlayerMP doll = dolls.get(player);
		if (doll == null) {
			doll = new EntityOtherPlayerMP(player.worldObj, player.getGameProfile());
			dolls.put(player, doll);
		}
		List<EntityDataManager.DataEntry<?>> allWatched = player.getDataManager().getAll();
		if (allWatched != null) {
			doll.getDataManager().setEntryValues(allWatched);
		}
		for (EntityEquipmentSlot slot : EntityEquipmentSlot.values()) {
			doll.setItemStackToSlot(slot, player.getItemStackFromSlot(slot));
		}
		transport.setPosition(0);

		GL11.glPushMatrix();
		RenderPlayerRiding.transport = transport;

		//TODO verify that this yOffset is ok
		double dx = 0;
		double dy = player.getYOffset() + 0.35D;
		double dz = 0;
		renderPlayer.doRender(doll, x + dx, y + dy, z + dz, entityYaw, partialTicks);
		RenderPlayerRiding.transport = null;
		GL11.glPopMatrix();
	}

	@Override
	protected ResourceLocation getEntityTexture(Entity entity) {

		return null;
	}

	public void copyFromEntityTransport(Entity doll, EntityTransport other, EntityPlayer player) {

		if (other.pos != null) {
			other.setPosition(0);
		}
		doll.worldObj = Minecraft.getMinecraft().theWorld;

		double dx = 0, dy = -(player.posY - (player.getEntityBoundingBox().maxY + player.getEntityBoundingBox().minY)), dz = 0;

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
