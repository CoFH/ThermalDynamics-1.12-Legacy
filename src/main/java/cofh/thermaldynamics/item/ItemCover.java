package cofh.thermaldynamics.item;

import cofh.lib.util.helpers.StringHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.attachments.cover.Cover;
import cofh.thermaldynamics.duct.attachments.cover.CoverHelper;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.block.Block;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumFacing;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.fml.common.registry.GameRegistry;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ItemCover extends ItemAttachment {

	private static List<ItemStack> coverList;

	public ItemCover() {

		this.setCreativeTab(ThermalDynamics.tabCovers);
		this.setUnlocalizedName("thermaldynamics.cover");
	}

	@Override
	public void getSubItems(Item item, CreativeTabs tab, List<ItemStack> list) {

		list.addAll(getCoverList());
	}

	@Override
	public Attachment getAttachment(EnumFacing side, ItemStack stack, TileGrid tile) {

		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null || !nbt.hasKey("Meta", 1) || !nbt.hasKey("Block", 8)) {
			return null;
		}
		int meta = nbt.getByte("Meta"); //FIXME Use a state instead of meta and block.
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
			if (StringHelper.canLocalize(unloc + unloc2 + ".name")) {
				return StringHelper.localize(unloc + unloc2 + ".name");
			}
			name = b.getDisplayName();
		}
		return StringHelper.localizeFormat(getUnlocalizedNameInefficiently(item) + ".name", name);
	}

	@Override
	@SideOnly (Side.CLIENT)
	public void addInformation(ItemStack stack, EntityPlayer playerIn, List<String> tooltip, boolean advanced) {

		ItemStack b = CoverHelper.getCoverItemStack(stack, false);
		if (b == null) {
			tooltip.add(StringHelper.getNoticeText("info.thermaldynamics.info.invalidCover"));
		}
	}

	@Override
	public boolean preInit() {
		CoverHelper.preInit();

		GameRegistry.register(this.setRegistryName("cover"));
		return true;
	}

	public static void createCoverList() {

		coverList = new ArrayList<>();

		ArrayList<ItemStack> stacks = new ArrayList<>();

		ArrayList<Item> data = new ArrayList<>();
		for (Item item : ForgeRegistries.ITEMS) {
			data.add(item);
		}
		data.sort(Comparator.comparingInt(Item.REGISTRY::getIDForObject));
		for (Item anItem : data) {
			if (anItem instanceof ItemBlock) {
				anItem.getSubItems(anItem, null, stacks);
			}
		}
		for (ItemStack stack : stacks) {
			if (!(stack.getItem() instanceof ItemBlock)) {
				continue;
			}
			if (!CoverHelper.isValid(((ItemBlock) stack.getItem()).getBlock(), stack.getItem().getMetadata(stack.getItemDamage()))) {
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
