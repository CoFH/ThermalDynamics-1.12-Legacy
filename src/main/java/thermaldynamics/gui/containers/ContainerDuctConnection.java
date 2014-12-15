package thermaldynamics.gui.containers;

import cofh.lib.util.helpers.ItemHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import thermaldynamics.ducts.attachments.ConnectionBase;
import thermaldynamics.ducts.attachments.filter.FilterLogic;

import java.util.LinkedList;

public class ContainerDuctConnection extends ContainerAttachmentBase {

    private ConnectionBase tile;
    public final FilterLogic filter;
    public LinkedList<SlotFilter> filterSlots = new LinkedList<SlotFilter>();
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
        gridY0 = 20;

        for (int i = 0; i < gridHeight; i++) {
            for (int j = 0; j < gridWidth; j++) {
                filterSlots.add(((SlotFilter) addSlotToContainer(new SlotFilter(filter, j + i * gridWidth, gridX0 + j * 18, gridY0 + i * 18))));
            }
        }
    }

    @Override
    public ItemStack transferStackInSlot(EntityPlayer player, int slotIndex) {

        Slot slot = (Slot) inventorySlots.get(slotIndex);

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
                    Slot slot1 = (Slot) inventorySlots.get(i);
                    if (!slot1.getHasStack()) {
                        if (k == null) k = slot1;
                    } else {
                        if (ItemHelper.itemsEqualWithMetadata(slot1.getStack(), stack))
                            return null;
                    }
                }
                if (k != null)
                    k.putStack(stack.copy());

                return null;
            } else {
                slot.putStack(null);
                slot.onSlotChanged();

            }
        }
        return null;
    }
}
