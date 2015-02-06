package cofh.thermaldynamics.ducts.attachments;

import net.minecraft.item.ItemStack;

public interface IStuffable {

	public void stuffItem(ItemStack item);

	public boolean canStuff();

	public boolean isStuffed();

}
