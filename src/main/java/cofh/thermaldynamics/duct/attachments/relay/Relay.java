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
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.render.RenderDuct;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Direction;
import net.minecraft.util.Facing;

public class Relay extends Attachment {

	public static final byte NON_THRESHOLD = -1;
	int type = 0;
	int powerLevel;
	boolean invert = false;
	byte threshold = 0;

	public Relay(TileTDBase tile, byte side) {

		super(tile, side);
	}

	public Relay(TileTDBase tile, byte side, int type) {

		super(tile, side);
		this.type = type;
	}

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

		return null;
	}

	@Override
	public BlockDuct.ConnectionTypes getRenderConnectionType() {

		return BlockDuct.ConnectionTypes.DUCT;
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

	@Override
	public void onNeighborChange() {

		super.onNeighborChange();

		if (type == 0) {
			int powerLevel = getRawRedstoneLevel();
			powerLevel = adjustPowerLevel(powerLevel);

			this.powerLevel = powerLevel;
		}

		if (tile.myGrid != null) {
			tile.myGrid.signalsUpToDate = false;
		}
	}

	public int adjustPowerLevel(int powerLevel) {

		if (threshold != NON_THRESHOLD) {
			powerLevel = powerLevel > threshold ? 15 : 0;
		}

		if (invert) {
			powerLevel = 15 - powerLevel;
		}
		return powerLevel;
	}

	public int getRawRedstoneLevel() {

		int dx = tile.xCoord + Facing.offsetsXForSide[side];
		int dy = tile.yCoord + Facing.offsetsYForSide[side];
		int dz = tile.zCoord + Facing.offsetsZForSide[side];
		int level = 0;

		Block block = tile.world().getBlock(dx, dy, dz);

		if (type == 0) { // should calc vanilla redstone level
			level = Math.max(level, tile.world().getIndirectPowerLevelTo(dx, dy, dz, side));

			if (block == Blocks.redstone_wire) {
				level = Math.max(level, tile.world().getBlockMetadata(dx, dy, dz));
			}
		}

		if (type == 2) { // should calc comparator redstone level
			if (block.hasComparatorInputOverride()) {
				level = block.getComparatorInputOverride(tile.world(), dx, dy, dz, Direction.facingToDirection[side ^ 1]);
			} else if (block.isNormalCube(tile.world(), dx, dy, dz)) {
				dx += Facing.offsetsXForSide[side];
				dy += Facing.offsetsYForSide[side];
				dz += Facing.offsetsZForSide[side];

				Block otherBlock = tile.world().getBlock(dx, dy, dz);

				if (otherBlock.hasComparatorInputOverride()) {
					level = otherBlock.getComparatorInputOverride(tile.world(), dx, dy, dz, Direction.facingToDirection[side ^ 1]);
				}
			}
		}

		return level;
	}

	public boolean isInput() {

		return type == 0;
	}

	public boolean isOutput() {

		return type == 1;
	}

	public int getPowerLevel() {

		if (type == 1 && tile.myGrid != null) {
			return adjustPowerLevel(tile.myGrid.redstoneLevel);
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
		tag.setBoolean("invert", invert);
		tag.setByte("threshold", threshold);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {

		super.readFromNBT(tag);
		type = tag.getByte("type");
		if (tag.hasKey("invert", 1)) {
			invert = tag.getBoolean("invert");
		}
		if (tag.hasKey("threshold", 1)) {
			threshold = tag.getByte("threshold");
		}
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
