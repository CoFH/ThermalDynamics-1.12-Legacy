package cofh.thermaldynamics.duct.attachments.filter;

import cofh.thermaldynamics.duct.AttachmentRegistry;
import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;

public class FilterItem extends FilterBase {

	public FilterItem(TileGrid tile, byte side, int type) {

		super(tile, side, type);
	}

	public FilterItem(TileGrid tile, byte side) {

		super(tile, side);
	}

	@Override
	public String getInfo() {

		return "tab.thermaldynamics.filterItem";
	}

	IItemHandler inventory;

	@Override
	public void clearCache() {

		inventory = null;
	}

	@Override
	public void cacheTile(TileEntity tile) {

		inventory = tile.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
	}

	@Override
	public boolean isValidTile(TileEntity tile) {

		return tile != null && tile.hasCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, EnumFacing.VALUES[side ^ 1]);
	}

	@Override
	public ResourceLocation getId() {

		return AttachmentRegistry.FILTER_ITEM;
	}

	@Override
	public FilterLogic createFilterLogic() {

		return new FilterLogic(type, Duct.Type.ITEM, this);
	}

}
