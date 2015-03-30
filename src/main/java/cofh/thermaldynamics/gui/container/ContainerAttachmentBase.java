package cofh.thermaldynamics.gui.container;

import cofh.lib.gui.slot.SlotFalseCopy;
import cofh.lib.util.helpers.ItemHelper;
import cofh.thermaldynamics.block.Attachment;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;

public class ContainerAttachmentBase extends Container {

	Attachment baseTile;

	public ContainerAttachmentBase() {

	}

	public ContainerAttachmentBase(Attachment tile) {

		baseTile = tile;
	}

	public ContainerAttachmentBase(InventoryPlayer inventory, Attachment tile) {

		baseTile = tile;

		/* Player Inventory */
		addPlayerInventory(inventory);
	}

	protected void addPlayerInventory(InventoryPlayer inventory) {

		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 9; j++) {
				addSlotToContainer(new Slot(inventory, j + i * 9 + 9, 8 + j * 18, 123 + i * 18));
			}
		}
		for (int i = 0; i < 9; i++) {
			addSlotToContainer(new Slot(inventory, i, 8 + i * 18, 181));
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {

		return baseTile == null || baseTile.isUseable(player);
	}

	@Override
	public void detectAndSendChanges() {

		super.detectAndSendChanges();

		if (baseTile == null) {
			return;
		}
		baseTile.sendGuiNetworkData(this, crafters, false);
	}

	@Override
	public void addCraftingToCrafters(ICrafting crafter) {

		super.addCraftingToCrafters(crafter);
		baseTile.sendGuiNetworkData(this, crafters, true);
	}

	@Override
	public void updateProgressBar(int i, int j) {

		if (baseTile == null) {
			return;
		}
		baseTile.receiveGuiNetworkData(i, j);
	}

	@Override
	public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {

		ItemStack stack = null;
		Slot slot = (Slot) inventorySlots.get(slotIndex);

		int invPlayer = 27;
		int invFull = invPlayer + 9;
		int invTile = invFull + (baseTile == null ? 0 : baseTile.getInvSlotCount());

		if (slot != null && slot.getHasStack()) {
			ItemStack stackInSlot = slot.getStack();
			stack = stackInSlot.copy();

			if (slotIndex < 0) {
				if (!this.mergeItemStack(stackInSlot, 0, invFull, true)) {
					return null;
				}
			} else if (slotIndex < invFull) {
				if (!this.mergeItemStack(stackInSlot, invFull, invTile, false)) {
					return null;
				}
			} else if (!this.mergeItemStack(stackInSlot, 0, invFull, true)) {
				return null;
			}
			if (stackInSlot.stackSize <= 0) {
				slot.putStack((ItemStack) null);
			} else {
				slot.onSlotChanged();
			}
			if (stackInSlot.stackSize == stack.stackSize) {
				return null;
			}
		}
		return stack;
	}

	@Override
	public ItemStack slotClick(int slotId, int mouseButton, int modifier, EntityPlayer player) {

		Slot slot = slotId < 0 ? null : (Slot) this.inventorySlots.get(slotId);
		if (slot instanceof SlotFalseCopy) {
			if (mouseButton == 2) {
				slot.putStack(null);
				slot.onSlotChanged();
			} else {
				slot.putStack(player.inventory.getItemStack() == null ? null : player.inventory.getItemStack().copy());
			}
			return player.inventory.getItemStack();
		}
		return super.slotClick(slotId, mouseButton, modifier, player);
	}

	@Override
	protected boolean mergeItemStack(ItemStack stack, int slotMin, int slotMax, boolean ascending) {

		boolean slotFound = false;
		int k = ascending ? slotMax - 1 : slotMin;

		Slot slot;
		ItemStack stackInSlot;

		if (stack.isStackable()) {
			while (stack.stackSize > 0 && (!ascending && k < slotMax || ascending && k >= slotMin)) {
				slot = (Slot) this.inventorySlots.get(k);
				stackInSlot = slot.getStack();

				if (slot.isItemValid(stack) && ItemHelper.itemsEqualWithMetadata(stack, stackInSlot, true)) {
					int l = stackInSlot.stackSize + stack.stackSize;
					int slotLimit = Math.min(stack.getMaxStackSize(), slot.getSlotStackLimit());

					if (l <= slotLimit) {
						stack.stackSize = 0;
						stackInSlot.stackSize = l;
						slot.onSlotChanged();
						slotFound = true;
					} else if (stackInSlot.stackSize < slotLimit) {
						stack.stackSize -= slotLimit - stackInSlot.stackSize;
						stackInSlot.stackSize = slotLimit;
						slot.onSlotChanged();
						slotFound = true;
					}
				}
				k += ascending ? -1 : 1;
			}
		}
		if (stack.stackSize > 0) {
			k = ascending ? slotMax - 1 : slotMin;

			while (!ascending && k < slotMax || ascending && k >= slotMin) {
				slot = (Slot) this.inventorySlots.get(k);
				stackInSlot = slot.getStack();

				if (slot.isItemValid(stack) && stackInSlot == null) {
					slot.putStack(ItemHelper.cloneStack(stack, Math.min(stack.stackSize, slot.getSlotStackLimit())));
					slot.onSlotChanged();

					if (slot.getStack() != null) {
						stack.stackSize -= slot.getStack().stackSize;
						slotFound = true;
					}
					break;
				}
				k += ascending ? -1 : 1;
			}
		}
		return slotFound;
	}

}
