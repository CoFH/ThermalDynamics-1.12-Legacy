package cofh.thermaldynamics.render;

import cofh.thermaldynamics.duct.fluid.DuctUnitFluid;
import cofh.thermaldynamics.duct.tiles.DuctToken;
import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.client.renderer.tileentity.TileEntitySpecialRenderer;

public class RenderDuctOmni extends RenderDuctItems {

	public static final TileEntitySpecialRenderer<TileGrid> INSTANCE = new RenderDuctOmni();

	@Override
	public void render(TileGrid tile, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {

		super.render(tile, x, y, z, partialTicks, destroyStage, alpha);

		DuctUnitFluid ductUnitFluid = tile.getDuct(DuctToken.FLUID);

		if (ductUnitFluid != null) {
			RenderDuctFluids.INSTANCE.renderFluids(ductUnitFluid, x, y, z);
		}
	}

}
