package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.block.TileTDBase;
import net.minecraft.world.World;

public interface IDuctFactory {

	TileTDBase createTileEntity(Duct duct, World worldObj);
}
