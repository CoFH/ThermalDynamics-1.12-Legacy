package cofh.thermaldynamics.multiblock;

import java.util.List;

public interface IGridHolder {

	List<IGridTile> getConnectedSides();

	void setInvalidForForming();

	void setValidForForming();

	boolean isValidForForming();
}
