package cofh.thermaldynamics.duct.tiles;

import cofh.core.network.PacketBase;
import cofh.core.network.PacketTileInfo;
import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.ConnectionType;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.ISingleTick;
import cofh.thermaldynamics.multiblock.MultiBlockFormer;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import com.google.common.collect.ImmutableList;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;
import java.util.Collection;

public abstract class DuctUnit<T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> implements IGridTile<T, G>, ISingleTick {

	public final TileGrid parent;
	@SuppressWarnings ("unchecked")
	public final C[] tileCache = createTileCache();
	@SuppressWarnings ("unchecked")
	public final T[] ductCache = createDuctCache();
	final Duct duct;
	@Nullable
	protected G grid;
	protected byte nodeMask;
	protected byte inputMask;
	private boolean isValidForForming = true;

	public DuctUnit(TileGrid parent, Duct duct) {

		this.parent = parent;
		this.duct = duct;
	}

	public static String getSideArrayNonNull(Object[] array) {

		StringBuilder builder = new StringBuilder("[");
		for (int i = 0; i < 6; i++) {
			if (array[i] != null) {
				builder.append(EnumFacing.VALUES[i].toString().substring(0, 1));
			}
		}
		builder.append("]");
		return builder.toString();
	}

	protected abstract C[] createTileCache();

	protected abstract T[] createDuctCache();

	@Override
	public String toString() {

		return "Duct{" + getClass().getSimpleName() + "," + getSideArrayNonNull(tileCache) + ",d=" + getSideArrayNonNull(ductCache) + "}";
	}

	@Nonnull
	public abstract DuctToken<T, G, C> getToken();

	@Nullable
	public G getGrid() {

		return grid;
	}

	@Override
	public void setGrid(@Nullable G newGrid) {

		grid = newGrid;
	}

	public abstract G createGrid();

	public void setInvalidForForming() {

		isValidForForming = false;
	}

	public void setValidForForming() {

		isValidForForming = true;
	}

	public boolean isValidForForming() {

		return isValidForForming;
	}

	public T getConnectedSide(int side) {

		return ductCache[side];
	}

	public void handleTileSideUpdate(@Nullable TileEntity tile, @Nullable IDuctHolder holder, byte side, @Nonnull ConnectionType type) {

		handleTileSideUpdate(tile, holder, side, type, (byte) (side ^ 1));
	}

	protected void handleTileSideUpdate(@Nullable TileEntity tile, @Nullable IDuctHolder holder, byte side, @Nonnull ConnectionType type, byte oppositeSide) {

		nodeMask &= ~(1 << side);
		inputMask &= ~(1 << side);

		setSideToNone(side);

		if (tile == null || !type.allowTransfer) {
			if (isInputTile(tile, side)) {
				inputMask |= (1 << side);
				nodeMask |= (1 << side);
			}
			return;
		}
		if (holder != null && !holder.isSideBlocked(oppositeSide)) {
			DuctUnit<T, G, C> adjDuct = holder.getDuct(getToken());
			if (adjDuct != null && canConnectToOtherDuct(adjDuct, side, oppositeSide) && adjDuct.canConnectToOtherDuct(this, oppositeSide, side)) {
				ductCache[side] = adjDuct.cast();
				return;
			}
		}
		loadSignificantCache(tile, side);
	}

	public abstract boolean canConnectToOtherDuct(DuctUnit<T, G, C> adjDuct, byte side, byte oppositeSide);

	protected void setSideToNone(byte side) {

		ductCache[side] = null;
		clearCache(side);
	}

	public boolean loadSignificantCache(TileEntity tile, byte side) {

		if (tile == null) {
			tileCache[side] = null;
			return false;
		}

		C c = cacheTile(tile, side);
		if (c != null) {
			tileCache[side] = c;
			if (isNode(c)) {
				nodeMask |= (1 << side);
			}
			return true;
		} else {
			if (isInputTile(tile, side)) {
				inputMask |= (1 << side);
				nodeMask |= (1 << side);
			}
			tileCache[side] = null;
			return false;
		}
	}

	public boolean isInputTile(@Nullable TileEntity tile, byte side) {

		return false;
	}

	public boolean isInput(int side) {

		return (inputMask & (1 << side)) != 0;
	}

	@Nullable
	public abstract C cacheTile(@Nonnull TileEntity tile, byte side);

	public boolean isNode(C cache) {

		return true;
	}

	public void clearCache(byte side) {

		tileCache[side] = null;
	}

	@OverridingMethodsMustInvokeSuper
	public void onChunkUnload() {

		if (grid != null) {
			grid.removeBlock(this.cast());
		}
	}

	@OverridingMethodsMustInvokeSuper
	public void invalidate() {

		if (grid != null) {
			grid.removeBlock(this.cast());
		}
	}

	public void updateSide(TileEntity tile, IDuctHolder holder, byte side) {

		int nodeState = nodeMask;
		handleTileSideUpdate(tile, holder, side, parent.getConnectionType(side));
		if (grid != null && nodeMask != nodeState) {
			if (nodeMask == 0 || nodeState == 0) { //changed from node to idle or vise versa
				grid.addBlock(this.cast());
			} else {
				grid.onMinorGridChange();
			}
		}
	}

