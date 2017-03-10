package cofh.thermaldynamics.block;

import cofh.core.network.PacketCoFHBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.nbt.NBTTagCompound;

public abstract class Attachment2 {

	private TileModular myTile;
	private byte mySide;

	public Attachment2() {

	}

	public void init(TileModular tile, byte side) {

		myTile = tile;
		mySide = side;
	}

	public abstract String getName();

	public abstract int getType();

	public boolean onWrench() {

		return false;
	}

	public void update() {

	}

	/* GUI METHODS */
	public Object getGuiClient(InventoryPlayer inventory) {

		return null;
	}

	public Object getGuiServer(InventoryPlayer inventory) {

		return null;
	}

	public boolean hasGui() {

		return false;
	}

	public boolean openGui(EntityPlayer player) {

		return false;
	}

	public void receiveGuiNetworkData(int id, int data) {

	}

	public void sendGuiNetworkData(Container container, IContainerListener player) {

	}

	/* NBT METHODS */
	public void readFromNBT(NBTTagCompound nbt) {

	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		return nbt;
	}

	/* NETWORK METHODS */
	public PacketCoFHBase addDescriptionToPacket(PacketCoFHBase packet) {

		return packet;
	}

	public PacketCoFHBase getDescriptionFromPacket(PacketCoFHBase packet) {

		return packet;
	}

}
