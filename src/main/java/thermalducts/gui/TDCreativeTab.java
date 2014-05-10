package thermalducts.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class TDCreativeTab extends CreativeTabs {

	static ItemStack iconStack;

	public static void initialize() {

		iconStack = new ItemStack(Items.apple);
	}

	public TDCreativeTab() {

		super("ThermalDucts");
	}

	@Override
	@SideOnly(Side.CLIENT)
	public ItemStack getIconItemStack() {

		return iconStack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public Item getTabIconItem() {

		return getIconItemStack().getItem();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public String getTabLabel() {

		return "thermalducts.creativeTab";
	}

}
