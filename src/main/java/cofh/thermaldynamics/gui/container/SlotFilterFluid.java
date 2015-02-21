package cofh.thermaldynamics.gui.container;

import cofh.lib.util.helpers.FluidHelper;
import cofh.thermaldynamics.ducts.attachments.filter.IFilterConfig;
import net.minecraft.item.ItemStack;

public class SlotFilterFluid extends SlotFilter {

	public SlotFilterFluid(IFilterConfig tile, int slotIndex, int x, int y) {

		super(tile, slotIndex, x, y);
	}

	@Override
	public void putStack(ItemStack stack) {

		if (stack == null || isItemValid(stack)) {
			super.putStack(stack);
		}
	}

	@Override
	public boolean isItemValid(ItemStack stack) {

		return stack != null && FluidHelper.getFluidForFilledItem(stack) != null;
	}

}
