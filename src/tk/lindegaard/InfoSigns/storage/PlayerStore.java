package tk.lindegaard.InfoSigns.storage;

import org.bukkit.OfflinePlayer;

public class PlayerStore
{
	public PlayerStore(OfflinePlayer player)
	{
		this.player = player;
	}
	
	public PlayerStore(OfflinePlayer player, int amount)
	{
		this.player = player;
	}
	
	public OfflinePlayer player;
	
	public int amount;
	
	@Override
	public String toString()
	{
		return String.format("PlayerStore: {player: %s}", player.getName());  
	}
}
