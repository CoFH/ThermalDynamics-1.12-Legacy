package cofh.thermaldynamics.duct.entity;

import cofh.CoFHCore;
import cofh.repack.codechicken.lib.raytracer.IndexedCuboid6;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import java.util.List;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public abstract class TileTransportDuctBase extends TileTDBase  {
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
	public BlockDuct.ConnectionTypes getConnectionType(int side) {

		if (connectionTypes[side] == ConnectionTypes.FORCED) {
			return BlockDuct.ConnectionTypes.DUCT;
		}
		return super.getConnectionType(side);
	}

	@Override
	public void addTraceableCuboids(List<IndexedCuboid6> cuboids) {

		EntityPlayer player = CoFHCore.proxy.getClientPlayer();
		if (player != null && player.ridingEntity != null && player.ridingEntity.getClass() == EntityTransport.class) {
			return;
		}
		super.addTraceableCuboids(cuboids);
	}

    public abstract boolean advanceEntity(EntityTransport transport);

    public IMultiBlock getPhysicalConnectedSide(byte direction) {
        return super.getConnectedSide(direction);
    }


    public boolean advanceEntityClient(EntityTransport t) {
        t.progress += t.step;
        if (t.progress >= EntityTransport.PIPE_LENGTH) {
            if (!t.trySimpleAdvance()) return true;
        }
        return false;
    }
}
