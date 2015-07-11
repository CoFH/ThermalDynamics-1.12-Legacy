package cofh.thermaldynamics.duct.entity;

import cofh.repack.codechicken.lib.raytracer.IndexedCuboid6;
import cofh.repack.codechicken.lib.raytracer.RayTracer;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.SubTileMultiBlock;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.item.TileItemDuct;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.IMultiBlockRoute;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.multiblock.Route;
import cofh.thermaldynamics.multiblock.RouteCache;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MovingObjectPosition;

public class TileTransportDuct extends TileTDBase implements IMultiBlockRoute {

    public TransportGrid internalGrid;

    @Override
    public void setGrid(MultiBlockGrid newGrid) {

        super.setGrid(newGrid);
        internalGrid = (TransportGrid) newGrid;
    }

    @Override
    public MultiBlockGrid getNewGrid() {
        return new TransportGrid(worldObj);
    }

    @Override
    public boolean cachesExist() {
        return true;
    }

    @Override
    public void createCaches() {

    }

    @Override
    public void cacheImportant(TileEntity tile, int side) {

    }

    @Override
    public void clearCache(int side) {

    }

    @Override
    public void handleTileSideUpdate(int i) {
        super.handleTileSideUpdate(i);

        if (connectionTypes[i] == ConnectionTypes.FORCED) {
            neighborMultiBlocks[i] = null;
            neighborTypes[i] = NeighborTypes.OUTPUT;
            isNode = true;
            isOutput = true;
        }
    }

    @Override
    public boolean openGui(EntityPlayer player) {
        if (super.openGui(player) || worldObj.isRemote)
            return true;

        if(internalGrid == null) return false;

        MovingObjectPosition movingObjectPosition = RayTracer.retraceBlock(worldObj, player, xCoord, yCoord, zCoord);
        if (movingObjectPosition == null) {
            return false;
        }

        int subHit = movingObjectPosition.subHit;
        int hitSide = movingObjectPosition.sideHit;

        if (subHit >= 0 && subHit <= 13) {
            int i = subHit == 13 ? hitSide : subHit < 6 ? subHit : subHit - 6;

            onNeighborBlockChange();

            if (neighborMultiBlocks[i] != null)
                return false;

            ItemStack heldItem = player.getHeldItem();

            if (connectionTypes[i] == ConnectionTypes.FORCED) {
                if (heldItem != null && heldItem.getItem() == Items.spawn_egg) {

                    Entity entity = EntityList.createEntityByID(heldItem.getItemDamage(), world());

                    if(entity == null || !(entity instanceof EntityLivingBase))
                        return false;

                    EntityTransport route = findRoute(entity, i ^ 1, (byte) 1);

                    if (route != null) {
                        entity.setPosition(x(), y(), z());
                        world().spawnEntityInWorld(entity);

                        route.start((EntityLivingBase) entity);
                    }

                    return true;
                }

                if (heldItem != null && heldItem.getItem() == Items.minecart) {
                    EntityTransport route = findRoute(player, i ^ 1, (byte) 1);

                    if (route != null) {
                        route.start(player);
                    }

                    return true;
                }

            }

            if(heldItem != null) return false;

            connectionTypes[i] = connectionTypes[i] == ConnectionTypes.FORCED ? ConnectionTypes.NORMAL : ConnectionTypes.FORCED;

            onNeighborBlockChange();

            worldObj.notifyBlocksOfNeighborChange(xCoord, yCoord, zCoord, getBlockType());

            if (myGrid != null) {
                myGrid.destroyAndRecreate();
            }

            for (SubTileMultiBlock subTile : subTiles) {
                subTile.destroyAndRecreate();
            }

            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            return true;
        }

        return false;
    }

    public RouteCache getCache() {

        return getCache(true);
    }

    public RouteCache getCache(boolean urgent) {

        return urgent ? internalGrid.getRoutesFromOutput(this) : internalGrid.getRoutesFromOutputNonUrgent(this);
    }

    public Route getRoute(Entity entity, int side, byte speed) {
        if (entity == null || entity.isDead) {
            return null;
        }

        for (Route outputRoute : getCache().outputRoutes) {
            if(outputRoute.endPoint == this)
                continue;

            Route route = outputRoute.copy();
            byte outSide = outputRoute.endPoint.getStuffedSide();
            route.pathDirections.add(outSide);
            return route;
        }
        return null;
    }

    public EntityTransport findRoute(Entity entity, int side, byte speed) {
        Route route = getRoute(entity, side, speed);
        return route != null ? new EntityTransport(this, route, (byte) side, speed) : null;
    }

    @Override
    public int getWeight() {
        return 1;
    }

    @Override
    public boolean canStuffItem() {
        return false;
    }

    @Override
    public boolean isOutput() {
        return isOutput;
    }

    @Override
    public int getMaxRange() {
        return Integer.MAX_VALUE;
    }

    @Override
    public NeighborTypes getCachedSideType(byte side) {

        return neighborTypes[side];
    }

    @Override
    public ConnectionTypes getConnectionType(byte side) {

        return connectionTypes[side];
    }

    @Override
    public IMultiBlock getCachedTile(byte side) {

        return neighborMultiBlocks[side];
    }

    @Override
    public TileItemDuct.RouteInfo canRouteItem(ItemStack stack) {
        return TileItemDuct.noRoute;
    }

    @Override
    public byte getStuffedSide() {
        for (byte i = 0; i < 6; i++) {
            if(neighborTypes[i] == NeighborTypes.OUTPUT)
                return i;
        }

        return 0;
    }

    @Override
    public boolean acceptingStuff() {
        return false;
    }

    @Override
    public BlockDuct.ConnectionTypes getConnectionType(int side) {

        if (connectionTypes[side] == ConnectionTypes.FORCED) {
            return BlockDuct.ConnectionTypes.DUCT;
        }
        return super.getConnectionType(side);
    }

    @Override
    public void addTraceableCuboids(List<IndexedCuboid6> cuboids) {

        EntityPlayer player = ThermalDynamics.proxy.getClientPlayerSafe();
        if (player != null && player.ridingEntity != null && player.ridingEntity.getClass() == EntityTransport.class) {
            return;
        }
        super.addTraceableCuboids(cuboids);
    }

}
