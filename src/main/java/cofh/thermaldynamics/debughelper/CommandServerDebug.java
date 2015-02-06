package cofh.thermaldynamics.debughelper;

import cofh.core.network.PacketHandler;

import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;

public class CommandServerDebug extends CommandBase {

	@Override
	public String getCommandName() {

		return "td_showdebugstats";
	}

	@Override
	public String getCommandUsage(ICommandSender p_71518_1_) {

		return "td_showdebugstats";
	}

	@Override
	public boolean canCommandSenderUseCommand(ICommandSender p_71519_1_) {

		return true;
	}

	@Override
	public void processCommand(ICommandSender p_71515_1_, String[] p_71515_2_) {

		PacketHandler.sendToServer(new PacketDebug());
	}

}
