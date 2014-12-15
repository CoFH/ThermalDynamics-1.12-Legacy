package thermaldynamics.ducts.attachments.filter;

import cofh.lib.gui.GuiBase;
import cofh.lib.gui.element.TabBase;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public interface IItemConfigurable {
    // Create a config Tab which is added to the gui when the itemstack is selected as a filter, you can modify the supplied nbt tag which will be applied to the itemstack. You cannot modify the itemstack directly. If the nbt tag has no keys, then the itemstack's nbt will be set to null
    public TabBase getConfigTab(ItemStack filter, GuiBase gui, NBTTagCompound nbtTag, Slot[] requestedSlots);

    public int numSlotsNeeded();

    public Slot[] createSlots(int[] slotNumbers);
}
