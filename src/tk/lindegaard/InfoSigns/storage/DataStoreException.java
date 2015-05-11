package tk.lindegaard.InfoSigns.storage;

public class DataStoreException extends Exception
{
	private static final long	serialVersionUID	= 4351411485448921448L;

	public DataStoreException()
	{
		
	}
	
	public DataStoreException(String message)
	{
		super(message);
	}
	
	public DataStoreException(Throwable inner)
	{
		super(inner);
	}
}
