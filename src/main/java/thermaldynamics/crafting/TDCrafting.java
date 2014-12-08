package thermaldynamics.crafting;

import cofh.lib.util.helpers.ItemHelper;
import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.oredict.ShapedOreRecipe;
import net.minecraftforge.oredict.ShapelessOreRecipe;
import thermaldynamics.ducts.Ducts;
import thermaldynamics.item.ItemServo;
import thermalexpansion.util.crafting.PulverizerManager;
import thermalexpansion.util.crafting.TransposerManager;
import thermalfoundation.fluid.TFFluids;
import thermalfoundation.item.TFItems;

public class TDCrafting {


    public static void loadRecipes() {
        //Energy
        GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(Ducts.ENERGY_BASIC.itemStack, 6), "RRR", "IGI", "RRR", 'I', "ingotLead", 'G', "blockGlass", 'R', "dustRedstone"));

        PulverizerManager.addRecipe(1600, Ducts.ENERGY_BASIC.itemStack, new ItemStack(Items.redstone), ItemHelper.cloneStack(TFItems.nuggetLead, 3), 100);

        GameRegistry.addRecipe(new ShapelessOreRecipe(Ducts.ENERGY_HARDENED.itemStack, Ducts.ENERGY_BASIC.itemStack, "dustRedstone", "nuggetInvar", "nuggetInvar", "nuggetInvar"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(Ducts.ENERGY_HARDENED.itemStack, 3), Ducts.ENERGY_BASIC.itemStack, Ducts.ENERGY_BASIC.itemStack, Ducts.ENERGY_BASIC.itemStack, "dustRedstone", "dustRedstone", "dustRedstone", "ingotInvar"));
        PulverizerManager.addRecipe(1600, Ducts.ENERGY_HARDENED.itemStack, new ItemStack(Items.redstone, 2), ItemHelper.cloneStack(TFItems.nuggetInvar, 3), 100);

        GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(Ducts.ENERGY_REINFORCED_EMPTY.itemStack, 6), "IGI", 'I', "ingotElectrum", 'G', "blockGlassHardened"));

        TransposerManager.addFillRecipe(800, Ducts.ENERGY_REINFORCED_EMPTY.itemStack, Ducts.ENERGY_REINFORCED.itemStack, new FluidStack(TFFluids.fluidRedstone, 200), false, false);

        GameRegistry.addRecipe(new ShapedOreRecipe(Ducts.ENERGY_SUPERCONDUCTOR_EMPTY.itemStack, "IGI", "GEG", "IGI", 'I', "ingotElectrum", 'G', "blockGlassHardened", 'E', Ducts.ENERGY_REINFORCED.itemStack));
        TransposerManager.addFillRecipe(4000, Ducts.ENERGY_SUPERCONDUCTOR_EMPTY.itemStack, Ducts.ENERGY_SUPERCONDUCTOR.itemStack, new FluidStack(TFFluids.fluidCryotheum, 4000), false, false);

        //Structure
        GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(Ducts.STRUCTURE.itemStack, 6), "iIi", 'i', "nuggetLead", 'I', "ingotLead"));

        // Fluid
        GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(Ducts.FLUID_OPAQUE.itemStack, 6), "IGI", 'I', "ingotCopper", 'G', "ingotLead"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(Ducts.FLUID_TRANS.itemStack, 6), "IGI", 'I', "ingotCopper", 'G', "blockGlassHardened"));

        GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(Ducts.FLUID_HARDENED_OPAQUE.itemStack, 6), " I ", "IGI", " I ", 'I', "ingotInvar", 'G', "ingotLead"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(Ducts.FLUID_HARDENED_TRANS.itemStack, 6), " I ", "IGI", " I ", 'I', "ingotInvar", 'G', "blockGlassHardened"));

        // Servo
        GameRegistry.addRecipe(new ShapedOreRecipe(ItemServo.iron, "iPi", 'P', "pneumaticServo", 'i', "nuggetIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ItemServo.iron, "iGi", "IRI", 'R', "dustRedstone", 'G', "blockGlass", 'I', "ingotIron", 'i', "nuggetIron"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ItemServo.invar, "IGI", 'G', ItemServo.iron, 'I', "ingotInvar"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ItemServo.electrum, "IGI", 'G', ItemServo.invar, 'I', "ingotElectrum"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ItemServo.signalum, "IGI", 'G', ItemServo.electrum, 'I', "ingotSignalum"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ItemServo.ender, "IGI", 'G', ItemServo.signalum, 'I', "ingotEnderium"));

        // Items
        GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(Ducts.ITEM_TRANS.itemStack, 6), "IGI", 'I', "ingotTin", 'G', "blockGlassHardened"));
        GameRegistry.addRecipe(new ShapedOreRecipe(ItemHelper.cloneStack(Ducts.ITEM_OPAQUE.itemStack, 6), "IGI", 'I', "ingotTin", 'G', "ingotLead"));

        TransposerManager.addFillRecipe(800, Ducts.ITEM_TRANS.itemStack, Ducts.ITEM_FAST_TRANS.itemStack, new FluidStack(TFFluids.fluidGlowstone, 200), false, false);
        TransposerManager.addFillRecipe(800, Ducts.ITEM_OPAQUE.itemStack, Ducts.ITEM_FAST_OPAQUE.itemStack, new FluidStack(TFFluids.fluidGlowstone, 200), false, false);

        TransposerManager.addFillRecipe(800, Ducts.ITEM_TRANS.itemStack, Ducts.ITEM_REDSTONE_TRANS.itemStack, new FluidStack(TFFluids.fluidRedstone, 50), false, false);
        TransposerManager.addFillRecipe(800, Ducts.ITEM_OPAQUE.itemStack, Ducts.ITEM_REDSTONE_OPAQUE.itemStack, new FluidStack(TFFluids.fluidRedstone, 50), false, false);


        GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(Ducts.ITEM_ENDERIUM_TRANS.itemStack, 3), Ducts.ITEM_TRANS.itemStack, Ducts.ITEM_TRANS.itemStack, Ducts.ITEM_TRANS.itemStack, "dustEnderium"));
        GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(Ducts.ITEM_ENDERIUM_OPAQUE.itemStack, 3), Ducts.ITEM_OPAQUE.itemStack, Ducts.ITEM_OPAQUE.itemStack, Ducts.ITEM_OPAQUE.itemStack, "dustEnderium"));

        // Conversions
        for (Ducts[] d : convert) {
            final ItemStack t = d[0].itemStack;
            final ItemStack o = d[1].itemStack;
            GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(t, 6), o, o, o, o, o, o, "glassHardened"));
            GameRegistry.addRecipe(new ShapelessOreRecipe(ItemHelper.cloneStack(o, 6), t, t, t, t, t, t, "ingotLead"));
        }
    }

    public static final Ducts[][] convert = {
            {Ducts.ITEM_TRANS, Ducts.ITEM_OPAQUE},
            {Ducts.ITEM_FAST_TRANS, Ducts.ITEM_FAST_OPAQUE},
            {Ducts.ITEM_REDSTONE_TRANS, Ducts.ITEM_REDSTONE_OPAQUE},
            {Ducts.ITEM_ENDERIUM_TRANS, Ducts.ITEM_ENDERIUM_OPAQUE},
            {Ducts.FLUID_TRANS, Ducts.FLUID_OPAQUE},
            {Ducts.FLUID_HARDENED_TRANS, Ducts.FLUID_HARDENED_OPAQUE},
    };

}
