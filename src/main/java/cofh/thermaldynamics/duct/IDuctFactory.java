package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import net.minecraft.world.World;

public interface IDuctFactory {

	TileGrid createTileEntity(Duct duct, World worldObj);

}
