package cofh.thermaldynamics.gui.slot;

import cofh.core.gui.slot.SlotFalseCopy;
import cofh.thermaldynamics.duct.entity.DuctUnitTransport;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.InventoryBasic;
import net.minecraft.item.ItemStack;

public class SlotIcon extends SlotFalseCopy {

	private static final IInventory INV = new InventoryBasic("[FALSE]", false, 0);

	private final DuctUnitTransport duct;

	public SlotIcon(int x, int y, DuctUnitTransport duct) {

		super(INV, 0, x, y);
		this.duct = duct;
	}

	@Override
	public ItemStack getStack() {

		return duct.data.item.copy();
	}

	@Override
	public void putStack(ItemStack stack) {

		if (!stack.isEmpty()) {
			stack.setCount(1);
		}
		duct.setIcon(stack);
	}

	@Override
	public void onSlotChanged() {

	}

	@Override
	public int getSlotStackLimit() {

		return 1;
	}

	@Override
	public ItemStack decrStackSize(int amount) {

		return ItemStack.EMPTY;
	}

	@Override
	public boolean isHere(IInventory inv, int slotIn) {

		return false;
	}

}
