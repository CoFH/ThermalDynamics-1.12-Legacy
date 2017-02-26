package cofh.thermaldynamics.multiblock;

import cofh.core.util.ChatHelper;
import cofh.thermaldynamics.block.Attachment;
import cofh.thermaldynamics.util.TickHandler;
import cofh.thermaldynamics.util.WorldGridList;
import cofh.thermaldynamics.duct.attachments.relay.Relay;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiBlockGrid {

	public NoComodSet<IMultiBlock> nodeSet = new NoComodSet<>();
	public NoComodSet<IMultiBlock> idleSet = new NoComodSet<>();
	public WorldGridList worldGrid;
	public boolean signalsUpToDate;

	public RedstoneControl rs;

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
			for (byte s = 0; s < EnumFacing.VALUES.length; s++) {
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

		if (rs != null && rs.nextRedstoneLevel != -128) {
			rs.redstoneLevel = rs.nextRedstoneLevel;
			rs.nextRedstoneLevel = -128;

			ArrayList<Attachment> signallersOut = rs.relaysOut;
			if (signallersOut != null) {
				for (Attachment output : signallersOut) {
					output.checkSignal();
				}
			}
		}

		if (signalsUpToDate) {
			return;
		}

		signalsUpToDate = true;

		if (rs == null || rs.relaysIn == null) {
			if (rs != null) {
				rs.relaysOut = null;
			}
			for (IMultiBlock multiBlock : nodeSet) {
				multiBlock.addRelays();
			}
		}

		if (rs == null) {
			return;
		}

		if (rs.relaysIn == null) {
			if (rs.relaysOut == null) {
				rs = null;
				return;
			} else {
				rs.nextRedstoneLevel = 0;
			}
			return;
		}

		int powered = 0;
		for (Relay signaller : rs.relaysIn) {
			powered = Math.max(powered, signaller.getPowerLevel());
			if (powered == 15) {
				break;
			}

		}

		rs.nextRedstoneLevel = (byte) powered;

	}

	public void addSignalInput(Relay signaller) {

		if (signaller.isInput()) {
			if (rs == null) {
				rs = new RedstoneControl();
			}

			if (rs.relaysIn == null) {
				rs.relaysIn = new ArrayList<>();
			}

			rs.relaysIn.add(signaller);
		}
	}

	public void addSignalOutput(Attachment attachment) {

		if (rs == null) {
			rs = new RedstoneControl();
		}

		if (rs.relaysOut == null) {
			rs.relaysOut = new ArrayList<>();
		}
		rs.relaysOut.add(attachment);
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

		if (rs != null) {
			rs.relaysIn = null;
			rs.relaysOut = null;
		}
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

	public void addInfo(List<ITextComponent> info, EntityPlayer player, boolean debug) {

		if (debug) {
			addInfo(info, "size", size());
		}

		if (rs != null) {
			int r = rs.redstoneLevel;
			if (rs.nextRedstoneLevel != -128) {
				r = rs.nextRedstoneLevel;
			}
			addInfo(info, "redstone", r);
		}
	}

	protected final void addInfo(List<ITextComponent> info, String type, Object value) {

		info.add(new TextComponentTranslation("info.thermaldynamics.info." + type).appendText(": ").appendSibling(ChatHelper.getChatComponent(value)));
	}

	public static class RedstoneControl {

		public byte nextRedstoneLevel = -128;
		public ArrayList<Relay> relaysIn;
		public ArrayList<Attachment> relaysOut;
		public int redstoneLevel;
	}

}
