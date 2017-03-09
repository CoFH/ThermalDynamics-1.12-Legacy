package cofh.thermaldynamics.duct.fluid;

import codechicken.lib.util.BlockUtils;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.duct.TileDuctBase;
import cofh.thermaldynamics.duct.attachments.filter.IFilterAttachment;
import cofh.thermaldynamics.duct.attachments.filter.IFilterFluid;
import cofh.thermaldynamics.duct.fluid.FluidGrid.FluidRenderType;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nullable;

public class TileFluidDuct extends TileDuctBase implements IFluidDuctInternal {

	public IFluidHandler[] cache;
	public IFilterFluid[] filterCache;

	public FluidGrid fluidGrid;

	public TileFluidDuct() {

	}

	@Override
	public MultiBlockGrid createGrid() {

		return new FluidGrid(worldObj);
	}

	public FluidStack mySavedFluid;
	public FluidStack myRenderFluid;
	public FluidStack fluidForGrid;
	public FluidStack myConnectionFluid;

	@Override
	public boolean isSignificantTile(TileEntity theTile, int side) {

		if (theTile instanceof IGridTile) {
			return false;
		}
		return theTile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side]);
	}

	@Override
	public boolean tickPass(int pass) {

		if (!super.tickPass(pass)) {
			return false;
		}
		if (fluidGrid == null || !cachesExist()) {
			return true;
		}
		if (pass == 0) {
			int available = fluidGrid.toDistribute;
			int sent = 0;

			for (int i = this.internalSideCounter; i < this.neighborTypes.length && sent < available; i++) {
				sent += transfer(i, available - sent, false, fluidGrid.myTank.getFluid(), true);

				if (sent >= available) {
					this.tickInternalSideCounter(i + 1);
					break;
				}

			}
			for (int i = 0; i < this.internalSideCounter && sent < available; i++) {
				sent += transfer(i, available - sent, false, fluidGrid.myTank.getFluid(), true);

				if (sent >= available) {
					this.tickInternalSideCounter(i + 1);
					break;
				}
			}
		}
		return true;
	}

	public int transfer(int available, boolean simulate, FluidStack base, boolean drainGridTank) {

		if (!cachesExist()) {
			return 0;
		}
		int sent = 0;

		for (int i = this.internalSideCounter; i < this.neighborTypes.length && sent < available; i++) {
			sent += transfer(i, available - sent, simulate, base, drainGridTank);

			if (sent >= available) {
				this.tickInternalSideCounter(i + 1);
				break;
			}
		}
		for (int i = 0; i < this.internalSideCounter && sent < available; i++) {
			sent += transfer(i, available - sent, simulate, base, drainGridTank);

			if (sent >= available) {
				this.tickInternalSideCounter(i + 1);
				break;
			}
		}
		return sent;
	}

	public int transfer(int bSide, int available, boolean simulate, FluidStack fluid, boolean drainGridTank) {

		if (neighborTypes[bSide] != NeighborTypes.OUTPUT || connectionTypes[bSide] == ConnectionTypes.BLOCKED) {
			return 0;
		}
		if (cache[bSide] == null || fluid == null) {
			return 0;
		}
		if (!filterCache[bSide].allowFluid(fluid)) {
			return 0;
		}
		FluidStack tempFluid = fluid.copy();
		tempFluid.amount = available;
		int amountSent = cache[bSide].fill(tempFluid, false);

		if (amountSent > 0) {
			if (simulate) {
				return amountSent;
			} else {
				if (drainGridTank) {
					tempFluid = fluidGrid.myTank.drain(amountSent, true);
				} else {
					tempFluid.amount = amountSent;
				}
				return cache[bSide].fill(tempFluid, true);
			}
		} else {
			return 0;
		}
	}

	@Override
	public int getLightValue() {

		if (getDuctType().opaque) {
			return 0;
		}
		int fullEnough = FluidRenderType.FULL * 6 / 8;
		int level = Math.min(getRenderFluidLevel(), fullEnough);
		int light = FluidHelper.getFluidLuminosity(getConnectionFluid()) * level / fullEnough;
		if (lightingUpdate != null && lightingUpdate != this) {
			--light;
		}
		return light & (~light >> 31);
	}

	// the logic for this field is required to ensure lighting is propagated the full distance for all nearby ducts
	// the lighting code is incapable of handling when a bunch of adjacent blocks all update state simultaneously
	private static TileFluidDuct lightingUpdate = null;

	@Override
	public void updateLighting() {

		lightingUpdate = this;
		super.updateLighting();
		lightingUpdate = null;
	}

	public void updateFluid() {

		if (!getDuctType().opaque) {
			sendRenderPacket();
		}
	}

	@Override
	public FluidStack getFluidForGrid() {

		return fluidForGrid;
	}

	@Override
	public void setFluidForGrid(FluidStack fluidForGrid) {

		fluidForGrid = null;
	}

	@Override
	public boolean isOpaque() {

		return getDuctType().opaque;
	}

	@Override
	public boolean shouldRenderInPass(int pass) {

		return !getDuctType().opaque && myRenderFluid != null && super.shouldRenderInPass(pass);
	}

	@Override
	public boolean isConnectable(TileEntity theTile, int side) {

		return theTile instanceof TileFluidDuct;
	}

	public FluidStack getConnectionFluid() {

		if (ServerHelper.isClientWorld(worldObj)) {
			return myRenderFluid;
		}
		return fluidGrid == null ? myConnectionFluid : fluidGrid.getFluid();
	}

	@Override
	public boolean canStoreFluid() {

		return true;
	}

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		fluidGrid = (FluidGrid) newGrid;
	}

	@Override
	public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {

		if (ServerHelper.isClientWorld(worldObj)) {
			byte b = payload.getByte();
			handleTileInfoPacketType(payload, b);
		}
	}

	@Override
	public boolean cachesExist() {

		return cache != null;
	}

	@Override
	public void createCaches() {

		cache = new IFluidHandler[6];
		filterCache = new IFilterFluid[] { IFilterFluid.nullFilter, IFilterFluid.nullFilter, IFilterFluid.nullFilter, IFilterFluid.nullFilter, IFilterFluid.nullFilter, IFilterFluid.nullFilter };
	}

	@Override
	public void cacheImportant(TileEntity tile, int side) {

		if (attachments[side] instanceof IFilterAttachment) {
			filterCache[side] = ((IFilterAttachment) attachments[side]).getFluidFilter();
		}
		cache[side] = tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
	}

	@Override
	public void cacheInputTile(TileEntity theTile, int side) {

		if (attachments[side] instanceof IFilterAttachment) {
			filterCache[side] = ((IFilterAttachment) attachments[side]).getFluidFilter();
		}
	}

	@Override
	public void clearCache(int side) {

		filterCache[side] = IFilterFluid.nullFilter;
		cache[side] = null;
	}

	public void handleTileInfoPacketType(PacketCoFHBase payload, byte b) {

		if (b == TileFluidPackets.UPDATE_RENDER) {
			myRenderFluid = payload.getFluidStack();
			BlockUtils.fireBlockUpdate(world(), getPos());
		}
	}

	@Override
	public void tileUnloading() {

		if (mySavedFluid != null && fluidGrid != null) {
			fluidGrid.myTank.drain(mySavedFluid.amount, true);
		}
	}

	public int getRenderFluidLevel() {

		if (myRenderFluid != null) {
			return myRenderFluid.amount;
		} else if (fluidGrid == null) {
			if (myConnectionFluid != null) {
				return myConnectionFluid.amount;
			}
		} else {
			return fluidGrid.getRenderLevel();
		}
		return 0;
	}

	@Override
	public PacketCoFHBase getTilePacket() {

		PacketCoFHBase packet = super.getTilePacket();
		if (fluidGrid != null) {
			packet.addFluidStack(fluidGrid.getRenderFluid());
		} else {
			packet.addFluidStack(myConnectionFluid);
		}
		return packet;
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);
		myRenderFluid = payload.getFluidStack();
	}

	public void sendRenderPacket() {

		if (fluidGrid == null) {
			return;
		}
		if (!getDuctType().opaque) {
			updateLighting();

			PacketTileInfo myPayload = PacketTileInfo.newPacket(this);
			myPayload.addByte(0);
			myPayload.addByte(TileFluidPackets.UPDATE_RENDER);
			myPayload.addFluidStack(fluidGrid.getRenderFluid());
			PacketHandler.sendToAllAround(myPayload, this);
		}
	}

	public class TileFluidPackets {

		public static final byte GUI_BUTTON = 0;
		public static final byte SET_FILTER = 1;
		public static final byte FILTERS = 2;
		public static final byte UPDATE_RENDER = 3;
		public static final byte TEMPERATURE = 4;
	}

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {

		return super.hasCapability(capability, facing) || capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, final EnumFacing from) {

		if (capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY) {
			return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new IFluidHandler() {

				@Override
				public IFluidTankProperties[] getTankProperties() {

					FluidStack info = fluidGrid != null ? fluidGrid.myTank.getInfo().fluid : null;
					int capacity = fluidGrid != null ? fluidGrid.myTank.getInfo().capacity : 0;
					return new IFluidTankProperties[] { new FluidTankProperties(info, capacity, isOpen(from), isOpen(from)) };
				}

				@Override
				public int fill(FluidStack resource, boolean doFill) {

					if (isOpen(from) && matchesFilter(from, resource)) {
						return fluidGrid.myTank.fill(resource, doFill);
					}
					return 0;
				}

				@Nullable
				@Override
				public FluidStack drain(FluidStack resource, boolean doDrain) {

					if (isOpen(from)) {
						return fluidGrid.myTank.drain(resource, doDrain);
					}
					return null;
				}

				@Nullable
				@Override
				public FluidStack drain(int maxDrain, boolean doDrain) {

					if (isOpen(from)) {
						return fluidGrid.myTank.drain(maxDrain, doDrain);
					}
					return null;
				}
			});
		}
		return super.getCapability(capability, from);
	}

	public boolean matchesFilter(EnumFacing from, FluidStack resource) {

		return filterCache == null || from == null || filterCache[from.ordinal()].allowFluid(resource);
	}

	public boolean isOpen(EnumFacing from) {

		return fluidGrid != null && (from == null || ((neighborTypes[from.ordinal()] == NeighborTypes.OUTPUT || neighborTypes[from.ordinal()] == NeighborTypes.INPUT) && connectionTypes[from.ordinal()] != ConnectionTypes.BLOCKED));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		if (fluidGrid != null && fluidGrid.hasValidFluid()) {
			mySavedFluid = fluidGrid.getNodeShare(this);
			if (mySavedFluid != null) {
				mySavedFluid.writeToNBT(nbt);
			}

			nbt.setTag("ConnFluid", new NBTTagCompound());
			myConnectionFluid = fluidGrid.getConnectionFluid();
			myConnectionFluid.writeToNBT(nbt.getCompoundTag("ConnFluid"));
		} else {
			mySavedFluid = null;
			myConnectionFluid = null;
		}
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		fluidForGrid = FluidStack.loadFluidStackFromNBT(nbt);
		if (nbt.hasKey("ConnFluid")) {
			myConnectionFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("ConnFluid"));
		}
	}

}
