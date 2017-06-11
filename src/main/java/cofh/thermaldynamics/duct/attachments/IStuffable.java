package cofh.thermaldynamics.duct.attachments;

import net.minecraft.item.ItemStack;

public interface IStuffable {

	void stuffItem(ItemStack item);

	boolean canStuff();

	boolean isStuffed();

}
