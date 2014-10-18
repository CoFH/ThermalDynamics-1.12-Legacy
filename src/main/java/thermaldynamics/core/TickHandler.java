package thermaldynamics.core;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import net.minecraft.world.World;
import thermaldynamics.multiblock.IMultiBlock;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.WeakHashMap;

public class TickHandler {

    public static TickHandler INSTANCE = new TickHandler();
    public final static WeakHashMap<World, WorldTickHandler> handlers = new WeakHashMap<World, WorldTickHandler>();
    public final static LinkedHashSet<IMultiBlock> multiBlocksToCalculate = new LinkedHashSet<IMultiBlock>();

    public static void addMultiBlockToCalculate(IMultiBlock multiBlock) {
        synchronized (multiBlocksToCalculate) {
            multiBlocksToCalculate.add(multiBlock);
        }
    }


    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event) {
        synchronized (multiBlocksToCalculate) {
            if (!multiBlocksToCalculate.isEmpty()) {
                Iterator<IMultiBlock> iterator = multiBlocksToCalculate.iterator();
                while (iterator.hasNext()) {
                    IMultiBlock multiBlock = iterator.next();
                    if (multiBlock.getWorldObj() != null) {
                        getTickHandler(multiBlock.getWorldObj()).tickingBlocks.add(multiBlock);
                        iterator.remove();
                    }
                }
            }
        }
    }


    public static WorldTickHandler getTickHandler(World world) {
        synchronized (handlers) {
            WorldTickHandler worldTickHandler = handlers.get(world);
            if (worldTickHandler != null)
                return worldTickHandler;

            worldTickHandler = new WorldTickHandler();
            handlers.put(world, worldTickHandler);
            return worldTickHandler;
        }
    }

    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent evt) {
        synchronized (handlers) {
            WorldTickHandler worldTickHandler = handlers.get(evt.world);
            if (worldTickHandler == null)
                return;

            if (evt.phase == TickEvent.Phase.START) {
                worldTickHandler.tickStart();
            } else {
                worldTickHandler.tickEnd();
            }
        }
    }

}
