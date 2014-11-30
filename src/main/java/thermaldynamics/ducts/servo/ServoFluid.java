package thermaldynamics.ducts.servo;

import cofh.lib.util.helpers.BlockHelper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.ICrafting;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.IFluidHandler;
import thermaldynamics.ThermalDynamics;
import thermaldynamics.block.AttachmentRegistry;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.fluid.TileFluidDuct;
import thermaldynamics.gui.GuiHandler;
import thermaldynamics.gui.containers.ContainerServo;
import thermaldynamics.gui.gui.GuiServo;


public
class ServoFluid extends ServoBase {
    TileFluidDuct fluidDuct;

    int[] maxthroughput = {50, 100, 200, 400, 10000};
    float[] throttle = {0.2F, 0.5F, 1F, 1F, 10F};

    @Override
    public int getID() {
        return AttachmentRegistry.SERVO_FLUID;
    }

    public ServoFluid(TileMultiBlock tile, byte side) {
        super(tile, side);
        fluidDuct = (TileFluidDuct) tile;
    }

    public ServoFluid(TileMultiBlock tile, byte side, int type) {
        super(tile, side, type);
        fluidDuct = (TileFluidDuct) tile;
    }

    IFluidHandler theTile;

    @Override
    public void clearCache() {
        theTile = null;
    }

    @Override
    public void cacheTile(TileEntity tile) {
        theTile = (IFluidHandler) tile;
    }

    @Override
    public boolean isValidTile(TileEntity tile) {
        return tile instanceof IFluidHandler;
    }

    @Override
    public boolean canAddToTile(TileMultiBlock tileMultiBlock) {
        return tileMultiBlock instanceof TileFluidDuct;
    }

    @Override
    public void tick(int pass) {
        super.tick(pass);
        if (pass != 1 || fluidDuct.fluidGrid == null || !isPowered || !isValidInput) {
            return;
        }

        int maxInput = Math.min(Math.min(fluidDuct.fluidGrid.myTank.getSpace(), (int) Math.ceil(fluidDuct.fluidGrid.myTank.fluidThroughput * throttle[type])), maxthroughput[type]);
        if (maxInput == 0)
            return;

        FluidStack returned = theTile.drain(ForgeDirection.VALID_DIRECTIONS[side ^ 1], maxInput, false);

        if (fluidPassesFiltering(returned)) {
            if (fluidDuct.fluidGrid.myTank.getFluid() == null || fluidDuct.fluidGrid.myTank.getFluid().fluidID == 0) {
                fluidDuct.fluidGrid.myTank.setFluid(theTile.drain(ForgeDirection.VALID_DIRECTIONS[side ^ 1], maxInput, true));
            } else if (fluidDuct.fluidGrid.myTank.getFluid().isFluidEqual(returned)) {
                fluidDuct.fluidGrid.myTank.getFluid().amount += theTile.drain(ForgeDirection.VALID_DIRECTIONS[side ^ 1], maxInput, true).amount;
            }
        }
    }

    private boolean fluidPassesFiltering(FluidStack theFluid) {
        return theFluid != null && theFluid.fluidID != 0;
    }


    @Override
    public void sendGuiNetworkData(Container container, ICrafting player) {
        super.sendGuiNetworkData(container, player);
    }

    @Override
    public void receiveGuiNetworkData(int i, int j) {
        super.receiveGuiNetworkData(i, j);
    }

    @Override
    public boolean openGui(EntityPlayer player) {
        player.openGui(ThermalDynamics.instance, GuiHandler.TILE_ATTACHMENT_ID + side, tile.getWorldObj(), tile.xCoord, tile.yCoord, tile.zCoord);
        return true;
    }

    @Override
    public Object getGuiServer(InventoryPlayer inventory) {
        return new ContainerServo(inventory, this);
    }

    @Override
    public Object getGuiClient(InventoryPlayer inventory) {
        return new GuiServo(inventory, this);
    }
}
