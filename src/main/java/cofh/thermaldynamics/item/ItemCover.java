package cofh.thermaldynamics.item;

import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.Attachment;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.attachments.cover.CoverHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.StatCollector;

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
		this.setTextureName("thermaldynamics:cover");
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {

		List<ItemStack> coverList = getCoverList();
		for (int i = 0; i < coverList.size(); i++) {
			list.add(coverList.get(i));
		}
	}

	@Override
	public Attachment getAttachment(int side, ItemStack stack, TileTDBase tile) {

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey("Meta", 1) || !nbt.hasKey("Block", 8)) {
			return null;
		}

		int meta = nbt.getByte("Meta");
		Block block = Block.getBlockFromName(nbt.getString("Block"));

		if (block == Blocks.air || meta < 0 || meta >= 16 || !CoverHelper.isValid(block, meta)) {
			nbt.removeTag("Meta");
			nbt.removeTag("Block");
			if (nbt.hasNoTags()) {
				stack.setTagCompound(null);
			}
			return null;
		}

		return new Cover(tile, ((byte) (side ^ 1)), block, meta);
	}

	@Override
	public String getItemStackDisplayName(ItemStack item) {

		ItemStack b = CoverHelper.getCoverItemStack(item, true);
		String name = "";
		if (b != null) {
			String unloc = getUnlocalizedNameInefficiently(item) + ".", unloc2 = b.getItem().getUnlocalizedNameInefficiently(b);
			if (StatCollector.canTranslate(unloc + unloc2 + ".name")) {
				return StatCollector.translateToLocal(unloc + unloc2 + ".name");
			}
			name = b.getDisplayName();
		}
		return StatCollector.translateToLocalFormatted(getUnlocalizedNameInefficiently(item) + ".name", name);
	}

	@Override
	public boolean preInit() {

		GameRegistry.registerItem(this, "cover");
		return true;
	}

	public static void createCoverList() {

		coverList = new ArrayList<ItemStack>();

		Iterator<Object> iterator = Item.itemRegistry.getKeys().iterator();
		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();

		ArrayList<Item> data = new ArrayList<Item>();
		while (iterator.hasNext()) {
			// iterate over the keySet instead of all values (compatible with overridden items)
			Item anItem = (Item) Item.itemRegistry.getObject(iterator.next());
			data.add(anItem);
		}
		Collections.sort(data, new Comparator<Item>() {

			@Override
			public int compare(Item o1, Item o2) {

				return Item.itemRegistry.getIDForObject(o1) - Item.itemRegistry.getIDForObject(o2);
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
			if (!CoverHelper.isValid(((ItemBlock) stack.getItem()).field_150939_a, stack.getItem().getMetadata(stack.getItemDamage()))) {
				continue;
			}
			coverList.add(CoverHelper.getCoverStack(((ItemBlock) stack.getItem()).field_150939_a, stack.getItem().getMetadata(stack.getItemDamage())));
		}
	}

	public static List<ItemStack> getCoverList() {

		if (coverList == null || coverList.size() <= 0) {
			createCoverList();
		}
		return coverList;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir) {

	}

}
