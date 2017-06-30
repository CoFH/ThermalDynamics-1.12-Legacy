package cofh.thermaldynamics.item;

import cofh.lib.util.helpers.StringHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.attachments.filter.FilterFluid;
import cofh.thermaldynamics.duct.attachments.filter.FilterItem;
import cofh.thermaldynamics.duct.attachments.filter.FilterLogic;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.ModelResourceLocation;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.EnumRarity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;

public class ItemFilter extends ItemAttachment {

	public static EnumRarity[] rarity = { EnumRarity.COMMON, EnumRarity.COMMON, EnumRarity.UNCOMMON, EnumRarity.UNCOMMON, EnumRarity.RARE };
	public static ItemStack basicFilter, hardenedFilter, reinforcedFilter, signalumFilter, resonantFilter;

	public ItemFilter() {

		super();
		this.setUnlocalizedName("thermaldynamics.filter");
	}

	@Override
	public String getUnlocalizedName(ItemStack item) {

		return super.getUnlocalizedName(item) + "." + item.getItemDamage();
	}

	@Override
	public void getSubItems(CreativeTabs tab, NonNullList<ItemStack> items) {

		if (isInCreativeTab(tab)) {
			for (int i = 0; i < 5; i++) {
				items.add(new ItemStack(this, 1, i));
			}
		}
	}

	@Override
	public EnumRarity getRarity(ItemStack stack) {

		return rarity[stack.getItemDamage() % 5];
	}

	@Override
	public Attachment getAttachment(EnumFacing side, ItemStack stack, TileGrid tile) {

		int type = stack.getItemDamage() % 5;

		if (tile.getDuct(DuctToken.FLUID) != null) {
			return new FilterFluid(tile, (byte) (side.ordinal() ^ 1), type);
		}
		if (tile.getDuct(DuctToken.ITEMS) != null) {
			return new FilterItem(tile, (byte) (side.ordinal() ^ 1), type);
		}
		return null;
	}

	@Override
	public void addInformation(ItemStack stack, @Nullable World worldIn, List<String> tooltip, ITooltipFlag flagIn) {

		super.addInformation(stack, worldIn, tooltip, flagIn);

		int type = stack.getItemDamage() % 5;

		if (!StringHelper.isShiftKeyDown()) {
			tooltip.add(StringHelper.getInfoText("item.thermaldynamics.filter.info"));

			if (StringHelper.displayShiftForDetail) {
				tooltip.add(StringHelper.shiftForDetails());
			}
			return;
		}
		tooltip.add(StringHelper.YELLOW + StringHelper.localize("info.cofh.items") + StringHelper.END);
		addFiltering(tooltip, type, Duct.Type.ITEM);
		tooltip.add(StringHelper.YELLOW + StringHelper.localize("info.cofh.fluids") + StringHelper.END);
		addFiltering(tooltip, type, Duct.Type.FLUID);
	}

	public static void addFiltering(List<String> list, int type, Duct.Type duct) {

		StringBuilder b = new StringBuilder();

		b.append(StringHelper.localize("info.thermaldynamics.filter.options")).append(": ").append(StringHelper.WHITE);
		boolean flag = false;
		for (int i = 0; i < FilterLogic.flagTypes.length; i++) {
			if (FilterLogic.canAlterFlag(duct, type, i)) {
				if (flag) {
					b.append(", ");
				} else {
					flag = true;
				}
				b.append(StringHelper.localize("info.thermaldynamics.filter." + FilterLogic.flagTypes[i]));
			}
		}
		flag = false;
		for (String s : Minecraft.getMinecraft().fontRenderer.listFormattedStringToWidth(b.toString(), 140)) {
			if (flag) {
				s = "  " + StringHelper.WHITE + s;
			}
			flag = true;
			list.add("  " + s + StringHelper.END);
		}
	}

	/* IInitializer */
	@Override
	public boolean preInit() {

		ForgeRegistries.ITEMS.register(setRegistryName("filter"));

		basicFilter = new ItemStack(this, 1, 0);
		hardenedFilter = new ItemStack(this, 1, 1);
		reinforcedFilter = new ItemStack(this, 1, 2);
		signalumFilter = new ItemStack(this, 1, 3);
		resonantFilter = new ItemStack(this, 1, 4);

		ThermalDynamics.proxy.addIModelRegister(this);

		return true;
	}

	@Override
	public boolean initialize() {

		return true;
	}

	@Override
	public void registerModels() {

		String[] names = { "basic", "hardened", "reinforced", "signalum", "resonant" };
		for (int i = 0; i < names.length; i++) {
			ModelResourceLocation location = new ModelResourceLocation("thermaldynamics:attachment", "type=" + this.getRegistryName().getResourcePath() + "_" + names[i]);
			ModelLoader.setCustomModelResourceLocation(this, i, location);
		}
	}
}
