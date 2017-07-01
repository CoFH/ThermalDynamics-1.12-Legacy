package cofh.thermaldynamics.duct.fluid;

import cofh.core.init.CoreProps;
import cofh.core.network.PacketHandler;
import cofh.core.util.ChunkCoord;
import cofh.core.util.TimeTracker;
import cofh.core.util.helpers.FluidHelper;
import cofh.core.util.helpers.MathHelper;
import cofh.thermaldynamics.init.TDProps;
import cofh.thermaldynamics.multiblock.IGridTile;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.multiblock.MultiBlockGridTracking;
import com.google.common.collect.Iterables;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidStack;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.List;

public class GridFluid extends MultiBlockGridTracking<DuctUnitFluid> {

	public final FluidTankGrid myTank = new FluidTankGrid(1000, this);
	public int toDistribute = 0;

	public boolean doesPassiveTicking = false;
	int numStorable = -1;

	/* Render Stuff */
	public HashSet<ChunkCoord> chunks;
	TimeTracker myTracker = new TimeTracker();
	boolean recentRenderUpdate = false;
	int renderFluidLevel = 0;
	FluidStack myRenderFluid = null;

	public GridFluid(World world) {

		super(world);

	}

	@Override
	public void addBlock(DuctUnitFluid aMultiBlock) {

		super.addBlock(aMultiBlock);

		if (aMultiBlock.getFluidForGrid() != null) {
			if (myTank.getFluid() == null) {
				myTank.setFluid(aMultiBlock.getFluidForGrid());
			} else {
				myTank.fill(aMultiBlock.getFluidForGrid(), true);
			}
			aMultiBlock.setFluidForGrid(null);
			recentRenderUpdate = true;
		}
		if (renderFluidLevel != 0) {
			aMultiBlock.updateFluid();
		}
	}

	@Override
	public void addInfo(List<ITextComponent> info, EntityPlayer player, boolean debug) {

		super.addInfo(info, player, debug);
		FluidStack fluid = getFluid();
		if (fluid != null) {
			if ((this instanceof GridFluidSuper)) {
				addInfo(info, "fluidThroughput", new TextComponentTranslation("info.thermaldynamics.filter.zeroRetainSize"));
			} else {
				addInfo(info, "fluidThroughput", myTank.fluidThroughput);
			}
		}

	}

	@Override
	public void balanceGrid() {

		reworkNumberStorableDucts();
		myTank.setCapacity(size() * myTank.fluidPerDuct);
	}

	@Override
	public void onMajorGridChange() {

		super.onMajorGridChange();
		numStorable = -1;
		chunks = null;
	}

	@Override
	public void onMinorGridChange() {

		super.onMinorGridChange();
		numStorable = -1;
	}

	@Override
	protected int getLevel() {

		return myTank.getFluidAmount();
	}

	@Override
	protected String getUnit() {

		return "mB";
	}

	protected int getStorableNumberDucts() {

		int numBalancable = this.numStorable;
		if (numBalancable == -1) {
			numBalancable = reworkNumberStorableDucts();
		}
		return numBalancable;
	}

	private int reworkNumberStorableDucts() {

		int numBalancable = 0;
		for (DuctUnitFluid duct : Iterables.concat(nodeSet, idleSet)) {
			if (duct.canStoreFluid()) {
				numBalancable++;
			}
		}
		this.numStorable = numBalancable;
		return numBalancable;
	}

	public float getThroughPutModifier() {

		return 1.0F;
	}

	public int getMaxFluidPerDuct() {

		return 3000;
	}

	@Override
	public void destroyNode(IGridTile node) {

		if (hasValidFluid()) {
			((DuctUnitFluid) node).setFluidForGrid(getNodeShare((DuctUnitFluid) node));
		}
		super.destroyNode(node);
	}