	public void updateAllSides(TileEntity[] tiles, IDuctHolder[] holders) {

		int nodeState = nodeMask;
		nodeMask = 0;
		for (byte side = 0; side < 6; side++) {
			handleTileSideUpdate(tiles[side], holders[side], side, parent.getConnectionType(side));
		}
		if (grid != null && nodeMask != nodeState) {
			if (nodeMask == 0 || nodeState == 0) { //changed from node to idle or vise versa
				grid.addBlock(this.cast());
			} else {
				grid.onMinorGridChange();
			}
		}
	}

	@SuppressWarnings ("unchecked")
	public final T cast() {

		return (T) this;
	}

	public void formGrid() {

		if (grid == null) {
			MultiBlockFormer<T, G, C> multiBlockFormer = new MultiBlockFormer<>();
			multiBlockFormer.formGrid(this.cast());
		}
	}

	@Override
	public World world() {

		return parent.getWorld();
	}

	@Override
	public BlockPos pos() {

		return parent.getPos();
	}

	@Override
	public boolean isNode() {

		return nodeMask != 0;
	}

	@Override
	public boolean isSideConnected(byte side) {

		return ductCache[side] != null;
	}

	@Override
	public void onNeighborBlockChange() {

		parent.onNeighborBlockChange();
	}

	@OverridingMethodsMustInvokeSuper
	public boolean tickPass(int pass) {

		return !parent.checkForChunkUnload();
	}

	public void readFromNBT(NBTTagCompound nbt) {

	}

	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		return nbt;
	}

	@Nullable
	public NBTTagCompound saveToNBT() {

		NBTTagCompound nbt = new NBTTagCompound();
		nbt = writeToNBT(nbt);
		return nbt;
	}

	public byte tickInternalSideCounter(int start) {

		for (byte a = (byte) start; a < 6; a++) {
			if (tileCache[a] != null) {
				return a;
			}
		}
		for (byte a = 0; a < start; a++) {
			if (tileCache[a] != null) {
				return a;
			}
		}
		return 0;
	}

	public void onConnectionRejected(int i) {

		ductCache[i] = null;
		parent.callBlockUpdate();
	}

	@Override
	public boolean existsYet() {

		World world = parent.getWorld();
		return world != null && world.isBlockLoaded(parent.getPos()) && world.getTileEntity(parent.getPos()) == parent;
	}

	@Override
	public boolean isOutdated() {

		return parent.isInvalid();
	}

	@Override
	public void singleTick() {

		if (parent.isInvalid()) {
			return;
		}
		formGrid();
	}

	@Nonnull
	public BlockDuct.ConnectionType getRenderConnectionType(int side) {

		if (tileCache[side] != null) {
			return getConnectionTypeTile(tileCache[side], side);
		} else if (ductCache[side] != null) {
			return getConnectionTypeDuct(ductCache[side], side);
		} else if (isInput(side)) {
			return getInputConnection(side);
		}
		return BlockDuct.ConnectionType.NONE;
	}

	@Nonnull
	public BlockDuct.ConnectionType getInputConnection(int side) {

		return BlockDuct.ConnectionType.TILE_CONNECTION;
	}

	@Nonnull
	protected BlockDuct.ConnectionType getConnectionTypeTile(C cacheValue, int side) {

		return BlockDuct.ConnectionType.TILE_CONNECTION;
	}

	@Nonnull
	protected BlockDuct.ConnectionType getConnectionTypeDuct(T duct, int side) {

		return BlockDuct.ConnectionType.DUCT;
	}

	public int getLightValue() {

		return 0;
	}

	public void handleInfoPacket(PacketBase payload, boolean isServer, EntityPlayer thePlayer) {

	}

	public void writeToTilePacket(PacketBase payload) {

	}

	public void handleTilePacket(PacketBase payload) {

	}

	public void onPlaced(EntityLivingBase living, ItemStack stack) {

	}

	public void randomDisplayTick() {

	}

	public boolean shouldRenderInPass(int pass) {

		return false;
	}

	public void dropAdditional(ArrayList<ItemStack> ret) {

	}

	public PacketTileInfo newPacketTileInfo() {

		PacketTileInfo packet = PacketTileInfo.newPacket(parent);
		packet.addByte(0);
		packet.addByte(getToken().getId());
		return packet;
	}

	public ItemStack addNBTToItemStackDrop(ItemStack stack) {

		return stack;
	}

	public Duct getDuctType() {

		return duct;
	}

	@SideOnly (Side.CLIENT)
	public TextureAtlasSprite getBaseIcon() {

		return getDuctType().iconBaseTexture;
	}

	public boolean isOutput(int side) {

		return tileCache[side] != null;
	}

	public boolean onWrench(EntityPlayer player, int side, RayTraceResult rayTrace) {

		return false;
	}

	public boolean openGui(EntityPlayer player) {

		return false;
	}

	public Object getGuiClient(InventoryPlayer inventory) {

		return null;
	}

	public Object getGuiServer(InventoryPlayer inventory) {

		return null;
	}

	public Object getConfigGuiServer(InventoryPlayer inventory) {

		return null;
	}

	public Object getConfigGuiClient(InventoryPlayer inventory) {

		return null;
	}

	public boolean shouldTrackChunk(byte i) {

		return isOutput(i) || isInput(i);
	}

	@Nonnull
	public Collection<BlockPos> getAdditionalImportantPositions() {

		return ImmutableList.of();
	}

	/* CAPABILITIES */
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {

		return false;
	}

	public <CAP> CAP getCapability(Capability<CAP> capability, EnumFacing facing) {

		return null;
	}

}
