package thermaldynamics.util;

import buildcraft.api.tools.IToolWrench;

import cofh.api.item.IToolHammer;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

public class Utils {

	public static boolean isHoldingUsableWrench(EntityPlayer player, int x, int y, int z) {

		Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolHammer) {
			return ((IToolHammer) equipped).isUsable(player.getCurrentEquippedItem(), player, x, y, z);
		} else if (bcWrenchExists) {
			return canHandleBCWrench(equipped, player, x, y, z);
		}
		return false;
	}

	public static void usedWrench(EntityPlayer player, int x, int y, int z) {

		Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
		if (equipped instanceof IToolHammer) {
			((IToolHammer) equipped).toolUsed(player.getCurrentEquippedItem(), player, x, y, z);
		} else if (bcWrenchExists) {
			bcWrenchUsed(equipped, player, x, y, z);
		}
	}

	// BCHelper {
	private static boolean bcWrenchExists = false;

	static {
		try {
			Class.forName("buildcraft.api.tools.IToolWrench");
			bcWrenchExists = true;
		} catch (Throwable t) {
			// pokemon!
		}
	}

	private static boolean canHandleBCWrench(Item item, EntityPlayer p, int x, int y, int z) {

		return item instanceof IToolWrench && ((IToolWrench) item).canWrench(p, x, y, z);
	}

	private static void bcWrenchUsed(Item item, EntityPlayer p, int x, int y, int z) {

		if (item instanceof IToolWrench) {
			((IToolWrench) item).wrenchUsed(p, x, y, z);
		}
	}

	// }

	public static boolean isHoldingMultimeter(EntityPlayer player) {

		return false;
	}

	public static boolean isHoldingDebugger(EntityPlayer player) {

		return false;
	}

}
