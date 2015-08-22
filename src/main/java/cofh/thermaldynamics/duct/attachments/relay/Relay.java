package cofh.thermaldynamics.duct.attachments.relay;

import cofh.api.block.IBlockConfigGui;
import cofh.api.tileentity.IPortableData;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.core.render.RenderUtils;
import cofh.lib.util.helpers.ServerHelper;
import cofh.repack.codechicken.lib.vec.Cuboid6;
import cofh.repack.codechicken.lib.vec.Translation;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.Attachment;
import cofh.thermaldynamics.block.AttachmentRegistry;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleRender;
import cofh.thermaldynamics.gui.GuiHandler;
import cofh.thermaldynamics.gui.client.GuiRelay;
import cofh.thermaldynamics.gui.container.ContainerRelay;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.render.RenderDuct;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.Direction;
import net.minecraft.util.Facing;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.common.util.ForgeDirection;

public class Relay extends Attachment implements IBlockConfigGui, IPortableData {

	public byte type = 0;
	public int powerLevel;
	public byte invert = 0;
	public byte threshold = 0;

	public Relay(TileTDBase tile, byte side) {

		super(tile, side);
	}

	public Relay(TileTDBase tile, byte side, int type) {

		super(tile, side);
		this.type = (byte) type;
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
		RenderDuct.modelConnection[1 + (type & 1)][side].render(trans, RenderUtils.getIconTransformation(RenderDuct.signalTexture));
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

		if (type == 0 || type == 2) {
			int powerLevel = getRawRedstoneLevel();
			powerLevel = adjustPowerLevel(powerLevel);

			this.setPowerLevel(powerLevel);
		}
	}

	public int adjustPowerLevel(int powerLevel) {

		if (shouldThreshold()) {
			powerLevel = powerLevel >= threshold ? 15 : 0;
		}

		if (shouldInvert()) {
			powerLevel = 15 - powerLevel;
		}
		return powerLevel;
	}

	public boolean shouldThreshold() {
		return (invert & 2) != 0;
	}

	public boolean shouldInvert() {
		return (invert & 1) == 1;
	}

