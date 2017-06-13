package cofh.thermaldynamics.duct.attachments.relay;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Cuboid6;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import cofh.api.block.IBlockConfigGui;
import cofh.api.core.IPortableData;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.BlockHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.AttachmentRegistry;
import cofh.thermaldynamics.duct.DuctUnitStructural;
import cofh.thermaldynamics.duct.GridStructural;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleRender;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import cofh.thermaldynamics.gui.GuiHandler;
import cofh.thermaldynamics.gui.client.GuiRelay;
import cofh.thermaldynamics.gui.container.ContainerRelay;
import cofh.thermaldynamics.init.TDBlocks;
import cofh.thermaldynamics.init.TDItems;
import cofh.thermaldynamics.init.TDTextures;
import cofh.thermaldynamics.render.RenderDuct;
import net.minecraft.block.Block;
import net.minecraft.block.BlockRedstoneWire;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IContainerListener;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import java.util.LinkedList;
import java.util.List;

public class Relay extends Attachment implements IBlockConfigGui, IPortableData {

	public final DuctUnitStructural structureUnit;
	public byte type = 0;
	public int powerLevel;
	public byte invert = 0;
	public byte threshold = 0;

	public Relay(TileGrid tile, byte side) {

		super(tile, side);
		structureUnit = tile.getDuct(DuctToken.STRUCTURAL);
	}

	public Relay(TileGrid tile, byte side, int type) {

		super(tile, side);
		this.type = (byte) type;
		structureUnit = tile.getDuct(DuctToken.STRUCTURAL);
	}

	public static boolean isBlockDuct(Block block) {

		for (BlockDuct blockDuct : TDBlocks.blockDuct) {
			if (block == blockDuct) {
				return true;
			}
		}
		return false;
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

		return TileGrid.subSelection[side].copy();
	}

	@Nonnull
	@Override
	public BlockDuct.ConnectionType getNeighborType() {

		return BlockDuct.ConnectionType.STRUCTURE_CONNECTION;
	}

	public BlockDuct.ConnectionType getRenderConnectionType() {

		return BlockDuct.ConnectionType.DUCT;
	}

	@Override
	public boolean isNode() {

		return true;
	}

	@Override
	public boolean render(IBlockAccess world, BlockRenderLayer layer, CCRenderState ccRenderState) {

		if (layer != BlockRenderLayer.SOLID) {
			return false;
		}

		Translation trans = Vector3.fromTileCenter(baseTile).translation();
		RenderDuct.modelConnection[1 + (type & 1)][side].render(ccRenderState, trans, new IconTransformation(TDTextures.SIGNALLER));
		return true;
	}

	@Override
	public ItemStack getPickBlock() {

		return new ItemStack(TDItems.itemRelay);
	}

