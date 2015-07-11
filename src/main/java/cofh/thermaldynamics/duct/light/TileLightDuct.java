package cofh.thermaldynamics.duct.light;

import cofh.core.network.PacketCoFHBase;
import cofh.lib.util.helpers.ServerHelper;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.IIcon;
import net.minecraft.world.EnumSkyBlock;
import net.minecraftforge.common.util.ForgeDirection;

public class TileLightDuct extends TileTDBase {

	public LightGrid gridGlow = null;

	@Override
	public MultiBlockGrid getNewGrid() {

		return new LightGrid(worldObj);
	}

	@Override
	public void setGrid(MultiBlockGrid newGrid) {

		super.setGrid(newGrid);
		gridGlow = (LightGrid) newGrid;
	}

	@Override
	public boolean cachesExist() {

		return true;
	}

	@Override
	public void createCaches() {

	}

	@Override
	public void cacheImportant(TileEntity tile, int side) {

	}

	@Override
	public void clearCache(int side) {

	}

	boolean lit = false;

	@Override
	public int getLightValue() {

		return isLit() ? 15 : 0;
	}

	public boolean isLit() {

		return ServerHelper.isClientWorld(worldObj) || gridGlow == null ? lit : gridGlow.lit;
	}

	@Override
	public void blockPlaced() {

		super.blockPlaced();
		if (ServerHelper.isServerWorld(worldObj)) {
			lit = worldObj.isBlockIndirectlyGettingPowered(x(), y(), z());
		} else {
			for (ForgeDirection dir : ForgeDirection.VALID_DIRECTIONS) {
				TileEntity tile = worldObj.getTileEntity(xCoord + dir.offsetX, yCoord + dir.offsetY, zCoord + dir.offsetZ);
				if (tile instanceof TileLightDuct) {
					if (((TileLightDuct) tile).lit) {
						lit = true;
						break;
					}
				}
			}
		}
	}

	@Override
	public void onNeighborBlockChange() {

		super.onNeighborBlockChange();
		if (ServerHelper.isClientWorld(worldObj)) {
			return;
		}

		lit = false;
		ForgeDirection[] valid_directions = ForgeDirection.VALID_DIRECTIONS;
		for (int i = 0; !lit && i < valid_directions.length; i++) {
			if (attachments[i] != null && attachments[i].shouldRSConnect()) {
				continue;
			}

			ForgeDirection dir = valid_directions[i];
			lit = worldObj.getIndirectPowerOutput(x() + dir.offsetX, y() + dir.offsetY, z() + dir.offsetZ, i);
		}

		if (gridGlow != null && gridGlow.lit != lit) {
			gridGlow.upToDate = false;
		}
	}

	@Override
	public PacketCoFHBase getPacket() {

		PacketCoFHBase packet = super.getPacket();
		packet.addBool(lit || (gridGlow != null && gridGlow.lit));
		return packet;
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);
		boolean b = payload.getBool();

		if (b != lit) {
			lit = b;
			checkLight();
		}
	}

	@Override
	public IIcon getBaseIcon() {

		return super.getBaseIcon();
	}

	public void checkLight() {

		// worldObj.func_147451_t(xCoord, yCoord, zCoord);
		worldObj.updateLightByType(EnumSkyBlock.Block, xCoord, yCoord, zCoord);
	}

	@Override
	public boolean isConnectable(TileEntity theTile, int side) {

		return theTile instanceof TileLightDuct;
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {

		super.writeToNBT(nbt);
		nbt.setBoolean("isLit", lit);
	}

	@Override
	public void readFromNBT(NBTTagCompound nbt) {

		super.readFromNBT(nbt);
		lit = nbt.getBoolean("isLit");
	}
}
