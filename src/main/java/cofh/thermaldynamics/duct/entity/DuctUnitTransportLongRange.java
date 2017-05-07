package cofh.thermaldynamics.duct.entity;

import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.IDuctHolder;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import cofh.thermaldynamics.multiblock.Route;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class DuctUnitTransportLongRange extends DuctUnitTransportBase {

	public DuctUnitTransportLongRange(TileGrid parent, Duct duct) {

		super(parent, duct);
	}

	@Override
	public TransportGrid createGrid() {

		return null;
	}

	@Override
	public void formGrid() {

	}

	@Override
	public boolean isValidForForming() {

		return false;
	}

	public byte nextDirection(byte k) {

		byte dir = -1;
		for (int i = 0; i < 6; i++) {
			if (k == (i ^ 1)) {
				continue;
			}
			if (pipeCache[i] != null) {
				if (dir != -1) {
					return -1;
				}
				dir = (byte) i;
			}
		}
		return dir;
	}

	@Override
	public void advanceEntity(EntityTransport t) {

		int v = t.progress;
		v += t.step * 2;
		t.progress = (byte) (v % EntityTransport.PIPE_LENGTH);
		if (v >= EntityTransport.PIPE_LENGTH) {
			if (pipeCache[t.direction] != null) {
				DuctUnitTransportBase newHome = getConnectedSide(t.direction);
				newHome.onNeighborBlockChange();
				if (newHome.pipeCache[t.direction ^ 1] != null) {
					t.pos = newHome.pos();

					t.oldDirection = t.direction;

					if (newHome instanceof DuctUnitTransportLongRange) {
						DuctUnitTransportLongRange lr = (DuctUnitTransportLongRange) newHome;
						t.direction = lr.nextDirection(t.direction);
						if (t.direction == -1) {
							t.dropPassenger();
						}
					} else if (t.myPath != null) {
						if (t.myPath.hasNextDirection()) {
							t.direction = t.myPath.getNextDirection();
						} else {
							t.reRoute = true;
						}
					}
				}
			} else {
				t.dropPassenger();
			}
		} else if (t.progress >= EntityTransport.PIPE_LENGTH2 && t.progress - t.step < EntityTransport.PIPE_LENGTH2) {
			if (pipeCache[t.direction] == null) {
				t.dropPassenger();
			}
		}
	}

	@Override
	public boolean advanceEntityClient(EntityTransport t) {

		int v = t.progress;
		v += t.step + (t.step);
		t.progress = (byte) (v % EntityTransport.PIPE_LENGTH);
		if (v >= EntityTransport.PIPE_LENGTH) {
			if (!t.trySimpleAdvance()) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean isRoutable() {

		return false;
	}

	@Override
	public boolean isCrossover() {

		return false;
	}

	@Override
	public boolean canConnectToOtherDuct(DuctUnit<DuctUnitTransportBase, TransportGrid, TransportDestination> adjDuct, byte side, byte oppositeSide) {

		return adjDuct.cast().isLongRange() || adjDuct.cast().isCrossover();
	}

	@Nullable
	@Override
	public TransportDestination cacheTile(@Nonnull TileEntity tile, byte side) {

		return null;
	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);
		return nbt;
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);
	}

	@Override
	public boolean hasTooManyConnections() {

		int i = 0;
		for (DuctUnitTransportBase ductUnitTransportBase : pipeCache) {
			if (ductUnitTransportBase != null) {
				i++;
				if (i > 2) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public Route getRoute(Entity entityTransport, int direction, byte step) {

		return null;
	}

	@Nonnull
	@Override
	protected BlockDuct.ConnectionType getConnectionTypeDuct(DuctUnitTransportBase duct, int side) {

		if (hasTooManyConnections() || duct.hasTooManyConnections()) {
			return BlockDuct.ConnectionType.NONE;
		}
		return super.getConnectionTypeDuct(duct, side);
	}

	@Override
	public void updateSide(TileEntity tile, IDuctHolder holder, byte side) {

		super.updateSide(tile, holder, side);
	}

	@Override
	public void updateAllSides(TileEntity[] tiles, IDuctHolder[] holders) {

		super.updateAllSides(tiles, holders);
	}
}
