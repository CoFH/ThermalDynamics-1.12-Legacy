package cofh.thermaldynamics.duct.attachments.retriever;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import cofh.lib.util.helpers.ItemHelper;
import cofh.thermaldynamics.block.AttachmentRegistry;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.attachments.servo.ServoItem;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.duct.item.TravelingItem;
import cofh.thermaldynamics.init.TDItems;
import cofh.thermaldynamics.init.TDTextures;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.render.RenderDuct;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.BlockRenderLayer;
import net.minecraft.world.IBlockAccess;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import net.minecraftforge.items.IItemHandler;

import java.util.Iterator;

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

		return new ItemStack(TDItems.itemRetriever, 1, type);
	}

	@Override
	public String getName() {

		return "item.thermaldynamics.retriever." + type + ".name";
	}

	@Override
	@SideOnly (Side.CLIENT)
	public boolean render(IBlockAccess world, BlockRenderLayer layer, CCRenderState ccRenderState) {

		if (layer != BlockRenderLayer.SOLID) {
			return false;
		}

		Translation trans = Vector3.fromTileCenter(tile).translation();
		RenderDuct.modelConnection[isPowered ? 1 : 2][side].render(ccRenderState, trans, new IconTransformation(TDTextures.RETRIEVER_BASE[stuffed ? 1 : 0][type]));
		return true;
	}

	@Override
	public void postNeighbourChange() {

		baseTileHasOtherOutputs = false;
		for (int i = 0; i < 6; i++) {
			if ((tile.neighborTypes[i] == TileTDBase.NeighborTypes.OUTPUT || tile.neighborTypes[i] == TileTDBase.NeighborTypes.INPUT) && (tile.attachments[i] == null || tile.attachments[i].getId() != AttachmentRegistry.RETRIEVER_ITEM)) {
				baseTileHasOtherOutputs = true;
				break;
			}
		}
		super.postNeighbourChange();

	}

	@Override
	public void handleItemSending() {

		IItemHandler simulatedInv = cachedInv;

		//StackMap travelingItems = itemDuct.internalGrid.travelingItems.get(itemDuct.getPos().offset(EnumFacing.VALUES[side]));
		//if (travelingItems != null) {
		//    for (TObjectIntIterator<StackMap.ItemEntry> iterator = travelingItems.iterator(); iterator.hasNext(); ) {
		//        iterator.advance();
		//        InventoryHelper.insertStackIntoInventory(simulatedInv, iterator.key().toItemStack(iterator.value()), false);
		//    }
		//}

		for (Route route : routeList) {
			TileItemDuct endPoint = (TileItemDuct) route.endPoint;

			for (int k = 0; k < 6; k++) {
				int i = (endPoint.internalSideCounter + k) % 6;

				if (endPoint.attachments[i] != null && endPoint.attachments[i].getId() == AttachmentRegistry.RETRIEVER_ITEM) {
					continue;
				}

				if (endPoint.cache == null || endPoint.cache.handlerCache[i] == null || (endPoint.neighborTypes[i] != TileTDBase.NeighborTypes.OUTPUT && endPoint.neighborTypes[i] != TileTDBase.NeighborTypes.INPUT) || !endPoint.connectionTypes[i].allowTransfer) {
					continue;
				}

				{
					IItemHandler inv = endPoint.cache.handlerCache[i];
					for (int slot = 0; slot < inv.getSlots(); slot++) {
						ItemStack item = inv.getStackInSlot(slot);
						if (item == null) {
							continue;
						}

						item = limitOutput(ItemHelper.cloneStack(item, multiStack[type] ? item.getMaxStackSize() : item.stackSize), simulatedInv, slot, side);
						if (item == null || item.stackSize == 0) {
							continue;
						}

						if (!filter.matchesFilter(item) || !endPoint.cache.filterCache[i].matchesFilter(item)) {
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
						item = inv.extractItem(slot, maxStackSize, false);
						if (item == null || item.stackSize == 0) {
							continue;
						}

						// No turning back now
						route1 = route1.copy();
						route1.pathDirections.add(side);

						if (multiStack[type] && item.stackSize < maxStackSize) {
							for (; item.stackSize < maxStackSize && slot < inv.getSlots(); slot++) {
								if (ItemHelper.itemsEqualWithMetadata(inv.getStackInSlot(slot), item, true)) {
									ItemStack extract = inv.extractItem(slot, maxStackSize - item.stackSize, false);
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

		for (Iterator<ItemStack> iterator = stuffedItems.iterator(); iterator.hasNext(); ) {
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
