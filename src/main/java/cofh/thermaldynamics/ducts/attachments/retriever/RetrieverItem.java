package cofh.thermaldynamics.ducts.attachments.retriever;

import cofh.core.render.RenderUtils;
import cofh.lib.util.helpers.InventoryHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.repack.codechicken.lib.vec.BlockCoord;
import cofh.repack.codechicken.lib.vec.Translation;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.AttachmentRegistry;
import cofh.thermaldynamics.block.TileMultiBlock;
import cofh.thermaldynamics.ducts.attachments.servo.ServoItem;
import cofh.thermaldynamics.ducts.item.SimulatedInv;
import cofh.thermaldynamics.ducts.item.StackMap;
import cofh.thermaldynamics.ducts.item.TileItemDuct;
import cofh.thermaldynamics.ducts.item.TravelingItem;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.render.RenderDuct;

import gnu.trove.iterator.TObjectIntIterator;

import java.util.Iterator;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;

public class RetrieverItem extends ServoItem {

	public RetrieverItem(TileMultiBlock tile, byte side) {

		super(tile, side);
	}

	public RetrieverItem(TileMultiBlock tile, byte side, int type) {

		super(tile, side, type);
	}

	@Override
	public ItemStack getPickBlock() {

		return new ItemStack(ThermalDynamics.itemRetriever, 1, type);
	}

	@Override
	public String getName() {

		return "item.thermaldynamics.retriever." + type + ".name";
	}

	@Override
	public boolean render(int pass, RenderBlocks renderBlocks) {

		if (pass == 1) {
			return false;
		}
		Translation trans = RenderUtils.getRenderVector(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5).translation();
		RenderDuct.modelConnection[isPowered ? 1 : 2][side].render(trans,
				RenderUtils.getIconTransformation(RenderDuct.retrieverTexture[type * 2 + (stuffed ? 1 : 0)]));
		return true;
	}

	@Override
	public int getId() {

		return AttachmentRegistry.RETRIEVER_ITEM;
	}

