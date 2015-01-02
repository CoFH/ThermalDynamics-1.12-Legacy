package thermaldynamics.block;

import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;

public class Utils {
    public static boolean isHoldingMultimeter(EntityPlayer player) {
        return false;
    }

    public static boolean isHoldingUsableWrench(EntityPlayer player, int x, int y, int z) {
        Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
        if (equipped instanceof IToolHammer) {
            return ((IToolHammer) equipped).isUsable(player.getCurrentEquippedItem(), player, x, y, z);
        } else if (bcWrenchExists) {
            return canHandleBCWrench(equipped, player, x, y, z);
        }
        return false;
    }

    private static boolean canHandleBCWrench(Item item, EntityPlayer p, int x, int y, int z) {
        return item instanceof IToolWrench && ((IToolWrench) item).canWrench(p, x, y, z);
    }

    private static boolean bcWrenchExists = false;

    static {
        try {
            Class.forName("buildcraft.api.tools.IToolWrench");
            bcWrenchExists = true;
        } catch (Throwable ignore) {

        }
    }

    public static boolean isHoldingDebugger(EntityPlayer player) {
        return false;
    }
}
