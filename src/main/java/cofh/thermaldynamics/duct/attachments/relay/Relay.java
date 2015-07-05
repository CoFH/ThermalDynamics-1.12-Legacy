package cofh.thermaldynamics.duct.attachments.relay;

import cofh.core.network.PacketCoFHBase;
import cofh.core.render.RenderUtils;
import cofh.lib.util.helpers.ServerHelper;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Translation;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.Attachment;
import cofh.thermaldynamics.block.AttachmentRegistry;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.render.RenderDuct;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Facing;

public class Relay extends Attachment {

	public Relay(TileTDBase tile, byte side) {

		super(tile, side);
	}

	public Relay(TileTDBase tile, byte side, int type) {

		super(tile, side);
		this.type = type;
	}

	int type = 0;

	@Override
	public String getName() {

		return "item.thermaldynamics.relay.name";
	}

	@Override
	public int getId() {

		return AttachmentRegistry.RELAY;
	}

	@Override
	public Cuboid6 getCuboid() {

		return TileTDBase.subSelection[side].copy();
	}

	@Override
	public TileTDBase.NeighborTypes getNeighborType() {

		return TileTDBase.NeighborTypes.DUCT_ATTACHMENT;
	}

	@Override
	public boolean isNode() {

		return true;
	}

	@Override
	public boolean render(int pass, RenderBlocks renderBlocks) {

		if (pass == 1) {
			return false;
		}
		Translation trans = RenderUtils.getRenderVector(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5).translation();
		RenderDuct.modelConnection[1 + type][side].render(trans, RenderUtils.getIconTransformation(RenderDuct.signalTexture));
		return true;
	}

	@Override
	public ItemStack getPickBlock() {

		return new ItemStack(ThermalDynamics.itemRelay);
	}

	@Override
	public List<ItemStack> getDrops() {

		LinkedList<ItemStack> drops = new LinkedList<ItemStack>();
		drops.add(getPickBlock());
		return drops;
	}

	int powerLevel;

	@Override
	public void onNeighborChange() {

		super.onNeighborChange();

		if (type == 0) {
			int dx = tile.xCoord + Facing.offsetsXForSide[side];
			int dy = tile.yCoord + Facing.offsetsYForSide[side];
			int dz = tile.zCoord + Facing.offsetsZForSide[side];
			powerLevel = tile.world().getIndirectPowerLevelTo(dx, dy, dz, side);

			if (tile.world().getBlock(dx, dy, dz) == Blocks.redstone_wire) {
				powerLevel = Math.max(powerLevel, tile.world().getBlockMetadata(dx, dy, dz));
			}

			if (powerLevel > 0) {
				powerLevel = 15;
			}
		}

		if (tile.myGrid != null) {
			tile.myGrid.signalsUpToDate = false;
		}
	}

	public boolean isInput() {

		return type == 0;
	}

	public boolean isOutput() {

		return type == 1;
	}

	public int getPowerLevel() {

		if (type == 1 && tile.myGrid != null) {
			return tile.myGrid.redstoneLevel;
		}
		return powerLevel;
	}

	@Override
	public int getRSOutput() {

		return isOutput() ? getPowerLevel() : 0;
	}

	public void setPowerLevel(int powerLevel) {

		if (this.powerLevel != powerLevel) {
			this.powerLevel = powerLevel;

			tile.world().notifyBlockOfNeighborChange(tile.xCoord + Facing.offsetsXForSide[side], tile.yCoord + Facing.offsetsYForSide[side],
					tile.zCoord + Facing.offsetsZForSide[side], tile.getBlockType());
		}

	}

	@Override
	public void checkSignal() {

		MultiBlockGrid grid = tile.myGrid;
		if (grid == null) {
			return;
		}
		setPowerLevel(grid.redstoneLevel);
	}

	@Override
	public boolean respondsToSignallum() {

		return true;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {

		super.writeToNBT(tag);
		tag.setByte("type", (byte) type);

	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {

		super.readFromNBT(tag);
		type = tag.getByte("type");
	}

	@Override
	public void addDescriptionToPacket(PacketCoFHBase packet) {

		packet.addByte(type);
	}

	@Override
	public void getDescriptionFromPacket(PacketCoFHBase packet) {

		type = packet.getByte();
	}

	@Override
	public boolean openGui(EntityPlayer player) {

		if (ServerHelper.isClientWorld(tile.world())) {
			return true;
		}

		type = type ^ 1;
		if (tile.myGrid != null) {
			tile.myGrid.resetRelays();
		}
		tile.world().notifyBlocksOfNeighborChange(tile.xCoord, tile.yCoord, tile.zCoord, tile.getBlockType());
		onNeighborChange();
		tile.world().markBlockForUpdate(tile.x(), tile.y(), tile.z());
		return true;
	}

	@Override
	public boolean shouldRSConnect() {

		return true;
	}

	@Override
	public boolean canAddToTile(TileTDBase tileMultiBlock) {

		return tileMultiBlock.getDuctType().frameType != 2;
	}

}
