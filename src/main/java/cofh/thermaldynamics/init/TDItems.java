package cofh.thermaldynamics.init;

import cofh.core.util.core.IInitializer;
import cofh.thermaldynamics.item.*;

import java.util.ArrayList;

public class TDItems {

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

	private static ArrayList<IInitializer> initList = new ArrayList<>();

	/* REFERENCES */
	public static ItemServo itemServo;
	public static ItemFilter itemFilter;
	public static ItemRetriever itemRetriever;
	public static ItemRelay itemRelay;
	public static ItemCover itemCover;

}
