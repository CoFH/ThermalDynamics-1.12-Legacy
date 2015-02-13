package cofh.thermaldynamics.ducts.item;

import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.thermaldynamics.core.TDProps;
import cofh.thermaldynamics.ducts.energy.subgrid.SubTileEnergyEnder;

public class TileItemDuctEnder extends TileItemDuctPower {

	public boolean powered = false;

	final SubTileEnergyEnder enderEnergy;

	public TileItemDuctEnder() {

		super();
		setSubEnergy(enderEnergy = new SubTileEnergyEnder(this));
	}

	@Override
	public void removeItem(TravelingItem travelingItem) {

		super.removeItem(travelingItem);
	}

	@Override
	public void transferItem(TravelingItem travelingItem) {

		super.transferItem(travelingItem);
	}

	@Override
	public int getPipeLength() {

		return isPowered() ? 1 : 60;
	}

	@Override
	public int getPipeHalfLength() {

		return isPowered() ? 1 : 30;
	}

	@Override
	public float[][] getSideCoordsModifier() {

		return _SIDE_MODS[isPowered() ? 3 : 2];
	}

	@Override
	public boolean acceptingItems() {

		return enderEnergy.internalGrid != null && enderEnergy.internalGrid.isPowered();
	}

	@Override
	public boolean isSubNode() {

		return true;
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
				if (travelingItem.reRoute || travelingItem.myPath == null) {
					travelingItem.bounceItem(this);
				} else if (energy.energyGrid != null && energy.energyGrid.myStorage.getEnergyStored() >= TDProps.ENDER_TRANSMIT_COST
						&& energy.energyGrid.myStorage.extractEnergy(TDProps.ENDER_TRANSMIT_COST, true) >= TDProps.ENDER_TRANSMIT_COST) {
					energy.energyGrid.myStorage.extractEnergy(TDProps.ENDER_TRANSMIT_COST, false);
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
		updateRender();
	}

	@Override
	public void insertNewItem(TravelingItem travelingItem) {

		if (energy.energyGrid != null && energy.energyGrid.myStorage.getEnergyStored() >= TDProps.ENDER_TRANSMIT_COST
				&& energy.energyGrid.myStorage.extractEnergy(TDProps.ENDER_TRANSMIT_COST, true) >= TDProps.ENDER_TRANSMIT_COST) {
			energy.energyGrid.myStorage.extractEnergy(TDProps.ENDER_TRANSMIT_COST, false);
			multiAdvance(travelingItem, true);
		} else
			super.insertNewItem(travelingItem);
	}

	public void multiAdvance(TravelingItem travelingItem, boolean newInsert) {

		TileItemDuct duct = this;

		while (true) {
			duct.pulseLine(travelingItem.direction, (byte) (travelingItem.oldDirection ^ 1));
			if (duct.neighborTypes[travelingItem.direction] == NeighborTypes.MULTIBLOCK) {
				TileItemDuct newHome = (TileItemDuct) duct.getConnectedSide(travelingItem.direction);
				if (newHome != null && newHome.neighborTypes[travelingItem.direction ^ 1] == NeighborTypes.MULTIBLOCK) {
					duct = newHome;
					if (travelingItem.myPath.hasNextDirection()) {
						travelingItem.oldDirection = travelingItem.direction;
						travelingItem.direction = travelingItem.myPath.getNextDirection();
					} else {
						travelingItem.reRoute = true;
						transferItem(travelingItem, duct, newInsert);
						return;
					}
					if (duct.getClass() != TileItemDuctEnder.class) {
						transferItem(travelingItem, duct, newInsert);
						return;
					}
				} else {
					travelingItem.reRoute = true;
					transferItem(travelingItem, duct, newInsert);
					return;
				}
			} else if (duct.neighborTypes[travelingItem.direction] == NeighborTypes.OUTPUT) {
				travelingItem.stack.stackSize = duct.insertIntoInventory(travelingItem.stack, travelingItem.direction);

				if (travelingItem.stack.stackSize > 0) {
					travelingItem.reRoute = true;
					transferItem(travelingItem, duct, newInsert);
				} else
					itemsToRemove.add(travelingItem);
				return;
			} else {
				travelingItem.reRoute = true;
				transferItem(travelingItem, duct, newInsert);
				return;
			}
		}
	}

	public void transferItem(TravelingItem travelingItem, TileItemDuct duct, boolean newInsert) {

		if (newInsert) {
			internalGrid.shouldRepoll = true;
			duct.transferItem(travelingItem);
		} else if (duct != this) {
			duct.transferItem(travelingItem);
			itemsToRemove.add(travelingItem);
		}
	}

	public void sendPowerPacket() {

		PacketTileInfo myPayload = PacketTileInfo.newPacket(this);
		myPayload.addByte(0);
		myPayload.addByte(TileInfoPackets.ENDER_POWER);
		myPayload.addBool(powered);
		PacketHandler.sendToAllAround(myPayload, this);
	}

	@Override
	public void handlePacketType(PacketCoFHBase payload, int b) {

		if (b == TileInfoPackets.ENDER_POWER) {
			powered = payload.getBool();
			centerLine = 0;
			for (int i = 0; i < centerLineSub.length; i++)
				centerLineSub[i] = 0;
		} else
			super.handlePacketType(payload, b);
	}

	@Override
	public boolean shouldRenderInPass(int pass) {

		return pass == 0 && (powered || super.shouldRenderInPass(pass));
	}

	@Override
	public PacketCoFHBase getPacket() {

		PacketCoFHBase packet = super.getPacket();

		packet.addBool(isPowered());

		return packet;
	}

	public boolean isPowered() {

		return enderEnergy.internalGrid != null ? enderEnergy.internalGrid.isPowered() : powered;
	}

	@Override
	public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {

		super.handleTilePacket(payload, isServer);

		powered = payload.getBool();
	}

    public void updateRender() {
        if (enderEnergy.internalGrid != null) {
            if (enderEnergy.internalGrid.isPowered() != powered) {
                powered = enderEnergy.internalGrid.isPowered();
                sendPowerPacket();
            }
        }
        if (!powered && hasChanged) {
            hasChanged = false;
            sendTravelingItemsPacket();
        }
    }
}
