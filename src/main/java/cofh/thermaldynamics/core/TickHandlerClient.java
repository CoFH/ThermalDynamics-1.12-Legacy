package cofh.thermaldynamics.core;

import cofh.thermaldynamics.duct.item.TileItemDuct;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import java.util.HashSet;
import java.util.Iterator;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;

public class TickHandlerClient {

	public static final TickHandlerClient instance = new TickHandlerClient();
	public static HashSet<TileItemDuct> tickBlocks = new HashSet<TileItemDuct>();
	public static HashSet<TileItemDuct> tickBlocksToAdd = new HashSet<TileItemDuct>();
	public static HashSet<TileItemDuct> tickBlocksToRemove = new HashSet<TileItemDuct>();

	boolean needsMenu = false;

	@SubscribeEvent
	@SideOnly(Side.CLIENT)
	public void tick(TickEvent.ClientTickEvent evt) {

		Minecraft mc = Minecraft.getMinecraft();

		if (evt.phase == TickEvent.Phase.END) {
			if (mc.currentScreen instanceof GuiMainMenu) {
				if (needsMenu) {
					onMainMenu();
					needsMenu = false;
				}
			} else if (mc.inGameHasFocus) {
				needsMenu = true;
			}

			if (!tickBlocksToAdd.isEmpty()) {
				tickBlocks.addAll(tickBlocksToAdd);
				tickBlocksToAdd.clear();
			}
			if (!mc.isGamePaused() && !tickBlocks.isEmpty()) {
				for (Iterator<TileItemDuct> iterator = tickBlocks.iterator(); iterator.hasNext();) {
					TileItemDuct aCond = iterator.next();
					if (aCond.isInvalid()) {
						iterator.remove();
					} else {
						aCond.tickItemsClient();
					}
				}
				tickBlocks.removeAll(tickBlocksToRemove);
				tickBlocksToRemove.clear();
			}
		}
	}

	public void onMainMenu() {

		synchronized (TickHandler.handlers) {
			TickHandler.handlers.clear();
		}

		tickBlocks.clear();
		tickBlocksToAdd.clear();
		tickBlocksToRemove.clear();
	}

}
