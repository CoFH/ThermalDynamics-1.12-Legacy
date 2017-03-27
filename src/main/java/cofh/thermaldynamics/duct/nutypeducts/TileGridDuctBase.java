package cofh.thermaldynamics.duct.nutypeducts;

import cofh.thermaldynamics.duct.DuctUnitStructural;
import cofh.thermaldynamics.multiblock.MultiBlockGrid;
import cofh.thermaldynamics.util.TickHandler;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;

import javax.annotation.Nullable;
import javax.annotation.OverridingMethodsMustInvokeSuper;

public abstract class TileGridDuctBase extends TileGrid {
	@Nullable
	DuctUnitStructural structural;

	Iterable<DuctUnit> ducts;

	@Nullable
	@Override
	@OverridingMethodsMustInvokeSuper
	public <T extends DuctUnit<T, G, C>, G extends MultiBlockGrid<T>, C> DuctUnit<T, G, C> getDuct(DuctToken<T, G, C> token) {
		if(token == DuctToken.STRUCTURAL){
			if(structural == null){
				structural = new DuctUnitStructural(this);
				TickHandler.addMultiBlockToCalculate(structural);
				ImmutableList.Builder<DuctUnit> list = ImmutableList.builder();
				list.addAll(ducts);
				list.add(structural);
				ducts = list.build();
			}
		}
		return null;
	}

	@Override
	public Iterable<DuctUnit> getDuctUnits() {
		return ducts;
	}

}
