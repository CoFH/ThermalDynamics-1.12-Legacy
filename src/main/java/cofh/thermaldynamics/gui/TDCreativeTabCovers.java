package cofh.thermaldynamics.gui;

import cofh.CoFHCore;
import cofh.core.util.CoreUtils;
import cofh.lib.util.TimeTracker;
import cofh.lib.util.helpers.MathHelper;
import cofh.thermaldynamics.item.ItemCover;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class TDCreativeTabCovers extends CreativeTabs {

	int iconIndex = 0;
	TimeTracker iconTracker = new TimeTracker();

	public TDCreativeTabCovers() {

		super("ThermalDynamicsCovers");
	}

	@Override
	public ItemStack getIconItemStack() {

		updateIcon();
		return ItemCover.getCoverList().get(iconIndex);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getTabIconItem() {

		return getIconItemStack().getItem();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getTabLabel() {

		return "thermaldynamics.creativeTabCovers";
	}

	private void updateIcon() {

		World world = CoFHCore.proxy.getClientWorld();

		if (CoreUtils.isClient() && iconTracker.hasDelayPassed(world, 80) && ItemCover.getCoverList().size() > 0) {
			int next = MathHelper.RANDOM.nextInt(ItemCover.getCoverList().size() - 1);
			iconIndex = next >= iconIndex ? next + 1 : next;
			iconTracker.markTime(world);
		}
	}

}
