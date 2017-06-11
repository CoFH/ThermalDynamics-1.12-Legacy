package cofh.thermaldynamics.duct.attachments.filter;

import net.minecraft.item.ItemStack;

public interface IFilterConfig {

	ItemStack[] getFilterStacks();

	void onChange();

	int filterStackGridWidth();

	boolean getFlag(int flagType);

	boolean setFlag(int flagType, boolean flag);

	boolean canAlterFlag(int flagType);

	String flagType(int flagType);

	int numFlags();

}
