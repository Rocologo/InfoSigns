package tk.lindegaard.InfoSigns.storage;

public interface DataCallback<T>
{
	public void onCompleted(T data);
	
	public void onError(Throwable error);
}
