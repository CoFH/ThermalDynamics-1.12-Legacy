package cofh.thermaldynamics.util;

import cofh.thermaldynamics.init.TDItems;
import com.google.gson.JsonObject;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommand;
import net.minecraft.command.ICommandSender;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.TextComponentString;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nullable;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.util.Collections;
import java.util.List;

@SideOnly (Side.CLIENT)
public class CoverBlacklistCommand implements ICommand {

	@Override
	public String getName() {

		return "td_blacklist_cover";
	}

	@Override
	public String getUsage(ICommandSender sender) {

		return "Gets the json object to blacklist the cover in hand.";
	}

	@Override//TODO localize
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {

		boolean wild = false;
		if (args.length >= 1) {
			if (args[0].equalsIgnoreCase("true")) {
				wild = true;
			} else {
				sender.sendMessage(new TextComponentString("Usage: /td_blacklist_cover [true]"));
				sender.sendMessage(new TextComponentString("True specifies to ignore metadata."));
				return;
			}
		}
		ItemStack stack = Minecraft.getMinecraft().player.getHeldItemMainhand();
		if (stack.isEmpty() || stack.getItem() != TDItems.itemCover) {
			sender.sendMessage(new TextComponentString("You need to be holding a cover in your main hand to use this command."));
			return;
		}
		NBTTagCompound nbt = stack.getTagCompound();

		if (nbt == null || !nbt.hasKey("Meta", 1) || !nbt.hasKey("Block", 8)) {
			sender.sendMessage(new TextComponentString("Invalid cover."));
			return;
		}
		int meta = nbt.getByte("Meta");
		Block block = Block.getBlockFromName(nbt.getString("Block"));
		if (block == Blocks.AIR) {
			sender.sendMessage(new TextComponentString("Invalid cover."));
			return;
		}
		IBlockState state = block.getStateFromMeta(meta);
		JsonObject object = new JsonObject();
		object.addProperty("block", block.getRegistryName().toString());
		if (wild) {
			object.addProperty("meta", false);
		} else {
			object.addProperty("meta", state.getBlock().getMetaFromState(state));
		}
		String json = object.toString();
		sender.sendMessage(new TextComponentString("Config entry json: " + json));
		StringSelection sel = new StringSelection(json);
		Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, sel);
		sender.sendMessage(new TextComponentString("Copied json to clipboard."));

	}

	//@formatter:off
	@Override public List<String> getAliases() { return Collections.emptyList(); }
	@Override public boolean checkPermission(MinecraftServer server, ICommandSender sender) { return true; }
	@Override public List<String> getTabCompletions(MinecraftServer server, ICommandSender sender, String[] args, @Nullable BlockPos targetPos) { return Collections.emptyList(); }
	@Override public boolean isUsernameIndex(String[] args, int index) { return false; }
	@Override public int compareTo(ICommand o) { return getName().compareTo(o.getName()); }
	//@formatter:on
}
