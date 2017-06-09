package cofh.thermaldynamics.plugins.jei;

import cofh.thermaldynamics.duct.attachments.cover.CoverHelper;
import cofh.thermaldynamics.init.TDItems;
import cofh.thermaldynamics.init.TDProps;
import cofh.thermaldynamics.item.ItemCover;
import mezz.jei.api.BlankModPlugin;
import mezz.jei.api.IModRegistry;
import mezz.jei.api.ISubtypeRegistry;
import mezz.jei.api.JEIPlugin;
import mezz.jei.api.ingredients.IIngredientBlacklist;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

@JEIPlugin
public class JEIPluginTD extends BlankModPlugin {

	@Override
	public void register(IModRegistry registry) {

		CoverRecipeCategory.initialize(registry);
		if (!TDProps.showCoversInJEI) {
			blacklistCovers(registry.getJeiHelpers().getIngredientBlacklist());
		}
	}

	@Override
	public void registerItemSubtypes(ISubtypeRegistry subtypeRegistry) {

		subtypeRegistry.registerSubtypeInterpreter(TDItems.itemCover, (itemStack -> {
			ItemStack block = CoverHelper.getCoverItemStack(itemStack, false);
			return "thermaldynamics:cover" + "|" + block.getItem().getRegistryName() + "@" + block.getMetadata();
		}));
	}

	private static void blacklistCovers(IIngredientBlacklist ingredientBlacklist) {

		for (ItemStack stack : ItemCover.getCoverList()) {
			ItemStack coverBlock = CoverHelper.getCoverItemStack(stack, false);
			if (coverBlock.getItem() == Item.getItemFromBlock(Blocks.STONE) && coverBlock.getMetadata() == 0) {
				continue;
			}
			ingredientBlacklist.addIngredientToBlacklist(stack);
		}
	}

}
