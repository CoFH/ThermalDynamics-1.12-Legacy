package cofh.thermaldynamics.multiblock;

import net.minecraft.world.World;

public interface ISingleTick {

	boolean existsYet();

	void singleTick();

	World world();

	boolean isOutdated();
}
