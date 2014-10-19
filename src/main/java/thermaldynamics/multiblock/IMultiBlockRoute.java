package thermaldynamics.multiblock;

public interface IMultiBlockRoute extends IMultiBlock {
    public abstract int getWeight();

    public abstract boolean canStuffItem();

    public boolean wasVisited();

    public void setVisited(boolean wasVisited);

    public boolean isOutput();

    public int getMaxRange();
}
