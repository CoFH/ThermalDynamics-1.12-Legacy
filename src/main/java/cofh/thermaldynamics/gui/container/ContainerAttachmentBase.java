package cofh.thermaldynamics.gui.container;

import cofh.thermaldynamics.block.Attachment;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.ICrafting;

public class ContainerAttachmentBase extends ContainerTDBase {

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
	public int numTileSlots() {

		return baseTile == null ? 0 : baseTile.getInvSlotCount();
	}

}
