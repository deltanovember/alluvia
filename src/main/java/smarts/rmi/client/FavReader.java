package smarts.rmi.client;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.Date;

import smarts.rmi.contract.*;

public class FavReader extends AbsFavReaderQueueProcessor 
					   implements IFavReaderRequest 
{
	private static final long serialVersionUID = 1L;
	private static final transient String interfaceURI = "FAVREADER-SERVER";
	
	IFavReaderRequest favReaderOnServer;
	
	private static long cnt = 0;
	
	public FavReader() throws MalformedURLException, RemoteException, NotBoundException {
		super();

		Remote RemoteObject =  Naming.lookup("rmi://o.ac3.cmcrc.com:1098/SMARTS-REMOTE-FACTORY");

		if (RemoteObject==null)
			throw new IllegalArgumentException("remote object by name " + interfaceURI + " is not accessible");

		ISmartsRemoteFactory smartsRemoteFactory = (ISmartsRemoteFactory)RemoteObject;
		favReaderOnServer = smartsRemoteFactory.createFavReaderRequest("asx_mq", this);
		
		if (favReaderOnServer==null)
			throw new IllegalArgumentException("the error occured on the server - it couldn't return interface to FavReader");
	}

	// == IFavReaderRequest
	@Override
	public void run(Date start, Date finish) throws RemoteException {
		//System.out.println("[DEBUG_CLIENT] -> run()");
		favReaderOnServer.run(start, finish);
		//System.out.println("[DEBUG_CLIENT]    run() ->");
	}
	
	@Override
	public void test_run(Date start, Date finish) throws RemoteException {
		favReaderOnServer.test_run(start, finish);
	}
	
	@Override
	public void abort() throws RemoteException {
		favReaderOnServer.abort();
	}
	
	// == AbsFavReaderQueueProcessor
	@Override
	public void onDayEnd(DayEndArgs args) {
		// TODO Auto-generated method stub
		//cnt++;
		System.out.println("[DEBUG_CLIENT] - onDayEnd " + (new Date()).toString() + " ");
	}

	@Override
	public void onDayStart(DayStartArgs args) {
		// TODO Auto-generated method stub
		//cnt++;
		System.out.println("[DEBUG_CLIENT] - onDayStart " + (new Date()).toString() + " " );
	}

	@Override
	public void onEnd(EndArgs args) {
		// TODO Auto-generated method stub
		//cnt++;
		//System.out.println("[DEBUG_CLIENT] - onEnd " + cnt);
	}

	@Override
	public void onStart(StartArgs args) {
		// TODO Auto-generated method stub
		//cnt++;		
		System.out.println("[DEBUG_CLIENT] - onStart " + cnt);
	}
	
	@Override
	public void onTrade(TradeArgs args) {
		//cnt++;
		//System.out.println("[DEBUG_CLIENT] - onTrade: " + (new Date()).toString() + " " + cnt /*+ " " + args*/);
	}
	
	@Override
	public void onControl(ControlArgs args) {
		//cnt++;		
		//System.out.println("[DEBUG_CLIENT] - onControl " + cnt);
	}

	@Override
	public void onQuote(QuoteArgs args) {
		//cnt++;
		//System.out.println("[DEBUG_CLIENT] - onQuote " + args);

    	if (args.security.equals("BHP.AX"))
    	{
    		//System.out.println(bidPrice);
	    	//System.out.println("[DEBUG_JAVA]: onQuote transId: " + args.transId + (new Date()).toString() );
    	}
	}
}
