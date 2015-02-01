package thermaldynamics.item;

import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.List;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

import thermaldynamics.block.Attachment;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.attachments.filter.FilterItem;
import thermaldynamics.ducts.item.TileItemDuct;

public class ItemFilter extends ItemAttachment {

	public ItemFilter() {

		super();
		this.setUnlocalizedName("thermaldynamics.filter");
	}

	@Override
	public String getUnlocalizedName(ItemStack item) {

		return super.getUnlocalizedName(item) + "." + item.getItemDamage();
	}

	@Override
	@SuppressWarnings("unchecked")
	public void getSubItems(Item item, CreativeTabs tab, List list) {

		for (int i = 0; i < 5; i++) {
			list.add(new ItemStack(item, 1, i));
		}
	}

	IIcon[] icons;

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir) {

		icons = new IIcon[5];
		for (int i = 0; i < 5; i++) {
			icons[i] = ir.registerIcon("thermaldynamics:filter" + i);
		}
		this.itemIcon = icons[0];
	}

	@Override
	public IIcon getIconFromDamage(int i) {

		return icons[i % icons.length];
	}

	@Override
	public Attachment getAttachment(int side, ItemStack stack, TileMultiBlock tile) {

		int type = stack.getItemDamage() % 5;
		// if (tile instanceof TileFluidDuct)
		// return new ServoFluid(tile, (byte) (side ^ 1), type);
		if (tile instanceof TileItemDuct)
			return new FilterItem(tile, (byte) (side ^ 1), type);
		return null;
	}

	public static ItemStack iron, invar, electrum, signalum, ender;

	@Override
	public boolean preInit() {

		GameRegistry.registerItem(this, "filter");

		iron = new ItemStack(this, 1, 0);
		invar = new ItemStack(this, 1, 1);
		electrum = new ItemStack(this, 1, 2);
		signalum = new ItemStack(this, 1, 3);
		ender = new ItemStack(this, 1, 4);

		return true;
	}

}
