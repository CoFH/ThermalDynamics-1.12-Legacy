package cofh.thermaldynamics.gui;

import cofh.core.block.TileCore;
import cofh.thermaldynamics.duct.Attachment;
import cofh.thermaldynamics.duct.nutypeducts.IDuctHolder;
import cofh.thermaldynamics.duct.nutypeducts.TileGrid;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.network.IGuiHandler;

public class GuiHandler implements IGuiHandler {

	public static final int TILE_ID = 0;
	public static final int TILE_CONFIG = 1;
	public static final int TILE_ATTACHMENT_ID = 10;

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

		BlockPos pos = new BlockPos(x, y, z);
		TileEntity tile = world.getTileEntity(pos);

		switch (id) {
			case TILE_ID:
				tile = world.getTileEntity(pos);
				if (tile instanceof TileCore) {
					return ((TileCore) tile).getGuiClient(player.inventory);
				}
				return null;
			case TILE_CONFIG:
				tile = world.getTileEntity(pos);
				if (tile instanceof TileGrid) {
					return ((TileGrid) tile).getConfigGuiClient(player.inventory);
				}
				return null;
			default:
				if (id >= TILE_ATTACHMENT_ID && id <= TILE_ATTACHMENT_ID + 5) {
					if (tile instanceof TileGrid) {
						Attachment attachment = ((TileGrid) tile).getAttachment(id - TILE_ATTACHMENT_ID);
						if (attachment != null) {
							return attachment.getGuiClient(player.inventory);
						}
					}
				}
				return null;
		}
	}

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

		BlockPos pos = new BlockPos(x, y, z);
		TileEntity tile = world.getTileEntity(pos);

		switch (id) {
			case TILE_ID:
				tile = world.getTileEntity(pos);
				if (tile instanceof TileCore) {
					return ((TileCore) tile).getGuiServer(player.inventory);
				}
				return null;
			case TILE_CONFIG:
				tile = world.getTileEntity(pos);
				if (tile instanceof TileGrid) {
					return ((TileGrid) tile).getConfigGuiServer(player.inventory);
				}
				return null;
			default:
				if (id >= TILE_ATTACHMENT_ID && id <= TILE_ATTACHMENT_ID + 5) {
					if (tile instanceof TileGrid) {
						Attachment attachment = ((TileGrid) tile).getAttachment(id - TILE_ATTACHMENT_ID);
						if (attachment != null) {
							return attachment.getGuiServer(player.inventory);
						}
					}
				}
				return null;
		}
	}

}
