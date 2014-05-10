package thermalducts.multiblock;

public interface IMultiBlock {

	public void setInvalidForForming();

	public void setValidForForming();

	public boolean isValidForForming();

	public MultiBlockGrid getNewGrid();

	public void setGrid(MultiBlockGrid newGrid);

	public MultiBlockGrid getGrid();

	public IMultiBlock getConnectedSide(byte side);

	public boolean isSideConnected(byte side);

	// This side contains a grid that will not form, mark that side as not connected.
	public void setNotConnected(byte side);

}
