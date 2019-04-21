package cofh.thermaldynamics.duct.attachments.servo;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import cofh.api.tileentity.IRedstoneControl;
import cofh.core.network.PacketBase;
import cofh.core.util.helpers.StringHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.attachments.ConnectionBase;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import cofh.thermaldynamics.init.TDItems;
import cofh.thermaldynamics.init.TDTextures;
import cofh.thermaldynamics.render.RenderDuct;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public abstract class ServoBase extends ConnectionBase {

	public static final String[] NAMES = { "basic", "hardened", "reinforced", "signalum", "resonant" };
	static boolean[] redstoneControl = { true, true, true, true, true };

	protected TileEntity myTile;

	public static void initialize() {

		String category = "Attachment.Servo.";

		for (int i = 0; i < NAMES.length; i++) {
			redstoneControl[i] = ThermalDynamics.CONFIG.get(category + StringHelper.titleCase(NAMES[i]), "RedstoneControl", redstoneControl[i]);
		}
	}

	public static boolean canAlterRS(int type) {

		return redstoneControl[type % redstoneControl.length];
	}

	public ServoBase(TileGrid tile, byte side) {

		super(tile, side);
	}

	public ServoBase(TileGrid tile, byte side, int type) {

		super(tile, side, type);
	}

	@Override
	public boolean isServo() {

		return true;
	}

	@Override
	public boolean isFilter() {

		return false;
	}

	@Override
	public boolean canSend() {

		return true;
	}

	@Override
	public String getName() {

		return "item.thermaldynamics.servo." + type + ".name";
	}

	@Override
	public boolean allowDuctConnection() {

		return false;
	}

	@Override
	public boolean canAlterRS() {

		return canAlterRS(type);
	}

	@Override
	public ItemStack getPickBlock() {

		return new ItemStack(TDItems.itemServo, 1, type);
	}

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound tag) {

		super.readFromNBT(tag);
		isPowered = tag.getBoolean("power");

		if (canAlterRS()) {
			rsMode = IRedstoneControl.ControlMode.values()[tag.getByte("rsMode")];
		}
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {

		super.writeToNBT(tag);
		tag.setBoolean("power", isPowered);
		if (canAlterRS()) {
			tag.setByte("rsMode", (byte) rsMode.ordinal());
		}
	}

	/* NETWORK METHODS */
	@Override
	public void addDescriptionToPacket(PacketBase packet) {

		super.addDescriptionToPacket(packet);
		packet.addBool(isPowered);
		if (canAlterRS()) {
			packet.addByte(rsMode.ordinal());
		}
	}

	@Override
	public void getDescriptionFromPacket(PacketBase packet) {

		super.getDescriptionFromPacket(packet);
		isPowered = packet.getBool();
		if (canAlterRS()) {
			rsMode = IRedstoneControl.ControlMode.values()[packet.getByte()];
		}
	}

	/* RENDER */
	@Override
	@SideOnly (Side.CLIENT)
	public boolean render(IBlockAccess world, BlockRenderLayer layer, CCRenderState ccRenderState) {

		if (layer != BlockRenderLayer.CUTOUT) {
			return false;
		}
		Translation trans = Vector3.fromTileCenter(baseTile).translation();
		IconTransformation iconTrans = new IconTransformation(TDTextures.SERVO_BASE[stuffed ? 1 : 0][type]);
		RenderDuct.modelConnection[isPowered ? 1 : 2][side].render(ccRenderState, trans, iconTrans);
		return true;
	}

	/* IPortableData */
	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {

		super.writePortableData(player, tag);
		tag.setString("DisplayType", "item.thermaldynamics.servo.0.name");
	}

}
