package tk.lindegaard.InfoSigns.storage.async;

import java.util.HashSet;
import java.util.Set;

import tk.lindegaard.InfoSigns.storage.DataStore;
import tk.lindegaard.InfoSigns.storage.DataStoreException;


public class StoreTask implements DataStoreTask<Void>
{
	//private HashSet<StatStore> mWaitingStats = new HashSet<StatStore>();
	//private HashSet<AchievementStore> mWaitingAchievements = new HashSet<AchievementStore>();
	
	public StoreTask(Set<Object> waiting)
	{
		synchronized(waiting)
		{
			//mWaitingStats.clear();
			//mWaitingAchievements.clear();
			
			//for(Object obj : waiting)
			//{
			//	if(obj instanceof StatStore)
			//		mWaitingStats.add((StatStore)obj);
			//	if(obj instanceof AchievementStore)
			//		mWaitingAchievements.add((AchievementStore)obj);
			//}
			
			waiting.clear();
		}
	}
	@Override
	public Void run( DataStore store ) throws DataStoreException
	{
		//if(!mWaitingStats.isEmpty())
		//	store.saveStats(mWaitingStats);

		//if(!mWaitingAchievements.isEmpty())
		//	store.saveAchievements(mWaitingAchievements);

		return null;
	}

	@Override
	public boolean readOnly()
	{
		return false;
	}
	
}
