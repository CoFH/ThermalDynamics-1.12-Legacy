package thermaldynamics.ducts.servo;

import net.minecraft.item.ItemStack;

public interface IStuffable {
    public void stuffItem(ItemStack item);

    public boolean canStuff();
}
