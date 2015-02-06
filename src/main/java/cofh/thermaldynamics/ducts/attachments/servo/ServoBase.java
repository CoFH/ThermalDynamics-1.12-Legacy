package cofh.thermaldynamics.ducts.attachments.servo;

import cofh.api.tileentity.IRedstoneControl;
import cofh.core.network.PacketCoFHBase;
import cofh.core.render.RenderUtils;
import cofh.repack.codechicken.lib.vec.Translation;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.TileMultiBlock;
import cofh.thermaldynamics.ducts.attachments.ConnectionBase;
import cofh.thermaldynamics.render.RenderDuct;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class ServoBase extends ConnectionBase {

	public ServoBase(TileMultiBlock tile, byte side) {

		super(tile, side);
	}

	public ServoBase(TileMultiBlock tile, byte side, int type) {

		super(tile, side, type);
	}

	@Override
	public String getName() {

		return "item.thermaldynamics.servo." + type + ".name";
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {

		super.writeToNBT(tag);
		tag.setBoolean("power", isPowered);
		if (canAlterRS()) {
			tag.setByte("rsMode", (byte) rsMode.ordinal());
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {

		super.readFromNBT(tag);
		isPowered = tag.getBoolean("power");

		if (canAlterRS()) {
			rsMode = IRedstoneControl.ControlMode.values()[tag.getByte("rsMode")];
		}
	}

	@Override
	public void addDescriptionToPacket(PacketCoFHBase packet) {

		super.addDescriptionToPacket(packet);
		packet.addBool(isPowered);
		if (canAlterRS()) {
			packet.addByte(rsMode.ordinal());
		}
	}

	@Override
	public void getDescriptionFromPacket(PacketCoFHBase packet) {

		super.getDescriptionFromPacket(packet);
		isPowered = packet.getBool();
		if (canAlterRS()) {
			rsMode = IRedstoneControl.ControlMode.values()[packet.getByte()];
		}
	}

	@Override
	public ItemStack getPickBlock() {

		return new ItemStack(ThermalDynamics.itemServo, 1, type);
	}

	@Override
	public boolean canAlterRS() {

		return canAlterRS(type);
	}

	public static boolean canAlterRS(int type) {

		return type >= 2;
	}

	@Override
	public void onNeighborChange() {

		super.onNeighborChange();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean render(int pass, RenderBlocks renderBlocks) {

		if (pass == 1) {
			return false;
		}
		Translation trans = RenderUtils.getRenderVector(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5).translation();
		RenderDuct.modelConnection[isPowered ? 1 : 2][side].render(trans,
				RenderUtils.getIconTransformation(RenderDuct.servoTexture[type * 2 + (stuffed ? 1 : 0)]));
		return true;
	}

	@Override
	public boolean doesTick() {

		return true;
	}

}
