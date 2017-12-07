package cofh.thermaldynamics.duct.attachments.cover;

import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.init.TDItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.IShearable;
import net.minecraftforge.fml.common.registry.ForgeRegistries;
import net.minecraftforge.oredict.OreDictionary;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CoverHelper {
	public static final List<String> BLOCK_WHITELIST = new ArrayList<>();

	public static boolean preInit() {
		String[] whitelist = {"minecraft:glass", "thermalfoundation:glass:32767", "minecraft:stained_glass:32767"};
		whitelist = ThermalDynamics.CONFIG.getConfiguration().get("Attachment.Cover", "BlockWhitelist", whitelist).getStringList();
		if( whitelist != null ) {
			Collections.addAll(BLOCK_WHITELIST, whitelist);
		}

		return true;
	}

	public static boolean isValid(ItemStack stack) {

		try {
			if (stack.getItem() instanceof ItemBlock) {
				if (isValid(((ItemBlock) stack.getItem()).getBlock(), stack.getItem().getMetadata(stack.getItemDamage()))) {
					return true;
				}
			}
			return false;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return false;
	}

	public static boolean isValid(Block block, int meta) {

		try {
			if (block == null || block instanceof IShearable) {
				return false;
			}
			IBlockState state = block.getStateFromMeta(meta);
			if( !(block.hasTileEntity(state) || block.hasTileEntity()) ) {
				if( state.isFullCube() ) {
					return true;
				} else {
					ResourceLocation wlKey = Block.REGISTRY.getNameForObject(block);
					return BLOCK_WHITELIST.contains(wlKey.toString())
						   || BLOCK_WHITELIST.contains(String.format("%s:%d", wlKey, meta))
						   || BLOCK_WHITELIST.contains(String.format("%s:%d", wlKey, OreDictionary.WILDCARD_VALUE));
				}
			}
			return false;
		} catch (Exception e) {
			return false;
		}
	}

	public static ItemStack getCoverStack(ItemStack stack) {

		if (stack.getItem() instanceof ItemBlock) {
			return getCoverStack(((ItemBlock) stack.getItem()).getBlock(), stack.getItem().getMetadata(stack.getItemDamage()));
		}
		return null;
	}

	public static ItemStack getCoverStack(IBlockState state) {

		return getCoverStack(state.getBlock(), state.getBlock().getMetaFromState(state));
	}

	public static ItemStack getCoverStack(Block block, int meta) {

		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("Block", ForgeRegistries.BLOCKS.getKey(block).toString());
		tag.setByte("Meta", ((byte) meta));

		ItemStack itemStack = new ItemStack(TDItems.itemCover, 1);
		itemStack.setTagCompound(tag);
		return itemStack;
	}

	public static ItemStack getCoverItemStack(ItemStack stack, boolean removeInvalidData) {

		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt == null || !nbt.hasKey("Meta", 1) || !nbt.hasKey("Block", 8)) {
			return null;
		}
		int meta = nbt.getByte("Meta");
		Block block = Block.getBlockFromName(nbt.getString("Block"));

		if (block == Blocks.AIR || meta < 0 || meta >= 16 || !isValid(block, meta)) {
			if (removeInvalidData) {
				nbt.removeTag("Meta");
				nbt.removeTag("Block");
				if (nbt.hasNoTags()) {
					stack.setTagCompound(null);
				}
			}
			return null;
		}
		return new ItemStack(block, 1, meta);
	}

}
