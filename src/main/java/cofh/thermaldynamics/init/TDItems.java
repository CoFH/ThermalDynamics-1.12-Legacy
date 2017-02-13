package cofh.thermaldynamics.init;

import cofh.api.core.IInitializer;
import cofh.thermaldynamics.item.*;

import java.util.ArrayList;

public class TDItems {

	private TDItems() {

	}

	public static void preInit() {

		for (IInitializer init : initList) {
			init.preInit();
		}
	}

	public static void initialize() {

		for (IInitializer init : initList) {
			init.initialize();
		}
	}

	public static void postInit() {

		for (IInitializer init : initList) {
			init.postInit();
		}
		initList.clear();
	}

	private static ArrayList<IInitializer> initList = new ArrayList<IInitializer>();

	/* REFERENCES */
	public static ItemServo itemServo;
	public static ItemFilter itemFilter;
	public static ItemRetriever itemRetriever;
	public static ItemRelay itemRelay;
	public static ItemCover itemCover;

}
