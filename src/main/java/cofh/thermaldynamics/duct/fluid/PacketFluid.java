package cofh.thermaldynamics.duct.fluid;

import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.helpers.BlockHelper;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.IDuctHolder;
import com.google.common.collect.Iterables;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fluids.FluidStack;

import java.io.ByteArrayInputStream;

public class PacketFluid extends PacketCoFHBase {

	public ByteArrayInputStream stream;

	public PacketFluid() {

		super();
	}

	public PacketFluid(GridFluid grid, int size) {

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
		World world = player.world;
		for (int i = 0; i < n; i++) {
			int x = getVarInt();
			int y = getVarInt();
			int z = getVarInt();
			BlockPos pos = new BlockPos(x, y, z);
			if (!world.isBlockLoaded(pos)) {
				continue;
			}

			TileEntity tile = world.getTileEntity(pos);

			if (tile instanceof IDuctHolder) {
				DuctUnitFluid duct = ((IDuctHolder) tile).getDuct(DuctToken.FLUID);
				if (duct != null) {
					duct.myRenderFluid = fluid;
					duct.updateLighting();
					BlockHelper.callBlockUpdate(world, new BlockPos(x, y, z));
				}
			}
		}
	}

}
