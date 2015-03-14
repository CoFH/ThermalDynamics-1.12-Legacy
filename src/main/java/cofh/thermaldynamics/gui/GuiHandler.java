package cofh.thermaldynamics.gui;

import cofh.core.block.TileCoFHBase;
import cofh.thermaldynamics.block.Attachment;
import cofh.thermaldynamics.block.TileTDBase;
import cpw.mods.fml.common.network.IGuiHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class GuiHandler implements IGuiHandler {

	public static final int TILE_ID = 0;
	public static final int TILE_ATTACHMENT_ID = 1;
	public static final int TILE_ATTACHMENT_ID_END = 6;

	@Override
	public Object getServerGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

		TileEntity tile = world.getTileEntity(x, y, z);
		if (id >= TILE_ATTACHMENT_ID && id <= TILE_ATTACHMENT_ID_END) {
			if (tile instanceof TileTDBase) {
				Attachment attachment = ((TileTDBase) tile).attachments[id - TILE_ATTACHMENT_ID];
				if (attachment != null)
					return attachment.getGuiServer(player.inventory);
			}
		}
		switch (id) {
		case TILE_ID:
			tile = world.getTileEntity(x, y, z);
			if (tile instanceof TileCoFHBase) {
				return ((TileCoFHBase) tile).getGuiServer(player.inventory);
			}
		default:
			return null;
		}
	}

	@Override
	public Object getClientGuiElement(int id, EntityPlayer player, World world, int x, int y, int z) {

		TileEntity tile = world.getTileEntity(x, y, z);
		if (id >= TILE_ATTACHMENT_ID && id <= TILE_ATTACHMENT_ID_END) {
			if (tile instanceof TileTDBase) {
				Attachment attachment = ((TileTDBase) tile).attachments[id - TILE_ATTACHMENT_ID];
				if (attachment != null)
					return attachment.getGuiClient(player.inventory);
			}
		}
		switch (id) {
		case TILE_ID:
			tile = world.getTileEntity(x, y, z);
			if (tile instanceof TileCoFHBase) {
				return ((TileCoFHBase) tile).getGuiClient(player.inventory);
			}
		default:
			return null;
		}
	}

}
