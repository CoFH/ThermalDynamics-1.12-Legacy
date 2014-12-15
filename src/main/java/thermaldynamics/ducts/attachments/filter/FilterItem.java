package thermaldynamics.ducts.attachments.filter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.ISidedInventory;
import net.minecraft.tileentity.TileEntity;
import thermaldynamics.block.AttachmentRegistry;
import thermaldynamics.block.TileMultiBlock;
import thermaldynamics.ducts.Ducts;
import thermaldynamics.gui.containers.ContainerDuctConnection;
import thermaldynamics.gui.gui.GuiDuctConnection;

public class FilterItem extends FilterBase {
    public FilterItem(TileMultiBlock tile, byte side, int type) {
        super(tile, side, type);
    }

    public FilterItem(TileMultiBlock tile, byte side) {
        super(tile, side);
    }

    IInventory inventory;
    ISidedInventory sidedInventory;

    @Override
    public void clearCache() {
        inventory = null;
        sidedInventory = null;
    }

    @Override
    public void cacheTile(TileEntity tile) {
        inventory = (IInventory) tile;
        if (tile instanceof ISidedInventory)
            sidedInventory = (ISidedInventory) tile;
    }

    @Override
    public boolean isValidTile(TileEntity tile) {
        return tile instanceof IInventory;
    }

    @Override
    public int getID() {
        return AttachmentRegistry.FILTER_INV;
    }

    @Override
    public FilterLogic createFilterLogic() {
        return new FilterLogic(type, Ducts.Type.Item, this);
    }




}
