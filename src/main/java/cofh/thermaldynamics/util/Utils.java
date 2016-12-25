package cofh.thermaldynamics.util;

import buildcraft.api.tools.IToolWrench;
import cofh.api.item.IToolHammer;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;

public class Utils {

    public static boolean isHoldingUsableWrench(EntityPlayer player, RayTraceResult traceResult) {

        EnumHand hand = EnumHand.MAIN_HAND;
        ItemStack stack = player.getHeldItem(hand);
        if (stack == null) {
            hand = EnumHand.OFF_HAND;
            stack = player.getHeldItem(hand);
        }
        if (stack == null) {
            return false;
        }
        if (stack.getItem() instanceof IToolHammer) {
            BlockPos pos = traceResult.getBlockPos();
            return ((IToolHammer) stack.getItem()).isUsable(stack, player, pos);
        } else if (bcWrenchExists) {
            return canHandleBCWrench(player, hand, stack, traceResult);
        }
        return false;
    }

    public static void usedWrench(EntityPlayer player, RayTraceResult traceResult) {

        EnumHand hand = EnumHand.MAIN_HAND;
        ItemStack stack = player.getHeldItem(hand);
        if (stack == null) {
            hand = EnumHand.OFF_HAND;
            stack = player.getHeldItem(hand);
        }
        if (stack == null) {
            return;
        }
        if (stack.getItem() instanceof IToolHammer) {
            BlockPos pos = traceResult.getBlockPos();
            ((IToolHammer) stack.getItem()).toolUsed(stack, player, pos);
        } else if (bcWrenchExists) {
            bcWrenchUsed(player, hand, stack, traceResult);
        }
    }

    // BCHelper {
    private static boolean bcWrenchExists = classExists("buildcraft.api.tools.IToolWrench");

    private static boolean classExists(String className) {

        try {
            Class.forName(className);
            return true;
        } catch (Throwable ignore) {
            return false;
        }
    }

    private static boolean canHandleBCWrench(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult result) {

        return wrench.getItem() instanceof IToolWrench && ((IToolWrench) wrench.getItem()).canWrench(player, hand, wrench, result);
    }

    private static void bcWrenchUsed(EntityPlayer player, EnumHand hand, ItemStack wrench, RayTraceResult result) {

        if (wrench.getItem() instanceof IToolWrench) {
            ((IToolWrench) wrench.getItem()).wrenchUsed(player, hand, wrench, result);
        }
    }
    // }

}
