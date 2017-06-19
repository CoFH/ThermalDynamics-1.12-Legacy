package cofh.thermaldynamics.duct.item;

import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.energy.DuctUnitEnergy;
import cofh.thermaldynamics.duct.energy.GridEnergy;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import cofh.thermaldynamics.init.TDProps;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

public class DuctUnitItemEnder extends DuctUnitItem {

	final DuctUnitEnergy enderEnergy;
	public boolean powered = false;

	public DuctUnitItemEnder(TileGrid grid, Duct duct, DuctUnitEnergy enderEnergy) {

		super(grid, duct);
		this.enderEnergy = enderEnergy;
	}

	@Override
	public void transferItem(TravelingItem travelingItem) {

		super.transferItem(travelingItem);
	}

	@Override
	public int getDuctLength() {

		return isPowered() ? 1 : 60;
	}

	@Override
	public int getPipeHalfLength() {

		return isPowered() ? 1 : 30;
	}

	@Override
	public boolean acceptingItems() {

		GridEnergy grid = enderEnergy.getGrid();
		return grid != null && grid.isPowered();
	}

	@Override
	public void tickItems() {

		if (itemsToAdd.size() > 0) {
			for (TravelingItem travelingItem : itemsToAdd) {
				myItems.add(travelingItem);
			}
			itemsToAdd.clear();
			hasChanged = true;
		}
		if (myItems.size() > 0) {
			for (TravelingItem travelingItem : myItems) {
				if (grid.repoll) {
					grid.poll(travelingItem);
				}
				if (travelingItem.reRoute || travelingItem.myPath == null) {
					travelingItem.bounceItem(this);
				} else if (enderEnergy.getGrid() != null && enderEnergy.getGrid().myStorage.getEnergyStored() >= TDProps.ENDER_TRANSMIT_COST && enderEnergy.getGrid().myStorage.extractEnergy(TDProps.ENDER_TRANSMIT_COST, true) >= TDProps.ENDER_TRANSMIT_COST) {
					enderEnergy.getGrid().myStorage.extractEnergy(TDProps.ENDER_TRANSMIT_COST, false);
					multiAdvance(travelingItem, false);
				} else {
					travelingItem.tickForward(this);
				}
			}
			if (itemsToRemove.size() > 0) {
				myItems.removeAll(itemsToRemove);
				itemsToRemove.clear();
				hasChanged = true;
			}
		}

		if (hasChanged) {
			grid.shouldRepoll = true;
		}

		updateRender();
	}

	@Override
	public void insertNewItem(TravelingItem travelingItem) {

		if (enderEnergy.getGrid() != null && enderEnergy.getGrid().myStorage.getEnergyStored() >= TDProps.ENDER_TRANSMIT_COST && enderEnergy.getGrid().myStorage.extractEnergy(TDProps.ENDER_TRANSMIT_COST, true) >= TDProps.ENDER_TRANSMIT_COST) {
			enderEnergy.getGrid().myStorage.extractEnergy(TDProps.ENDER_TRANSMIT_COST, false);
			multiAdvance(travelingItem, true);
		} else {
			super.insertNewItem(travelingItem);
		}
	}

	public void multiAdvance(TravelingItem travelingItem, boolean newInsert) {

		DuctUnitItem duct = this;

		while (true) {
			duct.pulseLine(travelingItem.direction, (byte) (travelingItem.oldDirection ^ 1));
			DuctUnitItem newHome = duct.getConnectedSide(travelingItem.direction);
			if (newHome != null) {
				if (newHome.getConnectedSide(travelingItem.direction ^ 1) != null) {
					duct = newHome;
					if (travelingItem.myPath.hasNextDirection()) {
						travelingItem.oldDirection = travelingItem.direction;
						travelingItem.direction = travelingItem.myPath.getNextDirection();
					} else {
						travelingItem.reRoute = true;
						transferItem(travelingItem, duct, newInsert);
						return;
					}
					if (duct.getClass() != DuctUnitItemEnder.class) {
						transferItem(travelingItem, duct, newInsert);
						return;
					}
				} else {
					travelingItem.reRoute = true;
					transferItem(travelingItem, duct, newInsert);
					return;
				}
			} else if (duct.isOutput(travelingItem.direction)) {
				travelingItem.stack.stackSize = duct.insertIntoInventory(travelingItem.stack, travelingItem.direction);

				if (travelingItem.stack.stackSize > 0) {
					travelingItem.reRoute = true;
					transferItem(travelingItem, duct, newInsert);
				} else if (!newInsert) {
					itemsToRemove.add(travelingItem);
				}
				return;
			} else {
				travelingItem.reRoute = true;
				transferItem(travelingItem, duct, newInsert);
				return;
			}
		}
	}

	public void transferItem(TravelingItem travelingItem, DuctUnitItem duct, boolean newInsert) {

		if (newInsert) {
			grid.shouldRepoll = true;
			duct.transferItem(travelingItem);
		} else if (duct != this) {
			duct.transferItem(travelingItem);
			itemsToRemove.add(travelingItem);
		}
	}

	public void sendPowerPacket() {

		PacketTileInfo myPayload = newPacketTileInfo();
		myPayload.addByte(TileInfoPackets.ENDER_POWER);
		myPayload.addBool(powered);
		PacketHandler.sendToAllAround(myPayload, parent);
	}

	@Override
	public void handlePacketType(PacketCoFHBase payload, int b) {

		if (b == TileInfoPackets.ENDER_POWER) {
			powered = payload.getBool();
			centerLine = 0;
			for (int i = 0; i < centerLineSub.length; i++) {
				centerLineSub[i] = 0;
			}
		} else {
			super.handlePacketType(payload, b);
		}
	}

	@Override
	public boolean shouldRenderInPass(int pass) {

		return pass == 0 && (powered || super.shouldRenderInPass(pass));
	}

	@Override
	public void writeToTilePacket(PacketCoFHBase payload) {

		payload.addBool(isPowered());
	}

	public boolean isPowered() {

		return enderEnergy.getGrid() != null ? enderEnergy.getGrid().isPowered() : powered;
	}

	@Override
	@SideOnly (Side.CLIENT)
	public void handleTilePacket(PacketCoFHBase payload) {

		powered = payload.getBool();
	}

	public void updateRender() {

		if (enderEnergy.getGrid() != null) {
			if (enderEnergy.getGrid().isPowered() != powered) {
				powered = enderEnergy.getGrid().isPowered();
				sendPowerPacket();
			}
		}
		if (!powered && hasChanged) {
			hasChanged = false;
			sendTravelingItemsPacket();
		}
	}

}
