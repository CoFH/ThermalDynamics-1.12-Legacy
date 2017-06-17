package cofh.thermaldynamics.plugins.jei;

import cofh.lib.util.helpers.ItemHelper;
import cofh.thermaldynamics.duct.TDDucts;
import cofh.thermaldynamics.duct.attachments.cover.CoverHelper;
import com.google.common.collect.ImmutableList;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeWrapper;
import net.minecraft.item.ItemStack;

public class CoverRecipeWrapper extends BlankRecipeWrapper {

	protected final ItemStack coverStack;
	protected final ItemStack coverBlock;

	public CoverRecipeWrapper(ItemStack cover) {

		coverStack = ItemHelper.cloneStack(cover, 6);
		coverBlock = CoverHelper.getCoverItemStack(coverStack, false);
	}

	@Override
	public void getIngredients(IIngredients ingredients) {

		ingredients.setInputs(ItemStack.class, ImmutableList.of(coverBlock, TDDucts.structure.itemStack));
		ingredients.setOutputs(ItemStack.class, ImmutableList.of(coverStack));
	}

}
