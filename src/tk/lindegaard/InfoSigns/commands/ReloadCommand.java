package tk.lindegaard.InfoSigns.commands;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import tk.lindegaard.InfoSigns.InfoSigns;
import tk.lindegaard.InfoSigns.Messages;

public class ReloadCommand implements ICommand {
	private InfoSigns instance;

	public ReloadCommand(InfoSigns instance) {
		this.instance = instance;
	}

	@Override
	public String getName() {
		return "reload";
	}

	@Override
	public String[] getAliases() {
		return null;
	}

	@Override
	public String getPermission() {
		return InfoSigns.pluginName + ".reload";
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { label };
	}

	@Override
	public String getDescription() {
		return Messages.getString(InfoSigns.pluginName
				+ ".commands.reload.description");
	}

	@Override
	public boolean canBeConsole() {
		return true;
	}

	@Override
	public boolean canBeCommandBlock() {
		return false;
	}

	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		// new
		if (label.equals(""))
			if (InfoSigns.config().load() && instance.reloadSignsConfig()) {
				sender.sendMessage(ChatColor.GREEN
						+ Messages.getString(InfoSigns.pluginName
								+ ".commands.reload.reload-complete"));
			} else
				sender.sendMessage(ChatColor.RED
						+ Messages.getString(InfoSigns.pluginName
								+ ".commands.reload.reload-error"));
		else if (label.equalsIgnoreCase("signs"))
			if (instance.reloadSignsConfig())
				sender.sendMessage(ChatColor.GREEN
						+ Messages.getString(InfoSigns.pluginName
								+ ".commands.reload.signs-complete"));
			else
				sender.sendMessage(ChatColor.RED
						+ Messages.getString(InfoSigns.pluginName
								+ ".commands.reload.signs-error"));
		else
			sender.sendMessage(ChatColor.RED
					+ Messages.getString(InfoSigns.pluginName
							+ ".commands.reload.syntax-error"));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label,
			String[] args) {
		return null;
	}

}
