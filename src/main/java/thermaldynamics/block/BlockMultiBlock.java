package thermaldynamics.block;

import buildcraft.api.tools.IToolWrench;

import cofh.block.BlockCoFHBase;
import cofh.repack.codechicken.lib.raytracer.IndexedCuboid6;
import cofh.repack.codechicken.lib.raytracer.RayTracer;
import cofh.repack.codechicken.lib.vec.BlockCoord;
import cofh.repack.codechicken.lib.vec.Vector3;
import cofh.util.ServerHelper;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import thermaldynamics.block.TileMultiBlock.NeighborTypes;
import thermalexpansion.util.Utils;

public abstract class BlockMultiBlock extends BlockCoFHBase implements ITileEntityProvider {

	protected BlockMultiBlock(Material material) {

		super(material);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {

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
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int hitSide, float hitX, float hitY, float hitZ) {

		if (Utils.isHoldingDebugger(player)) {
			try {
				((TileMultiBlock) world.getTileEntity(x, y, z)).doDebug(player);
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (Utils.isHoldingMultimeter(player)) {
			return true;
		}
		Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;

		if (player.isSneaking()) {
			if (Utils.isHoldingUsableWrench(player, x, y, z)) {
				if (ServerHelper.isServerWorld(world) && canDismantle(player, world, x, y, z)) {
					dismantleBlock(player, world, x, y, z, false);
				}
				((IToolWrench) equipped).wrenchUsed(player, x, y, z);
				return true;
			}
			return false;
		}
		TileMultiBlock tile = (TileMultiBlock) world.getTileEntity(x, y, z);

		if (tile == null) {
			return false;
		}
		if (Utils.isHoldingUsableWrench(player, x, y, z)) {
			if (ServerHelper.isServerWorld(world)) {
				tile.onWrench(player, hitSide);
			}
			((IToolWrench) equipped).wrenchUsed(player, x, y, z);
			return true;
		}
		return tile.openGui(player);
	}

}
