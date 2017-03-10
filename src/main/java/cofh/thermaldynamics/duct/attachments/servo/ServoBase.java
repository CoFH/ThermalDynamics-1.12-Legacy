package cofh.thermaldynamics.duct.attachments.servo;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import cofh.core.util.tileentity.IRedstoneControl;
import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.helpers.StringHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.TileDuctBase;
import cofh.thermaldynamics.duct.attachments.ConnectionBase;
import cofh.thermaldynamics.init.TDItems;
import cofh.thermaldynamics.init.TDTextures;
import cofh.thermaldynamics.render.RenderDuct;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ServoBase extends ConnectionBase {

	public static final String[] NAMES = { "basic", "hardened", "reinforced", "signalum", "resonant" };
	static boolean[] redstoneControl = { true, true, true, true, true };

	public static void initialize() {

		String category = "Attachment.Servo.";

		for (int i = 0; i < NAMES.length; i++) {
			redstoneControl[i] = ThermalDynamics.CONFIG.get(category + StringHelper.titleCase(NAMES[i]), "RedstoneControl", redstoneControl[i]);
		}
	}

	public ServoBase(TileDuctBase tile, byte side) {

		super(tile, side);
	}

	public ServoBase(TileDuctBase tile, byte side, int type) {

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

		return new ItemStack(TDItems.itemServo, 1, type);
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
	@SideOnly (Side.CLIENT)
	public boolean render(IBlockAccess world, BlockRenderLayer layer, CCRenderState ccRenderState) {

		if (layer != BlockRenderLayer.SOLID) {
			return false;
		}

		Translation trans = Vector3.fromTileCenter(tile).translation();
		IconTransformation iconTrans = new IconTransformation(TDTextures.SERVO_BASE[stuffed ? 1 : 0][type]);
		RenderDuct.modelConnection[isPowered ? 1 : 2][side].render(ccRenderState, trans, iconTrans);
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
