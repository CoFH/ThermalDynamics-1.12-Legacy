package cofh.thermaldynamics.render;

import cofh.thermaldynamics.duct.fluid.DuctUnitFluid;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class RenderDuctOmni extends RenderDuctItems {
	public static final TileEntitySpecialRenderer<TileGrid> instance = new RenderDuctOmni();

	@Override
	public void renderTileEntityAt(TileGrid tile, double x, double y, double z, float frame, int destroyStage) {
		super.renderTileEntityAt(tile, x, y, z, frame, destroyStage);
		DuctUnitFluid ductUnitFluid = tile.getDuct(DuctToken.FLUID);
		if(ductUnitFluid != null){
			RenderDuctFluids.instance.renderFluids(ductUnitFluid, x, y, z);
		}
	}
}
