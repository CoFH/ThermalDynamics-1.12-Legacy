package cofh.thermaldynamics.plugins.jei;

import cofh.core.util.helpers.StringHelper;
import cofh.thermaldynamics.item.ItemCover;
import mezz.jei.api.IGuiHelper;
import mezz.jei.api.IJeiHelpers;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.gui.*;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.BlankRecipeCategory;
import mezz.jei.api.recipe.IRecipeCategoryRegistration;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;

import java.util.LinkedList;
import java.util.List;

public class CoverRecipeCategory extends BlankRecipeCategory<CoverRecipeWrapper> {

	private static final int craftOutputSlot = 0;
	private static final int craftInputSlot1 = 1;

	public static final int width = 116;
	public static final int height = 54;

	public static void register(IRecipeCategoryRegistration registry) {

		IJeiHelpers jeiHelpers = registry.getJeiHelpers();
		IGuiHelper guiHelper = jeiHelpers.getGuiHelper();

		registry.addRecipeCategories(new CoverRecipeCategory(guiHelper));
	}

	public static void initialize(IModRegistry registry) {

		registry.addRecipes(getRecipes(), RecipeUidsTD.COVERS);
		registry.addRecipeCatalyst(new ItemStack(Blocks.CRAFTING_TABLE), RecipeUidsTD.COVERS);
	}

	public static List<CoverRecipeWrapper> getRecipes() {

		List<CoverRecipeWrapper> recipes = new LinkedList<>();

		for (ItemStack stack : ItemCover.getCoverList()) {
			recipes.add(new CoverRecipeWrapper(stack));
		}
		return recipes;
	}

	private final IDrawableStatic background;
	private final String localizedName;
	private final ICraftingGridHelper craftingGridHelper;

	public CoverRecipeCategory(IGuiHelper guiHelper) {

		ResourceLocation location = new ResourceLocation("minecraft", "textures/gui/container/crafting_table.png");

		background = guiHelper.createDrawable(location, 29, 16, width, height);
		localizedName = StringHelper.localize("recipe.thermaldynamics.covers");
		craftingGridHelper = guiHelper.createCraftingGridHelper(craftInputSlot1, craftOutputSlot);
	}

	@Override
	public String getUid() {

		return RecipeUidsTD.COVERS;
	}

	@Override
	public String getTitle() {

		return localizedName;
	}

	@Override
	public String getModName() {

		return "ThermalDynamics";
	}

	@Override
	public IDrawable getBackground() {

		return background;
	}

	@Override
	public void setRecipe(IRecipeLayout recipeLayout, CoverRecipeWrapper recipeWrapper, IIngredients ingredients) {

		IGuiItemStackGroup guiItemStacks = recipeLayout.getItemStacks();
		guiItemStacks.init(craftOutputSlot, false, 94, 18);

		for (int y = 0; y < 3; ++y) {
			for (int x = 0; x < 3; ++x) {
				int index = craftInputSlot1 + x + (y * 3);
				guiItemStacks.init(index, true, x * 18, y * 18);
			}
		}
		craftingGridHelper.setInputStacks(guiItemStacks, ingredients.getInputs(ItemStack.class));
		guiItemStacks.set(craftOutputSlot, ingredients.getOutputs(ItemStack.class).get(0));
	}
}
