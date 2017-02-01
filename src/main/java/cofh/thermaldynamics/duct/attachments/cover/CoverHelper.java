package cofh.thermaldynamics.duct.attachments.cover;

import cofh.thermaldynamics.ThermalDynamics;

import java.util.HashMap;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidContainerRegistry;
import net.minecraftforge.fluids.FluidRegistry;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.registry.ForgeRegistries;

public class CoverHelper {

	public static HashMap<Fluid, Block> fluidToBlockMap;

	public static void initFluids() {

		fluidToBlockMap = new HashMap<Fluid, Block>();

		fluidToBlockMap.put(FluidRegistry.WATER, Blocks.WATER);
		fluidToBlockMap.put(FluidRegistry.LAVA, Blocks.LAVA);
		for (Block block : ForgeRegistries.BLOCKS) {
			if (block instanceof IFluidBlock) {
				Fluid fluid = ((IFluidBlock) block).getFluid();
				if (fluid != null) {
					fluidToBlockMap.put(fluid, block);
				}
			}
		}
	}

	public static Block getFluidBlock(FluidStack fluidStack) {

		if (fluidStack == null) {
			return null;
		}
		if (fluidToBlockMap == null) {
			initFluids();
		}
		return fluidToBlockMap.get(fluidStack.getFluid());
	}

	public static boolean isValid(ItemStack stack) {
        try {
            if (stack.getItem() instanceof ItemBlock) {
                if (isValid(((ItemBlock) stack.getItem()).getBlock(), stack.getItem().getMetadata(stack.getItemDamage()))) {
                    return true;
                }
            }
            return getFluidBlock(FluidContainerRegistry.getFluidForFilledItem(stack)) != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
	}

	public static boolean isValid(Block block, int meta) {

		if (block == null) {
			return false;
		}
		return !(block.hasTileEntity(block.getStateFromMeta(meta)) || block.hasTileEntity());

	}

	public static ItemStack getCoverStack(ItemStack stack) {

		if (stack.getItem() instanceof ItemBlock) {
			return getCoverStack(((ItemBlock) stack.getItem()).getBlock(), stack.getItem().getMetadata(stack.getItemDamage()));
		}
		Block fluidBlock = getFluidBlock(FluidContainerRegistry.getFluidForFilledItem(stack));
		if (fluidBlock != null) {
			return getCoverStack(fluidBlock, 0);
		}
		return null;
	}

	public static ItemStack getCoverStack(Block block, int meta) {

		NBTTagCompound tag = new NBTTagCompound();
		tag.setString("Block", ForgeRegistries.BLOCKS.getKey(block).toString());
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
