package cofh.thermaldynamics.duct.light;

import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.lib.util.position.ChunkCoord;
import cofh.thermaldynamics.core.WorldGridList;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import com.google.common.collect.Iterables;

import java.util.HashSet;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.management.PlayerChunkMap;
import net.minecraft.server.management.PlayerChunkMapEntry;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;

public class LightGrid extends MultiBlockGrid {

	public HashSet<ChunkCoord> chunks;
	public boolean upToDate = false;

	public LightGrid(WorldGridList worldGrid) {

		super(worldGrid);
	}

	public LightGrid(World worldObj) {

		super(worldObj);
	}

	@Override
	public boolean canAddBlock(IMultiBlock aBlock) {

		return aBlock instanceof TileLightDuct;
	}

	@Override
	public void onMajorGridChange() {

		super.onMajorGridChange();
		upToDate = false;
		chunks = null;
	}

	@Override
	public void onMinorGridChange() {

		super.onMinorGridChange();
		upToDate = false;
	}

	@Override
	public void tickGrid() {

		super.tickGrid();

		if (upToDate && worldGrid.worldObj.getTotalWorldTime() % 160 != 0) {
			if (rs != null && rs.nextRedstoneLevel != -128) {
				upToDate = false;
			}
			return;
		}

		upToDate = rs == null || rs.nextRedstoneLevel == -128;

		boolean shouldBeLit;

		if (rs != null) {
			if (rs.nextRedstoneLevel != -128) {
				shouldBeLit = rs.nextRedstoneLevel > 0;
			} else {
				shouldBeLit = rs.redstoneLevel > 0;
			}
		} else {
			shouldBeLit = false;
		}

		// shouldBeLit = false;

		if (!shouldBeLit) {
			for (Object object : Iterables.concat(nodeSet, idleSet)) {
				TileLightDuct lamp = (TileLightDuct) object;
				if (lamp.lit) {
					shouldBeLit = true;
					break;
				}
			}
		}

		if (lit != shouldBeLit) {
			setLight(shouldBeLit);
		}
	}

	boolean lit = false;

	@SuppressWarnings("unchecked")
	public void setLight(boolean lit) {

		this.lit = lit;

		if (chunks == null) {
			buildMap();
		}

		if (worldGrid.worldObj instanceof WorldServer) {
			PacketCoFHBase packet = new PacketLight(lit, this);
			WorldServer dimension = (WorldServer) worldGrid.worldObj;
			PlayerChunkMap manger = dimension.getPlayerChunkMap();
			for (EntityPlayer player : (List<EntityPlayer>) dimension.playerEntities) {
				for (ChunkCoord chunk : chunks) {

					PlayerChunkMapEntry inst = manger.getEntry(chunk.chunkX, chunk.chunkZ);
					if (inst != null && inst.players.contains(player)) {
						PacketHandler.sendTo(packet, player);
						break;
					}
				}
			}

			for (Object block : Iterables.concat(nodeSet, idleSet)) {
				((TileLightDuct) block).checkLight();
			}
		}
	}

	public void buildMap() {

		chunks = new HashSet<ChunkCoord>();
		for (IMultiBlock iMultiBlock : Iterables.concat(nodeSet, idleSet)) {
			buildMapEntry(iMultiBlock);
		}
	}

	private void buildMapEntry(IMultiBlock iMultiBlock) {

		chunks.add(new ChunkCoord(iMultiBlock.x() >> 4, iMultiBlock.z() >> 4));
	}

}
