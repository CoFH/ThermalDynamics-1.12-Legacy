package cofh.thermaldynamics.duct.fluid;

import cofh.core.network.PacketCoFHBase;
import com.google.common.collect.Iterables;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

public class PacketFluid extends PacketCoFHBase {

	public ByteArrayInputStream stream;

	public PacketFluid() {

		super();
	}

	public PacketFluid(FluidGrid grid, int size) {

		addFluidStack(grid.getRenderFluid());
		addVarInt(size);

		for (Object block : Iterables.concat(grid.nodeSet, grid.idleSet)) {
			TileFluidDuct duct = ((TileFluidDuct) block);
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
			if (!world.blockExists(x, y, z)) {
				continue;
			}

			TileEntity tile = world.getTileEntity(x, y, z);
			if (tile instanceof TileFluidDuct) {
				TileFluidDuct duct = (TileFluidDuct) tile;
				duct.myRenderFluid = fluid;
				duct.updateLighting();
				world.markBlockForUpdate(x, y, z);
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
