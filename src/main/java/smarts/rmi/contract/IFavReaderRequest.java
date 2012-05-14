package smarts.rmi.contract;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;

public interface IFavReaderRequest extends Remote {

	/**
	 * This methods is called by client. 
	 * The server just saves the client FavReader instance.
	 * 
	 * This methods sets client FavReader instance on the server 
	 * in order the server called on-methods on the client.
	 * 
	 * @param fr - client FavReader instance.
	 * @throws RemoteException
	 */
	public void run(Date start, Date finish) throws RemoteException;
	public void abort() throws RemoteException;
	
	/**
	 * This methods doesn't read data from SMARTS. It raises emulating thread. 
	 */
	public void test_run(Date start, Date finish) throws RemoteException;
}
