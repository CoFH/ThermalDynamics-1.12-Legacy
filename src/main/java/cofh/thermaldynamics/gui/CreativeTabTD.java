package cofh.thermaldynamics.gui;

import cofh.CoFHCore;
import cofh.core.util.CoreUtils;
import cofh.lib.util.TimeTracker;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.block.ItemBlockDuct;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.TDDucts;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.ItemStack;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class CreativeTabTD extends CreativeTabs {

	int iconIndex = 0;
	TimeTracker iconTracker = new TimeTracker();

	public CreativeTabTD() {

		super("ThermalDynamics");
	}

	@Override
	@SideOnly (Side.CLIENT)
	public ItemStack getIconItemStack() {

		updateIcon();
		return TDDucts.getDuct(iconIndex).itemStack;
	}

	@Override
	@SideOnly (Side.CLIENT)
	public ItemStack getTabIconItem() {

		return getIconItemStack();
	}

	@Override
	@SideOnly (Side.CLIENT)
	public String getTabLabel() {

		return "thermaldynamics.creativeTab";
	}

	@Override
	@SideOnly (Side.CLIENT)
	public void displayAllRelevantItems(NonNullList<ItemStack> list) {

		NonNullList<ItemStack> stacks = NonNullList.create();

		// TODO: Revisit this.
		super.displayAllRelevantItems(stacks);

		for (Duct d : TDDucts.getSortedDucts()) {
			list.add(d.itemStack.copy());

			//			if (d instanceof DuctItem) {
			//				list.add(((DuctItem) d).getDenseItemStack());
			//				list.add(((DuctItem) d).getVacuumItemStack());
			//			}
		}
		for (ItemStack item : stacks) {
			if (!(item.getItem() instanceof ItemBlockDuct)) {
				list.add(item);
			}
		}
	}

	private void updateIcon() {

		World world = CoFHCore.proxy.getClientWorld();

		if (CoreUtils.isClient() && iconTracker.hasDelayPassed(world, 80)) {
			int next = MathHelper.RANDOM.nextInt(TDDucts.ductList.size() - 1);
			iconIndex = next >= iconIndex ? next + 1 : next;
			iconTracker.markTime(world);
		}
	}

}
