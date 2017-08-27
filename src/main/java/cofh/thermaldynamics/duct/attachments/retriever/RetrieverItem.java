package cofh.thermaldynamics.duct.attachments.retriever;

import codechicken.lib.render.CCRenderState;
import codechicken.lib.vec.Translation;
import codechicken.lib.vec.Vector3;
import codechicken.lib.vec.uv.IconTransformation;
import cofh.lib.util.helpers.ItemHelper;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.AttachmentRegistry;
import cofh.thermaldynamics.duct.attachments.servo.ServoItem;
import cofh.thermaldynamics.duct.item.DuctUnitItem;
import cofh.thermaldynamics.duct.item.TravelingItem;
import cofh.thermaldynamics.duct.tiles.TileGrid;
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

	public RetrieverItem(TileGrid tile, byte side) {

		super(tile, side);
	}

	public RetrieverItem(TileGrid tile, byte side, int type) {

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
	public boolean allowDuctConnection() {

		return true;
	}

	@Override
	@SideOnly (Side.CLIENT)
	public boolean render(IBlockAccess world, BlockRenderLayer layer, CCRenderState ccRenderState) {

		if (layer != BlockRenderLayer.SOLID) {
			return false;
		}

		Translation trans = Vector3.fromTileCenter(baseTile).translation();
		RenderDuct.modelConnection[isPowered ? 1 : 2][side].render(ccRenderState, trans, new IconTransformation(TDTextures.RETRIEVER_BASE[stuffed ? 1 : 0][type]));
		return true;
	}

	@Override
	public void postNeighborChange() {

		baseTileHasOtherOutputs = false;
		for (int i = 0; i < 6; i++) {
			if ((itemDuct.isOutput(side) || itemDuct.isInput(side)) && (baseTile.getAttachment(side) == null || baseTile.getAttachment(side).getId() != AttachmentRegistry.RETRIEVER_ITEM)) {
				baseTileHasOtherOutputs = true;
				break;
			}
		}
		super.postNeighborChange();

	}

	@Override
	public void handleItemSending() {

		IItemHandler simulatedInv = getCachedInv();

		for (Route route : routesWithInsertSideList) {
			DuctUnitItem endPoint = (DuctUnitItem) route.endPoint;

			int i = route.getLastSide();

			Attachment attachment = endPoint.parent.getAttachment(i);
			if (attachment != null && attachment.getId() == AttachmentRegistry.RETRIEVER_ITEM) {
				continue;
			}

			DuctUnitItem.Cache cache = endPoint.tileCache[i];

			if (cache == null || (!endPoint.isInput(i) && !endPoint.isOutput(i)) || !endPoint.parent.getConnectionType(i).allowTransfer) {
				continue;
			}

			{
				IItemHandler inv = cache.getItemHandler(i ^ 1);
				if (inv == null) {
					continue;
				}

				for (int slot = 0; slot < inv.getSlots(); slot++) {
					ItemStack item = inv.getStackInSlot(slot);
					if (item.isEmpty()) {
						continue;
					}

					item = limitOutput(ItemHelper.cloneStack(item, multiStack[type] ? item.getMaxStackSize() : item.getCount()), simulatedInv, slot, side);
					if (item.isEmpty() || item.getCount() == 0) {
						continue;
					}

					if (!filter.matchesFilter(item) || !cache.filter.matchesFilter(item)) {
						continue;
					}

					ItemStack remainder = DuctUnitItem.simulateInsertItemStackIntoInventory(simulatedInv, item.copy(), side ^ 1, filter.getMaxStock());

					if (!remainder.isEmpty()) {
						item.shrink(remainder.getCount());
					}
					if (item.getCount() <= 0) {
						continue;
					}

					Route route1 = endPoint.getRoute(itemDuct);
					if (route1 == null) {
						continue;
					}

					int maxStackSize = item.getCount();
					item = inv.extractItem(slot, maxStackSize, false);
					if (item.isEmpty() || item.getCount() == 0) {
						continue;
					}

					// No turning back now
					route1 = route1.copy();
					route1.pathDirections.add(side);

					if (multiStack[type] && item.getCount() < maxStackSize) {
						for (; item.getCount() < maxStackSize && slot < inv.getSlots(); slot++) {
							if (ItemHelper.itemsEqualWithMetadata(inv.getStackInSlot(slot), item, true)) {
								ItemStack extract = inv.extractItem(slot, maxStackSize - item.getCount(), false);
								if (!extract.isEmpty()) {
									item.grow(extract.getCount());
								}
							}
						}
					}

					endPoint.insertNewItem(new TravelingItem(item, endPoint, route1, (byte) (i ^ 1), getSpeed()));
					routesWithInsertSideList.advanceCursor();
					return;
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

			stuffedItem.setCount(itemDuct.insertIntoInventory(stuffedItem, side));
			if (stuffedItem.getCount() <= 0) {
				iterator.remove();
			}
		}
		super.handleStuffedItems();
	}

	/* IPortableData */
	@Override
	public void writePortableData(EntityPlayer player, NBTTagCompound tag) {

		super.writePortableData(player, tag);
		tag.setString("DisplayType", "item.thermaldynamics.retriever.0.name");
	}

}
