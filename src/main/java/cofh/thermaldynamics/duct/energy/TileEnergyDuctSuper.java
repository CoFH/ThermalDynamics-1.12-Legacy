package cofh.thermaldynamics.duct.energy;

import cofh.thermaldynamics.duct.BlockDuct;
import cofh.thermaldynamics.duct.attachments.cover.CoverHoleRender;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.common.util.ForgeDirection;

public class TileEnergyDuctSuper extends TileEnergyDuct {

	private EnergyGridSuper internalGridSC;

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		internalGridSC = (EnergyGridSuper) newGrid;
	}

	@Override
	public MultiBlockGrid getNewGrid() {

		return new EnergyGridSuper(worldObj, getDuctType().type);
	}

	@Override
	public int receiveEnergy(ForgeDirection from, int maxReceive, boolean simulate) {

		if (this.internalGridSC != null && canConnectEnergy(from)) {
			return internalGridSC.sendEnergy(maxReceive, simulate);
		}
		return 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public CoverHoleRender.ITransformer[] getHollowMask(byte side) {

		BlockDuct.ConnectionTypes connectionType = getRenderConnectionType(side);
		return connectionType == BlockDuct.ConnectionTypes.NONE ? null : CoverHoleRender.hollowDuctCryo;
	}
}
