package cofh.thermaldynamics.duct.entity;

import cofh.repack.codechicken.lib.raytracer.RayTracer;
import cofh.thermaldynamics.block.SubTileMultiBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MovingObjectPosition;

public class TileTransportDuct extends TileTransportDuctBaseRoute {
    @Override
    public boolean openGui(EntityPlayer player) {

        if (super.openGui(player) || worldObj.isRemote) {
            return true;
        }

        if (internalGrid == null) {
            return false;
        }

        MovingObjectPosition movingObjectPosition = RayTracer.retraceBlock(worldObj, player, xCoord, yCoord, zCoord);
        if (movingObjectPosition == null) {
            return false;
        }

        int subHit = movingObjectPosition.subHit;
        int hitSide = movingObjectPosition.sideHit;

        if (subHit >= 0 && subHit <= 13) {
            int i = subHit == 13 ? hitSide : subHit < 6 ? subHit : subHit - 6;

            onNeighborBlockChange();

            if (neighborMultiBlocks[i] != null) {
                return false;
            }

            ItemStack heldItem = player.getHeldItem();

            if (connectionTypes[i] == ConnectionTypes.FORCED) {
                if (heldItem != null && heldItem.getItem() == Items.spawn_egg) {

                    Entity entity = EntityList.createEntityByID(heldItem.getItemDamage(), world());

                    if (entity == null || !(entity instanceof EntityLivingBase)) {
                        return false;
                    }

                    EntityTransport route = findRoute(entity, i ^ 1, (byte) 1);

                    if (route != null) {
                        entity.setPosition(x(), y(), z());
                        world().spawnEntityInWorld(entity);

                        route.start((EntityLivingBase) entity);
                    }

                    return true;
                }

                if (heldItem != null && heldItem.getItem() == Items.minecart) {
                    EntityTransport route = findRoute(player, i ^ 1, (byte) 50);

                    if (route != null) {
                        route.start(player);
                    }

                    return true;
                }

            }

            if (heldItem != null) {
                return false;
            }

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


}
