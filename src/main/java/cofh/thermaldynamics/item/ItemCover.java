package cofh.thermaldynamics.item;

import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.Attachment;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.attachments.cover.CoverHelper;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.text.translation.I18n;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ItemCover extends ItemAttachment {

	private static float[] hitX = { 0.5F, 0.5F, 0.5F, 0.5F, 0, 1 };
	private static float[] hitY = { 0, 1, 0.5F, 0.5F, 0.5F, 0.5F };
	private static float[] hitZ = { 0.5F, 0.5F, 0, 1, 0.5F, 0.5F };

	public static boolean enableCreativeTab = true;
	public static boolean showInNEI = false;

	private static List<ItemStack> coverList;

	public ItemCover() {

		this.setCreativeTab(ThermalDynamics.tabCovers);
		this.setUnlocalizedName("thermaldynamics.cover");
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {

		List<ItemStack> coverList = getCoverList();
		for (int i = 0; i < coverList.size(); i++) {
			list.add(coverList.get(i));
		}
	}

	@Override
	public Attachment getAttachment(EnumFacing side, ItemStack stack, TileTDBase tile) {

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey("Meta", 1) || !nbt.hasKey("Block", 8)) {
			return null;
		}

		int meta = nbt.getByte("Meta");//FIXME Use a state instead of meta and block.
		Block block = Block.getBlockFromName(nbt.getString("Block"));

		if (block == Blocks.AIR || meta < 0 || meta >= 16 || !CoverHelper.isValid(block, meta)) {
			nbt.removeTag("Meta");
			nbt.removeTag("Block");
			if (nbt.hasNoTags()) {
				stack.setTagCompound(null);
			}
			return null;
		}

		return new Cover(tile, ((byte) (side.ordinal() ^ 1)), block.getStateFromMeta(meta));
	}

	@Override
	public String getItemStackDisplayName(ItemStack item) {

		ItemStack b = CoverHelper.getCoverItemStack(item, true);
		String name = "";
		if (b != null) {
			String unloc = getUnlocalizedNameInefficiently(item) + ".", unloc2 = b.getItem().getUnlocalizedNameInefficiently(b);
			if (I18n.canTranslate(unloc + unloc2 + ".name")) {
				return I18n.translateToLocal(unloc + unloc2 + ".name");
			}
			name = b.getDisplayName();
		}
		return I18n.translateToLocalFormatted(getUnlocalizedNameInefficiently(item) + ".name", name);
	}

	@Override
	public boolean preInit() {

		GameRegistry.registerItem(this, "cover");
		return true;
	}

	public static void createCoverList() {

		coverList = new ArrayList<ItemStack>();

		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();

		ArrayList<Item> data = new ArrayList<Item>();
		for (Item item : ForgeRegistries.ITEMS) {
			// iterate over the keySet instead of all values (compatible with overridden items)
			data.add(item);
		}
		Collections.sort(data, new Comparator<Item>() {

			@Override
			public int compare(Item o1, Item o2) {

				return Item.REGISTRY.getIDForObject(o1) - Item.REGISTRY.getIDForObject(o2);
			}

		});
		for (Item anItem : data) {
			if (anItem instanceof ItemBlock) {
				anItem.getSubItems(anItem, null, stacks);
			}
		}
		for (ItemStack stack : stacks) {
			if (!(stack.getItem() instanceof ItemBlock)) {
				continue;
			}
			try {
				if (!CoverHelper.isValid(((ItemBlock) stack.getItem()).getBlock(), stack.getItem().getMetadata(stack.getItemDamage()))) {
					continue;
				}
			} catch (Exception e) {
				ThermalDynamics.LOG.error("Crashing on checking viability of cover, This cover will be ignored. " + stack.toString(), e);
				continue;
			}
			coverList.add(CoverHelper.getCoverStack(((ItemBlock) stack.getItem()).getBlock(), stack.getItem().getMetadata(stack.getItemDamage())));
		}
	}

	public static List<ItemStack> getCoverList() {

		if (coverList == null || coverList.size() <= 0) {
			createCoverList();
		}
		return coverList;
	}

}
