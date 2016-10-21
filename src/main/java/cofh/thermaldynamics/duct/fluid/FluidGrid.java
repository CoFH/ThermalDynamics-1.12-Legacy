package cofh.thermaldynamics.duct.fluid;

import cofh.core.CoFHProps;
import cofh.core.network.PacketHandler;
import cofh.lib.util.TimeTracker;
import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.helpers.MathHelper;
import cofh.lib.util.position.ChunkCoord;
import cofh.thermaldynamics.core.TDProps;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.multiblock.MultiBlockGridTracking;
import com.google.common.collect.Iterables;

import java.util.HashSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import net.minecraftforge.fluids.FluidStack;

public class FluidGrid extends MultiBlockGridTracking {

	public final FluidTankGrid myTank = new FluidTankGrid(1000, this);
	public int toDistribute = 0;

	/* Render Stuff */
	public HashSet<ChunkCoord> chunks;
	TimeTracker myTracker = new TimeTracker();
	boolean recentRenderUpdate = false;
	int renderFluidLevel = 0;
	FluidStack myRenderFluid = null;

	public FluidGrid(World world) {

		super(world);

	}

	@Override
	public void onMajorGridChange() {

		super.onMajorGridChange();
		chunks = null;
	}

	@Override
	public void onMinorGridChange() {

		super.onMinorGridChange();
	}

	@Override
	public int getLevel() {

		return myTank.getFluidAmount();
	}

	@Override
	public void addBlock(IMultiBlock aMultiBlock) {

		super.addBlock(aMultiBlock);

		TileFluidDuct theCondF = (TileFluidDuct) aMultiBlock;
		if (theCondF.fluidForGrid != null) {
			if (myTank.getFluid() == null) {
				myTank.setFluid(theCondF.fluidForGrid);
			} else {
				myTank.fill(theCondF.fluidForGrid, true);
			}
			theCondF.fluidForGrid = null;
			recentRenderUpdate = true;
		}
		if (renderFluidLevel != 0) {
			theCondF.updateFluid();
		}
	}

	@Override
	public void balanceGrid() {

		myTank.setCapacity(size() * myTank.fluidPerDuct);
	}

	public float getThroughPutModifier() {

		return 1F;
	}

	public int getMaxFluidPerDuct() {

		return 3000;
	}

	@Override
	public void destroyNode(IMultiBlock node) {

		if (hasValidFluid()) {
			((TileFluidDuct) node).fluidForGrid = getNodeShare((TileFluidDuct) node);
		}
		super.destroyNode(node);
	}

	@Override
	public boolean canAddBlock(IMultiBlock aBlock) {

		return aBlock instanceof TileFluidDuct && FluidHelper.isFluidEqualOrNull(((TileFluidDuct) aBlock).getConnectionFluid(), myTank.getFluid());
	}

	public boolean doesPassiveTicking = false;

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
				for (IMultiBlock m : nodeSet) {
					if (!m.tickPass(0) || m.getGrid() == null) {
						break;
					}
				}
			}
		}
		if (!nodeSet.isEmpty()) {
			for (IMultiBlock m : nodeSet) {
				if (!m.tickPass(1) || m.getGrid() == null) {
					break;
				}
			}
		}

		if (doesPassiveTicking) {
			if (!nodeSet.isEmpty()) {
				for (IMultiBlock m : nodeSet) {
					if (!m.tickPass(2) || m.getGrid() == null) {
						break;
					}
				}
			}

			if (!idleSet.isEmpty()) {
				for (IMultiBlock m : idleSet) {
					if (!m.tickPass(2) || m.getGrid() == null) {
						break;
					}
				}
			}
		}
	}

	@Override
	public void mergeGrids(MultiBlockGrid theGrid) {

		super.mergeGrids(theGrid);

		FluidGrid fluidGrid = (FluidGrid) theGrid;
		doesPassiveTicking = doesPassiveTicking || fluidGrid.doesPassiveTicking;
		myTank.fill(fluidGrid.getFluid(), true);
		recentRenderUpdate = true;
	}

	@Override
	public boolean canGridsMerge(MultiBlockGrid grid) {

		return super.canGridsMerge(grid) && FluidHelper.isFluidEqualOrNull(((FluidGrid) grid).getFluid(), getFluid());
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

	public FluidStack getNodeShare(TileFluidDuct theCond) {

		FluidStack toReturn = myTank.getFluid().copy();
		toReturn.amount = getNodeAmount(theCond);
		return toReturn;
	}

	public int getNodeAmount(TileFluidDuct theCond) {

		return size() == 1 ? myTank.getFluidAmount() : isFirstMultiblock(theCond) ? myTank.getFluidAmount() / size() + myTank.getFluidAmount() % size()
				: myTank.getFluidAmount() / size();
	}

	public FluidStack getFluid() {

		return myTank.getFluid();
	}

	public boolean hasValidFluid() {

		return myTank.getFluid() != null;
	}

	/* Renders */
	public void buildMap() {

		chunks = new HashSet<ChunkCoord>();
		for (IMultiBlock iMultiBlock : Iterables.concat(nodeSet, idleSet)) {
			buildMapEntry(iMultiBlock);
		}
	}

	private void buildMapEntry(IMultiBlock iMultiBlock) {

		chunks.add(new ChunkCoord(iMultiBlock.x() >> 4, iMultiBlock.z() >> 4));
	}

	@SuppressWarnings("unchecked")
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

				l: if (worldGrid.worldObj instanceof WorldServer) {

					int ducts = 0;
					for (Object block : Iterables.concat(nodeSet, idleSet)) {
						TileFluidDuct duct = ((TileFluidDuct) block);
						if (!duct.getDuctType().opaque) {
							++ducts;
							duct.updateLighting();
						}
					}
					if (ducts == 0) {
						break l;
					}
					PacketFluid packet = new PacketFluid(this, ducts);
					WorldServer dimension = (WorldServer) worldGrid.worldObj;
					for (EntityPlayer player : (List<EntityPlayer>) dimension.playerEntities) {
						for (ChunkCoord chunk : chunks) {
							int dx = (chunk.chunkX - (MathHelper.floor(player.posX) >> 4)) * 16;
							int dz = (chunk.chunkZ - (MathHelper.floor(player.posZ) >> 4)) * 16;

							if (dx * dx + dz * dz <= CoFHProps.NETWORK_UPDATE_RANGE * CoFHProps.NETWORK_UPDATE_RANGE) {
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

	@Override
	public void addInfo(List<ITextComponent> info, EntityPlayer player, boolean debug) {

		super.addInfo(info, player, debug);
		FluidStack fluid = getFluid();
		if (fluid != null) {
			if ((this instanceof FluidGridSuper)) {
				addInfo(info, "fluidThroughput", new TextComponentTranslation("info.thermaldynamics.filter.zeroRetainSize"));
			} else {
				addInfo(info, "fluidThroughput", myTank.fluidThroughput);
			}
		}

	}

	@Override
	protected String getUnit() {

		return "mB";
	}
}
