package cofh.thermaldynamics.duct;

import cofh.thermaldynamics.duct.tiles.TileGrid;
import net.minecraft.world.World;

public interface IDuctFactory {

	TileGrid createTileEntity(Duct duct, World worldObj);

}
