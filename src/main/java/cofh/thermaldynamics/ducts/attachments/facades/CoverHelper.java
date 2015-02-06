package cofh.thermaldynamics.ducts.attachments.facades;

import cofh.thermaldynamics.ThermalDynamics;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class CoverHelper {

    public static boolean isValid(ItemStack stack) {
        return stack.getItem() instanceof ItemBlock && isValid(((ItemBlock) stack.getItem()).field_150939_a, stack.getItem().getMetadata(stack.getItemDamage()));
    }

    public static boolean isValid(Block block, int meta) {
        //noinspection deprecation
        if (block.hasTileEntity(meta) || block.hasTileEntity())
            return false;

        if (block.isOpaqueCube())
            return true;

        return true;
    }

    public static ItemStack getCoverStack(ItemStack stack) {
        return getCoverStack(((ItemBlock) stack.getItem()).field_150939_a, stack.getItem().getMetadata(stack.getItemDamage()));
    }

    public static ItemStack getCoverStack(Block block, int meta) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Block", Block.blockRegistry.getNameForObject(block));
        tag.setByte("Meta", ((byte) meta));

        ItemStack itemStack = new ItemStack(ThermalDynamics.itemCover, 1);
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

        if (block == Blocks.air || meta < 0 || meta >= 16 || !isValid(block, meta)) {
            if (removeInvalidData) {
                nbt.removeTag("Meta");
                nbt.removeTag("Block");
                if (nbt.hasNoTags()) stack.setTagCompound(null);
            }
            return null;
        }

        return new ItemStack(block, 1, meta);
    }
}
