package cofh.thermaldynamics.duct.light;

import cofh.core.network.PacketCoFHBase;
import cofh.thermaldynamics.multiblock.IMultiBlock;
import com.google.common.collect.Iterables;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PacketLight extends PacketCoFHBase {

	public ByteArrayInputStream stream;

	public PacketLight() {

		super();
	}

	public PacketLight(boolean lit, LightGrid grid) {

		addBool(lit);

		addVarInt(grid.idleSet.size() + grid.nodeSet.size());

		for (IMultiBlock iMultiBlock : Iterables.concat(grid.nodeSet, grid.idleSet)) {
			addVarInt(iMultiBlock.x());
			addVarInt(iMultiBlock.y());
			addVarInt(iMultiBlock.z());
		}
	}

	@Override
	public void handlePacket(EntityPlayer player, boolean isServer) {

		boolean lit = getBool();

		int n = getVarInt();
		World world = player.worldObj;
		for (int i = 0; i < n; i++) {
			int x = getVarInt();
			int y = getVarInt();
			int z = getVarInt();
			BlockPos pos = new BlockPos(x, y, z);
			if (!world.isBlockLoaded(pos)) {
				continue;
			}

			TileEntity tile = world.getTileEntity(pos);
			if (tile instanceof TileLightDuct) {
				TileLightDuct lamp = (TileLightDuct) tile;
				lamp.lit = lit;
				lamp.checkLight();
			}
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
