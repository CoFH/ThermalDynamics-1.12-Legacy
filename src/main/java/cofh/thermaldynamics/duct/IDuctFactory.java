package cofh.thermaldynamics.duct;

import net.minecraft.world.World;

public interface IDuctFactory {

	TileDuctBase createTileEntity(Duct duct, World worldObj);
}
