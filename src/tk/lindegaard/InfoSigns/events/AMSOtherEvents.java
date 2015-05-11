package tk.lindegaard.InfoSigns.events;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.block.BlockRedstoneEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.metadata.MetadataValue;

import tk.lindegaard.InfoSigns.InfoSigns;
import tk.lindegaard.InfoSigns.SignType;

public class AMSOtherEvents implements Listener {

	private InfoSigns plugin;
	
	public AMSOtherEvents(InfoSigns plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler (priority = EventPriority.LOWEST)
	public void onPlayerJoin(PlayerJoinEvent e) {
		plugin.sm.update(e.getPlayer().getWorld());
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onPlayerChangeWorld(PlayerChangedWorldEvent e) {
		plugin.sm.update(e.getFrom());
		plugin.sm.update(e.getPlayer().getWorld());
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onPlayerQuit(PlayerQuitEvent e) {
		plugin.sm.update(e.getPlayer().getWorld());
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onRedstoneUpdate(BlockRedstoneEvent e) {
		if (e.getBlock().hasMetadata("AMSRedStone")) {
			for (MetadataValue mdv : e.getBlock().getMetadata("AMSRedStone")) {
				if (mdv.getOwningPlugin().getDescription().getName().equalsIgnoreCase(plugin.getDescription().getName())) {
					e.setNewCurrent(15);
					if (mdv.asInt() >= 1) {
						e.getBlock().removeMetadata("AMSRedStone", plugin);
					} else {
						e.getBlock().setMetadata("AMSRedStone", new FixedMetadataValue(plugin, mdv.asInt()+1));
					}
				}
			}
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onPlayerMove(PlayerMoveEvent e) {
		if (e.getFrom().getBlockX() != e.getTo().getBlockX() || e.getFrom().getBlockY() != e.getTo().getBlockY() || e.getFrom().getBlockZ() != e.getTo().getBlockZ()) {
			plugin.sm.update(SignType.REGION);
		}
	}
	
	@EventHandler (priority = EventPriority.NORMAL)
	public void onRedstoneableBlockPlaced(BlockPlaceEvent e) {
		if (plugin.sm.supportedmats.contains(e.getBlock().getType())) {
			plugin.sm.update(e.getBlock().getWorld());
		}
	}
}
