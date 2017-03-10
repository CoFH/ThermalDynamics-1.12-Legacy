package cofh.thermaldynamics.block;

import cofh.api.core.IPortableData;
import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.RayTracer;
import cofh.thermaldynamics.duct.Attachment;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.RayTraceResult;

public abstract class TileModular extends TileGrid implements IPortableData {

	static final int ATTACHMENT_SUB_HIT = 14;
	static final int COVER_SUB_HIT = 20;

	Attachment2 attachments[] = new Attachment2[6];
	Cover2 covers[] = new Cover2[6];

	int attachmentMask = 0;
	int coverMask = 0;

	protected boolean readPortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		return true;
	}

	protected boolean writePortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		return true;
	}

	public boolean addAttachment(Attachment attachment) {

		return false;
	}

	public boolean removeAttachment(Attachment attachment) {

		return false;
	}

	public boolean addCover(Cover2 cover, byte side) {

		if (covers[side] != null) {
			return false;
		}
		covers[side] = cover;
		covers[side].init(this, side);
		calcCoverMask();

		callNeighborStateChange();
		onNeighborBlockChange();
		callBlockUpdate();

		return true;
	}

	public boolean removeCover(byte side) {

		if (covers[side] == null) {
			return false;
		}
		covers[side] = null;
		calcCoverMask();

		callNeighborStateChange();
		onNeighborBlockChange();
		callBlockUpdate();

		return true;
	}

	/* INTERNAL METHODS */
	void calcAttachmentMask() {

		attachmentMask = 0;
		for (byte i = 0; i < 6; i++) {
			if (attachments[i] != null) {
				attachmentMask = attachmentMask | (1 << i);
			}
		}
	}

	void calcCoverMask() {

		coverMask = 0;
		for (byte i = 0; i < 6; i++) {
			if (covers[i] != null) {
				coverMask = coverMask | (1 << i);
			}
		}
	}

	Attachment2 getAttachmentSelected(EntityPlayer player) {

		RayTraceResult rayTrace = RayTracer.retraceBlock(worldObj, player, getPos());
		if (rayTrace == null) {
			return null;
		}
		int subHit = rayTrace.subHit;
		if (subHit >= ATTACHMENT_SUB_HIT && subHit < ATTACHMENT_SUB_HIT + 6) {
			return attachments[subHit - ATTACHMENT_SUB_HIT];
		}
		if (subHit >= COVER_SUB_HIT && subHit < COVER_SUB_HIT + 6) {
			return covers[subHit - COVER_SUB_HIT];
		}
		return null;
	}

	/* GUI METHODS */

	/* NBT METHODS */
	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		readAttachmentsFromNBT(nbt);
		readCoversFromNBT(nbt);
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		writeAttachmentsToNBT(nbt);
		writeCoversToNBT(nbt);

		return nbt;
	}

	public void readAttachmentsFromNBT(NBTTagCompound nbt) {

		NBTTagList list = nbt.getTagList("Attachments", 10);

		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			byte side = tag.getByte("Side");
			// TODO: create
			attachments[side].readFromNBT(nbt);
		}
	}

	public void writeAttachmentsToNBT(NBTTagCompound nbt) {

		NBTTagList list = new NBTTagList();
		for (byte i = 0; i < 6; i++) {
			if (attachments[i] != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger("Side", i);
				attachments[i].writeToNBT(tag);
				list.appendTag(tag);
			}
		}
		nbt.setTag("Attachments", list);
	}

	public void readCoversFromNBT(NBTTagCompound nbt) {

		NBTTagList list = nbt.getTagList("Covers", 10);

		for (int i = 0; i < list.tagCount(); i++) {
			NBTTagCompound tag = list.getCompoundTagAt(i);
			byte side = tag.getByte("Side");
			// TODO: create
			covers[side].readFromNBT(nbt);
		}
	}

	public void writeCoversToNBT(NBTTagCompound nbt) {

		NBTTagList list = new NBTTagList();
		for (byte i = 0; i < 6; i++) {
			if (covers[i] != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger("Side", i);
				covers[i].writeToNBT(tag);
				list.appendTag(tag);
			}
		}
		nbt.setTag("Covers", list);
	}

	/* NETWORK METHODS */

	/* SERVER -> CLIENT */
	@Override
	public PacketCoFHBase getTilePacket() {

		PacketCoFHBase payload = super.getTilePacket();

		payload.addByte(attachmentMask);
		for (byte i = 0; i < 6; i++) {
			if (attachments[i] != null) {
				payload.addByte(attachments[i].getType());
				attachments[i].addDescriptionToPacket(payload);
			}
		}
		payload.addByte(coverMask);
		for (byte i = 0; i < 6; i++) {
			if (covers[i] != null) {
				covers[i].addDescriptionToPacket(payload);
			}
		}
		return payload;
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		// super.handleTilePacket(payload, isServer);

		attachmentMask = payload.getByte();
		for (byte i = 0; i < 6; i++) {
			if ((attachmentMask & (1 << i)) != 0) {
				// TODO: CREATE
				attachments[i].init(this, i);
				attachments[i].getDescriptionFromPacket(payload);
			} else {
				attachments[i] = null;
			}
		}
		coverMask = payload.getByte();
		for (byte i = 0; i < 6; i++) {
			if ((coverMask & (1 << i)) != 0) {
				covers[i] = new Cover2();
				covers[i].init(this, i);
				covers[i].getDescriptionFromPacket(payload);
			} else {
				covers[i] = null;
			}
		}
		// calcAttachmentMask();
		// calcCoverMask();

		callBlockUpdate();
	}

	/* IPortableData */
	@Override
	public void readPortableData(EntityPlayer player, NBTTagCompound tag) {

		Attachment2 attachment = getAttachmentSelected(player);

		if (attachment instanceof IPortableData) {
			((IPortableData) attachment).readPortableData(player, tag);
		}
	}

	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {

		Attachment2 attachment = getAttachmentSelected(player);

		if (attachment instanceof IPortableData) {
			((IPortableData) attachment).writePortableData(player, tag);
		}
	}

	/* PLUGIN METHODS */

}
