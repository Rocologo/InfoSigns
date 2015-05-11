package tk.lindegaard.InfoSigns.storage.async;

import tk.lindegaard.InfoSigns.storage.DataStore;
import tk.lindegaard.InfoSigns.storage.DataStoreException;

public interface DataStoreTask<T>
{
	public T run(DataStore store) throws DataStoreException;
	
	public boolean readOnly();
}
