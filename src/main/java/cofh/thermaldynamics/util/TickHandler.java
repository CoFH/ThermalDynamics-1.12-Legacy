package cofh.thermaldynamics.util;

import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.multiblock.IGridTile;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.WeakHashMap;

public class TickHandler {

	public static final TickHandler INSTANCE = new TickHandler();
	public static final WeakHashMap<World, WorldGridList> HANDLERS = new WeakHashMap<>();
	public static final LinkedHashSet<WeakReference<IGridTile>> MULTI_BLOCKS_TO_CALCULATE = new LinkedHashSet<>();

	public static void addMultiBlockToCalculate(IGridTile multiBlock) {

		if (multiBlock.world() != null) {
			if (ServerHelper.isServerWorld(multiBlock.world())) {
				getTickHandler(multiBlock.world()).tickingBlocks.add(multiBlock);
			}
		} else {
			synchronized (MULTI_BLOCKS_TO_CALCULATE) {
				MULTI_BLOCKS_TO_CALCULATE.add(new WeakReference<>(multiBlock));
			}
		}
	}

	public static WorldGridList getTickHandler(World world) {

		if (ServerHelper.isClientWorld(world)) {
			throw new IllegalStateException("World Grid called Client-side");
		}
		synchronized (HANDLERS) {
			WorldGridList worldGridList = HANDLERS.get(world);

			if (worldGridList != null) {
				return worldGridList;
			}
			worldGridList = new WorldGridList(world);
			HANDLERS.put(world, worldGridList);
			return worldGridList;
		}
	}

	@SubscribeEvent
	public void onServerTick(TickEvent.ServerTickEvent event) {

		if (event.phase != TickEvent.Phase.END) {
			return;
		}
		synchronized (MULTI_BLOCKS_TO_CALCULATE) {
			if (!MULTI_BLOCKS_TO_CALCULATE.isEmpty()) {
				Iterator<WeakReference<IGridTile>> iterator = MULTI_BLOCKS_TO_CALCULATE.iterator();
				while (iterator.hasNext()) {
					IGridTile multiBlock = iterator.next().get();
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

	@SubscribeEvent
	public void tick(TickEvent.WorldTickEvent event) {

		synchronized (HANDLERS) {
			WorldGridList worldGridList = HANDLERS.get(event.world);

			if (worldGridList == null) {
				return;
			}
			if (event.phase == TickEvent.Phase.START) {
				worldGridList.tickStart();
			} else {
				worldGridList.tickEnd();
			}
		}
	}

	@SubscribeEvent
	public void worldUnload(WorldEvent.Unload event) {

		World world = event.getWorld();

		if (world.isRemote) {
			return;
		}
		synchronized (HANDLERS) {
			HANDLERS.remove(world);
			HANDLERS.isEmpty();
		}
		synchronized (MULTI_BLOCKS_TO_CALCULATE) {
			if (!MULTI_BLOCKS_TO_CALCULATE.isEmpty()) {
				Iterator<WeakReference<IGridTile>> iterator = MULTI_BLOCKS_TO_CALCULATE.iterator();
				while (iterator.hasNext()) {
					IGridTile multiBlock = iterator.next().get();
					if (multiBlock == null || multiBlock.world() == world) {
						iterator.remove();
					}
				}
			}
		}
	}

}
