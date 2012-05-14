package smarts.rmi.contract;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface IEventQueueProcessor extends Remote {
	void process(Object[] queue) throws RemoteException;
}
