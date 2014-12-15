package thermaldynamics.ducts.attachments.filter;

import net.minecraft.item.ItemStack;

public interface IFilterConfig {
    public ItemStack[] getFilterStacks();

    public void onChange();

    public int filterStackGridWidth();

    public boolean getFlag(int flagType);

    public void setFlag(int flagType, boolean flag);

    public boolean canAlterFlag(int flagType);

    public String flagType(int flagType);

    public int numFlags();
}
