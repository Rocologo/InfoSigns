package tk.lindegaard.InfoSigns.commands;

import java.util.AbstractMap;
import java.util.Map.Entry;
import java.util.WeakHashMap;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import tk.lindegaard.InfoSigns.InfoSigns;
import tk.lindegaard.InfoSigns.Messages;
import tk.lindegaard.InfoSigns.compatability.WorldEditCompat;

public class SelectCommandHelper {
	private static WeakHashMap<Player, Entry<Location, Location>> mPoints = new WeakHashMap<Player, Entry<Location, Location>>();
	private final static String cmd = SelectCommand.cmd;

	public static Location getPointA(Player player)
			throws IllegalArgumentException {
		if (needsCommands()) {
			Entry<Location, Location> existing = mPoints.get(player);
			if (existing == null || existing.getKey() == null)
				throw new IllegalArgumentException(
						Messages.getString(InfoSigns.pluginName + ".commands."
								+ cmd + ".point1-unset"));

			if (!existing.getKey().getWorld().equals(player.getWorld()))
				throw new IllegalArgumentException(
						Messages.getString(InfoSigns.pluginName + ".commands."
								+ cmd + ".point1-unset"));

			return existing.getKey();
		} else
			return WorldEditCompat.getPointA(player);
	}

	public static Location getPointB(Player player)
			throws IllegalArgumentException {
		if (needsCommands()) {
			Entry<Location, Location> existing = mPoints.get(player);
			if (existing == null || existing.getValue() == null)
				throw new IllegalArgumentException(
						Messages.getString(InfoSigns.pluginName + ".commands."
								+ cmd + ".point2-unset"));

			if (!existing.getValue().getWorld().equals(player.getWorld()))
				throw new IllegalArgumentException(
						Messages.getString(InfoSigns.pluginName
								+ ".commands.select.point2-unset")); //$NON-NLS-1$

			return existing.getValue();
		} else
			return WorldEditCompat.getPointB(player);
	}

	public static boolean needsCommands() {
		return !Bukkit.getPluginManager().isPluginEnabled("WorldEdit");
	}

	public static void setPointA(Player player, Location location) {
		Entry<Location, Location> existing = mPoints.get(player);
		if (existing == null)
			mPoints.put(player,
					new AbstractMap.SimpleEntry<Location, Location>(location,
							null));
		else {
			if (existing.getValue() != null
					&& !existing.getValue().getWorld()
							.equals(location.getWorld()))
				mPoints.put(player,
						new AbstractMap.SimpleEntry<Location, Location>(
								location, null));
			else
				mPoints.put(player,
						new AbstractMap.SimpleEntry<Location, Location>(
								location, existing.getValue()));
		}
	}

	public static void setPointB(Player player, Location location) {
		Entry<Location, Location> existing = mPoints.get(player);
		if (existing == null)
			mPoints.put(player,
					new AbstractMap.SimpleEntry<Location, Location>(null,
							location));
		else {
			if (existing.getKey() != null
					&& !existing.getKey().getWorld()
							.equals(location.getWorld()))
				mPoints.put(player,
						new AbstractMap.SimpleEntry<Location, Location>(null,
								location));
			else
				mPoints.put(player,
						new AbstractMap.SimpleEntry<Location, Location>(
								existing.getKey(), location));
		}
	}
}
