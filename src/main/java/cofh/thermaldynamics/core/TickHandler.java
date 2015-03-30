package cofh.thermaldynamics.core;

import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.WeakHashMap;

import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;

public class TickHandler {

	public static TickHandler INSTANCE = new TickHandler();
	public final static WeakHashMap<World, WorldGridList> handlers = new WeakHashMap<World, WorldGridList>();
	public final static LinkedHashSet<WeakReference<IMultiBlock>> multiBlocksToCalculate = new LinkedHashSet<WeakReference<IMultiBlock>>();

	public static void addMultiBlockToCalculate(IMultiBlock multiBlock) {

		if (multiBlock.world() != null) {
			if (ServerHelper.isServerWorld(multiBlock.world())) {
				getTickHandler(multiBlock.world()).tickingBlocks.add(multiBlock);
			}
		} else {
			synchronized (multiBlocksToCalculate) {
				multiBlocksToCalculate.add(new WeakReference<IMultiBlock>(multiBlock));
			}
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {

		if (event.phase != TickEvent.Phase.END) {
			return;
		}

		synchronized (multiBlocksToCalculate) {
			if (!multiBlocksToCalculate.isEmpty()) {
				Iterator<WeakReference<IMultiBlock>> iterator = multiBlocksToCalculate.iterator();
				while (iterator.hasNext()) {
					IMultiBlock multiBlock = iterator.next().get();
					if (multiBlock == null) {
						iterator.remove();
					} else if (multiBlock.world() != null) {
						if (ServerHelper.isServerWorld(multiBlock.world())) {
							getTickHandler(multiBlock.world()).tickingBlocks.add(multiBlock);
						}
						iterator.remove();
					}
				}
			}
		}

	}

	public static WorldGridList getTickHandler(World world) {

		if (ServerHelper.isClientWorld(world)) {
			throw new IllegalStateException("World Grid called client-side");
		}

		synchronized (handlers) {
			WorldGridList worldGridList = handlers.get(world);
			if (worldGridList != null) {
				return worldGridList;
			}

			worldGridList = new WorldGridList(world);
			handlers.put(world, worldGridList);
			return worldGridList;
		}
	}

	@SubscribeEvent
	public void tick(TickEvent.WorldTickEvent evt) {

		if (handlers.isEmpty()) {
			return;
		}

		synchronized (handlers) {
			WorldGridList worldGridList = handlers.get(evt.world);
			if (worldGridList == null) {
				return;
			}

			if (evt.phase == TickEvent.Phase.START) {
				worldGridList.tickStart();
			} else {
				worldGridList.tickEnd();
			}
		}
	}

	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload evt) {

		synchronized (handlers) {
			handlers.remove(evt.world);
		}
	}

}
