package thermaldynamics.ducts.energy;

import cofh.lib.util.helpers.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class EnergySuperConductorGrid extends EnergyGrid {

    public EnergySuperConductorGrid(World world, int type) {
        super(world, type);
        myStorage.setMaxExtract(myStorage.getMaxEnergyStored());
    }

    @Override
    public void tickGrid() {
        super.tickGrid();
        int i = 0;
        if (nodeList == null) {
            nodeList = new TileEnergyDuct[nodeSet.size()];
            for (IMultiBlock multiBlock : nodeSet) {
                nodeList[i] = (TileEnergyDuct) multiBlock;
                i++;
            }
            overSent = new boolean[nodeList.length];
        }

//        if (recentlySent && worldGrid.worldObj.getTotalWorldTime() % 4 == 0) {
//            recentlySent = false;
//        }
    }

    TileEnergyDuct[] nodeList = null;
    boolean recentlySent = false;
    boolean[] overSent = null;

    public int sendEnergy(int maxSent, boolean simulate) {
        if (nodeList == null || nodeList.length == 0 || maxSent <= 0)
            return myStorage.receiveEnergy(maxSent, simulate);

        int curSent = 0;
        int toDistribute = maxSent / nodeList.length;


        for (int i = 0; i < nodeList.length && curSent < maxSent; i++) {
            int t = sendEnergytoTile(nodeList[i], 0, maxSent - curSent, toDistribute);
            overSent[i] = t >= toDistribute && toDistribute < maxSent;
            curSent += t;
        }


        for (int i = 0; i < nodeList.length; i++) {
            if (overSent[i] && curSent < maxSent) {
                curSent = sendEnergytoTile(nodeList[i], curSent, maxSent, maxSent - toDistribute);
            }

            if (!simulate && i > 0) {
                int j = MathHelper.RANDOM.nextInt(i + 1);
                if (i != j) {
                    TileEnergyDuct t = nodeList[i];
                    nodeList[i] = nodeList[j];
                    nodeList[j] = t;
                }
            }
        }


        curSent += myStorage.receiveEnergy(maxSent - curSent, simulate);

        recentlySent = true;

        return curSent;
    }


    public int sendEnergytoTile(TileEnergyDuct dest, int curSent, int maxSent, int toDistribute) {

        for (int i = 0; i < 6 && curSent < maxSent; i++) {
            if (dest.neighborTypes[i] == TileMultiBlock.NeighborTypes.TILE) {
                if (dest.cache[i] != null) {
                    curSent += dest.cache[i].receiveEnergy(ForgeDirection.VALID_DIRECTIONS[i ^ 1], Math.min(toDistribute, maxSent - curSent), false);
                }
            }
        }
        return curSent;
    }

    @Override
    public void onMajorGridChange() {
        super.onMajorGridChange();
        nodeList = null;
    }

    @Override
    public boolean canGridsMerge(MultiBlockGrid grid) {
        return grid instanceof EnergySuperConductorGrid;
    }

    @Override
    public void destory() {
        nodeList = null;
        super.destory();
    }

}
