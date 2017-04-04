package cofh.thermaldynamics.multiblock;

import cofh.core.util.helpers.ChatHelper;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.attachments.relay.Relay;
import cofh.thermaldynamics.util.TickHandler;
import cofh.thermaldynamics.util.WorldGridList;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextComponentTranslation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public abstract class MultiBlockGrid<T extends IGridTile> {

	public NoComodSet<T> nodeSet = new NoComodSet<>();
	public NoComodSet<T> idleSet = new NoComodSet<>();
	public WorldGridList worldGrid;


	public MultiBlockGrid(WorldGridList worldGrid) {

		this.worldGrid = worldGrid;
		worldGrid.newGrids.add(this);
	}

	public MultiBlockGrid(World worldObj) {

		this(TickHandler.getTickHandler(worldObj));
	}

	public void addIdle(T aMultiBlock) {

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

	public void addNode(T aMultiBlock) {

		nodeSet.add(aMultiBlock);
		if (idleSet.contains(aMultiBlock)) {
			idleSet.remove(aMultiBlock);
		}

		onMajorGridChange();
		balanceGrid();
	}

	public void mergeGrids(MultiBlockGrid<T> otherGrid) {

		if (!otherGrid.nodeSet.isEmpty()) {
			for (T aBlock : otherGrid.nodeSet) {
				aBlock.setGrid(this);
				addBlock(aBlock);
			}

			onMajorGridChange();
		}

		if (!otherGrid.idleSet.isEmpty()) {
			for (T aBlock : otherGrid.idleSet) {
				aBlock.setGrid(this);
				addBlock(aBlock);
			}

			onMajorGridChange();
		}

		onMinorGridChange();
		otherGrid.destroy();
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

		for (IGridTile aBlock : nodeSet) {
			aBlock.setValidForForming();
		}
		for (IGridTile aBlock : idleSet) {
			aBlock.setValidForForming();
		}
	}

	/*
	 * Called at the end of a world tick
	 */
	public void tickGrid(){

	}


	/*
	 * Called whenever a set changes so that grids that rely on set sizes can rebalance.
	 */
	public void balanceGrid() {

	}

	public void addBlock(T aBlock) {

		if (aBlock.isNode()) {
			addNode(aBlock);
		} else {
			addIdle(aBlock);
		}
	}

	public void destroyAndRecreate() {

		worldGrid.gridsToRecreate.add(this);
	}

	public void removeBlock(T oldBlock) {

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

	}

	public void onMajorGridChange() {

	}

	public int size() {

		return nodeSet.size() + idleSet.size();
	}

	public void doTickProcessing(long deadline) {

	}

	public boolean isTickProcessing() {

		return false;
	}

	public void destroyNode(IGridTile node) {

		node.setGrid(null);
	}

	public boolean isFirstMultiblock(T block) {

		return !nodeSet.isEmpty() ? nodeSet.iterator().next() == block : !idleSet.isEmpty() && idleSet.iterator().next() == block;
	}

	public abstract boolean canAddBlock(IGridTile aBlock);

	public void addInfo(List<ITextComponent> info, EntityPlayer player, boolean debug) {

		if (debug) {
			addInfo(info, "Type", getClass().getSimpleName());
			addInfo(info, "size", size());
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
