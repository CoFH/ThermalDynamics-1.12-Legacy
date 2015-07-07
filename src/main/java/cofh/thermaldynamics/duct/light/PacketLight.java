package cofh.thermaldynamics.duct.light;

import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.position.BlockPosition;
import cofh.lib.util.position.ChunkCoord;
import cofh.thermaldynamics.multiblock.IMultiBlock;

import com.google.common.collect.Iterables;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;

public class PacketLight extends PacketCoFHBase {

	public ByteArrayInputStream stream;
	public byte[] data;

	public static HashMap<ChunkCoord, List<BlockPosition>> buildList(Collection<IMultiBlock> tiles) {

		HashMap<ChunkCoord, List<BlockPosition>> map = new HashMap<ChunkCoord, List<BlockPosition>>();
		for (IMultiBlock tile : tiles) {
			BlockPosition e = new BlockPosition(tile.x(), tile.y(), tile.z());
			ChunkCoord key = new ChunkCoord(e);

			List<BlockPosition> blockPositions = map.get(key);
			if (blockPositions == null) {
				blockPositions = new LinkedList<BlockPosition>();
				map.put(key, blockPositions);
			}

			blockPositions.add(e);
		}
		return map;
	}

	public PacketLight() {

		super();
	}

	public PacketLight(boolean lit, LightGrid grid) {

		addBool(lit);

		addInt(grid.idleSet.size() + grid.nodeSet.size() );

		for (IMultiBlock iMultiBlock : Iterables.concat(grid.nodeSet, grid.idleSet)) {
			addInt(iMultiBlock.x());
			addByte(iMultiBlock.y());
			addInt(iMultiBlock.z());
		}
	}

	@Override
	public void handlePacket(EntityPlayer player, boolean isServer) {

		boolean lit = getBool();

		int n = getInt();
		ArrayList<TileLightDuct> tiles = new ArrayList<TileLightDuct>(n);
		for (int i = 0; i < n; i++) {
			int x = getInt();
			int y = getByte();
			int z = getInt();

			TileEntity tile = player.worldObj.getTileEntity(x, y, z);
			if (tile instanceof TileLightDuct) {
				TileLightDuct lamp = (TileLightDuct) tile;
				tiles.add(lamp);
				lamp.lit = lit;
			}
		}

		for (TileLightDuct tile : tiles) {
			tile.checkLight();
		}
	}

	@Override
	public void decodeInto(ChannelHandlerContext ctx, ByteBuf buffer) {

		datain = new DataInputStream(stream = new ByteArrayInputStream(buffer.array()));
		try {
			datain.skipBytes(1);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