	@Override
	public List<ItemStack> getDrops() {

		LinkedList<ItemStack> drops = new LinkedList<>();
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

		EnumFacing side = EnumFacing.VALUES[this.side];
		BlockPos offsetPos = baseTile.getPos().offset(side);
		int level = 0;

		IBlockState state = baseTile.getWorld().getBlockState(offsetPos);
		Block block = state.getBlock();

		if (type == 0) { // should calc vanilla redstone level
			if (isBlockDuct(block)) {
				TileGrid t = (TileGrid) baseTile.world().getTileEntity(offsetPos);
				Attachment attachment = t.getAttachment(this.side ^ 1);
				if (attachment != null) {
					level = attachment.getRSOutput();
				}

			} else {
				level = Math.max(level, baseTile.world().getRedstonePower(offsetPos, side));

				if (block == Blocks.REDSTONE_WIRE) {
					level = Math.max(level, state.getValue(BlockRedstoneWire.POWER));
				}
			}
		}

		if (type == 2) { // should calc comparator redstone level
			if (state.hasComparatorInputOverride()) {
				level = state.getComparatorInputOverride(baseTile.world(), offsetPos);
			} else if (block.isNormalCube(state, baseTile.world(), offsetPos)) {
				BlockPos offset2 = offsetPos.offset(side);

				IBlockState otherState = baseTile.getWorld().getBlockState(offset2);

				if (otherState.hasComparatorInputOverride()) {
					level = otherState.getComparatorInputOverride(baseTile.world(), offsetPos);
				}
			}
		}

		return level;
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

	public void setPowerLevel(int powerLevel) {

		if (this.powerLevel != powerLevel) {
			this.powerLevel = powerLevel;

			GridStructural grid = structureUnit.getGrid();
			if (grid != null) {
				grid.signalsUpToDate = false;
			}

			if (isOutput()) {
				BlockPos offsetPos = baseTile.getPos().offset(EnumFacing.VALUES[side]);
				baseTile.world().neighborChanged(offsetPos, baseTile.getBlockType(), baseTile.getPos());

				for (int i = 0; i < 6; i++) {
					if (side == (i ^ 1)) {
						continue;
					}
					offsetPos = baseTile.getPos().offset(EnumFacing.VALUES[side]);
					baseTile.world().neighborChanged(offsetPos, baseTile.getBlockType(), baseTile.getPos());
				}

			}
		}

	}

	@Override
	public int getRSOutput() {

		return isOutput() ? getPowerLevel() : 0;
	}

	@Override
	public void checkSignal() {

		GridStructural grid = structureUnit.getGrid();
		if (grid == null || grid.rs == null) {
			return;
		}
		setPowerLevel(grid.rs.redstoneLevel);
	}

	@Override
	@SideOnly (Side.CLIENT)
	public CoverHoleRender.ITransformer[] getHollowMask() {

		return CoverHoleRender.hollowDuctTile;
	}

	@Override
	public boolean respondsToSignalum() {

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

		if (ServerHelper.isClientWorld(baseTile.world())) {
			return true;
		}
		PacketHandler.sendTo(getPacket(), player);
		player.openGui(ThermalDynamics.instance, GuiHandler.TILE_ATTACHMENT_ID + this.side, baseTile.getWorld(), baseTile.getPos().getX(), baseTile.getPos().getY(), baseTile.getPos().getZ());
		return true;
	}

	@Override
	public boolean shouldRSConnect() {

		return true;
	}

	@Override
	public boolean canAddToTile(TileGrid tile) {

		return !tile.getDuctType().isLargeTube();
	}

	public void setInvert(byte invert) {

		this.invert = invert;
	}

	public void setThreshold(byte threshold) {

		this.threshold = threshold;
	}

	@Override
	public boolean openConfigGui(IBlockAccess world, BlockPos pos, EnumFacing side, EntityPlayer player) {

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
	public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer player) {

		super.handleInfoPacket(payload, isServer, player);
		byte prevType = type;
		type = payload.getByte();
		threshold = payload.getByte();
		invert = payload.getByte();

		if (isServer) {
			baseTile.world().notifyNeighborsOfStateChange(baseTile.getPos(), baseTile.getBlockType(), false);
			onNeighborChange();
			GridStructural grid = structureUnit.getGrid();
			if (type != prevType && grid != null) {
				grid.resetRelays();
			}

		}
		BlockHelper.callBlockUpdate(baseTile.getWorld(), baseTile.getPos());
	}

	@Override
	public void sendGuiNetworkData(Container container, List<IContainerListener> player, boolean newListener) {

		super.sendGuiNetworkData(container, player, newListener);

		if (newListener) {
			for (Object p : player) {
				if (p instanceof EntityPlayer) {
					PacketHandler.sendTo(getPacket(), (EntityPlayer) p);
				}
			}
		}
	}

	@Override
	public String getDataType() {

		return "RedstoneRelay";
	}

	@Override
	public void readPortableData(EntityPlayer player, NBTTagCompound tag) {

		readFromNBT(tag);

		baseTile.world().notifyNeighborsOfStateChange(baseTile.getPos(), baseTile.getBlockType(), false);
		onNeighborChange();
		GridStructural grid = structureUnit.getGrid();
		if (grid != null) {
			grid.resetRelays();
		}
		onNeighborChange();
		BlockHelper.callBlockUpdate(baseTile.getWorld(), baseTile.getPos());
	}

	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {

		writeToNBT(tag);
	}

}
