package com.alluvial.mds;
 
import java.io.IOException;
import java.net.BindException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;

import com.alluvial.mds.common.MDSHelper;
import com.alluvial.mlink.contract.IMarketConnector;
import com.alluvial.mlink.iress.IressConnector;

public class MDSContext extends Thread {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 148 $");
	
	private AbsState state = null;
	
	// market connection
	private IMarketConnector connector;
	private MarketObserver observer;

	public static void main(String[] args) {
		try {
			long version = MDSHelper.getVersion();
			
			System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: Market Data Service (r" + version + ") is running");
			
			MDSContext mdsc = new MDSContext(args);
			mdsc.runIt();
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	MDSContext(String[] args) throws NumberFormatException, IOException {
		connector = new IressConnector(Configuration.getHost(), Configuration.getQuotePort(),
				Configuration.getDepthPort(), Configuration.getTradePort(), Configuration.getUsername(),
				Configuration.getPassword(), Configuration.getExchange(), Configuration.getDataSource(),
				Configuration.getDataSourceBoard(), Configuration.getEmulationMode(), 
				Configuration.getDumpData(), Configuration.getDumpDirectory());
		
		observer = new MarketObserver(connector);
		connector.setListener(observer);
	}
	
	public void runIt() {
		Runtime.getRuntime().addShutdownHook(this);
		
		// initial state is connection state
		changeState(new ConnectionState(this));
		
		while (state!=null)
		{
			try {
				state.process();
			} 
			catch (Exception e)	{
				System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: MDSContext::run() - error occurred");
				e.printStackTrace();
			}
		}                    

        System.out.println("[INFO][T" + Thread.currentThread().getId() + "]: market Data Service has finished");
	}
	
	public void shutDown()
	{
		if (state!=null)
			state.stop();
	}

	public String status()
	{
		return state!=null?state.getStatus():"final state";
	}
	
	public void changeState(AbsState state) {
		this.state = state;
		System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: MDS state: " + status());
	}
	
	public IMarketConnector getMarketConnector() {
		return connector;
	}
	
	public MarketObserver getObserver() {
		return observer;
	}
	
	private boolean serverRunning = false;
	Selector serverSelector = null;
	/**
	 * This methods raises the thread that accepts incoming connections and performs requests of MDS clients (algos).
	 */
	public void runServer() {
		Thread subscribtionServerThread = new Thread( new Runnable() {
			@Override
			public void run() {
				serverRunning = true;

				// open accepting socket and process incoming connections
				try {
					ServerSocketChannel server = null;
					serverSelector = Selector.open(); 
					server = ServerSocketChannel.open(); 
					server.configureBlocking(false); 
					server.socket().bind(new InetSocketAddress(Configuration.getMDSPort()));
					server.register(serverSelector, SelectionKey.OP_ACCEPT); 

					while (serverRunning) {
						serverSelector.select();

						for (Iterator<SelectionKey> i = serverSelector.selectedKeys().iterator(); i.hasNext();)
						{
							SelectionKey key = (SelectionKey)i.next();
							i.remove();

							if (!key.isValid())
								continue;

							if (key.isAcceptable()) {
								SocketChannel client = server.accept();
								client.configureBlocking(false);
								
								SelectionKey clientkey = client.register(serverSelector, SelectionKey.OP_READ);
								client.socket().setTcpNoDelay(true);
								
								// send MDS info and attach subscriber to the key
								try {
									MDSSubscriber subscriber = new MDSSubscriber(clientkey, getObserver());
									subscriber.sendMDSInfo();
									clientkey.attach(subscriber);
								}
								catch(IOException ex) {
									ex.printStackTrace();
								}
							}
							else if (key.isReadable()) { 
								MDSSubscriber subscriber = (MDSSubscriber)key.attachment();
								subscriber.processRequest();
							}
						}
					}
				}
				catch (BindException e) {
					System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: error occurred while binding socket for incoming connections " + e);
					shutDown();

				}
				catch (ClosedSelectorException e) {
					System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: selector was closed " + e);
					shutDown();
				}
				catch (IOException e) {
					System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: I/O error occurred in receiving state " + e);
					shutDown();
				}
				catch (Exception e) {
					System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: error occurred in receiving state");
					e.printStackTrace();
					shutDown();
				}
			}
		});
		
		subscribtionServerThread.setPriority(Thread.NORM_PRIORITY);
		subscribtionServerThread.start();
	
		System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: MDS opened port for incoming connections");
	}
	
	public void stopServer() {
		serverRunning = false;

		if (serverSelector!=null)
			serverSelector.wakeup();
	}
	
	// === Thread
	/**
	 * This method is used for shutdown by kill signal
	 */
	public void run() {
		System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: shutting down IressMDS...");
		try {
			shutDown();
		} catch (Exception e) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: error occured while shutting down IressMDS");
			e.printStackTrace();
		}
    }
}
