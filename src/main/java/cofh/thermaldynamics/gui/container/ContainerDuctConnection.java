package cofh.thermaldynamics.gui.container;

import cofh.lib.util.helpers.ItemHelper;
import cofh.thermaldynamics.duct.attachments.ConnectionBase;
import cofh.thermaldynamics.duct.attachments.filter.FilterLogic;
import cofh.thermaldynamics.gui.slot.SlotFilter;
import cofh.thermaldynamics.gui.slot.SlotFilterFluid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

import java.util.LinkedList;

public class ContainerDuctConnection extends ContainerAttachmentBase {

	private final ConnectionBase tile;
	public final FilterLogic filter;
	public LinkedList<SlotFilter> filterSlots = new LinkedList<>();
	public final int gridWidth;
	public final int gridHeight;
	public final int gridX0;
	public final int gridY0;

	public ContainerDuctConnection(InventoryPlayer inventory, ConnectionBase tile) {

		super(inventory, tile);
		this.tile = tile;

		filter = tile.getFilter();

		assert filter != null;

		int n = filter.getFilterStacks().length;
		gridWidth = filter.filterStackGridWidth();
		gridHeight = n / gridWidth;

		gridX0 = 89 - gridWidth * 9;

		switch (gridHeight) {
			case 1:
				gridY0 = 38;
				break;
			case 2:
				gridY0 = 29;
				break;
			default:
				gridY0 = 20;
				break;
		}
		for (int i = 0; i < gridHeight; i++) {
			for (int j = 0; j < gridWidth; j++) {
				if (filter.isItem()) {
					filterSlots.add(((SlotFilter) addSlotToContainer(new SlotFilter(filter, j + i * gridWidth, gridX0 + j * 18, gridY0 + i * 18))));
				} else {
					filterSlots.add(((SlotFilter) addSlotToContainer(new SlotFilterFluid(filter, j + i * gridWidth, gridX0 + j * 18, gridY0 + i * 18))));
				}
			}
		}
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {

		Slot slot = inventorySlots.get(slotIndex);

		int invPlayer = 27;
		int invFull = invPlayer + 9;
		int invTile = invFull + filter.getFilterStacks().length;

		if (slot != null && slot.getHasStack()) {
			ItemStack stack = slot.getStack();
			if (slotIndex < 0) {
				return null;
			} else if (slotIndex < invFull) {
				Slot k = null;
				for (int i = invFull; i < invTile; i++) {
					Slot slot1 = inventorySlots.get(i);
					if (!slot1.getHasStack()) {
						if (k == null) {
							k = slot1;
						}
					} else {
						if (ItemHelper.itemsEqualWithMetadata(slot1.getStack(), stack)) {
							return null;
						}
					}
				}
				if (k != null) {
					k.putStack(stack.copy());
				}

				return null;
			} else {
				slot.putStack(null);
				slot.onSlotChanged();

			}
		}
		return null;
	}

}