	public int getRawRedstoneLevel() {

		int dx = tile.xCoord + Facing.offsetsXForSide[side];
		int dy = tile.yCoord + Facing.offsetsYForSide[side];
		int dz = tile.zCoord + Facing.offsetsZForSide[side];
		int level = 0;

		Block block = tile.world().getBlock(dx, dy, dz);

		if (type == 0) { // should calc vanilla redstone level
			if (isBlockDuct(block)) {
				TileTDBase t = (TileTDBase) tile.world().getTileEntity(dx, dy, dz);
				Attachment attachment = t.attachments[side ^ 1];
				if (attachment != null)
					level = attachment.getRSOutput();

			} else {
				level = Math.max(level, tile.world().getIndirectPowerLevelTo(dx, dy, dz, side));

				if (block == Blocks.redstone_wire) {
					level = Math.max(level, tile.world().getBlockMetadata(dx, dy, dz));
				}
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

	public static boolean isBlockDuct(Block block) {
		for (BlockDuct blockDuct : ThermalDynamics.blockDuct) {
			if (block == blockDuct)
				return true;
		}
		return false;
	}

	public boolean isInput() {

		return type == 0 || type == 2;
	}

	public boolean isOutput() {

		return type == 1;
	}

	public int getPowerLevel() {

		if (type == 1) {
			return adjustPowerLevel(powerLevel);
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

			if (tile.myGrid != null) {
				tile.myGrid.signalsUpToDate = false;
			}

			if (isOutput()) {
				tile.world().notifyBlockOfNeighborChange(
						tile.xCoord + Facing.offsetsXForSide[side],
						tile.yCoord + Facing.offsetsYForSide[side],
						tile.zCoord + Facing.offsetsZForSide[side],
						tile.getBlockType());

				for (int i = 0; i < 6; i++) {
					if (side == (i ^ 1))
						continue;

					tile.world().notifyBlockOfNeighborChange(
							tile.xCoord + Facing.offsetsXForSide[side] + Facing.offsetsXForSide[i],
							tile.yCoord + Facing.offsetsYForSide[side] + Facing.offsetsYForSide[i],
							tile.zCoord + Facing.offsetsZForSide[side] + Facing.offsetsZForSide[i],
							tile.getBlockType());
				}


			}
		}

	}

	@Override
	public void checkSignal() {

		MultiBlockGrid grid = tile.myGrid;
		if (grid == null || grid.rs == null) {
			return;
		}
		setPowerLevel(grid.rs.redstoneLevel);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public CoverHoleRender.ITransformer[] getHollowMask() {
		return CoverHoleRender.hollowDuctTile;
	}

	@Override
	public boolean respondsToSignallum() {

		return true;
	}

	@Override
	public void writeToNBT(NBTTagCompound tag) {

		super.writeToNBT(tag);
		tag.setByte("type", type);
		tag.setByte("invert", invert);
		tag.setByte("threshold", threshold);
		tag.setByte("power", (byte) powerLevel);
	}

	@Override
	public void readFromNBT(NBTTagCompound tag) {

		super.readFromNBT(tag);
		this.type = tag.getByte("type");
		if (tag.hasKey("invert", 1)) {
			setInvert(tag.getByte("invert"));
		}
		if (tag.hasKey("threshold", 1)) {
			setThreshold(tag.getByte("threshold"));
		}
		powerLevel = tag.getByte("power");
	}

	@Override
	public void addDescriptionToPacket(PacketCoFHBase packet) {

		packet.addByte(type);
	}

	@Override
	public void getDescriptionFromPacket(PacketCoFHBase packet) {

		this.type = packet.getByte();
	}

	@Override
	public boolean openGui(EntityPlayer player) {

		if (ServerHelper.isClientWorld(tile.world())) {
			return true;
		}

		PacketHandler.sendTo(getPacket(), player);
		player.openGui(ThermalDynamics.instance, GuiHandler.TILE_ATTACHMENT_ID + this.side, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
		return true;
	}

	@Override
	public boolean shouldRSConnect() {

		return true;
	}

	@Override
	public boolean canAddToTile(TileTDBase tileMultiBlock) {

		return !tileMultiBlock.getDuctType().isLargeTube();
	}

	public void setInvert(byte invert) {

		this.invert = invert;
	}

	public void setThreshold(byte threshold) {

		this.threshold = threshold;
	}

	@Override
	public boolean openConfigGui(IBlockAccess world, int x, int y, int z, ForgeDirection side, EntityPlayer player) {

		return true;
	}

	@Override
	public Object getGuiClient(InventoryPlayer inventory) {

		return new GuiRelay(this);
	}

	@Override
	public Object getGuiServer(InventoryPlayer inventory) {

		return new ContainerRelay(this);
	}

	public void sendUpdatePacket() {

		PacketHandler.sendToServer(getPacket());
	}

	public PacketTileInfo getPacket() {

		PacketTileInfo pkt = getNewPacket();
		pkt.addByte(type);
		pkt.addByte(threshold);
		pkt.addByte(invert);
		return pkt;
	}

	@Override
	public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {

		super.handleInfoPacket(payload, isServer, thePlayer);
		byte prevType = type;
		type = payload.getByte();
		threshold = payload.getByte();
		invert = payload.getByte();

		if (isServer) {
			tile.world().notifyBlocksOfNeighborChange(tile.xCoord, tile.yCoord, tile.zCoord, tile.getBlockType());
			onNeighborChange();
			if (type != prevType && tile.myGrid != null) {
				tile.myGrid.resetRelays();
			}

		}

		tile.world().markBlockForUpdate(tile.x(), tile.y(), tile.z());

	}

	@Override
	public void sendGuiNetworkData(Container container, List player, boolean newGuy) {

		super.sendGuiNetworkData(container, player, newGuy);

		if (newGuy)
			for (Object p : player) {
				if (p instanceof EntityPlayer)
					PacketHandler.sendTo(getPacket(), (EntityPlayer) p);
			}

	}

	@Override
	public String getDataType() {
		return "RedstoneRelay";
	}

	@Override
	public void readPortableData(EntityPlayer player, NBTTagCompound tag) {
		readFromNBT(tag);

		tile.world().notifyBlocksOfNeighborChange(tile.xCoord, tile.yCoord, tile.zCoord, tile.getBlockType());
		onNeighborChange();
		if (tile.myGrid != null) {
			tile.myGrid.resetRelays();
		}

		onNeighborChange();
		tile.world().markBlockForUpdate(tile.x(), tile.y(), tile.z());
	}

	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {
		writeToNBT(tag);
	}
}
