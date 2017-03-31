package cofh.thermaldynamics.duct.nutypeducts;

import cofh.thermaldynamics.duct.Duct;
import cofh.thermaldynamics.duct.DuctUnitStructural;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.util.TickHandler;
import com.google.common.collect.ImmutableSortedMap;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;
import java.util.Map;

public abstract class TileGridStructureBase extends TileGrid {


	private Map<DuctToken, DuctUnit> ducts = null;

	public TileGridStructureBase() {

	}

	public <T extends TileGridStructureBase> T setDuctUnits(Map<DuctToken, DuctUnit> ducts) {
		this.ducts = ducts;
		return (T) this;
	}

	public <T extends TileGridStructureBase> T setDuctUnits(DuctToken token, DuctUnit unit) {
		this.ducts = ImmutableSortedMap.of(token, unit);
		return (T) this;
	}




	@SuppressWarnings("unchecked")
	@Nullable
	@Override
	@OverridingMethodsMustInvokeSuper
	public <T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> T getDuct(DuctToken<T, G, C> token) {
		DuctUnit ductUnit = ducts.get(token);
		if (ductUnit == null && token == DuctToken.STRUCTURAL) {
			ImmutableSortedMap.Builder<DuctToken, DuctUnit> builder = ImmutableSortedMap.naturalOrder();
			builder.putAll(ducts);
			DuctUnitStructural structural;
			builder.put(DuctToken.STRUCTURAL, structural = new DuctUnitStructural(this, ducts.get(getPrimaryDuctToken())));
			TickHandler.addMultiBlockToCalculate(structural);
			ducts = builder.build();
			return (T) structural;
		}
		return (T) ductUnit;
	}

	protected abstract DuctToken getPrimaryDuctToken();

	public DuctUnit getPrimaryDuctUnit() {
		return getDuct(getPrimaryDuctToken());
	}

	@Override
	public Iterable<DuctUnit> getDuctUnits() {
		return ducts.values();
	}

	@Nullable
	public DuctUnitStructural getStructureUnitIfPresent() {
		return (DuctUnitStructural) ducts.get(DuctToken.STRUCTURAL);
	}


	@Override
	public boolean isPowered() {
		DuctUnitStructural ductUnit = (DuctUnitStructural) ducts.get(DuctToken.STRUCTURAL);
		if (ductUnit != null && ductUnit.grid != null && ductUnit.grid.rs != null) {
			if (ductUnit.grid.rs.redstoneLevel > 0) {
				return true;
			}
		}

		return super.isPowered();
	}

	@Override
	public Duct getDuctType() {
		return getPrimaryDuctUnit().getDuctType();
	}

	@Override
	public TextureAtlasSprite getBaseIcon() {
		return getPrimaryDuctUnit().getBaseIcon();
	}
}
