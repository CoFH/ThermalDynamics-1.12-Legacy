package thermaldynamics.multiblock;

import net.minecraft.world.World;

public interface IMultiBlock {



    public World getWorldObj();

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

	// Used by some multiblocks to start their formations. Removed from the ticking list after initial tick.
	public void tickMultiBlock();

	// Used to do multiblock steps passed off by the grid. IE: Distribute liquids.
	public void tickPass(int pass);

	public boolean isNode();

}
