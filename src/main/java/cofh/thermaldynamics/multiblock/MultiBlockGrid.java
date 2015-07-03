package cofh.thermaldynamics.multiblock;

import cofh.thermaldynamics.block.Attachment;
import cofh.thermaldynamics.core.TickHandler;
import cofh.thermaldynamics.core.WorldGridList;
import cofh.thermaldynamics.debughelper.NoComodSet;
import cofh.thermaldynamics.duct.attachments.relay.Relay;

import java.util.ArrayList;

import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

public abstract class MultiBlockGrid {

	public NoComodSet<IMultiBlock> nodeSet = new NoComodSet<IMultiBlock>();
	public NoComodSet<IMultiBlock> idleSet = new NoComodSet<IMultiBlock>();
	public WorldGridList worldGrid;
	public boolean signalsUpToDate;
	public int redstoneLevel;
	public ArrayList<Relay> relaysIn;
	public ArrayList<Attachment> relaysOut;

	public MultiBlockGrid(WorldGridList worldGrid) {

		this.worldGrid = worldGrid;
		worldGrid.newGrids.add(this);
	}

	public MultiBlockGrid(World worldObj) {

		this(TickHandler.getTickHandler(worldObj));
	}

	public void addIdle(IMultiBlock aMultiBlock) {

		idleSet.add(aMultiBlock);

		if (nodeSet.contains(aMultiBlock)) {
			nodeSet.remove(aMultiBlock);
			onMajorGridChange();
		} else {
			boolean flag = false;
			for (byte s = 0; s < ForgeDirection.VALID_DIRECTIONS.length; s++) {
				if (aMultiBlock.isSideConnected(s)) {
					if (flag) {
						onMajorGridChange();
						break;
					} else {
						flag = true;
					}
				}
			}
		}

		balanceGrid();
	}

	public void addNode(IMultiBlock aMultiBlock) {

		nodeSet.add(aMultiBlock);
		if (idleSet.contains(aMultiBlock)) {
			idleSet.remove(aMultiBlock);
		}

		onMajorGridChange();
		balanceGrid();
	}

	public void mergeGrids(MultiBlockGrid theGrid) {

		if (!theGrid.nodeSet.isEmpty()) {
			for (IMultiBlock aBlock : theGrid.nodeSet) {
				aBlock.setGrid(this);
				addBlock(aBlock);
			}

			onMajorGridChange();
		}

		if (!theGrid.idleSet.isEmpty()) {
			for (IMultiBlock aBlock : theGrid.idleSet) {
				aBlock.setGrid(this);
				addBlock(aBlock);
			}

			onMajorGridChange();
		}

		onMinorGridChange();
		theGrid.destroy();
	}

	public void destroy() {

		nodeSet.clear();
		idleSet.clear();

		worldGrid.oldGrids.add(this);
	}

	public boolean canGridsMerge(MultiBlockGrid grid) {

		return grid.getClass() == this.getClass();
	}

	public void resetMultiBlocks() {

		for (IMultiBlock aBlock : nodeSet) {
			aBlock.setValidForForming();
		}
		for (IMultiBlock aBlock : idleSet) {
			aBlock.setValidForForming();
		}
	}

	/*
	 * Called at the end of a world tick
	 */
	public void tickGrid() {

		if (signalsUpToDate) {
			return;
		}

		signalsUpToDate = true;

		if (relaysIn == null) {
			relaysOut = null;
			for (IMultiBlock multiBlock : nodeSet) {
				multiBlock.addSignallers();
			}
		}

		if (relaysIn == null) {
			redstoneLevel = 0;
			if (relaysOut != null) {
				for (Attachment signaller : relaysOut) {
					signaller.checkSignal();
				}
			}
			return;
		}

		if (relaysOut == null) {
			return;
		}

		int powered = 0;
		for (Relay signaller : relaysIn) {
			powered = Math.max(powered, signaller.getPowerLevel());
			if (powered == 15) {
				break;
			}

		}

		redstoneLevel = powered;
		ArrayList<Attachment> signallersOut = this.relaysOut;

		for (Attachment output : signallersOut) {
			output.checkSignal();
		}
	}

	public void addSignalInput(Relay signaller) {

		if (signaller.isInput()) {
			if (relaysIn == null) {
				relaysIn = new ArrayList<Relay>();
			}

			relaysIn.add(signaller);
		}
	}

	public void addSignalOutput(Attachment attachment) {

		if (relaysOut == null) {
			relaysOut = new ArrayList<Attachment>();
		}
		relaysOut.add(attachment);
	}

	/*
	 * Called whenever a set changes so that grids that rely on set sizes can rebalance.
	 */
	public void balanceGrid() {

	}

	public void addBlock(IMultiBlock aBlock) {

		if (aBlock.isNode()) {
			addNode(aBlock);
		} else {
			addIdle(aBlock);
		}
	}

	public void destroyAndRecreate() {

		worldGrid.gridsToRecreate.add(this);
	}

	public void removeBlock(IMultiBlock oldBlock) {

		destroyNode(oldBlock);

		if (oldBlock.isNode()) {
			nodeSet.remove(oldBlock);
			onMajorGridChange();
		} else {
			idleSet.remove(oldBlock);
		}

		if (nodeSet.isEmpty() && idleSet.isEmpty()) {
			worldGrid.oldGrids.add(this);
			return;
		}

		byte s = 0;
		for (byte i = 0; i < 6; i++) {
			if (oldBlock.isSideConnected(i)) {
				s++;
			}
		}

		if (s <= 1) {
			balanceGrid();
			onMinorGridChange();
			return;
		}

		onMajorGridChange();

		worldGrid.gridsToRecreate.add(this);
	}

	public void onMinorGridChange() {

		resetRelays();
	}

	public void onMajorGridChange() {

		resetRelays();
	}

	public void resetRelays() {

		relaysIn = null;
		relaysOut = null;
		signalsUpToDate = false;
	}

	public int size() {

		return nodeSet.size() + idleSet.size();
	}

	public void doTickProcessing(long deadline) {

	}

	public boolean isTickProcessing() {

		return false;
	}

	public void destroyNode(IMultiBlock node) {

		node.setGrid(null);
	}

	public boolean isFirstMultiblock(IMultiBlock block) {

		return !nodeSet.isEmpty() ? nodeSet.iterator().next() == block : !idleSet.isEmpty() && idleSet.iterator().next() == block;
	}

	public abstract boolean canAddBlock(IMultiBlock aBlock);

}
