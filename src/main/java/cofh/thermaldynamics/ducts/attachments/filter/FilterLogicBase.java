package cofh.thermaldynamics.ducts.attachments.filter;

import net.minecraft.item.ItemStack;

public abstract class FilterLogicBase implements IFilterConfig {

	@Override
	public ItemStack[] getFilterStacks() {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void onChange() {

		// TODO Auto-generated method stub

	}

	@Override
	public int filterStackGridWidth() {

		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean getFlag(int flagType) {

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean setFlag(int flagType, boolean flag) {

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean canAlterFlag(int flagType) {

		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String flagType(int flagType) {

		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int numFlags() {

		// TODO Auto-generated method stub
		return 0;
	}

}
