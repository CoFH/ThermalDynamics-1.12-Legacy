package thermaldynamics.ducts.item;

import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import thermaldynamics.block.SubTileMultiBlock;
import thermaldynamics.ducts.energy.subgrid.SubTileEnergyEnder;

public class TileItemDuctEnder extends TileItemDuctPower {

    public boolean powered = false;

    SubTileEnergyEnder enderEnergy;

    public TileItemDuctEnder() {
        super();
        setSubEnergy(enderEnergy = new SubTileEnergyEnder(this));
    }

    @Override
    public void removeItem(TravelingItem travelingItem) {
        super.removeItem(travelingItem);
    }

    @Override
    public void insertItem(TravelingItem travelingItem) {
        super.insertItem(travelingItem);
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
        return _SIDE_MODS[isPowered() ? 3  : 2];
    }

    @Override
    public boolean isSubNode() {
        return true;
    }

    @Override
    public void tickItems() {
        int c = 0;
        if (itemsToAdd.size() > 0) {
            for (TravelingItem travelingItem : itemsToAdd) {
                c = c | (1 << travelingItem.direction) | (1 << travelingItem.oldDirection);
                myItems.add(travelingItem);
            }

            itemsToAdd.clear();
            hasChanged = true;
        }

        if (myItems.size() > 0) {
            for (TravelingItem travelingItem : myItems) {
                if (travelingItem.reRoute || travelingItem.myPath == null) {
                    travelingItem.bounceItem(this);
                } else if (energy.energyGrid != null && neighborTypes[travelingItem.direction] == NeighborTypes.MULTIBLOCK &&
                        energy.energyGrid.myStorage.getEnergyStored() >= PropsConduit.ENDER_TRANSMIT_COST &&
                        energy.energyGrid.myStorage.extractEnergy(PropsConduit.ENDER_TRANSMIT_COST, true) >= PropsConduit.ENDER_TRANSMIT_COST
                        ) {
                    energy.energyGrid.myStorage.extractEnergy(PropsConduit.ENDER_TRANSMIT_COST, false);
                    TileItemDuct duct = this;

                    while (true) {
                        duct.pulseLine(travelingItem.direction, (byte) (travelingItem.oldDirection ^ 1));
                        if (duct.neighborTypes[travelingItem.direction] == NeighborTypes.MULTIBLOCK) {
                            TileItemDuct newHome = (TileItemDuct) duct.getConnectedSide(travelingItem.direction);
                            if (newHome != null) {
                                if (newHome.neighborTypes[travelingItem.direction ^ 1] == NeighborTypes.MULTIBLOCK) {
                                    duct = newHome;
                                    if (travelingItem.myPath.hasNextDirection()) {
                                        travelingItem.oldDirection = travelingItem.direction;
                                        travelingItem.direction = travelingItem.myPath.getNextDirection();
                                    } else {
                                        travelingItem.reRoute = true;
                                        duct.insertItem(travelingItem);
                                        itemsToRemove.add(travelingItem);
                                        break;
                                    }

                                    if (duct.getClass() != TileItemDuctEnder.class) {
                                        duct.insertItem(travelingItem);
                                        itemsToRemove.add(travelingItem);
                                        break;
                                    }

                                } else {
                                    travelingItem.reRoute = true;
                                    duct.insertItem(travelingItem);
                                    itemsToRemove.add(travelingItem);
                                    break;
                                }
                            } else {
                                travelingItem.reRoute = true;
                                duct.insertItem(travelingItem);
                                itemsToRemove.add(travelingItem);
                                break;
                            }
                        } else {
                            duct.insertItem(travelingItem);
                            itemsToRemove.add(travelingItem);
                            break;
                        }
                    }
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


}
