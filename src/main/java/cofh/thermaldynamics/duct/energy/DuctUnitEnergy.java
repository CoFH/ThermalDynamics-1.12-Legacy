package cofh.thermaldynamics.duct.energy;

import cofh.redstoneflux.api.IEnergyHandler;
import cofh.redstoneflux.api.IEnergyProvider;
import cofh.redstoneflux.api.IEnergyReceiver;
import cofh.thermaldynamics.duct.ConnectionType;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.tiles.*;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DuctUnitEnergy extends DuctUnit<DuctUnitEnergy, GridEnergy, IEnergyReceiver> implements cofh.redstoneflux.api.IEnergyStorage {

	public int energyForGrid = 0;
	public int lastStoredValue = 0;
	byte internalSideCounter;
	private int transferLimit;
	private int capacity;

	public DuctUnitEnergy(TileGrid parent, Duct duct, int transferLimit, int capacity) {

		super(parent, duct);
		this.transferLimit = transferLimit;
		this.capacity = capacity;
	}

	public DuctUnitEnergy(TileDuctEnergy parent, Duct duct) {

		super(parent, duct);
		transferLimit = GridEnergy.NODE_TRANSFER[duct.type];
		capacity = GridEnergy.NODE_STORAGE[duct.type];
	}

	@Override
	protected IEnergyReceiver[] createTileCache() {

		return new Cache[6];
	}

	@Override
	protected DuctUnitEnergy[] createDuctCache() {

		return new DuctUnitEnergy[6];
	}

	@Nonnull
	@Override
	public DuctToken<DuctUnitEnergy, GridEnergy, IEnergyReceiver> getToken() {

		return DuctToken.ENERGY;
	}

	@Override
	public GridEnergy createGrid() {

		return new GridEnergy(world(), getTransferLimit(), getCapacity());
	}

	@Override
	protected void handleTileSideUpdate(@Nullable TileEntity tile, @Nullable IDuctHolder holder, byte side, @Nonnull ConnectionType type, byte oppositeSide) {

		nodeMask &= ~(1 << side);
		inputMask &= ~(1 << side);

		setSideToNone(side);

		if (tile == null || !type.allowEnergy) {
			if (isInputTile(tile, side)) {
				inputMask |= (1 << side);
				nodeMask |= (1 << side);
			}
			return;
		}
		if (holder != null && !holder.isSideBlocked(oppositeSide)) {
			DuctUnit<DuctUnitEnergy, GridEnergy, IEnergyReceiver> adjDuct = holder.getDuct(getToken());
			if (adjDuct != null && canConnectToOtherDuct(adjDuct, side, oppositeSide) && adjDuct.canConnectToOtherDuct(this, oppositeSide, side)) {
				ductCache[side] = adjDuct.cast();
				return;
			}
		}
		loadSignificantCache(tile, side);
	}

	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitEnergy, GridEnergy, IEnergyReceiver> adjDuct, byte side, byte oppositeSide) {

		return getTransferLimit() == adjDuct.cast().getTransferLimit();
	}

	@Override
	@Nullable
	public Cache cacheTile(@Nonnull TileEntity tile, byte side) {

		EnumFacing facing = EnumFacing.values()[side ^ 1];

		if (tile instanceof IEnergyReceiver) {
			IEnergyReceiver receiver = (IEnergyReceiver) tile;
			if (receiver.canConnectEnergy(facing) || tile instanceof IDuctHolder) {
				return new Cache(receiver);
			}
		}
		if (tile instanceof IEnergyProvider) {
			IEnergyProvider provider = (IEnergyProvider) tile;
			if (provider.canConnectEnergy(facing) || tile instanceof IDuctHolder) {
				return new Cache(provider);
			}
		}
		if (tile.hasCapability(CapabilityEnergy.ENERGY, facing)) {
			IEnergyStorage capability = tile.getCapability(CapabilityEnergy.ENERGY, facing);
			if (capability != null) {
				if (capability.canReceive()) {
					IEnergyReceiver capReceiver = new IEnergyReceiver() {

						public IEnergyStorage getStorage(EnumFacing facing1) {

							if (tile.hasCapability(CapabilityEnergy.ENERGY, facing1)) {
								return tile.getCapability(CapabilityEnergy.ENERGY, facing1);
							}
							return null;
						}

						@Override
						public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {

							IEnergyStorage storage = getStorage(from);
							return storage == null ? 0 : storage.receiveEnergy(maxReceive, simulate);
						}

						@Override
						public int getEnergyStored(EnumFacing from) {

							IEnergyStorage storage = getStorage(from);
							return storage == null ? 0 : storage.getEnergyStored();
						}

						@Override
						public int getMaxEnergyStored(EnumFacing from) {

							IEnergyStorage storage = getStorage(from);
							return storage == null ? 0 : storage.getMaxEnergyStored();
						}

						@Override
						public boolean canConnectEnergy(EnumFacing from) {

							return true;
						}
					};
					return new Cache(capReceiver);

				} else {
					IEnergyProvider capProvider = new IEnergyProvider() {

						public IEnergyStorage getStorage(EnumFacing facing1) {

							if (tile.hasCapability(CapabilityEnergy.ENERGY, facing1)) {
								return tile.getCapability(CapabilityEnergy.ENERGY, facing1);
							}
							return null;
						}

						@Override
						public int extractEnergy(EnumFacing from, int maxReceive, boolean simulate) {

							IEnergyStorage storage = getStorage(from);
							return storage == null ? 0 : storage.extractEnergy(maxReceive, simulate);
						}

						@Override
						public int getEnergyStored(EnumFacing from) {

							IEnergyStorage storage = getStorage(from);
							return storage == null ? 0 : storage.getEnergyStored();
						}

						@Override
						public int getMaxEnergyStored(EnumFacing from) {

							IEnergyStorage storage = getStorage(from);
							return storage == null ? 0 : storage.getMaxEnergyStored();
						}

						@Override
						public boolean canConnectEnergy(EnumFacing from) {

							return true;
						}
					};
					return new Cache(capProvider);
				}
			}
		}
		return null;
	}

	@Override
	public boolean tickPass(int pass) {

		return super.tickPass(pass) && sendEnergy();

	}

	public boolean sendEnergy() {

		int power = this.grid.getSendableEnergy();
		int usedPower = transmitEnergy(power, false);

		this.grid.useEnergy(usedPower);
		return true;
	}

	public int getEnergyForGrid() {

		return energyForGrid;
	}

	public void setEnergyForGrid(int energy) {

		energyForGrid = energy;
	}

	public int transmitEnergy(int energy, boolean simulate) {

		int usedEnergy = 0;

		for (byte i = this.internalSideCounter; i < 6 && usedEnergy < energy; i++) {

			if (tileCache[i] != null) {
				IEnergyReceiver receiver = tileCache[i];
				if (receiver.canConnectEnergy(EnumFacing.VALUES[i ^ 1])) {
					usedEnergy += sendEnergy(receiver, energy - usedEnergy, i, simulate);
				}
				if (!simulate && usedEnergy >= energy) {
					internalSideCounter = tickInternalSideCounter(i + 1);
					break;
				}
			}
		}
		for (byte i = 0; i < this.internalSideCounter && usedEnergy < energy; i++) {
			if (tileCache[i] != null) {
				IEnergyReceiver receiver = tileCache[i];
				if (receiver.canConnectEnergy(EnumFacing.VALUES[i ^ 1])) {
					usedEnergy += sendEnergy(receiver, energy - usedEnergy, i, simulate);
				}
				if (!simulate && usedEnergy >= energy) {
					internalSideCounter = tickInternalSideCounter(i + 1);
					break;
				}
			}
		}
		return usedEnergy;
	}

	public int getTransferLimit() {

		return transferLimit;
	}

	public int getCapacity() {

		return capacity;
	}

	protected int sendEnergy(IEnergyReceiver receiver, int maxReceive, byte side, boolean simulate) {

		return receiver.receiveEnergy(EnumFacing.VALUES[side ^ 1], maxReceive, simulate);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);

		energyForGrid = nbt.getInteger("Energy");
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);

		if (grid != null) {
			if (isNode()) {
				lastStoredValue = grid.getNodeShare(this);
				nbt.setInteger("Energy", lastStoredValue);
			}
		} else if (energyForGrid > 0) {
			nbt.setInteger("Energy", energyForGrid);
		} else {
			energyForGrid = 0;
		}
		return nbt;
	}

	public boolean canExtract() {

		return false;
		// return true;
	}

	/* IEnergyStorage */
	@Override
	public int receiveEnergy(int maxReceive, boolean simulate) {

		return grid != null ? grid.receiveEnergy(maxReceive, simulate) : 0;
	}

	@Override
	public int extractEnergy(int maxExtract, boolean simulate) {

		return 0;
		// return (canExtract() && grid != null) ? grid.myStorage.extractEnergy(maxExtract, simulate) : 0;
	}

	public int getEnergyStored() {

		return grid != null ? grid.myStorage.getEnergyStored() : 0;
	}

	@Override
	public int getMaxEnergyStored() {

		return grid != null ? grid.myStorage.getMaxEnergyStored() : 0;
	}

	/* CAPABILITIES */
	@Override
	public boolean hasCapability(Capability<?> capability, EnumFacing facing) {

		return capability == CapabilityEnergy.ENERGY;
	}

	@Override
	public <T> T getCapability(Capability<T> capability, EnumFacing facing) {

		return CapabilityEnergy.ENERGY.cast(new IEnergyStorage() {

			@Override
			public int receiveEnergy(int maxReceive, boolean simulate) {

				return DuctUnitEnergy.this.receiveEnergy(maxReceive, simulate);
			}

			@Override
			public int extractEnergy(int maxExtract, boolean simulate) {

				return 0;
				// return DuctUnitEnergy.this.extractEnergy(maxExtract, simulate);
			}

			@Override
			public int getEnergyStored() {

				return DuctUnitEnergy.this.getEnergyStored();
			}

			@Override
			public int getMaxEnergyStored() {

				return DuctUnitEnergy.this.getMaxEnergyStored();
			}

			@Override
			public boolean canExtract() {

				return false;
				// return DuctUnitEnergy.this.canExtract();
			}

			@Override
			public boolean canReceive() {

				return true;
			}
		});
	}

	/* CACHE CLASS */
	public static class Cache implements IEnergyReceiver, IEnergyProvider {

		public IEnergyHandler handler;
		public IEnergyReceiver receiver;
		public IEnergyProvider provider;

		public Cache(IEnergyReceiver receiver) {

			this.handler = receiver;
			this.receiver = receiver;

			if (receiver instanceof IEnergyProvider) {
				this.provider = (IEnergyProvider) receiver;
			}
		}

		public Cache(IEnergyProvider provider) {

			this.handler = provider;
			this.provider = provider;

			if (provider instanceof IEnergyReceiver) {
				this.receiver = (IEnergyReceiver) provider;
			}
		}

		@Override
		public boolean canConnectEnergy(EnumFacing from) {

			return handler.canConnectEnergy(from);
		}

		@Override
		public int getEnergyStored(EnumFacing from) {

			return handler.getEnergyStored(from);
		}

		@Override
		public int getMaxEnergyStored(EnumFacing from) {

			return handler.getMaxEnergyStored(from);
		}

		/* IEnergyReceiver */
		@Override
		public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {

			return receiver == null ? 0 : receiver.receiveEnergy(from, maxReceive, simulate);
		}

		/* IEnergyProvider */
		@Override
		public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {

			return provider == null ? 0 : provider.extractEnergy(from, maxExtract, simulate);
		}
	}

}
