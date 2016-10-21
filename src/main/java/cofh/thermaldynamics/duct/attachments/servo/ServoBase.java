package cofh.thermaldynamics.duct.attachments.servo;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Vector3;
import cofh.api.tileentity.IRedstoneControl;
import cofh.core.network.PacketCoFHBase;
import cofh.core.render.RenderUtils;
import cofh.lib.util.helpers.StringHelper;
import codechicken.lib.vec.Translation;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.attachments.ConnectionBase;
import cofh.thermaldynamics.render.RenderDuct;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public abstract class ServoBase extends ConnectionBase {

	public static final String[] NAMES = { "basic", "hardened", "reinforced", "signalum", "resonant" };
	static boolean[] redstoneControl = { true, true, true, true, true };

	public static void initialize() {

		String category = "Attachment.Servo.";

		for (int i = 0; i < NAMES.length; i++) {
			redstoneControl[i] = ThermalDynamics.config.get(category + StringHelper.titleCase(NAMES[i]), "RedstoneControl", redstoneControl[i]);
		}
	}

	public ServoBase(TileTDBase tile, byte side) {

		super(tile, side);
	}

	public ServoBase(TileTDBase tile, byte side, int type) {

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

		return redstoneControl[type % redstoneControl.length];
	}

	@Override
	public void onNeighborChange() {

		super.onNeighborChange();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean render(int pass, CCRenderState ccRenderState) {

		if (pass == 1) {
			return false;
		}
		Translation trans = Vector3.fromTileCenter(tile).translation();
		RenderDuct.modelConnection[isPowered ? 1 : 2][side].render(ccRenderState, trans,
				RenderUtils.getIconTransformation(RenderDuct.servoTexture[type * 2 + (stuffed ? 1 : 0)]));
		return true;
	}

	@Override
	public boolean doesTick() {

		return true;
	}

	/* IPortableData */
	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {

		super.writePortableData(player, tag);
		tag.setString("DisplayType", "item.thermaldynamics.servo.0.name");
	}

}
