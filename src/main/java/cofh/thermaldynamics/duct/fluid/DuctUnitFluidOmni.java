package cofh.thermaldynamics.duct.fluid;

import cofh.core.network.PacketBase;
import cofh.thermaldynamics.block.BlockDuct;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DuctUnitFluidOmni extends DuctUnitFluid {

	byte fluidMask;

	public DuctUnitFluidOmni(TileGrid parent, Duct duct) {

		super(parent, duct);
	}

	@Override
	public void writeToTilePacket(PacketBase payload) {

		super.writeToTilePacket(payload);

		byte fluidMask = 0;
		for (int i = 0; i < ductCache.length; i++) {
			if (ductCache[i] != null) {
				fluidMask |= (1 << i);
			}
		}
		payload.addByte(fluidMask);
	}

	@Override
	@SideOnly (Side.CLIENT)
	public void handleTilePacket(PacketBase payload) {

		super.handleTilePacket(payload);
		this.fluidMask = payload.getByte();
	}

	@Override
	public int[] getRenderFluidConnections() {

		int[] renderFluidConnections = super.getRenderFluidConnections();
		for (int i = 0; i < 6; i++) {
			if ((fluidMask & (1 << i)) == 0) {
				renderFluidConnections[i] = BlockDuct.ConnectionType.STRUCTURE_CONNECTION.ordinal();
			}
		}
		return renderFluidConnections;
	}
}
