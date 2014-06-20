package thermalducts.block;

import codechicken.lib.raytracer.IndexedCuboid6;
import codechicken.lib.raytracer.RayTracer;
import codechicken.lib.vec.BlockCoord;
import codechicken.lib.vec.Vector3;
import cofh.block.BlockCoFHBase;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import thermalducts.block.TileMultiBlock.NeighborTypes;
import thermalexpansion.util.Utils;

public abstract class BlockMultiBlock extends BlockCoFHBase implements ITileEntityProvider {

	protected BlockMultiBlock(Material p_i45394_1_) {

		super(p_i45394_1_);
	}

	@Override
	public TileEntity createNewTileEntity(World var1, int var2) {

		return new TileMultiBlock();
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axis, List list, Entity entity) {

		this.setBlockBounds(0.30F, 0.30F, 0.30F, 0.70F, 0.70F, 0.70F);
		super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
		TileMultiBlock theTile = (TileMultiBlock) world.getTileEntity(x, y, z);

		if (theTile != null) {
			if (theTile.neighborTypes[0] != NeighborTypes.NONE) {
				this.setBlockBounds(0.30F, 0.0F, 0.30F, 0.70F, 0.70F, 0.70F);
				super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
			}
			if (theTile.neighborTypes[1] != NeighborTypes.NONE) {
				this.setBlockBounds(0.30F, 0.30F, 0.30F, 0.70F, 1.0F, 0.70F);
				super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
			}
			if (theTile.neighborTypes[2] != NeighborTypes.NONE) {
				this.setBlockBounds(0.30F, 0.30F, 0.0F, 0.70F, 0.70F, 0.70F);
				super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
			}
			if (theTile.neighborTypes[3] != NeighborTypes.NONE) {
				this.setBlockBounds(0.30F, 0.30F, 0.30F, 0.70F, 0.70F, 1.0F);
				super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
			}
			if (theTile.neighborTypes[4] != NeighborTypes.NONE) {
				this.setBlockBounds(0.0F, 0.30F, 0.30F, 0.70F, 0.70F, 0.70F);
				super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
			}
			if (theTile.neighborTypes[5] != NeighborTypes.NONE) {
				this.setBlockBounds(0.30F, 0.30F, 0.30F, 1.0F, 0.70F, 0.70F);
				super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
			}
		}
		// this.setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end) {

		TileMultiBlock theTile = (TileMultiBlock) world.getTileEntity(x, y, z);
		if (theTile == null) {
			return null;
		}
		List<IndexedCuboid6> cuboids = new LinkedList<IndexedCuboid6>();
		theTile.addTraceableCuboids(cuboids);
		return RayTracer.instance().rayTraceCuboids(new Vector3(start), new Vector3(end), cuboids, new BlockCoord(x, y, z), this);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer thePlayer, int p_149727_6_, float p_149727_7_, float p_149727_8_, float p_149727_9_) {

		if (Utils.isHoldingDebugger(thePlayer)) {
			try {
				((TileMultiBlock) world.getTileEntity(x, y, z)).doDebug(thePlayer);
			} catch (Exception _) {
				_.printStackTrace();
			}
		}
		return false;
	}

}
