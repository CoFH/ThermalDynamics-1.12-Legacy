package thermaldynamics.crafting;

import cofh.lib.util.helpers.ItemHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import thermaldynamics.ducts.Ducts;
import thermalexpansion.util.crafting.PulverizerManager;
import thermalexpansion.util.crafting.TransposerManager;
import thermalfoundation.fluid.TFFluids;
import thermalfoundation.item.TFItems;

public class TDCrafting {


    public static void loadRecipes() {
        GameRegistry.addRecipe(new ShapedOreRecipe(Ducts.ENERGY_BASIC.itemStack, "RRR", "IGI", "RRR", 'I', "ingotLead", 'G', "blockGlass", 'R', "dustRedstone"));

        PulverizerManager.addRecipe(1600, ItemHelper.cloneStack(Ducts.ENERGY_BASIC.itemStack, 6), new ItemStack(Items.redstone), ItemHelper.cloneStack(TFItems.nuggetLead, 3), 100);

        GameRegistry.addRecipe(new ShapelessOreRecipe(Ducts.ENERGY_HARDENED.itemStack, Ducts.ENERGY_BASIC.itemStack, "dustRedstone", "nuggetInvar", "nuggetInvar", "nuggetInvar"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(Ducts.ENERGY_HARDENED.itemStack, 3), Ducts.ENERGY_BASIC.itemStack,
                Ducts.ENERGY_BASIC.itemStack, Ducts.ENERGY_BASIC.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotInvar"));
        PulverizerManager.addRecipe(1600, Ducts.ENERGY_HARDENED.itemStack, new ItemStack(Items.redstone, 2), ItemHelper.cloneStack(TFItems.nuggetInvar, 3), 100);

        GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(Ducts.ENERGY_REINFORCED_EMPTY.itemStack, 6), "IGI", 'I', "ingotElectrum", 'G', "blockGlassHardened"));

        TransposerManager.addFillRecipe(800, Ducts.ENERGY_REINFORCED_EMPTY.itemStack, Ducts.ENERGY_REINFORCED.itemStack, new FluidStack(TFFluids.fluidRedstone, 200), false, false);
    }
}
