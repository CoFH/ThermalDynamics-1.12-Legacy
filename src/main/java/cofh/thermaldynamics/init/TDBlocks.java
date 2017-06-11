package cofh.thermaldynamics.init;

import cofh.core.util.core.IInitializer;
import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.TDDucts;

import java.util.ArrayList;

public class TDBlocks {

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
	public static BlockDuct[] blockDuct;

}
