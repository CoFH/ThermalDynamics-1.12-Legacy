package cofh.thermaldynamics.multiblock;

public interface IOccasionalTick {
	boolean occasionalTick(int pass);

	int numPasses();
}
