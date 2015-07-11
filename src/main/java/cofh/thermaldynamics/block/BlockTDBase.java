package cofh.thermaldynamics.block;

import cofh.api.energy.IEnergyHandler;
import cofh.api.tileentity.ITileInfo;
import cofh.core.block.BlockCoFHBase;
import cofh.lib.util.helpers.ServerHelper;
import cofh.lib.util.helpers.StringHelper;
import cofh.repack.codechicken.lib.raytracer.IndexedCuboid6;
import cofh.repack.codechicken.lib.raytracer.RayTracer;
import cofh.repack.codechicken.lib.vec.BlockCoord;
import cofh.repack.codechicken.lib.vec.Vector3;
import cofh.thermaldynamics.block.TileTDBase.NeighborTypes;
import cofh.thermaldynamics.duct.entity.EntityTransport;
import cofh.thermaldynamics.util.Utils;
import cpw.mods.fml.common.eventhandler.Event.Result;

import java.util.LinkedList;
import java.util.List;

import net.minecraft.block.ITileEntityProvider;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;

public abstract class BlockTDBase extends BlockCoFHBase implements ITileEntityProvider {

	protected BlockTDBase(Material material) {

		super(material);
	}

	@Override
	public TileEntity createNewTileEntity(World world, int metadata) {

		return null;
	}

	@Override
	public boolean isSideSolid(IBlockAccess world, int x, int y, int z, ForgeDirection side) {

		TileTDBase theTile = (TileTDBase) world.getTileEntity(x, y, z);
		return (theTile != null && (theTile.covers[side.ordinal()] != null || theTile.attachments[side.ordinal()] != null
				&& theTile.attachments[side.ordinal()].makesSideSolid()))
				|| super.isSideSolid(world, x, y, z, side);
	}

	public float getSize(World world, int x, int y, int z) {

		return 0.3F;
	}

	@Override
	public void addCollisionBoxesToList(World world, int x, int y, int z, AxisAlignedBB axis, List list, Entity entity) {

		if (entity instanceof EntityTransport) {
			return;
		}

		float min = getSize(world, x, y, z);
		float max = 1 - min;

		this.setBlockBounds(min, min, min, max, max, max);
		super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
		TileTDBase theTile = (TileTDBase) world.getTileEntity(x, y, z);

		if (theTile != null) {
			for (byte i = 0; i < 6; i++) {
				if (theTile.attachments[i] != null) {
					theTile.attachments[i].addCollisionBoxesToList(axis, list, entity);
				}
				if (theTile.covers[i] != null) {
					theTile.covers[i].addCollisionBoxesToList(axis, list, entity);
				}
			}
			if (theTile.neighborTypes[0] != NeighborTypes.NONE) {
				this.setBlockBounds(min, 0.0F, min, max, max, max);
				super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
			}
			if (theTile.neighborTypes[1] != NeighborTypes.NONE) {
				this.setBlockBounds(min, min, min, max, 1.0F, max);
				super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
			}
			if (theTile.neighborTypes[2] != NeighborTypes.NONE) {
				this.setBlockBounds(min, min, 0.0F, max, max, max);
				super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
			}
			if (theTile.neighborTypes[3] != NeighborTypes.NONE) {
				this.setBlockBounds(min, min, min, max, max, 1.0F);
				super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
			}
			if (theTile.neighborTypes[4] != NeighborTypes.NONE) {
				this.setBlockBounds(0.0F, min, min, max, max, max);
				super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
			}
			if (theTile.neighborTypes[5] != NeighborTypes.NONE) {
				this.setBlockBounds(min, min, min, 1.0F, max, max);
				super.addCollisionBoxesToList(world, x, y, z, axis, list, entity);
			}
		}
		this.setBlockBounds(min, min, min, max, max, max);
	}

	@Override
	public MovingObjectPosition collisionRayTrace(World world, int x, int y, int z, Vec3 start, Vec3 end) {

		TileTDBase theTile = (TileTDBase) world.getTileEntity(x, y, z);
		if (theTile == null) {
			return null;
		}
		List<IndexedCuboid6> cuboids = new LinkedList<IndexedCuboid6>();
		theTile.addTraceableCuboids(cuboids);
		return RayTracer.instance().rayTraceCuboids(new Vector3(start), new Vector3(end), cuboids, new BlockCoord(x, y, z), this);
	}

	@Override
	public boolean onBlockActivated(World world, int x, int y, int z, EntityPlayer player, int hitSide, float hitX, float hitY, float hitZ) {

		PlayerInteractEvent event = new PlayerInteractEvent(player, Action.RIGHT_CLICK_BLOCK, x, y, z, hitSide, world);
		if (MinecraftForge.EVENT_BUS.post(event) || event.getResult() == Result.DENY || event.useBlock == Result.DENY) {
			return false;
		}
		if (player.isSneaking()) {
			if (Utils.isHoldingUsableWrench(player, x, y, z)) {
				if (ServerHelper.isServerWorld(world) && canDismantle(player, world, x, y, z)) {
					dismantleBlock(player, world, x, y, z, false);
				}
				Utils.usedWrench(player, x, y, z);
				return true;
			}
			return false;
		}
		TileTDBase tile = (TileTDBase) world.getTileEntity(x, y, z);

		if (tile == null) {
			return false;
		}
		if (Utils.isHoldingUsableWrench(player, x, y, z)) {
			if (ServerHelper.isServerWorld(world)) {
				tile.onWrench(player, hitSide);
			}
			Utils.usedWrench(player, x, y, z);
			return true;
		}

		// Item equipped = player.getCurrentEquippedItem() != null ? player.getCurrentEquippedItem().getItem() : null;
		//
		// if (equipped instanceof ItemBlock) {
		// Block block = ((ItemBlock) equipped).field_150939_a;
		// int meta = player.getHeldItem().getItem().getMetadata(player.getHeldItem().getItemDamage());
		// int side = -1;
		// MovingObjectPosition position = RayTracer.retraceBlock(world, player, x, y, z);
		// if (position != null) {
		// int subHit = position.subHit;
		// if (subHit < 6)
		// side = subHit;
		// else if (subHit < 12)
		// side = subHit - 6;
		// else if (subHit == 13)
		// side = hitSide;
		// if (side != -1) {
		// if (!world.isRemote) {
		// tile.addFacade(new Facade(tile, (byte) side, block, meta));
		// }
		// return true;
		// }
		// }
		//
		// }

		return tile.openGui(player);
	}

	/* IBlockDebug */
	@Override
	public void debugBlock(IBlockAccess world, int x, int y, int z, ForgeDirection side, EntityPlayer player) {

		try {
			((TileTDBase) world.getTileEntity(x, y, z)).doDebug(player);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/* IBlockInfo */
	@Override
	public void getBlockInfo(IBlockAccess world, int x, int y, int z, ForgeDirection side, EntityPlayer player, List<IChatComponent> info, boolean debug) {

		TileEntity tile = world.getTileEntity(x, y, z);

		if (tile instanceof ITileInfo) {
			((ITileInfo) tile).getTileInfo(info, side, player, debug);
		} else {
			if (tile instanceof IEnergyHandler) {
				IEnergyHandler eHandler = (IEnergyHandler) tile;
				if (eHandler.getMaxEnergyStored(side) <= 0) {
					return;
				}
				info.add(new ChatComponentText(StringHelper.localize("info.cofh.energy") + ": " + eHandler.getEnergyStored(side) + "/"
						+ eHandler.getMaxEnergyStored(side) + " RF."));
			}
		}
	}

}