	@Override
	public void handleItemSending() {

		SimulatedInv simulatedInv = cacheType == TileItemDuct.CacheType.ISIDEDINV ? SimulatedInv.wrapInvSided(cachedSidedInv) : SimulatedInv.wrapInv(cachedInv);

		StackMap travelingItems = itemDuct.internalGrid.travelingItems.get(new BlockCoord(itemDuct).offset(side));
		if (travelingItems != null) {

			for (TObjectIntIterator<StackMap.ItemEntry> iterator = travelingItems.iterator(); iterator.hasNext();) {
				iterator.advance();
				InventoryHelper.insertItemStackIntoInventory(simulatedInv, iterator.key().toItemStack(iterator.value()), iterator.key().side ^ 1);
			}
		}

		for (Route route : routeList) {
			TileItemDuct endPoint = (TileItemDuct) route.endPoint;

			for (int k = 0; k < 6; k++) {
				int i = (endPoint.internalSideCounter + k) % 6;

				if (endPoint.attachments[i] != null && endPoint.attachments[i].getId() == this.getId())
					continue;

				if (!endPoint.cachesExist()
						|| endPoint.cache[i] == null
						|| (endPoint.neighborTypes[i] != TileMultiBlock.NeighborTypes.OUTPUT && endPoint.neighborTypes[i] != TileMultiBlock.NeighborTypes.INPUT)
						|| !endPoint.connectionTypes[i].allowTransfer)
					continue;

				if (endPoint.cache2[i] != null) {
					ISidedInventory inv = endPoint.cache2[i];
					int[] accessibleSlotsFromSide = inv.getAccessibleSlotsFromSide(i ^ 1);
					for (int j = 0; j < accessibleSlotsFromSide.length; j++) {
						int slot = accessibleSlotsFromSide[j];

						ItemStack item = inv.getStackInSlot(slot);
						if (item == null)
							continue;

						item = limitOutput(ItemHelper.cloneStack(item, multiStack[type] ? item.getMaxStackSize() : item.stackSize), simulatedInv, slot, side);
						if (item == null || item.stackSize == 0)
							continue;

						if (!inv.canExtractItem(slot, item, i ^ 1))
							continue;

						if (!filter.matchesFilter(item) || !endPoint.filterCache[i].matchesFilter(item))
							continue;

						ItemStack remainder = InventoryHelper.simulateInsertItemStackIntoInventory(simulatedInv, item, side ^ 1);

						if (remainder != null)
							item.stackSize -= remainder.stackSize;
						if (item.stackSize == 0)
							continue;

						Route route1 = endPoint.getRoute(itemDuct);
						if (route1 == null)
							continue;

						int maxStackSize = item.stackSize;
						item = inv.decrStackSize(slot, maxStackSize);
						if (item == null || item.stackSize == 0)
							continue;

						// No turning back now
						route1 = route1.copy();
						route1.pathDirections.add(side);

						if (multiStack[type] && item.stackSize < maxStackSize) {
							for (; item.stackSize < maxStackSize && j < accessibleSlotsFromSide.length; j++) {
								slot = accessibleSlotsFromSide[j];
								ItemStack inSlot = inv.getStackInSlot(slot);
								if (ItemHelper.itemsEqualWithMetadata(inSlot, item, true) && inv.canExtractItem(slot, inSlot, i ^ 1)) {
									ItemStack extract = inv.decrStackSize(slot, maxStackSize - item.stackSize);
									if (extract != null)
										item.stackSize += extract.stackSize;
								}
							}
						}

						endPoint.insertNewItem(new TravelingItem(item, endPoint, route1, (byte) (i ^ 1), getSpeed()));
						return;
					}
				} else {
					IInventory inv = endPoint.cache[i];
					for (int slot = 0; slot < inv.getSizeInventory(); slot++) {
						ItemStack item = inv.getStackInSlot(slot);
						if (item == null)
							continue;

						item = limitOutput(ItemHelper.cloneStack(item, multiStack[type] ? item.getMaxStackSize() : item.stackSize), simulatedInv, slot, side);
						if (item == null || item.stackSize == 0)
							continue;

						if (!filter.matchesFilter(item) || !endPoint.filterCache[i].matchesFilter(item))
							continue;

						ItemStack remainder = InventoryHelper.simulateInsertItemStackIntoInventory(simulatedInv, item, side ^ 1);

						if (remainder != null)
							item.stackSize -= remainder.stackSize;
						if (item.stackSize == 0)
							continue;

						Route route1 = endPoint.getRoute(itemDuct);
						if (route1 == null)
							continue;

						int maxStackSize = item.stackSize;
						item = inv.decrStackSize(slot, maxStackSize);
						if (item == null || item.stackSize == 0)
							continue;

						// No turning back now
						route1 = route1.copy();
						route1.pathDirections.add(side);

						if (multiStack[type] && item.stackSize < maxStackSize) {
							for (; item.stackSize < maxStackSize && slot < inv.getSizeInventory(); slot++) {
								if (ItemHelper.itemsEqualWithMetadata(inv.getStackInSlot(slot), item, true)) {
									ItemStack extract = inv.decrStackSize(slot, maxStackSize - item.stackSize);
									if (extract != null)
										item.stackSize += extract.stackSize;
								}
							}
						}

						endPoint.insertNewItem(new TravelingItem(item, endPoint, route1, (byte) (i ^ 1), getSpeed()));
						return;
					}
				}
			}
		}
	}

	@Override
	public void handleStuffedItems() {

		for (Iterator<ItemStack> iterator = stuffedItems.iterator(); iterator.hasNext();) {
			ItemStack stuffedItem = iterator.next();
			if (!filter.matchesFilter(stuffedItem))
				continue;

			ItemStack itemStack = InventoryHelper.insertItemStackIntoInventory(cachedInv, stuffedItem, side ^ 1);
			if (itemStack == null)
				iterator.remove();
		}
		super.handleStuffedItems();
	}

	@Override
	public TileMultiBlock.NeighborTypes getNeighborType() {

		return isValidInput ? TileMultiBlock.NeighborTypes.OUTPUT : TileMultiBlock.NeighborTypes.DUCT_ATTACHMENT;
	}
}
