package thermaldynamics.ducts.attachments.facades;

import net.minecraft.block.Block;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import thermaldynamics.ThermalDynamics;

public class CoverHelper {


    public static boolean isValid(Block block, int meta){
        if(block.hasTileEntity(meta) || block.hasTileEntity())
            return false;

        if(block.isOpaqueCube())
            return true;

        return true;
    }

    public static ItemStack getFacadeItemStack(Block block, int meta) {
        NBTTagCompound tag = new NBTTagCompound();
        tag.setString("Block", Block.blockRegistry.getNameForObject(block));
        tag.setByte("Meta", ((byte) meta));

        ItemStack itemStack = new ItemStack(ThermalDynamics.itemFacade, 1);
        itemStack.setTagCompound(tag);
        return itemStack;
    }
}
