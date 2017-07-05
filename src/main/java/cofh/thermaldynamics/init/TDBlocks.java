package cofh.thermaldynamics.init;

import cofh.core.util.core.IInitializer;
import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.TDDucts;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

public class TDBlocks {

	public static final TDBlocks INSTANCE = new TDBlocks();

	private TDBlocks() {

	}

	public static void preInit() {

		TDDucts.addDucts();

		int numBlocks = (int) Math.ceil(cofh.thermaldynamics.duct.TDDucts.ductList.size() / 16.0);
		blockDuct = new BlockDuct[numBlocks];

		for (int i = 0; i < numBlocks; i++) {
			blockDuct[i] = new BlockDuct(i);
			initList.add(blockDuct[i]);
		}
		for (IInitializer init : initList) {
			init.initialize();
		}
		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}

	/* EVENT HANDLING */
	@SubscribeEvent
	public void registerRecipes(RegistryEvent.Register<IRecipe> event) {

		for (IInitializer init : initList) {
			init.register();
		}
	}

	private static ArrayList<IInitializer> initList = new ArrayList<>();

	/* REFERENCES */
	public static BlockDuct[] blockDuct;

}
