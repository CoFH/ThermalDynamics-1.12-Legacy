package cofh.thermaldynamics.duct.attachments.cover;

import cofh.thermaldynamics.init.TDItems;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CoverHelper {

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
			if (block == null) {
				return false;
			}
			IBlockState state = block.getStateFromMeta(meta);
			return !(block.hasTileEntity(state) || block.hasTileEntity()) && state.isFullCube();
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
