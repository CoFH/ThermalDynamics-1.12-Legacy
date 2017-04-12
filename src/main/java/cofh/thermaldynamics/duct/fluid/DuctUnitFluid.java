package cofh.thermaldynamics.duct.fluid;

import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.util.BlockUtils;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.ConnectionType;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.attachments.filter.IFilterAttachment;
import cofh.thermaldynamics.duct.attachments.filter.IFilterFluid;
import cofh.thermaldynamics.duct.fluid.FluidGrid.FluidRenderType;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.IDuctHolder;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.common.capabilities.Capability;
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

	public byte internalSideCounter;
	public FluidStack mySavedFluid;
	public FluidStack myRenderFluid;
	public FluidStack fluidForGrid;
	public FluidStack myConnectionFluid;

	public DuctUnitFluid(TileGrid parent, Duct duct) {
		super(parent, duct);
	}

	@Override
	protected Cache[] createTileCaches() {
		return new Cache[6];
	}

	@Override
	protected DuctUnitFluid[] createPipeCache() {
		return new DuctUnitFluid[6];
	}

	@Override
	public boolean tickPass(int pass) {

		if (!super.tickPass(pass)) {
			return false;
		}

		for (Attachment attachment : parent.getTickingAttachments(DuctToken.FLUID)) {
			attachment.tick(pass);
		}

		if (grid == null) {
			return true;
		}
		if (pass == 0) {
			int available = grid.toDistribute;
			int sent = 0;

			for (int i = this.internalSideCounter; i < 6 && sent < available; i++) {
				sent += transfer(i, available - sent, false, grid.myTank.getFluid(), true);

				if (sent >= available) {
					internalSideCounter = this.tickInternalSideCounter(i + 1);
					break;
				}

			}
			for (int i = 0; i < this.internalSideCounter && sent < available; i++) {
				sent += transfer(i, available - sent, false, grid.myTank.getFluid(), true);

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
					tempFluid = grid.myTank.drain(amountSent, true);
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

		this.fluidForGrid = fluidForGrid;
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
		return grid == null ? myConnectionFluid : grid.getFluid();
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

	@Nonnull
	@Override
	public DuctToken<DuctUnitFluid, FluidGrid, Cache> getToken() {
		return DuctToken.FLUID;
	}

	@Override
	public FluidGrid createGrid() {
		return new FluidGrid(world());
	}

	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitFluid, FluidGrid, Cache> adjDuct, byte side, byte oppositeSide) {
		DuctUnitFluid ductUnitFluid = adjDuct.cast();
		FluidStack myFluid = getConnectionFluid();
		if (myFluid != null) {
			FluidStack connectionFluid = ductUnitFluid.getConnectionFluid();
			if (connectionFluid != null) {
				if (!myFluid.isFluidEqual(connectionFluid)) {
					return false;
				}
			}
		}

		return true;
	}


	@Nullable
	@Override
	public Cache cacheTile(@Nonnull TileEntity tile, byte side) {
		if (tile.hasCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, EnumFacing.values()[side ^ 1])) {
			Attachment attachment = parent.getAttachment(side);
			IFilterFluid filter;
			if (attachment instanceof IFilterAttachment) {
				filter = ((IFilterAttachment) attachment).getFluidFilter();
			} else {
				filter = IFilterFluid.nullFilter;
			}
			return new Cache(tile, filter);
		}

		return null;
	}

	@Override
	public void tileUnloading() {

		if (mySavedFluid != null && grid != null) {
			grid.myTank.drain(mySavedFluid.amount, true);
		}
	}

	public int getRenderFluidLevel() {

		if (myRenderFluid != null) {
			return myRenderFluid.amount;
		} else if (grid == null) {
			if (myConnectionFluid != null) {
				return myConnectionFluid.amount;
			}
		} else {
			return grid.getRenderLevel();
		}
		return 0;
	}

	@Override
	public void writeToTilePacket(PacketCoFHBase payload) {
		if (grid != null) {
			payload.addFluidStack(grid.getRenderFluid());
		} else {
			payload.addFluidStack(myConnectionFluid);
		}
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload) {

		myRenderFluid = payload.getFluidStack();
	}

	public void sendRenderPacket() {

		if (grid == null) {
			return;
		}
		if (!getDuctType().opaque) {
			updateLighting();

			PacketTileInfo myPayload = newPacketTileInfo();
			myPayload.addByte(TileFluidPackets.UPDATE_RENDER);
			myPayload.addFluidStack(grid.getRenderFluid());
			PacketHandler.sendToAllAround(myPayload, parent);
		}
	}

	public IFluidHandler getFluidCapability(final EnumFacing from) {
		return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(new IFluidHandler() {

			@Override
			public IFluidTankProperties[] getTankProperties() {

				FluidStack info = grid != null ? grid.myTank.getInfo().fluid : null;
				int capacity = grid != null ? grid.myTank.getInfo().capacity : 0;
				return new IFluidTankProperties[]{new FluidTankProperties(info, capacity, isOpen(from), isOpen(from))};
			}

			@Override
			public int fill(FluidStack resource, boolean doFill) {

				if (isOpen(from) && matchesFilter(from, resource)) {
					return grid.myTank.fill(resource, doFill);
				}
				return 0;
			}

			@Nullable
			@Override
			public FluidStack drain(FluidStack resource, boolean doDrain) {

				if (isOpen(from)) {
					return grid.myTank.drain(resource, doDrain);
				}
				return null;
			}

			@Nullable
			@Override
			public FluidStack drain(int maxDrain, boolean doDrain) {

				if (isOpen(from)) {
					return grid.myTank.drain(maxDrain, doDrain);
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

		return grid != null && (from == null || ((isOutput(from.ordinal()) || isInput(from.ordinal())) && parent.getConnectionType(from.ordinal()).allowTransfer));
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		if (grid != null && grid.hasValidFluid()) {
			mySavedFluid = grid.getNodeShare(this);
			if (mySavedFluid != null) {
				mySavedFluid.writeToNBT(nbt);
			}

			nbt.setTag("ConnFluid", new NBTTagCompound());
			myConnectionFluid = grid.getConnectionFluid();
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

	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {
		return capability == CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {
		return CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY.cast(this.getFluidCapability(facing));
	}

	@Override
	public void onPlaced(EntityLivingBase living, ItemStack stack) {
		if (ServerHelper.isClientWorld(world())) return;

		EnumFacing placingSide = null;
		FluidStack fluidStack = null;

		if (living instanceof EntityPlayer) {
			EntityPlayer player = (EntityPlayer) living;
			RayTraceResult retrace;
			try {
				BlockDuct.IGNORE_RAY_TRACE.set(pos());
				retrace = RayTracer.retrace(player, false);
			} finally {
				BlockDuct.IGNORE_RAY_TRACE.set(null);
			}

			if (retrace != null && retrace.sideHit != null) {
				EnumFacing sideHit = retrace.sideHit.getOpposite();
				DuctUnitFluid fluids = IDuctHolder.getTokenFromTile(world().getTileEntity(pos().offset(sideHit)), DuctToken.FLUID);
				if (fluids != null) {
					fluidStack = fluids.getConnectionFluid();
				}
			}
		}

		for (EnumFacing facing : EnumFacing.values()) {
			TileEntity tileEntity = world().getTileEntity(pos().offset(facing));
			DuctUnitFluid fluids = IDuctHolder.getTokenFromTile(tileEntity, DuctToken.FLUID);
			if (fluids != null) {
				FluidStack connectionFluid = fluids.getConnectionFluid();
				if (fluidStack == null) {
					fluidStack = connectionFluid;
				} else if (connectionFluid != null && !fluidStack.isFluidEqual(connectionFluid)) {
					parent.setConnectionType(facing.ordinal(), ConnectionType.BLOCKED);
					((TileGrid) tileEntity).setConnectionType(facing.ordinal() ^ 1, ConnectionType.BLOCKED);
					((TileGrid) tileEntity).callBlockUpdate();
				}
			}
		}
	}

	public int[] getRenderFluidConnections() {
		int[] connections = new int[6];
		for (int i = 0; i < 6; i++) {
			connections[i] = parent.getVisualConnectionType(i).ordinal();
		}
		return connections;
	}

	public static class Cache {
		public TileEntity tile;
		public IFilterFluid filter;

		public Cache(TileEntity tile, @Nonnull IFilterFluid filter) {
			this.tile = tile;
			this.filter = filter;
		}

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
