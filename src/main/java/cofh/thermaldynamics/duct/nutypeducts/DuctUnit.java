package cofh.thermaldynamics.duct.nutypeducts;

import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketTileInfo;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.ISingleTick;
import cofh.thermaldynamics.multiblock.MultiBlockFormer;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.ArrayList;

public abstract class DuctUnit<T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> implements IGridTile<T, G>, ISingleTick {

	public final TileGrid parent;
	@SuppressWarnings("unchecked")
	public final C[] tileCaches = createTileCaches();

	protected abstract C[] createTileCaches();

	@SuppressWarnings("unchecked")
	public final T[] pipeCache = createPipeCache();

	protected abstract T[] createPipeCache();

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
				builder.append(EnumFacing.values()[i].toString().substring(0, 1));
			}
		}
		builder.append("]");
		return builder.toString();
	}

	@Override
	public String toString() {
		return "Duct{"
//				+ duct.unlocalizedName + ","
				+ getClass().getSimpleName() + ","
//				+ getToken() + ",t="
				+ getSideArrayNonNull(tileCaches) + ",p="
				+ getSideArrayNonNull(pipeCache)
				+ "}";
	}

	@Nonnull
	public PacketTileInfo getPacketTileInfo() {
		return PacketTileInfo.newPacket(parent);
	}

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

		return pipeCache[side];
	}

	public void handleTileSideUpdate(@Nullable TileEntity tile, @Nullable IDuctHolder holder, byte side, @Nonnull cofh.thermaldynamics.duct.ConnectionType type) {
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

		if (holder != null && !holder.isSideBlocked(side ^ 1)) {
			DuctUnit<T, G, C> adjDuct = holder.getDuct(getToken());
			if (adjDuct != null && canConnectToOtherDuct(adjDuct, side) && adjDuct.canConnectToOtherDuct(this, side)) {
				pipeCache[side] = adjDuct.cast();
				return;
			}
		}

		loadSignificantCache(tile, side);
	}

	public abstract boolean canConnectToOtherDuct(DuctUnit<T, G, C> adjDuct, byte side);

	protected void setSideToNone(byte side) {

		pipeCache[side] = null;
		clearCache(side);
	}

	public boolean loadSignificantCache(TileEntity tile, byte side) {

		if (tile == null) {
			tileCaches[side] = null;
			return false;
		}

		C c = cacheTile(tile, side);
		if (c != null) {
			tileCaches[side] = c;
			if (isNode(c)) {
				nodeMask |= (1 << side);
			}

			return true;
		} else {
			if (isInputTile(tile, side)) {
				inputMask |= (1 << side);
				nodeMask |= (1 << side);
			}

			tileCaches[side] = null;
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

		tileCaches[side] = null;
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

		boolean nodeState = nodeMask == 0;
		handleTileSideUpdate(tile, holder, side, parent.getConnectionType(side));
		if (nodeMask == 0 != nodeState && grid != null) {
			grid.addBlock(this.cast());
		}
	}

	public void updateAllSides(TileEntity[] tiles, IDuctHolder[] holders) {

		boolean nodeState = nodeMask == 0;
		nodeMask = 0;
		for (byte side = 0; side < 6; side++) {
			handleTileSideUpdate(tiles[side], holders[side], side, parent.getConnectionType(side));
		}
		if (nodeMask == 0 != nodeState && grid != null) {
			grid.addBlock(this.cast());
		}
	}

	@SuppressWarnings("unchecked")
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
	public boolean isBlockedSide(int side) {

		return parent.isSideBlocked(side);
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

		return pipeCache[side] != null;
	}

	@Override
	public void onNeighborBlockChange() {

		parent.onNeighborBlockChange();
	}

	public void tileUnloading() {

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
			if (tileCaches[a] != null) {
				return a;
			}
		}
		for (byte a = 0; a < start; a++) {
			if (tileCaches[a] != null) {
				return a;
			}
		}
		return 0;
	}

	public void onConnectionRejected(int i) {

		pipeCache[i] = null;
	}

	@Override
	public boolean existsYet() {
		World world = parent.getWorld();
		return world != null
				&& world.isBlockLoaded(parent.getPos())
				&& world.getTileEntity(parent.getPos()) == parent;
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

		onNeighborBlockChange();

		formGrid();
	}

	@Nonnull
	protected BlockDuct.ConnectionType getConnectionTypeTile(C cacheValue, int side) {
		return BlockDuct.ConnectionType.TILE_CONNECTION;
	}

	@Nonnull
	public BlockDuct.ConnectionType getRenderConnectionType(int side) {
		if (tileCaches[side] != null) {
			return getConnectionTypeTile(tileCaches[side], side);
		} else if (pipeCache[side] != null) {
			return getConnectionTypeDuct(pipeCache[side], side);
		}
		return BlockDuct.ConnectionType.NONE;
	}

	@Nonnull
	protected BlockDuct.ConnectionType getConnectionTypeDuct(T duct, int side) {
		return BlockDuct.ConnectionType.DUCT;
	}

	public int getLightValue() {
		return 0;
	}

	public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {

	}

	public void writeToTilePacket(PacketCoFHBase payload) {

	}

	public void handleTilePacket(PacketCoFHBase payload) {

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

	@SideOnly(Side.CLIENT)
	public TextureAtlasSprite getBaseIcon() {
		return getDuctType().iconBaseTexture;
	}

	public boolean isOutput(int side) {
		return tileCaches[side] != null;
	}

	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return false;
	}

	public <CAP> CAP getCapability(Capability<CAP> capability, EnumFacing facing) {
		return null;
	}
}
