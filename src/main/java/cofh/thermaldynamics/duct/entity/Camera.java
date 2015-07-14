package cofh.thermaldynamics.duct.entity;

import cofh.core.render.ShaderHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;

public class Camera extends EntityLivingBase {

	public Camera() {

		super(null);
		this.width = 0;
		this.height = 0;
		invulnerable = true;
	}

	@Override
	protected boolean canTriggerWalking() {

		return false;
	}

	@Override
	public ItemStack getHeldItem() {

		return null;
	}

	@Override
	public ItemStack getEquipmentInSlot(int p_71124_1_) {

		return null;
	}

	@Override
	public void setCurrentItemOrArmor(int p_70062_1_, ItemStack p_70062_2_) {

	}

	@Override
	public ItemStack[] getLastActiveItems() {

		return new ItemStack[0];
	}

	public void copyFromEntityTransport(EntityTransport other, EntityPlayer player) {

		if (other.pos != null) {
			other.setPosition(ShaderHelper.midGameTick);
		}

		worldObj = Minecraft.getMinecraft().theWorld;

		double dx = 0, dy = -(player.posY - player.boundingBox.minY), dz = 0;

		posX = other.posX + dx;
		posY = other.posY + dy;
		posZ = other.posZ + dz;

		lastTickPosX = other.lastTickPosX + dx;
		lastTickPosY = other.lastTickPosY + dy;
		lastTickPosZ = other.lastTickPosZ + dz;

		prevPosX = other.prevPosX + dx;
		prevPosY = other.prevPosY + dy;
		prevPosZ = other.prevPosZ + dz;

		rotationYaw = player.rotationYaw;
		rotationPitch = player.rotationPitch;

		prevRotationYaw = player.prevRotationYaw;
		prevRotationPitch = player.prevRotationPitch;
	}

	@Override
	public float getEyeHeight() {

		return 0;
	}
}
