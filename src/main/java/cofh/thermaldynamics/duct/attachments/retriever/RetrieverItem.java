package cofh.thermaldynamics.duct.attachments.retriever;

import cofh.core.render.RenderUtils;
import cofh.lib.util.helpers.InventoryHelper;
import cofh.lib.util.helpers.ItemHelper;
import cofh.repack.codechicken.lib.vec.BlockCoord;
import cofh.repack.codechicken.lib.vec.Translation;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.AttachmentRegistry;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.attachments.servo.ServoItem;
import cofh.thermaldynamics.duct.item.SimulatedInv;
import cofh.thermaldynamics.duct.item.StackMap;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TravelingItem;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.render.RenderDuct;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import gnu.trove.iterator.TObjectIntIterator;

import java.util.Iterator;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class RetrieverItem extends ServoItem {

	boolean baseTileHasOtherOutputs = false;

	public RetrieverItem(TileTDBase tile, byte side) {

		super(tile, side);
	}

	public RetrieverItem(TileTDBase tile, byte side, int type) {

		super(tile, side, type);
	}

	@Override
	public int getId() {

		return AttachmentRegistry.RETRIEVER_ITEM;
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
	@SideOnly(Side.CLIENT)
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
	public void postNeighbourChange() {

		baseTileHasOtherOutputs = false;
		for (int i = 0; i < 6; i++) {
			if ((tile.neighborTypes[i] == TileTDBase.NeighborTypes.OUTPUT || tile.neighborTypes[i] == TileTDBase.NeighborTypes.INPUT)
					&& (tile.attachments[i] == null || tile.attachments[i].getId() != AttachmentRegistry.RETRIEVER_ITEM)) {
				baseTileHasOtherOutputs = true;
				break;
			}
		}
		super.postNeighbourChange();

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

				if (endPoint.attachments[i] != null && endPoint.attachments[i].getId() == AttachmentRegistry.RETRIEVER_ITEM) {
					continue;
				}

				if (!endPoint.cachesExist() || endPoint.cache[i] == null
						|| (endPoint.neighborTypes[i] != TileTDBase.NeighborTypes.OUTPUT && endPoint.neighborTypes[i] != TileTDBase.NeighborTypes.INPUT)
						|| !endPoint.connectionTypes[i].allowTransfer) {
					continue;
				}

				if (endPoint.cache2[i] != null) {
					ISidedInventory inv = endPoint.cache2[i];
					int[] accessibleSlotsFromSide = inv.getAccessibleSlotsFromSide(i ^ 1);
					for (int j = 0; j < accessibleSlotsFromSide.length; j++) {
						int slot = accessibleSlotsFromSide[j];

						ItemStack item = inv.getStackInSlot(slot);
						if (item == null || item.stackSize <= 0) {
							continue;
						}

						int realSize = item.stackSize;
						item = limitOutput(ItemHelper.cloneStack(item, multiStack[type] ? item.getMaxStackSize() : item.stackSize), simulatedInv, slot, side);
						if (item == null || item.stackSize == 0) {
							continue;
						}

						if (!inv.canExtractItem(slot, item, i ^ 1)) {
							continue;
						}

						if (!filter.matchesFilter(item) || !endPoint.filterCache[i].matchesFilter(item)) {
							continue;
						}

						ItemStack remainder = TileItemDuct.simulateInsertItemStackIntoInventory(simulatedInv, item.copy(), side ^ 1, filter.getMaxStock());

						if (remainder != null) {
							item.stackSize -= remainder.stackSize;
						}
						if (item.stackSize == 0) {
							continue;
						}

						Route route1 = endPoint.getRoute(itemDuct);
						if (route1 == null) {
							continue;
						}

						int maxStackSize = item.stackSize;
						item = inv.decrStackSize(slot, Math.min(maxStackSize, realSize));
						if (item == null || item.stackSize == 0) {
							continue;
						}

						// No turning back now
						route1 = route1.copy();
						route1.pathDirections.add(side);

						if (multiStack[type] && item.stackSize < maxStackSize) {
							for (; item.stackSize < maxStackSize && j < accessibleSlotsFromSide.length; j++) {
								slot = accessibleSlotsFromSide[j];
								ItemStack inSlot = inv.getStackInSlot(slot);
								if (inSlot != null && inSlot.stackSize> 0 && inv.canExtractItem(slot, inSlot, i ^ 1) && ItemHelper.itemsEqualWithMetadata(inSlot, item, true)) {
									ItemStack extract = inv.decrStackSize(slot, Math.min(maxStackSize - item.stackSize, inSlot.stackSize));
									if (extract != null) {
										item.stackSize += extract.stackSize;
									}
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
						if (item == null || item.stackSize <= 0) {
							continue;
						}

						int realSize = item.stackSize;
						item = limitOutput(ItemHelper.cloneStack(item, multiStack[type] ? item.getMaxStackSize() : item.stackSize), simulatedInv, slot, side);
						if (item == null || item.stackSize == 0) {
							continue;
						}

						if (!filter.matchesFilter(item) || !endPoint.filterCache[i].matchesFilter(item)) {
							continue;
						}

						ItemStack remainder = TileItemDuct.simulateInsertItemStackIntoInventory(simulatedInv, item.copy(), side ^ 1, filter.getMaxStock());

						if (remainder != null) {
							item.stackSize -= remainder.stackSize;
						}
						if (item.stackSize <= 0) {
							continue;
						}

						Route route1 = endPoint.getRoute(itemDuct);
						if (route1 == null) {
							continue;
						}

						int maxStackSize = item.stackSize;
						item = inv.decrStackSize(slot, Math.min(maxStackSize, realSize));
						if (item == null || item.stackSize == 0) {
							continue;
						}

						// No turning back now
						route1 = route1.copy();
						route1.pathDirections.add(side);

						if (multiStack[type] && item.stackSize < maxStackSize) {
							for (; item.stackSize < maxStackSize && slot < inv.getSizeInventory(); slot++) {
								ItemStack inSlot = inv.getStackInSlot(slot);
								if (inSlot != null && inSlot.stackSize > 0 && ItemHelper.itemsEqualWithMetadata(inSlot, item, true)) {
									ItemStack extract = inv.decrStackSize(slot, Math.min(maxStackSize - item.stackSize, inSlot.stackSize));
									if (extract != null) {
										item.stackSize += extract.stackSize;
									}
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
			if (!filter.matchesFilter(stuffedItem)) {
				continue;
			}

			stuffedItem.stackSize = itemDuct.insertIntoInventory(stuffedItem, side);
			if (stuffedItem.stackSize <= 0) {
				iterator.remove();
			}
		}
		super.handleStuffedItems();
	}

	@Override
	public TileTDBase.NeighborTypes getNeighborType() {

		return isValidInput ? TileTDBase.NeighborTypes.OUTPUT : TileTDBase.NeighborTypes.DUCT_ATTACHMENT;
	}

	/* IPortableData */
	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {

		super.writePortableData(player, tag);
		tag.setString("DisplayType", "item.thermaldynamics.retriever.0.name");
	}

}
