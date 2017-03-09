package cofh.thermaldynamics.plugins.jei;

import mezz.jei.api.recipe.IRecipeHandler;
import mezz.jei.api.recipe.IRecipeWrapper;

/**
 * Created by covers1624 on 26/02/2017.
 */
public class CoverRecipeHandler implements IRecipeHandler<CoverRecipeWrapper> {

	@Override
	public Class<CoverRecipeWrapper> getRecipeClass() {

		return CoverRecipeWrapper.class;
	}

	@Override
	public String getRecipeCategoryUid() {

		return RecipeUidsTD.COVER;
	}

	@Override
	public String getRecipeCategoryUid(CoverRecipeWrapper recipe) {

		return getRecipeCategoryUid();
	}

	@Override
	public IRecipeWrapper getRecipeWrapper(CoverRecipeWrapper recipe) {

		return recipe;
	}

	@Override
	public boolean isRecipeValid(CoverRecipeWrapper recipe) {

		return true;
	}
}
