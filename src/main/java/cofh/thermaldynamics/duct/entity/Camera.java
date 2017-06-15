package cofh.thermaldynamics.duct.entity;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHandSide;

public class Camera extends EntityLivingBase {

	public Camera() {

		super(null);
		this.width = 0;
		this.height = 0;
		this.setEntityInvulnerable(true);
	}

	@Override
	public boolean canTriggerWalking() {

		return false;
	}

	@Override
	public ItemStack getHeldItemMainhand() {

		return ItemStack.EMPTY;
	}

	@Override
	public ItemStack getHeldItemOffhand() {

		return ItemStack.EMPTY;
	}

	@Override
	public EnumHandSide getPrimaryHand() {

		return EnumHandSide.RIGHT;
	}

	@Override
	public ItemStack getItemStackFromSlot(EntityEquipmentSlot slotIn) {

		return ItemStack.EMPTY;
	}

	@Override
	public void setItemStackToSlot(EntityEquipmentSlot slotIn, ItemStack stack) {

	}

	@Override
	public Iterable<ItemStack> getArmorInventoryList() {

		return ImmutableList.of();
	}

	public void copyFromEntityTransport(EntityTransport other, EntityPlayer player) {

		if (other.pos != null) {
			other.setPosition(0);
		}
		if (world == null) {
			world = Minecraft.getMinecraft().world;
		}
		double dx = 0, dy = player.getYOffset() + 0.35D, dz = 0;

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
