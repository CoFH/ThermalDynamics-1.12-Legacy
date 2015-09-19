package cofh.thermaldynamics.duct.entity.gui;

import cofh.lib.util.helpers.ItemHelper;
import cofh.thermaldynamics.duct.entity.TileTransportDuct;
import cofh.thermaldynamics.gui.container.ContainerTDBase;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerTransportConfig extends ContainerTDBase {

	private final InventoryPlayer inventory;
	private final TileTransportDuct transportDuct;

	public ContainerTransportConfig(InventoryPlayer inventory, TileTransportDuct transportDuct) {

		this.inventory = inventory;
		this.transportDuct = transportDuct;

		addPlayerInventory(inventory);

		addSlotToContainer(new SlotIcon(8, 15, transportDuct));
	}

	@Override
	protected void addPlayerInventory(InventoryPlayer inventory) {

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 53 + i * 18));
			}
		}
		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventory, i, 8 + i * 18, 111));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer p_75145_1_) {

		return true;
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {

		Slot slot = (Slot) inventorySlots.get(slotIndex);

		int invPlayer = 27;
		int invFull = invPlayer + 9;
		int invTile = invFull + 1;

		if (slot != null && slot.getHasStack()) {
			ItemStack stack = slot.getStack();
			if (slotIndex < 0) {
				return null;
			} else if (slotIndex < invFull) {
				Slot k = null;
				for (int i = invFull; i < invTile; i++) {
					Slot slot1 = (Slot) inventorySlots.get(i);
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

	@Override
	protected int numTileSlots() {

		return 1;
	}
}
