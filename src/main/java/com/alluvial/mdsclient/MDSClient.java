package com.alluvial.mdsclient;

import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Timer;
import java.util.TimerTask;

import com.alluvial.mds.common.MDSHelper;
import com.alluvial.mds.contract.ConsolidatedOrder;
import com.alluvial.mds.contract.ContractHelper;
import com.alluvial.mds.contract.DictionaryRequest;
import com.alluvial.mds.contract.DictionaryResponse;
import com.alluvial.mds.contract.MDSInfo;
import com.alluvial.mds.contract.OffMktTrade;
import com.alluvial.mds.contract.Quote;
import com.alluvial.mds.contract.QuoteFull;
import com.alluvial.mds.contract.QuoteMatch;
import com.alluvial.mds.contract.ReplayConfirmation;
import com.alluvial.mds.contract.ReplayDateEnd;
import com.alluvial.mds.contract.ReplayDateStart;
import com.alluvial.mds.contract.ReplayReject;
import com.alluvial.mds.contract.ReplayRequest;
import com.alluvial.mds.contract.SingleOrder;
import com.alluvial.mds.contract.Subscription;
import com.alluvial.mds.contract.SubscriptionConfirmation;
import com.alluvial.mds.contract.Trade;

/**
 * This class represents generic MDS client.
 * @author erepekto
 */
public class MDSClient {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 158 $");

	// constants defining data source type
	private final static String[] modeDescriptions = new String[] {"NONE", "LIVE", "REPLAY"};
	private final static byte NONE   			= 0x0;
	private final static byte LIVE_SOURCE	  	= 0x1;
	private final static byte REPLAY_SOURCE 	= 0x2;
	
	// communication related fields
	private byte connectedDataSource = NONE;
	private String host; 
	private int port;
	private SocketChannel sChannel;
	private Selector selector; 
	private ByteBuffer inBuffer = ByteBuffer.allocate(64*1024*1024);

	// receiving thread fields
	Thread receivingThread = null;
	private boolean isReceivingThreadActive = false;


	/**
	 * Connects to the requested data source if not yet connected.
	 * @param requestedDataSource
	 * @throws Exception
	 */
	private void connect(byte requestedDataSource) throws Exception
	{
		if (connectedDataSource==requestedDataSource)
			return;
		
		if (connectedDataSource!=NONE)
			throw new IllegalArgumentException("can't connect to "+ modeDescriptions[requestedDataSource] +" since not disconnected from " + modeDescriptions[connectedDataSource] + " data source");
		
		if (sChannel!=null || selector!=null)
			throw new IllegalArgumentException("MDSClient is in between connection state");
		
		if (requestedDataSource==LIVE_SOURCE) {
			this.host = Configuration.getMdsLiveHost();
			this.port = Configuration.getMdsLivePort();
		}
		else if(requestedDataSource==REPLAY_SOURCE){
			this.host = Configuration.getMdsReplayHost();
			this.port = Configuration.getMdsReplayPort();
		}
		else
			throw new IllegalArgumentException("passed data source is undefined");			

        System.out.println("Connecting to: " + host + ":" + port);
		//Create socket connection
		selector = Selector.open();
		sChannel = SocketChannel.open();
		sChannel.configureBlocking(false);
		sChannel.register(selector, SelectionKey.OP_READ);
		sChannel.connect(new InetSocketAddress(host, port));
		
		while(!sChannel.finishConnect()) {};

		connectedDataSource = requestedDataSource;
		
		// when connection finished it needs to check the contract version the MDS uses
		MDSInfo mi = receiveMDSInfo();
		
		if (mi.contractVersion!=ContractHelper.getVersion()) {
			disconnect();
			throw new Exception("MDS contract version (" + mi.contractVersion + ") differs from client version (" + ContractHelper.getVersion() + ")");
		}
	}
	
