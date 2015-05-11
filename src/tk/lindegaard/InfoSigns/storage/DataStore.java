package tk.lindegaard.InfoSigns.storage;

import java.util.List;
import java.util.Set;

import org.bukkit.OfflinePlayer;

public interface DataStore
{
	public void initialize() throws DataStoreException;
	
	public void shutdown() throws DataStoreException;
	
	//public void saveStats(Set<StatStore> stats) throws DataStoreException;
	//public void saveAchievements(Set<AchievementStore> achievements) throws DataStoreException;
	
	//public Set<AchievementStore> loadAchievements(OfflinePlayer player) throws DataStoreException;

	//public List<StatStore> loadStats( StatType type, TimePeriod period, int count ) throws DataStoreException;
	
	public OfflinePlayer getPlayerByName(String name) throws DataStoreException;

	void savePlayer(Set<PlayerStore> stats) throws DataStoreException;

	List<PlayerStore> loadPlayer(TimePeriod period, int count)
			throws DataStoreException;
}
