package cofh.thermaldynamics.duct.tiles;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.nutypeducts.DuctToken;
import cofh.thermaldynamics.duct.nutypeducts.DuctUnit;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import cofh.thermaldynamics.duct.nutypeducts.TileGridStructureBase;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

public class TileItem extends TileGridStructureBase {
	@Override
	protected DuctToken getPrimaryDuctToken() {
		return DuctToken.ITEMS;
	}


}
