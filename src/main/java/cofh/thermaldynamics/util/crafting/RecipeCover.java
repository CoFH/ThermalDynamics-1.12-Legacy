package cofh.thermaldynamics.util.crafting;

import cofh.lib.util.helpers.ItemHelper;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.ducts.TDDucts;
import cofh.thermaldynamics.ducts.attachments.facades.CoverHelper;
import net.minecraft.block.Block;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.world.World;

public class RecipeCover implements IRecipe {

	public static final RecipeCover instance = new RecipeCover();

	@Override
	public boolean matches(InventoryCrafting craft, World world) {

		boolean a = false;
		boolean b = false;

		for (int i = 0; i < craft.getSizeInventory(); i++) {
			ItemStack stack = craft.getStackInSlot(i);
			if (stack == null) {
				continue;
			}
			if (ItemHelper.itemsEqualForCrafting(stack, TDDucts.structure.itemStack)) {
				if (a) {
					return false;
				} else {
					a = true;
				}
			} else {
				if (!CoverHelper.isValid(stack)) {
					return false;
				}
				if (b) {
					return false;
				} else {
					b = true;
				}
			}
		}
		return a && b;
	}

	@Override
	public ItemStack getCraftingResult(InventoryCrafting craft) {

		Block block;
		int meta;

		for (int i = 0; i < craft.getSizeInventory(); i++) {
			ItemStack stack = craft.getStackInSlot(i);
			if (stack == null) {
				continue;
			}
			if (!ItemHelper.itemsEqualForCrafting(stack, TDDucts.structure.itemStack)) {
				block = ((ItemBlock) stack.getItem()).field_150939_a;
				meta = stack.getItem().getMetadata(stack.getItemDamage());

				return ItemHelper.cloneStack(CoverHelper.getCoverStack(block, (byte) meta), 6);
			}
		}
		return null;
	}

	@Override
	public int getRecipeSize() {

		return 2;
	}

	@Override
	public ItemStack getRecipeOutput() {

		return new ItemStack(ThermalDynamics.itemCover, 6);
	}

}