	@Override
	public void tickGrid() {

		super.tickGrid();

		if (worldGrid.worldObj.getTotalWorldTime() % TDProps.FLUID_UPDATE_DELAY == 0) {
			updateAllRenders();
		}
		if (myTank.getFluid() != null && nodeSet.size() > 0) {
			toDistribute = Math.min(myTank.getFluidAmount() / size(), getFluidThroughput());

			if (toDistribute <= 0) {
				toDistribute = Math.min(myTank.getFluidAmount() % size(), getFluidThroughput());
			}

			if (toDistribute > 0) {
				for (DuctUnitFluid m : nodeSet) {
					if (!m.tickPass(0) || m.getGrid() == null) {
						break;
					}
				}
			}
		}
		if (!nodeSet.isEmpty()) {
			for (DuctUnitFluid m : nodeSet) {
				if (!m.tickPass(1) || m.getGrid() == null) {
					break;
				}
			}
		}

		if (doesPassiveTicking) {
			if (!nodeSet.isEmpty()) {
				for (DuctUnitFluid m : nodeSet) {
					if (!m.tickPass(2) || m.getGrid() == null) {
						break;
					}
				}
			}

			if (!idleSet.isEmpty()) {
				for (DuctUnitFluid m : idleSet) {
					if (!m.tickPass(2) || m.getGrid() == null) {
						break;
					}
				}
			}
		}
	}

	@Override
	public void mergeGrids(MultiBlockGrid<DuctUnitFluid> theGrid) {

		super.mergeGrids(theGrid);

		GridFluid gridFluid = (GridFluid) theGrid;
		doesPassiveTicking = doesPassiveTicking || gridFluid.doesPassiveTicking;
		myTank.fill(gridFluid.getFluid(), true);
		recentRenderUpdate = true;
	}

	@Override
	public boolean canAddBlock(IGridTile aBlock) {

		return aBlock instanceof DuctUnitFluid && FluidHelper.isFluidEqualOrNull(((DuctUnitFluid) aBlock).getConnectionFluid(), myTank.getFluid());
	}

	@Override
	public boolean canGridsMerge(MultiBlockGrid grid) {

		return super.canGridsMerge(grid) && FluidHelper.isFluidEqualOrNull(((GridFluid) grid).getFluid(), getFluid());
	}

	public int getFluidThroughput() {

		if (myTank.getFluid() == null) {
			return 100;
		}
		int capacity = myTank.getCapacity();
		// if over 3/4 full, full throughput
		if (myTank.getFluid().amount >= capacity * 3 / 4) {
			return myTank.fluidThroughput;
		}
		// if under 1/4 full, half throughput
		if (myTank.getFluid().amount <= capacity / 4) {
			return myTank.fluidThroughput >> 1;
		}
		// otherwise scale between half and full
		return (myTank.fluidThroughput >> 1) + (myTank.fluidThroughput >> 1) * (myTank.getFluid().amount - (capacity >> 2)) / (capacity >> 1);
	}

	public void fluidChanged() {

		balanceGrid();
	}

	@Nullable
	public FluidStack getNodeShare(DuctUnitFluid duct) {

		if (!duct.canStoreFluid()) {
			return null;
		}
		FluidStack fluid = myTank.getFluid();
		if (fluid == null) {
			return null;
		}
		FluidStack toReturn = fluid.copy();
		toReturn.amount = getNodeAmount(duct);
		return toReturn.amount > 0 ? toReturn : null;
	}

	public int getNodeAmount(DuctUnitFluid duct) {

		if (!duct.canStoreFluid()) {
			return 0;
		}
		int size = getStorableNumberDucts();
		if (size == 0) {
			return 0;
		}
		return size == 1 ? myTank.getFluidAmount() : isFirstMultiblock(duct) ? myTank.getFluidAmount() / size + myTank.getFluidAmount() % size : myTank.getFluidAmount() / size;
	}

	public FluidStack getFluid() {

		return myTank.getFluid();
	}

	public boolean hasValidFluid() {

		return myTank.getFluid() != null;
	}

	/* RENDERS */
	public void buildMap() {

		chunks = new HashSet<>();
		for (DuctUnitFluid iMultiBlock : Iterables.concat(nodeSet, idleSet)) {
			buildMapEntry(iMultiBlock);
		}
	}

	private void buildMapEntry(DuctUnitFluid iMultiBlock) {

		chunks.add(new ChunkCoord(iMultiBlock.x() >> 4, iMultiBlock.z() >> 4));
	}

