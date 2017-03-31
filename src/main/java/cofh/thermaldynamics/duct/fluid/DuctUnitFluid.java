package cofh.thermaldynamics.duct.fluid;

import codechicken.lib.util.BlockUtils;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.attachments.filter.IFilterFluid;
import cofh.thermaldynamics.duct.fluid.FluidGrid.FluidRenderType;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.FluidTankProperties;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.fluids.capability.IFluidTankProperties;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DuctUnitFluid extends DuctUnit<DuctUnitFluid, FluidGrid, DuctUnitFluid.Cache> {
	// the logic for this field is required to ensure lighting is propagated the full distance for all nearby ducts
	// the lighting code is incapable of handling when a bunch of adjacent blocks all update state simultaneously
	private static DuctUnitFluid lightingUpdate = null;
	public FluidGrid fluidGrid;
	public byte internalSideCounter;
	public FluidStack mySavedFluid;
	public FluidStack myRenderFluid;
	public FluidStack fluidForGrid;
	public FluidStack myConnectionFluid;

	public DuctUnitFluid(TileGrid parent, Duct duct) {
		super(parent, duct);
	}

	@Override
	public boolean tickPass(int pass) {

		if (!super.tickPass(pass)) {
			return false;
		}
		if (fluidGrid == null) {
			return true;
		}
		if (pass == 0) {
			int available = fluidGrid.toDistribute;
			int sent = 0;

			for (int i = this.internalSideCounter; i < 6 && sent < available; i++) {
				sent += transfer(i, available - sent, false, fluidGrid.myTank.getFluid(), true);

				if (sent >= available) {
					internalSideCounter = this.tickInternalSideCounter(i + 1);
					break;
				}

			}
			for (int i = 0; i < this.internalSideCounter && sent < available; i++) {
				sent += transfer(i, available - sent, false, fluidGrid.myTank.getFluid(), true);

				if (sent >= available) {
					internalSideCounter = this.tickInternalSideCounter(i + 1);
					break;
				}
			}
		}
		return true;
	}

	public int transfer(int available, boolean simulate, FluidStack base, boolean drainGridTank) {

		int sent = 0;

		for (int i = this.internalSideCounter; i < 6 && sent < available; i++) {
			sent += transfer(i, available - sent, simulate, base, drainGridTank);

			if (sent >= available) {
				internalSideCounter = this.tickInternalSideCounter(i + 1);
				break;
			}
		}
		for (int i = 0; i < this.internalSideCounter && sent < available; i++) {
			sent += transfer(i, available - sent, simulate, base, drainGridTank);

			if (sent >= available) {
				internalSideCounter = this.tickInternalSideCounter(i + 1);
				break;
			}
		}
		return sent;
	}

	public int transfer(int bSide, int available, boolean simulate, FluidStack fluid, boolean drainGridTank) {
		if (fluid == null) return 0;

		DuctUnitFluid.Cache cache = tileCaches[bSide];

		if (cache == null) {
			return 0;
		}

		EnumFacing facing = EnumFacing.values()[bSide ^ 1];
		if (!cache.tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, facing)) {
			return 0;
		}

		IFluidHandler capability = cache.getHandler(facing.ordinal());

		if (capability == null) return 0;

		if (!cache.filter.allowFluid(fluid)) {
			return 0;
		}

		FluidStack tempFluid = fluid.copy();
		tempFluid.amount = available;
		int amountSent = capability.fill(tempFluid, false);

		if (amountSent > 0) {
			if (simulate) {
				return amountSent;
			} else {
				if (drainGridTank) {
					tempFluid = fluidGrid.myTank.drain(amountSent, true);
				} else {
					tempFluid.amount = amountSent;
				}
				return capability.fill(tempFluid, true);
			}
		} else {
			return 0;
		}
	}

	@Override
	public int getLightValue() {

		if (isOpaque()) {
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

	public void updateLighting() {

		lightingUpdate = this;
		parent.updateLighting();
		lightingUpdate = null;
	}

	public void updateFluid() {

		if (!isOpaque()) {
			sendRenderPacket();
		}
	}


	public FluidStack getFluidForGrid() {

		return fluidForGrid;
	}


	public void setFluidForGrid(FluidStack fluidForGrid) {

		fluidForGrid = null;
	}


	public boolean isOpaque() {

		return getDuctType().opaque;
	}

	@Override
	public boolean shouldRenderInPass(int pass) {

		return !getDuctType().opaque && myRenderFluid != null && super.shouldRenderInPass(pass);
	}

	public FluidStack getConnectionFluid() {

		if (ServerHelper.isClientWorld(parent.getWorld())) {
			return myRenderFluid;
		}
		return fluidGrid == null ? myConnectionFluid : fluidGrid.getFluid();
	}


	public boolean canStoreFluid() {

		return true;
	}

	@Override
	public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {

		if (ServerHelper.isClientWorld(world())) {
			byte b = payload.getByte();
			handleTileInfoPacketType(payload, b);
		}
	}

	public void handleTileInfoPacketType(PacketCoFHBase payload, byte b) {

		if (b == TileFluidPackets.UPDATE_RENDER) {
			myRenderFluid = payload.getFluidStack();
			BlockUtils.fireBlockUpdate(world(), pos());
		}
	}

	@Override
	public DuctToken<DuctUnitFluid, FluidGrid, Cache> getToken() {
		return DuctToken.FLUID;
	}

	@Override
	public FluidGrid createGrid() {
		return new FluidGrid(world());
	}

	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitFluid, FluidGrid, Cache> adjDuct, byte side) {
		//TODO: SOLVE
		return false;
	}

	@Nullable
	@Override
	public Cache cacheTile(@Nonnull TileEntity tile, byte side) {
		return null;
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
	public void writeToTilePacket(PacketCoFHBase payload) {
		if (fluidGrid != null) {
			payload.addFluidStack(fluidGrid.getRenderFluid());
		} else {
			payload.addFluidStack(myConnectionFluid);
		}
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload) {

		myRenderFluid = payload.getFluidStack();
	}

	public void sendRenderPacket() {

		if (fluidGrid == null) {
			return;
		}
		if (!getDuctType().opaque) {
			updateLighting();

			PacketTileInfo myPayload = newPacketTileInfo();
			myPayload.addByte(TileFluidPackets.UPDATE_RENDER);
			myPayload.addFluidStack(fluidGrid.getRenderFluid());
			PacketHandler.sendToAllAround(myPayload, parent);
		}
	}

	public IFluidHandler getFluidCapability(final EnumFacing from) {
		return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new IFluidHandler() {

			@Override
			public IFluidTankProperties[] getTankProperties() {

				FluidStack info = fluidGrid != null ? fluidGrid.myTank.getInfo().fluid : null;
				int capacity = fluidGrid != null ? fluidGrid.myTank.getInfo().capacity : 0;
				return new IFluidTankProperties[]{new FluidTankProperties(info, capacity, isOpen(from), isOpen(from))};
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

	public boolean matchesFilter(EnumFacing from, FluidStack resource) {
		Cache cache = tileCaches[from.ordinal()];
		return from == null || cache == null || cache.filter.allowFluid(resource);
	}

	public boolean isOpen(EnumFacing from) {

		return fluidGrid != null && (from == null || ((isOutput(from.ordinal()) || isInput(from.ordinal())) && parent.getConnectionType(from.ordinal()).allowTransfer));
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

	public static class Cache {
		public TileEntity tile;
		public IFilterFluid filter;

		public IFluidHandler getHandler(int side) {
			if (tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side])) {
				return tile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.VALUES[side]);
			}
			return null;
		}

	}

	public class TileFluidPackets {

		public static final byte GUI_BUTTON = 0;
		public static final byte SET_FILTER = 1;
		public static final byte FILTERS = 2;
		public static final byte UPDATE_RENDER = 3;
		public static final byte TEMPERATURE = 4;
	}


}
