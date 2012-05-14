package smarts.rmi.contract;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface ISmartsRemoteFactory extends Remote {
	public abstract IFavReaderRequest createFavReaderRequest(String market, IEventQueueProcessor client) throws RemoteException;
}
