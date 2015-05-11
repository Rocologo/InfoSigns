package tk.lindegaard.InfoSigns.commands;

import java.util.HashSet;
import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import tk.lindegaard.InfoSigns.InfoSigns;
import tk.lindegaard.InfoSigns.Messages;

public class SelectCommand implements ICommand {

	public final static String cmd = "select";

	@Override
	public String getName() {
		return cmd;
	}

	@Override
	public String[] getAliases() {
		return new String[] { "sel" };
	}

	@Override
	public String getPermission() {
		return InfoSigns.pluginName + "." + cmd;
	}

	@Override
	public String[] getUsageString(String label, CommandSender sender) {
		return new String[] { label + ChatColor.GOLD + " (1|2)" };
	}

	@Override
	public String getDescription() {
		return Messages.getString(InfoSigns.pluginName + ".commands." + cmd
				+ ".description");
	}

	@Override
	public boolean canBeConsole() {
		return false;
	}

	@Override
	public boolean canBeCommandBlock() {
		return false;
	}

	@SuppressWarnings("deprecation")
	@Override
	public boolean onCommand(CommandSender sender, String label, String[] args) {
		if (args.length != 1)
			return false;

		Player player = (Player) sender;

		boolean pointA = false;
		if (args[0].equalsIgnoreCase("1"))
			pointA = true;
		else if (!args[0].equalsIgnoreCase("2")) {
			sender.sendMessage(ChatColor.RED
					+ Messages.getString(InfoSigns.pluginName + ".commands."
							+ cmd + ".unknown-point", "point", args[0]));
			return true;
		}

		HashSet<Byte> transparent = new HashSet<Byte>();
		transparent.add((byte) 0);

		Block target = player.getTargetBlock(transparent, 10);
		if (target == null) {
			sender.sendMessage(ChatColor.RED
					+ Messages.getString(InfoSigns.pluginName + ".commands."
							+ cmd + ".too-far"));
			return true;
		}

		if (pointA)
			SelectCommandHelper.setPointA(player, target.getLocation());
		else
			SelectCommandHelper.setPointB(player, target.getLocation());

		sender.sendMessage(ChatColor.GREEN
				+ Messages.getString(
						InfoSigns.pluginName + ".commands." + cmd + ".done",
						"point",
						args[0],
						"coords",
						String.format("%d, %d, %d", target.getX(),
								target.getY(), target.getZ())));
		return true;
	}

	@Override
	public List<String> onTabComplete(CommandSender sender, String label,
			String[] args) {
		return null;
	}

}