	/**
	 * Disconnects from the currently connected data source.
	 * @throws Exception 
	 */
	public void disconnect()
	{
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				if (isReceivingThreadActive) {
					isReceivingThreadActive = false;
					selector.wakeup();
					waitWhileReceiving();
				}
				
				try {
					if (sChannel!=null)
						sChannel.close();
					
					if (selector!=null)
						selector.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
				sChannel = null;
				selector = null;
				connectedDataSource = NONE;				
			}
		} ).start();
	}
	
	public void requestDictionary() throws Exception {
		// connect if not connected
		connect(LIVE_SOURCE);
		
		DictionaryRequest dr = new DictionaryRequest();
		
		sendMessage(dr);	
		
		raiseContinuouslyReceivingThread();
	}

	/**
	 * Subscribes the client for a given securities set and raises receiving thread.
	 * Receiving thread can be stopped only using disconnect() method.
	 * @param securities
	 * @throws Exception
	 */
	public void subscribeForLive(String[] securities) throws Exception
	{
		// connect if not connected
		connect(LIVE_SOURCE);
		
		Subscription subscription = new Subscription();
		subscription.securities = securities;
		
		sendMessage(subscription);	
		
		raiseContinuouslyReceivingThread();
	}
	
	/**
	 * Subscribes the client for a given date and raises receiving thread.
	 * Receiving thread can be stopped only using disconnect() method.
	 * @param securities
	 * @throws Exception
	 */
	public void subscribeForReplay(String date) throws Exception
	{
		// connect if not connected
		connect(REPLAY_SOURCE);
		
		ReplayRequest rr = new ReplayRequest();
		rr.date = date;
		
		sendMessage(rr);	
		
		raiseContinuouslyReceivingThread();
	}
	
	public void sendMessage(Object message) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(message);
		oos.close();
		
		byte[] bytesToSend = bos.toByteArray();
		ByteBuffer bb = ByteBuffer.wrap(bytesToSend);
		sChannel.write(bb);

		if (bb.remaining()!=0)
			throw new RuntimeException("couldn't send all data");
	}
	
	private void raiseContinuouslyReceivingThread() {
		if (receivingThread!=null)
			return;
		
		isReceivingThreadActive = true;
		receivingThread = new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					receiving();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				isReceivingThreadActive = false;
			}
		});
		
		receivingThread.setPriority(Thread.MAX_PRIORITY);
		receivingThread.start();
	}
	
	private MDSInfo receiveMDSInfo() throws Exception {
		inBuffer.clear();
		
		while (true)
		{
		    try {
		        // Wait for an event
		        selector.select();
		    } catch (IOException e) {
		        // Handle error with selector
		    	throw new Exception("IO error occurred while waiting data from MDS", e);
		    }

		    // Get list of selection keys with pending events
		    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
		    SelectionKey selKey = (SelectionKey)it.next();

	        // Remove it from the list to indicate that it is being processed
	        it.remove();

	    	if (sChannel.read(inBuffer)==-1)
	    		throw new Exception("disconnected from the MDS while receiving information message");

	    	inBuffer.flip();

	    	int posBeforeRead = inBuffer.position();
	    	byte[] readBytes = new byte[inBuffer.limit()];
	    	inBuffer.get(readBytes);
	    	inBuffer.position(posBeforeRead);
	    	ByteArrayInputStream bis = new ByteArrayInputStream(readBytes);
	    	
	    	while (bis.available()!=0)
	    	{
		    	try
		    	{
			    	ObjectInputStream ois = new ObjectInputStream(bis);
					Object obj = ois.readObject();
		    		inBuffer.position(inBuffer.limit() - bis.available());

					Class<?> cls = obj.getClass();

					m_cnt++;

					if (cls==MDSInfo.class) {
						System.out.println((MDSInfo)obj);
						return (MDSInfo)obj;
					}
					else 
						throw new Exception("MDSInfo message has not been received");
		    	}
		    	catch (StreamCorruptedException ex)
		    	{
		    		throw new Exception("MDSInfo message has not been received, because stream is corrupted", ex);
		    	}
		    	catch (EOFException ex)
		    	{
		    		throw new Exception("MDSInfo message has not been received, because of EOFException", ex);
		    	}
	    	}
	    	
    		inBuffer.compact();
		}
	}
	
	long m_cnt=0;

	private void receiving() throws IOException
	{
		inBuffer.clear();

		// for debug
		Timer timer = startMPSTimer();
		boolean isMDSDisconnected = false;
		
		while (isReceivingThreadActive)
		{
		    try {
		        // Wait for an event
		        selector.select();
		    } catch (IOException e) {
		        // Handle error with selector
		    	System.out.println("IO error occurred while waiting data from MDS" + e);
		        break;
		    }
		    
		    // Get list of selection keys with pending events
		    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
		    if (!it.hasNext())
		    	continue;
		    
	    	it.next();
		    
	    	// Remove it from the list to indicate that it is being processed
	    	it.remove();

	    	if (sChannel.read(inBuffer)==-1) {
	    		isMDSDisconnected = true;
	    		break;
	    	}

	    	inBuffer.flip();
	    	
	    	int posBeforeRead = inBuffer.position();
	    	byte[] readBytes = new byte[inBuffer.limit()];
	    	inBuffer.get(readBytes);
	    	inBuffer.position(posBeforeRead);
	    	ByteArrayInputStream bis = new ByteArrayInputStream(readBytes);
	    	
	    	while (bis.available()!=0)
	    	{
		    	try
		    	{
			    	ObjectInputStream ois = new ObjectInputStream(bis);
					Object obj = ois.readObject();
		    		inBuffer.position(inBuffer.limit() - bis.available());

					Class<?> cls = obj.getClass();

					m_cnt++;

					if (cls==SubscriptionConfirmation.class) {
						onSubscriptionConfirmation((SubscriptionConfirmation)obj);
					}
					onEvent(obj);
		    	}
		    	catch (StreamCorruptedException ex)
		    	{
		    		//System.out.println(ex);
		    		break;
		    	}
		    	catch (EOFException ex)
		    	{
		    		//System.out.println(ex);
		    		break;
		    	}
		    	catch (Exception ex)
		    	{
		    		System.out.println(ex);
		    		return;
		    	}
	    	}
	    	
    		inBuffer.compact();
		}
		
		timer.cancel();
		
		if (isMDSDisconnected) {
			disconnect();
			onMDSDisconnected();
		}
	}

	private void onMDSDisconnected() {
		System.out.println("MDS disconnected");
	}

	private void onQuoteFull(QuoteFull obj) {
		//System.out.println("onReplayReject: " + obj);
	}

	private Timer startMPSTimer() {
		Timer timer = new Timer();
		
		timer.schedule( new TimerTask() {
							            public void run()
							            {
							            	System.out.println("mps: " + m_cnt/5);
							            	m_cnt = 0;
							            }
						    		},
						    		5000, 5000);
		return timer;
	}

	public void waitWhileReceiving() {
		while(receivingThread!=null && receivingThread.isAlive()) {
			try {
				if (receivingThread!=null)
					receivingThread.join();
				else
					return;
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	protected void onReplayConfirmation(ReplayConfirmation obj) {
//		System.out.println("onReplayConfirmation: " + obj);
	}
	
	protected void onReplayReject(ReplayReject obj) {
//		System.out.println("onReplayReject: " + obj);
	}

	protected void onReplayDateStart(ReplayDateStart obj) {
//		System.out.println("onReplayDateStart: " + obj);
	}

	protected void onReplayDateEnd(ReplayDateEnd obj) {
//		System.out.println("onReplayDateEnd: " + obj);
	}

	protected void onSubscriptionConfirmation(SubscriptionConfirmation obj) {
		//System.out.println("onSubscriptionConfirmation: " + obj);
	}

	protected void onDictionary(DictionaryResponse dict) {
		//System.out.println("onDictionary: " + dict);
	}

    protected void onEvent(Object obj) {
        
    }
	protected void onQuoteMatch(QuoteMatch quoteMatch) {
		//System.out.println("onQuoteMatch: " + quoteMatch);
	}

	protected void onQuote(Quote quote) {
		//if (quote.Security.equals("VBA"))
			//System.out.println("onQuote: " + quote);
	}

	protected void onSingleOrder(SingleOrder obj) {
		//System.out.println("MDSClient.onSingleOrder: " + obj);
	}

	protected void onConsolidatedOrder(ConsolidatedOrder obj) {
		//System.out.println("MDSClient.onConsolidatedOrder: " + obj);
	}

	protected void onTrade(Trade obj)
	{
		//if (obj.Security.equals("VBA"))
			//System.out.println("onTrade: " + obj);
	}
	
	protected void onOffMktTrade(OffMktTrade obj) {
		//System.out.println("onOffMktTrade: " + obj);
	}
}
