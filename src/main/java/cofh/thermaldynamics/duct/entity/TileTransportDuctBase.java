package cofh.thermaldynamics.duct.entity;

import codechicken.lib.raytracer.IndexedCuboid6;
import cofh.CoFHCore;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

import java.util.List;

public abstract class TileTransportDuctBase extends TileTDBase {

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
	public void addTraceableCuboids(List<IndexedCuboid6> cuboids) {

		EntityPlayer player = CoFHCore.proxy.getClientPlayer();
		if (player != null && player.getRidingEntity() != null && player.getRidingEntity().getClass() == EntityTransport.class) {
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
			if (!t.trySimpleAdvance()) {
				return true;
			}
		}
		return false;
	}
}
