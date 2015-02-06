package cofh.thermaldynamics.debughelper;

import cofh.core.network.ITileInfoPacketHandler;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.position.BlockPosition;
import cofh.lib.util.position.ChunkCoord;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.chunk.Chunk;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.util.*;

public class PacketMultiTileInfo extends PacketCoFHBase {

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

    public PacketMultiTileInfo() {
        super();
    }

    public PacketMultiTileInfo(HashMap<ChunkCoord, List<BlockPosition>> map) {
        addInt(map.entrySet().size());
        for (Map.Entry<ChunkCoord, List<BlockPosition>> entry : map.entrySet()) {
            addInt(entry.getKey().chunkX);
            addInt(entry.getKey().chunkZ);
            addShort(entry.getValue().size());
            for (BlockPosition pos : entry.getValue()) {
                addByte((pos.x & 15) << 4 | (pos.z & 15));
                addByte(pos.y);
            }
        }
    }

    @Override
    public void handlePacket(EntityPlayer player, boolean isServer) {
        int n, n2, xz;
        Chunk chunk;

        n = getInt();
        ArrayList<ITileInfoPacketHandler> tiles = new ArrayList<ITileInfoPacketHandler>();
        for (int i = 0; i < n; i++) {
            chunk = player.worldObj.getChunkFromChunkCoords(getInt(), getInt());

            n2 = getShort();
            for (int j = 0; j < n2; j++) {
                xz = getByte();
                TileEntity tile = chunk.getTileEntityUnsafe(xz >> 4, getInt(), xz & 15);
                if (tile instanceof ITileInfoPacketHandler)
                    tiles.add(((ITileInfoPacketHandler) tile));
            }
        }

        byte[] tilePacket = new byte[stream.available()];

        assert stream.read(tilePacket, 0, tilePacket.length) == tilePacket.length;

        PacketTileInfo packetTileInfo = new PacketTileInfo();
        for (ITileInfoPacketHandler tile : tiles) {
            packetTileInfo.datain = new DataInputStream(new ByteArrayInputStream(tilePacket));
            tile.handleTileInfoPacket(packetTileInfo, isServer, player);
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
