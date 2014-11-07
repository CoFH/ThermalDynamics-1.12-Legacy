package thermaldynamics.core;

import cofh.lib.util.helpers.ServerHelper;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.world.World;
import net.minecraftforge.event.world.WorldEvent;
import thermaldynamics.multiblock.IMultiBlock;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.WeakHashMap;

public class TickHandler {
    public static TickHandler INSTANCE = new TickHandler();
    public final static WeakHashMap<World, WorldGridList> handlers = new WeakHashMap<World, WorldGridList>();
    public final static LinkedHashSet<IMultiBlock> multiBlocksToCalculate = new LinkedHashSet<IMultiBlock>();

    public static void addMultiBlockToCalculate(IMultiBlock multiBlock) {
        if (multiBlock.world() != null)
            getTickHandler(multiBlock.world()).tickingBlocks.add(multiBlock);
        else
            synchronized (multiBlocksToCalculate) {
                multiBlocksToCalculate.add(multiBlock);
            }
    }


    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END)
            return;

        synchronized (multiBlocksToCalculate) {
            if (!multiBlocksToCalculate.isEmpty()) {
                Iterator<IMultiBlock> iterator = multiBlocksToCalculate.iterator();
                while (iterator.hasNext()) {
                    IMultiBlock multiBlock = iterator.next();
                    if (multiBlock.world() != null) {
                        getTickHandler(multiBlock.world()).tickingBlocks.add(multiBlock);
                        iterator.remove();
                    }
                }
            }
        }
    }


    public static WorldGridList getTickHandler(World world) {
        if (ServerHelper.isClientWorld(world))
            throw new IllegalStateException("World Grid called client-side");

        synchronized (handlers) {
            WorldGridList worldGridList = handlers.get(world);
            if (worldGridList != null)
                return worldGridList;

            worldGridList = new WorldGridList(world);
            handlers.put(world, worldGridList);
            return worldGridList;
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent evt) {
        synchronized (handlers) {
            WorldGridList worldGridList = handlers.get(evt.world);
            if (worldGridList == null)
                return;

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
