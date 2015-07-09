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

public class ItemCover extends ItemAttachment {

	private static float[] hitX = { 0.5F, 0.5F, 0.5F, 0.5F, 0, 1 };
	private static float[] hitY = { 0, 1, 0.5F, 0.5F, 0.5F, 0.5F };
	private static float[] hitZ = { 0.5F, 0.5F, 0, 1, 0.5F, 0.5F };

	public static boolean enableCreativeTab = true;
	public static boolean showInNEI = false;

	public static ArrayList<ItemStack> coverList = new ArrayList<ItemStack>();

	public ItemCover() {

		this.setCreativeTab(ThermalDynamics.tabCovers);
		this.setUnlocalizedName("thermaldynamics.cover");
		this.setTextureName("thermaldynamics:cover");

		if (!enableCreativeTab) {
			setCreativeTab(null);
		}
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List list) {

		// Iterator iterator = Item.itemRegistry.iterator();
		//
		// ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();
		//
		// while (iterator.hasNext()) {
		// Item anItem = (Item) iterator.next();
		//
		// if (anItem != null && anItem != this) {
		// anItem.getSubItems(anItem, null, stacks);
		// }
		// }
		// for (ItemStack stack : stacks) {
		// if (!(stack.getItem() instanceof ItemBlock)) {
		// continue;
		// }
		// if (!CoverHelper.isValid(((ItemBlock) stack.getItem()).field_150939_a, stack.getItem().getMetadata(stack.getItemDamage()))) {
		// continue;
		// }
		// list.add(CoverHelper.getCoverStack(((ItemBlock) stack.getItem()).field_150939_a, stack.getItem().getMetadata(stack.getItemDamage())));
		// }
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

		StringBuilder builder = new StringBuilder();
		ItemStack b = CoverHelper.getCoverItemStack(item, true);
		if (b != null) {
			builder.append(b.getDisplayName());
			builder.append(" ");
		}
		builder.append(super.getItemStackDisplayName(item));
		return builder.toString();
	}

	@Override
	public boolean preInit() {

		GameRegistry.registerItem(this, "cover");
		return true;
	}

	@Override
	public boolean postInit() {

		coverList.clear();
		Iterator iterator = Item.itemRegistry.iterator();

		ArrayList<ItemStack> stacks = new ArrayList<ItemStack>();

		while (iterator.hasNext()) {
			Item anItem = (Item) iterator.next();

			if (anItem != null && anItem != this) {
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
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void registerIcons(IIconRegister ir) {

	}

}
