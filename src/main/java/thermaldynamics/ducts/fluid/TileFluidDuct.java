package thermaldynamics.ducts.fluid;

import cofh.core.network.PacketCoFHBase;
import cofh.core.network.PacketHandler;
import cofh.core.network.PacketTileInfo;
import cofh.lib.util.helpers.FluidHelper;
import cofh.lib.util.helpers.ServerHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidTankInfo;
import net.minecraftforge.fluids.IFluidHandler;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.core.TDProps;
import thermaldynamics.multiblock.IMultiBlock;
import thermaldynamics.multiblock.MultiBlockGrid;

public class TileFluidDuct extends TileMultiBlock implements IFluidHandler {
    public static void initialize() {
        //guiId = ThermalDynamics.proxy.registerGui("FluidFilter", "conduit", "TEBase", null, true);
    }


    IFluidHandler[] cache = new IFluidHandler[6];

    public TileFluidDuct() {

    }


    @Override
    public MultiBlockGrid getNewGrid() {
        return new FluidGrid(worldObj, getDuctType().type);
    }


    protected static int guiId;

    /* Filtering Variables */
    public boolean isWhitelist;
    public boolean useNBT;
    public FluidStack[] filterStacks;

    IFluidHandler[] importantCache = new IFluidHandler[6];

    public FluidStack mySavedFluid;
    public FluidStack myRenderFluid = new FluidStack(0, 0);
    public FluidStack fluidForGrid;
    public FluidStack myConnectionFluid;

    @Override
    public boolean isSignificantTile(TileEntity theTile, int side) {
        return FluidHelper.isFluidHandler(theTile) && !(theTile instanceof IMultiBlock);
    }

    @Override
    public boolean tickPass(int pass) {
        if (!super.tickPass(pass)) return false;

        if (fluidGrid == null) {
            return true;
        }


        if (pass == 0) {
            int available = fluidGrid.toDistribute;
            int sent = 0;

            for (int i = this.internalSideCounter; i < this.neighborTypes.length && sent < available; i++) {

                sent += transfer(i, available - sent);

                if (sent >= available) {
                    this.tickInternalSideCounter(i + 1);
                    break;
                }

            }

            for (int i = 0; i < this.internalSideCounter && sent < available; i++) {
                sent += transfer(i, available - sent);

                if (sent >= available) {
                    this.tickInternalSideCounter(i + 1);
                    break;
                }
            }
        }
        return true;
    }


    protected int transfer(int bSide, int available) {

        if (neighborTypes[bSide] == NeighborTypes.OUTPUT && connectionTypes[bSide] != ConnectionTypes.BLOCKED) {

            if (cache[bSide] != null && fluidGrid.myTank.getFluid() != null) {
                FluidStack tempFluid = fluidGrid.myTank.getFluid().copy();
                tempFluid.amount = available;
                int amountSent = cache[bSide].fill(ForgeDirection.VALID_DIRECTIONS[bSide ^ 1], tempFluid, false);

                if (amountSent > 0) {
                    return cache[bSide].fill(ForgeDirection.VALID_DIRECTIONS[bSide ^ 1], fluidGrid.myTank.drain(amountSent, true), true);
                }
            }
        }
        return 0;
    }

    @Override
    public int getLightValue() {
        if (getDuctType().opaque) {
            return 0;
        }
        if (ServerHelper.isClientWorld(world())) {
            return FluidHelper.getFluidLuminosity(myRenderFluid);
        }
        if (fluidGrid != null) {
            return FluidHelper.getFluidLuminosity(fluidGrid.getFluid());
        }

        return super.getLightValue();
    }

    public void updateFluid() {
        if (!getDuctType().opaque) {
            sendRenderPacket();
        }
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return !getDuctType().opaque && myRenderFluid != null && super.shouldRenderInPass(pass);
    }

    public boolean isConnectable(TileEntity theTile, int side) {
        return theTile instanceof TileFluidDuct && ((TileFluidDuct) theTile).getDuctType().type == getDuctType().type
                && FluidHelper.isFluidEqualOrNull(((TileFluidDuct) theTile).getConnectionFluid(), this.getConnectionFluid());
    }

    public FluidStack getConnectionFluid() {
        return fluidGrid == null ? myConnectionFluid : fluidGrid.getFluid();
    }

    public FluidGrid fluidGrid;

    @Override
    public void setGrid(MultiBlockGrid newGrid) {
        super.setGrid(newGrid);
        fluidGrid = (FluidGrid) newGrid;
    }

    @Override
    public void handleInfoPacket(PacketCoFHBase payload, boolean isServer, EntityPlayer thePlayer) {
        if (ServerHelper.isClientWorld(worldObj)) {
            byte b = payload.getByte();
            handleTileInfoPacketType(payload, b);
        }
    }

    @Override
    public void cacheImportant(TileEntity tile, int side) {
        cache[side] = (IFluidHandler) tile;
    }