	public void updateAllRenders() {

		int fl = renderFluidLevel;
		if (updateRender()) {
			if (fl != renderFluidLevel) {
				if (myTank.getFluid() != null) {
					myRenderFluid = myTank.getFluid().copy();
					myRenderFluid.amount = renderFluidLevel;
				} else {
					myRenderFluid = null;
				}

				if (chunks == null) {
					buildMap();
				}

				l:
				if (worldGrid.worldObj instanceof WorldServer) {

					int ducts = 0;
					for (DuctUnitFluid block : Iterables.concat(nodeSet, idleSet)) {
						if (!block.isOpaque()) {
							++ducts;
							block.updateLighting();
						}
					}
					if (ducts == 0) {
						break l;
					}
					PacketFluid packet = new PacketFluid(this, ducts);
					WorldServer dimension = (WorldServer) worldGrid.worldObj;
					for (EntityPlayer player : dimension.playerEntities) {
						for (ChunkCoord chunk : chunks) {
							int dx = (chunk.chunkX - (MathHelper.floor(player.posX) >> 4)) * 16;
							int dz = (chunk.chunkZ - (MathHelper.floor(player.posZ) >> 4)) * 16;

							if (dx * dx + dz * dz <= CoreProps.NETWORK_UPDATE_RANGE * CoreProps.NETWORK_UPDATE_RANGE) {
								PacketHandler.sendTo(packet, player);
								break;
							}
						}
					}
				}
			}
		}
	}

	public boolean updateRender() {

		if (recentRenderUpdate && myTracker.hasDelayPassed(worldGrid.worldObj, TDProps.FLUID_EMPTY_UPDATE_DELAY)) {
			recentRenderUpdate = false;
		}
		if (myTank.getFluid() != null && myTank.getCapacity() > 0) {
			double fullPercent = 10000 * myTank.getFluid().amount / myTank.getCapacity();

			if (fullPercent >= 0 && fullPercent <= (renderFluidLevel == FluidRenderType.LOW_MED ? 500 : 700)) {
				renderFluidLevel = FluidRenderType.LOW;
				return true;
			}
			if (fullPercent >= 500 && fullPercent <= (renderFluidLevel == FluidRenderType.MEDIUM ? 2000 : 2500)) {
				renderFluidLevel = FluidRenderType.LOW_MED;
				return true;
			}
			if (fullPercent >= 2000 && fullPercent <= (renderFluidLevel == FluidRenderType.MED_HIGH ? 4000 : 4500)) {
				renderFluidLevel = FluidRenderType.MEDIUM;
				return true;
			}
			if (fullPercent >= 4000 && fullPercent <= (renderFluidLevel == FluidRenderType.HIGH ? 6000 : 6500)) {
				renderFluidLevel = FluidRenderType.MED_HIGH;
				return true;
			}
			if (fullPercent >= 6000 && fullPercent <= (renderFluidLevel == FluidRenderType.FULL ? 8000 : 8500)) {
				renderFluidLevel = FluidRenderType.HIGH;
				return true;
			}
			renderFluidLevel = FluidRenderType.FULL;
			return true;
		} else if (renderFluidLevel != FluidRenderType.EMPTY && !recentRenderUpdate) {
			renderFluidLevel = FluidRenderType.EMPTY;
			recentRenderUpdate = true;
			myTracker.markTime(worldGrid.worldObj);
			return true;
		}
		return false;
	}

	public FluidStack getRenderFluid() {

		return myRenderFluid;
	}

	public FluidStack getConnectionFluid() {

		int fl = renderFluidLevel;
		if (updateRender()) {
			if (fl != renderFluidLevel) {
				if (myTank.getFluid() != null) {
					myRenderFluid = myTank.getFluid().copy();
					myRenderFluid.amount = renderFluidLevel;
				} else {
					myRenderFluid = null;
				}
			}
		}
		return myRenderFluid;
	}

	public int getRenderLevel() {

		return renderFluidLevel;
	}

	/* FLUID RENDER TYPE */
	public static final class FluidRenderType {

		public static final byte EMPTY = 0;
		public static final byte LOW = 1;
		public static final byte LOW_MED = 2;
		public static final byte MEDIUM = 3;
		public static final byte MED_HIGH = 4;
		public static final byte HIGH = 5;
		public static final byte FULL = 6;
	}

}
