package cofh.thermaldynamics.duct.fluid;

import codechicken.lib.util.BlockUtils;
import cofh.core.network.PacketCoFHBase;
import com.google.common.collect.Iterables;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class PacketFluid extends PacketCoFHBase {

	public ByteArrayInputStream stream;

	public PacketFluid() {

		super();
	}

	public PacketFluid(FluidGrid grid, int size) {

		addFluidStack(grid.getRenderFluid());
		addVarInt(size);

		for (Object block : Iterables.concat(grid.nodeSet, grid.idleSet)) {
			DuctUnitFluid duct = ((DuctUnitFluid) block);
			if (!duct.getDuctType().opaque) {
				addVarInt(duct.x());
				addVarInt(duct.y());
				addVarInt(duct.z());
			}
		}
	}

	@Override
	public void handlePacket(EntityPlayer player, boolean isServer) {

		FluidStack fluid = getFluidStack();

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
			if (tile instanceof DuctUnitFluid) {
				DuctUnitFluid duct = (DuctUnitFluid) tile;
				duct.myRenderFluid = fluid;
				duct.updateLighting();
				BlockUtils.fireBlockUpdate(world, new BlockPos(x, y, z));
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
