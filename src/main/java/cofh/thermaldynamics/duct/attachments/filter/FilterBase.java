package cofh.thermaldynamics.duct.attachments.filter;

import cofh.core.render.RenderUtils;
import cofh.repack.codechicken.lib.vec.Translation;
import cofh.thermaldynamics.ThermalDynamics;
import cofh.thermaldynamics.block.TileTDBase;
import cofh.thermaldynamics.duct.attachments.ConnectionBase;
import cofh.thermaldynamics.render.RenderDuct;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import net.minecraft.client.renderer.RenderBlocks;
import net.minecraft.item.ItemStack;

public abstract class FilterBase extends ConnectionBase {

	public FilterBase(TileTDBase tile, byte side) {

		super(tile, side);
	}

	public FilterBase(TileTDBase tile, byte side, int type) {

		super(tile, side, type);

	}

	@Override
	public String getName() {

		return "item.thermaldynamics.filter." + type + ".name";
	}

	@Override
	public TileTDBase.NeighborTypes getNeighborType() {

		return isValidInput ? TileTDBase.NeighborTypes.OUTPUT : TileTDBase.NeighborTypes.DUCT_ATTACHMENT;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean render(int pass, RenderBlocks renderBlocks) {

		if (pass == 1) {
			return false;
		}

		Translation trans = RenderUtils.getRenderVector(tile.xCoord + 0.5, tile.yCoord + 0.5, tile.zCoord + 0.5).translation();
		RenderDuct.modelConnection[stuffed ? 2 : 1][side].render(trans, RenderUtils.getIconTransformation(RenderDuct.filterTexture[type]));
		return true;
	}

	@Override
	public ItemStack getPickBlock() {

		return new ItemStack(ThermalDynamics.itemFilter, 1, type);
	}

}
