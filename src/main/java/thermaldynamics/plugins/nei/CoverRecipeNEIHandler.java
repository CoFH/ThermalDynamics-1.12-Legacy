package thermaldynamics.plugins.nei;

import codechicken.nei.ItemPanel;
import codechicken.nei.NEIClientUtils;
import codechicken.nei.PositionedStack;
import codechicken.nei.recipe.ShapelessRecipeHandler;
import cofh.lib.util.helpers.ItemHelper;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

import thermaldynamics.ThermalDynamics;
import thermaldynamics.ducts.TDDucts;
import thermaldynamics.ducts.attachments.facades.CoverHelper;

public class CoverRecipeNEIHandler extends ShapelessRecipeHandler {

	public static CoverRecipeNEIHandler instance = new CoverRecipeNEIHandler();

	public class CachedCoverRecipeSimple extends CachedShapelessRecipe {

		public CachedCoverRecipeSimple(ItemStack block) {

			super(new Object[] { block, TDDucts.structure.itemStack }, CoverHelper.getCoverStack(block));
		}
	}

	public class CachedCoverRecipeAll extends CachedShapelessRecipe {

		@SuppressWarnings("unchecked")
		public CachedCoverRecipeAll() {

			List<ItemStack> items = new ArrayList<ItemStack>();
			for (ItemStack item : ItemPanel.items) {
				if (CoverHelper.isValid(item)) {
					items.add(item);
				}
			}

			ArrayList objects = new ArrayList();
			objects.add(items);
			objects.add(TDDucts.structure.itemStack);
			setIngredients(objects);
		}

		@Override
		public PositionedStack getResult() {

			for (PositionedStack positionedStack : ingredients) {
				ItemStack item = positionedStack.item;
				if (item != TDDucts.structure.itemStack) {
					if (CoverHelper.isValid(item)) {
						setResult(CoverHelper.getCoverStack(item));
						return super.getResult();
					}
				}
			}
			return null;
		}
	}

	@Override
	public void loadCraftingRecipes(ItemStack result) {

		if (result.getItem() != ThermalDynamics.itemCover)
			return;

		NBTTagCompound nbt = result.getTagCompound();
		if (nbt == null || !nbt.hasKey("Meta", 1) || !nbt.hasKey("Block", 8))
			return;

		int meta = nbt.getByte("Meta");
		Block block = Block.getBlockFromName(nbt.getString("Block"));

		if (block == Blocks.air || meta < 0 || meta >= 16 || !CoverHelper.isValid(block, meta))
			return;

		arecipes.add(new CachedCoverRecipeSimple(new ItemStack(block, 1, meta)));
	}

	@Override
	public void loadCraftingRecipes(String outputId, Object... results) {

		if (outputId.equals("crafting")) {
			arecipes.add(new CachedCoverRecipeAll());
		} else
			super.loadCraftingRecipes(outputId, results);

	}

	@Override
	public void loadUsageRecipes(ItemStack ingredient) {

		if (CoverHelper.isValid(ingredient)) {
			arecipes.add(new CachedCoverRecipeSimple(ingredient));
		} else if (ItemHelper.itemsEqualForCrafting(TDDucts.structure.itemStack, ingredient)) {
			arecipes.add(new CachedCoverRecipeAll());
		}
	}

	@Override
	public String getRecipeName() {

		return NEIClientUtils.translate("recipe.thermaldynamics.covers");
	}

	@Override
	public boolean isRecipe2x2(int recipe) {

		return true;
	}
}