    @Override
    public void clearCache(int side) {
        cache[side] = null;
    }

    public void handleTileInfoPacketType(PacketCoFHBase payload, byte b) {
        if (b == TileFluidPackets.UPDATE_RENDER) {
            myRenderFluid = payload.getFluidStack();
            worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
            worldObj.func_147451_t(xCoord, yCoord, zCoord);
        }
    }


    @Override
    public void tileUnloading() {
        if (mySavedFluid != null && fluidGrid != null) {
            fluidGrid.myTank.drain(mySavedFluid.amount, true);
        }
    }

    public int getRenderFluidLevel() {
        return myRenderFluid == null ? 0 : myRenderFluid.amount;
    }


    @Override
    public PacketCoFHBase getPacket() {
        PacketCoFHBase packet = super.getPacket();
        if (fluidGrid != null) {
            packet.addFluidStack(fluidGrid.getRenderFluid());
        } else {
            packet.addFluidStack(new FluidStack(0, 0));
        }
        return packet;
    }

    @Override
    public void handleTilePacket(PacketCoFHBase payload, boolean isServer) {
        super.handleTilePacket(payload, isServer);
        myRenderFluid = payload.getFluidStack();

        if (worldObj != null) {
            worldObj.func_147451_t(xCoord, yCoord, zCoord);
        }
    }

    public void sendRenderPacket() {
        if (fluidGrid == null)
            return;
        if (!getDuctType().opaque) {
            PacketTileInfo myPayload = PacketTileInfo.newPacket(this);
            myPayload.addByte(0);
            myPayload.addByte(TileFluidPackets.UPDATE_RENDER);
            myPayload.addFluidStack(fluidGrid.getRenderFluid());
            PacketHandler.sendToAllAround(myPayload, this);
        }
    }

    public class TileFluidPackets {
        public static final byte GUI_BUTTON = 0;
        public static final byte SET_FILTER = 1;
        public static final byte FILTERS = 2;
        public static final byte UPDATE_RENDER = 3;
        public static final byte TEMPERATURE = 4;
    }

    @Override
    public int fill(ForgeDirection from, FluidStack resource, boolean doFill) {
        if (fluidGrid != null && neighborTypes[from.ordinal()] == NeighborTypes.OUTPUT && connectionTypes[from.ordinal()] != ConnectionTypes.BLOCKED) {
            return fluidGrid.myTank.fill(resource, doFill);
        }
        return 0;
    }

    @Override
    public FluidStack drain(ForgeDirection from, FluidStack resource, boolean doDrain) {
        if (fluidGrid != null && neighborTypes[from.ordinal()] == NeighborTypes.OUTPUT && connectionTypes[from.ordinal()] != ConnectionTypes.BLOCKED) {
            return fluidGrid.myTank.drain(resource, doDrain);
        }
        return null;
    }

    @Override
    public FluidStack drain(ForgeDirection from, int maxDrain, boolean doDrain) {
        if (fluidGrid != null && neighborTypes[from.ordinal()] == NeighborTypes.OUTPUT && connectionTypes[from.ordinal()] != ConnectionTypes.BLOCKED) {
            return fluidGrid.myTank.drain(maxDrain, doDrain);
        }
        return null;
    }

    @Override
    public boolean canFill(ForgeDirection from, Fluid fluid) {
        return neighborTypes[from.ordinal()] == NeighborTypes.OUTPUT && connectionTypes[from.ordinal()] != ConnectionTypes.BLOCKED;
    }

    @Override
    public boolean canDrain(ForgeDirection from, Fluid fluid) {
        return neighborTypes[from.ordinal()] == NeighborTypes.OUTPUT && connectionTypes[from.ordinal()] != ConnectionTypes.BLOCKED;
    }

    @Override
    public FluidTankInfo[] getTankInfo(ForgeDirection from) {
        return fluidGrid != null ? new FluidTankInfo[]{fluidGrid.myTank.getInfo()} : TDProps.EMPTY_TANK_INFO;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);

        if (fluidGrid != null && fluidGrid.hasValidFluid()) {

            mySavedFluid = fluidGrid.getNodeShare(this);
            mySavedFluid.writeToNBT(nbt);

            nbt.setTag("ConnFluid", new NBTTagCompound());
            myConnectionFluid = fluidGrid.getFluid().copy();
            myConnectionFluid.writeToNBT(nbt.getCompoundTag("ConnFluid"));
        } else {
            mySavedFluid = null;
            myConnectionFluid = null;
        }
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        fluidForGrid = FluidStack.loadFluidStackFromNBT(nbt);
        if (nbt.hasKey("ConnFluid")) {
            myConnectionFluid = FluidStack.loadFluidStackFromNBT(nbt.getCompoundTag("ConnFluid"));
        }

        if (fluidForGrid != null && fluidForGrid.fluidID == 0) {
            fluidForGrid = null;
        }
    }
}
