package thermaldynamics.gui.containers;

import cofh.lib.util.helpers.FluidHelper;
import net.minecraft.item.ItemStack;
import thermaldynamics.ducts.attachments.filter.IFilterConfig;

public class SlotFilterFluid extends SlotFilter {
    public SlotFilterFluid(IFilterConfig tile, int slotIndex, int x, int y) {
        super(tile, slotIndex, x, y);
    }

    @Override
    public void putStack(ItemStack stack) {
        if (isItemValid(stack)) super.putStack(stack);
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return stack != null && FluidHelper.getFluidForFilledItem(stack) != null;
    }
}
