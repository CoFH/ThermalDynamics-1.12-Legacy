package cofh.thermaldynamics.init;

import cofh.core.util.core.IInitializer;
import cofh.thermaldynamics.item.*;
import cofh.thermaldynamics.util.TDCrafting;
import cofh.thermalfoundation.init.TFProps;
import net.minecraft.item.crafting.IRecipe;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;

public class TDItems {

	public static final TDItems INSTANCE = new TDItems();

	private TDItems() {

	}

	public static void preInit() {

		itemServo = new ItemServo();
		itemFilter = new ItemFilter();
		itemRetriever = new ItemRetriever();
		itemRelay = new ItemRelay();
		itemCover = new ItemCover();

		initList.add(itemServo);
		initList.add(itemFilter);
		initList.add(itemRetriever);
		initList.add(itemRelay);
		initList.add(itemCover);

		for (IInitializer init : initList) {
			init.preInit();
		}
		TFProps.miscList.addAll(ItemCover.getCoverList());

		MinecraftForge.EVENT_BUS.register(INSTANCE);
	}

	/* EVENT HANDLING */
	@SubscribeEvent
	public void registerRecipes(RegistryEvent.Register<IRecipe> event) {

		for (IInitializer init : initList) {
			init.initialize();
		}
		TDCrafting.loadRecipes();
	}

	static ArrayList<IInitializer> initList = new ArrayList<>();

	/* REFERENCES */
	public static ItemServo itemServo;
	public static ItemFilter itemFilter;
	public static ItemRetriever itemRetriever;
	public static ItemRelay itemRelay;
	public static ItemCover itemCover;

}
