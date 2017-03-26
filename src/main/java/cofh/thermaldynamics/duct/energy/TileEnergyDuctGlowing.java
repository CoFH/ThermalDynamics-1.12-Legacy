package cofh.thermaldynamics.duct.energy;

import cofh.api.energy.IEnergyReceiver;
import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.thermaldynamics.duct.NeighborType;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;

public class TileEnergyDuctGlowing extends DuctUnitEnergy {

	public float[] fluxIn = new float[6];
	public float[] fluxOut = new float[6];
	int totalFlux = 0;
	int prevHash;

	@Override
	public MultiBlockGrid createGrid() {

		return new EnergyGridGlowing(worldObj, getDuctType().type);
	}

	public void resetFlux() {

		for (int i = 0; i < 6; i++) {
			fluxIn[i] = 0;
			fluxOut[i] = 0;
		}
		totalFlux = 0;
	}

	public void updateFlux() {

		int hash = 0;
		int sideMask = 0;
		for (int i = 0; i < 6; i++) {
			float aFlux = fluxIn[i];
			if (aFlux != 0) {
				sideMask |= 1 << i;
				hash = hash * 31 + Float.floatToIntBits(aFlux);
			}
		}

		for (int i = 0; i < 6; i++) {
			float aFlux = fluxOut[i];
			if (aFlux != 0) {
				sideMask |= 1 << (6 + i);
				hash = hash * 31 + Float.floatToIntBits(aFlux);
			}
		}

		hash = hash * 31 + sideMask;
		if (hash != prevHash) {
			prevHash = hash;
			PacketTileInfo myPayload = PacketTileInfo.newPacket(this);
			myPayload.addByte(0);
			myPayload.addByte(0);

			myPayload.addInt(sideMask);
			for (float v : fluxIn) {
				if (v != 0) {
					myPayload.addFloat(v);
				}
			}
			for (float v : fluxOut) {
				if (v != 0) {
					myPayload.addFloat(v);
				}
			}

			PacketHandler.sendToAllAround(myPayload, this);
		}
	}

	@Override
	public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {

		byte b = payload.getByte();
		if (b == 0) {
			int sideMask = payload.getInt();

			for (int i = 0; i < 6; i++) {
				if ((sideMask & (1 << i)) != 0) {
					fluxIn[i] = payload.getFloat();
				} else {
					fluxIn[i] = 0;
				}
			}

			for (int i = 0; i < 6; i++) {
				if ((sideMask & (1 << (1 + i))) != 0) {
					fluxOut[i] = payload.getFloat();
				} else {
					fluxOut[i] = 0;
				}
			}
		}
	}

	public void addFlux(float amount, byte side, boolean output) {

		if (amount == 0 || side == -1) {
			return;
		}
		totalFlux += Math.abs(amount);

		float[] fluxArray = output ? this.fluxOut : this.fluxIn;

		fluxArray[side] += amount;
	}

	public void addFlux(int amount, int sideMask, boolean output) {

		if (amount == 0 || sideMask == 0) {
			return;
		}
		totalFlux += Math.abs(amount);

		float[] fluxArray = output ? this.fluxOut : this.fluxIn;
		switch (sideMask) {
			case 1:
				fluxArray[0] += amount;
				return;
			case 2:
				fluxArray[1] += amount;
				return;
			case (4):
				fluxArray[2] += amount;
				return;
			case (8):
				fluxArray[3] += amount;
				return;
			case (16):
				fluxArray[4] += amount;
				return;
			case (32):
				fluxArray[5] += amount;
				return;
		}
		int numSides = 0;
		for (int i = 0; i < 6; i++) {
			if ((sideMask & (1 << i)) != 0 && neighborTypes[i] != NeighborType.NONE && connectionTypes[i].allowTransfer) {
				numSides++;
			}
		}
		if (numSides == 0) {
			return;
		}
		float perSide = ((float) amount) / numSides;
		for (int i = 0; i < 6; i++) {
			if ((sideMask & (1 << i)) != 0 && neighborTypes[i] != NeighborType.NONE && connectionTypes[i].allowTransfer) {
				fluxArray[i] += perSide;
			}
		}
	}

	@Override
	protected int sendEnergy(IEnergyReceiver receiver, int maxReceive, byte side, boolean simulate) {

		int energy = super.sendEnergy(receiver, maxReceive, side, simulate);
		if (!simulate && grid != null) {
			((EnergyGridGlowing) grid).noteExtractingEnergy(this, side, energy);
		}
		return energy;
	}

	@Override
	public int extractEnergy(EnumFacing from, int maxExtract, boolean simulate) {

		int energy = super.extractEnergy(from, maxExtract, simulate);
		if (!simulate && grid != null) {
			((EnergyGridGlowing) grid).noteExtractingEnergy(this, (byte) from.ordinal(), energy);
		}
		return energy;
	}

	@Override
	public int receiveEnergy(EnumFacing from, int maxReceive, boolean simulate) {

		int energy = super.receiveEnergy(from, maxReceive, simulate);
		if (!simulate && grid != null) {
			((EnergyGridGlowing) grid).noteReceivingEnergy(this, (byte) from.ordinal(), energy);
		}
		return energy;
	}

	@Override
	public void randomDisplayTick() {

		super.randomDisplayTick();
		//		for (int i = 0; i < flux.length; i++) {
		//			float v = flux[i];
		//			if (v != 0) {
		//				int n = (int) (Math.ceil(Math.log1p(Math.abs(v))));
		//				for (int i1 = 0; i1 < n; i1++) {
		//					float d = worldObj.rand.nextFloat();
		//					EnumFacing side = EnumFacing.values()[i];
		//					float x = x() + 0.5F + side.getFrontOffsetX() * d;
		//					float y = y() + 0.5F + side.getFrontOffsetY() * d;
		//					float z = z() + 0.5F + side.getFrontOffsetZ() * d;
		//					worldObj.spawnParticle(EnumParticleTypes.REDSTONE, x, y, z, 0, 0, 0);
		//				}
		//			}
		//		}
	}
}
