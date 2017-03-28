package cofh.thermaldynamics.block;

import codechicken.lib.util.BlockUtils;
import cofh.api.core.IPortableData;
import cofh.core.block.TileCore;
import cofh.core.network.ITileInfoPacketHandler;
import cofh.core.network.ITilePacketHandler;
import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.RayTracer;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.duct.*;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleRender;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.util.LinkedList;

public abstract class TileDuct extends TileCore implements IPortableData, ITileInfoPacketHandler, ITilePacketHandler {

	static final int ATTACHMENT_SUB_HIT = 14;
	static final int COVER_SUB_HIT = 20;
	@Nullable
	AttachmentData attachmentData;

	@Override
	public String getTileName() {

		return "";
	}

	/* IPortableData */
	@Override
	public String getDataType() {

		return "";
	}

	protected boolean readPortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		return true;
	}

	protected boolean writePortableTagInternal(EntityPlayer player, NBTTagCompound tag) {

		return true;
	}

	public boolean addAttachment(Attachment attachment) {

		if (!attachment.canAddToTile(this)) {
			return false;
		}

		if (attachmentData != null && attachmentData.attachments[attachment.side] != null) {
			return false;
		}

		if (ServerHelper.isClientWorld(worldObj)) {
			return true;
		}

		if (attachmentData == null) {
			attachmentData = new AttachmentData();
		}

		attachmentData.attachments[attachment.side] = attachment;

		if (attachment.doesTick()) {
			attachmentData.tickingAttachments.add(attachment);
		}

		callNeighborStateChange();
		onNeighborBlockChange();
		callBlockUpdate();
		onAttachmentsChanged();

		return false;
	}

	public boolean removeAttachment(Attachment attachment) {

		if (attachment == null || attachmentData == null) {
			return false;
		}
		attachmentData.attachments[attachment.side] = null;
		attachmentData.tickingAttachments.remove(attachment);
		worldObj.notifyNeighborsOfStateChange(getPos(), getBlockType());
		onNeighborBlockChange();

		onAttachmentsChanged();

		BlockUtils.fireBlockUpdate(getWorld(), getPos());
		return true;
	}

	public boolean addCover(Cover cover, byte side) {

		if (cover == null) {
			return false;
		}
		if (attachmentData == null) {
			attachmentData = new AttachmentData();
		} else if (attachmentData.covers[side] != null) {
			return false;
		}
		attachmentData.covers[side] = cover;

		callNeighborStateChange();
		onNeighborBlockChange();
		callBlockUpdate();
		onAttachmentsChanged();

		return true;
	}

	protected void onAttachmentsChanged() {

	}

	public boolean removeCover(byte side) {

		if (attachmentData == null || attachmentData.covers[side] == null) {
			return false;
		}
		attachmentData.covers[side] = null;

		callNeighborStateChange();
		onNeighborBlockChange();
		callBlockUpdate();
		onAttachmentsChanged();

		return true;
	}

	Attachment getAttachmentSelected(EntityPlayer player) {

		if (attachmentData == null) {
			return null;
		}
		RayTraceResult rayTrace = RayTracer.retraceBlock(worldObj, player, getPos());
		if (rayTrace == null) {
			return null;
		}
		int subHit = rayTrace.subHit;
		if (subHit >= TileGrid.ATTACHMENT_SUB_HIT && subHit < TileGrid.ATTACHMENT_SUB_HIT + 6) {
			return attachmentData.attachments[subHit - TileGrid.ATTACHMENT_SUB_HIT];
		}
		if (subHit >= TileGrid.COVER_SUB_HIT && subHit < TileGrid.COVER_SUB_HIT + 6) {
			return attachmentData.covers[subHit - TileGrid.COVER_SUB_HIT];
		}
		return null;
	}

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
			if (attachmentData == null) {
				attachmentData = new AttachmentData();
			}

			attachmentData.attachments[side].readFromNBT(nbt);
		}
	}

	public void writeAttachmentsToNBT(NBTTagCompound nbt) {

		NBTTagList list = new NBTTagList();
		if (attachmentData != null) {
			for (byte i = 0; i < 6; i++) {
				if (attachmentData.attachments[i] != null) {
					NBTTagCompound tag = new NBTTagCompound();
					tag.setInteger("Side", i);
					attachmentData.attachments[i].writeToNBT(tag);
					list.appendTag(tag);
				}
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
			if (attachmentData == null) {
				attachmentData = new AttachmentData();
			}
			attachmentData.covers[side] = new Cover(this, side);
			attachmentData.covers[side].readFromNBT(nbt);
		}
	}

	public void writeCoversToNBT(NBTTagCompound nbt) {

		if (attachmentData == null) {
			return;
		}
		NBTTagList list = new NBTTagList();
		for (byte i = 0; i < 6; i++) {
			if (attachmentData.covers[i] != null) {
				NBTTagCompound tag = new NBTTagCompound();
				tag.setInteger("Side", i);
				attachmentData.covers[i].writeToNBT(tag);
				list.appendTag(tag);
			}
		}
		nbt.setTag("Covers", list);
	}

	/* SERVER -> CLIENT */
	@Override
	public PacketCoFHBase getTilePacket() {

		PacketCoFHBase payload = super.getTilePacket();

		if (attachmentData == null) {
			payload.addByte(0);
			payload.addByte(0);
			return payload;
		}

		int attachmentMask = 0;
		for (byte i = 0; i < 6; i++) {
			if (attachmentData.attachments[i] != null) {
				attachmentMask |= (1 << i);
			}
		}

		payload.addByte(attachmentMask);
		for (byte i = 0; i < 6; i++) {
			if (attachmentData.attachments[i] != null) {
				payload.addByte(attachmentData.attachments[i].getId());
				attachmentData.attachments[i].addDescriptionToPacket(payload);
			}
		}

		int coverMask = 0;
		for (byte i = 0; i < 6; i++) {
			if (attachmentData.covers[i] != null) {
				coverMask |= (1 << i);
			}
		}

		payload.addByte(coverMask);
		for (byte i = 0; i < 6; i++) {
			if (attachmentData.covers[i] != null) {
				attachmentData.covers[i].addDescriptionToPacket(payload);
			}
		}
		return payload;
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		int attachmentMask = payload.getByte();
		for (byte i = 0; i < 6; i++) {
			if ((attachmentMask & (1 << i)) != 0) {
				if (attachmentData == null) {
					attachmentData = new AttachmentData();
				}
				int id = payload.getByte();
				attachmentData.attachments[i] = AttachmentRegistry.createAttachment(this, i, id);
				attachmentData.attachments[i].getDescriptionFromPacket(payload);
			} else if (attachmentData != null) {
				attachmentData.attachments[i] = null;
			}
		}

		int coverMask = payload.getByte();
		for (byte i = 0; i < 6; i++) {
			if ((coverMask & (1 << i)) != 0) {
				if (attachmentData == null) {
					attachmentData = new AttachmentData();
				}
				attachmentData.covers[i] = new Cover(this, i);
				attachmentData.covers[i].getDescriptionFromPacket(payload);
			} else if (attachmentData != null) {
				attachmentData.covers[i] = null;
			}
		}

		if (coverMask == 0 && attachmentMask == 0) {
			attachmentData = null;
		}

		callBlockUpdate();
	}

	/* IPortableData */
	@Override
	public void readPortableData(EntityPlayer player, NBTTagCompound tag) {

		Attachment attachment = getAttachmentSelected(player);

		if (attachment instanceof IPortableData) {
			((IPortableData) attachment).readPortableData(player, tag);
		}
	}

	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {

		Attachment attachment = getAttachmentSelected(player);

		if (attachment instanceof IPortableData) {
			((IPortableData) attachment).writePortableData(player, tag);
		}
	}

	public Attachment getAttachment(byte side) {

		AttachmentData attachmentData = this.attachmentData;
		if (attachmentData == null) {
			return null;
		}
		return attachmentData.attachments[side];
	}

	@SideOnly (Side.CLIENT)
	public CoverHoleRender.ITransformer[] getHollowMask(byte side) {

		BlockDuct.ConnectionType connectionType = getRenderConnectionType(side);
		if (connectionType == BlockDuct.ConnectionType.TILECONNECTION) {
			return CoverHoleRender.hollowDuctTile;
		} else if (connectionType == BlockDuct.ConnectionType.NONE) {
			return null;
		} else {
			return CoverHoleRender.hollowDuct;
		}
	}

	public static class AttachmentData {

		public final Attachment attachments[] = new Attachment[] { null, null, null, null, null, null };
		public final LinkedList<Attachment> tickingAttachments = new LinkedList<>();
		public final Cover[] covers = new Cover[6];
	}
}
