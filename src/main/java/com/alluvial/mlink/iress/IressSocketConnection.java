package com.alluvial.mlink.iress;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import com.alluvial.mds.common.MDSHelper;
import com.alluvial.mlink.contract.IMarketListener;
import com.alluvial.mlink.contract.MCException;
import com.alluvial.mlink.iress.structures.FeedDataPacketPayload;
import com.alluvial.mlink.iress.structures.LoginRejectedPacketPayload;
import com.alluvial.mlink.iress.structures.ServerInfo;
import com.alluvial.mlink.iress.structures.SubscriptionConfirmationMessageContent;
import com.alluvial.mlink.iress.structures.TradeRecoveryRequestMessageContent;
import com.alluvial.mlink.iress.util.JSMHexConverter;

/**
 * This class handles socket connection to IRESS. It is supposed to have three this class instances.
 * @author erepekto
 */
public class IressSocketConnection {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 152 $");
	
	IMarketListener listener = null;
	
	// buffer
	SocketChannel socket;
	ByteBuffer inputBufer = ByteBuffer.wrap(new byte[131072]);

	// context
	private String host; 
	private int port; 
	private String exchange; 
	private String dataSource; 
	private String dataSourceBoard;
	
	private String username;			// login user name
	private String password;			// password
	private short loginRequestFlag;		// log flag
	
	private Selector selector;
	
	// the booleans below are used to define current state of connection
	boolean loginPassed = false;
	boolean subscribed = false;

	private Timer timerHeartBeat;

	private String IRESSChannelName;

	private IressConnector iressConnector;
	
	public IressSocketConnection(IressConnector iressConnector,
								 String IRESSChannelName, String host, int port, String username, String password, 
								 String exchange, String dataSource, String dataSourceBoard,
								 boolean emulationMode, boolean dumpData, String dumpDirectory)
	
	{
		this.iressConnector = iressConnector;
		
		this.IRESSChannelName = IRESSChannelName;
		this.host = host;
		this.port = port;
		this.username = username;
		this.password = password;
		this.exchange = exchange;
		this.dataSource = dataSource;
		this.dataSourceBoard = dataSourceBoard;
//		this.dumpData = dumpData;
//		this.emulationMode = emulationMode;
//		this.dumpDirectory = dumpDirectory;

		// prepare buffer
		inputBufer.clear();
		inputBufer.order(ByteOrder.LITTLE_ENDIAN);
		
//		dataDumper = new DataDumper(IRESSChannelName, dumpDirectory);
	}

	/* (non-Javadoc)
	 * @see com.alluvial.mds.iress.IMarketConnector#getHost()
	 */
	public String getHost() {
		return host;
	}

	public int getPort() {
		return port;
	}

	/* (non-Javadoc)
	 * @see com.alluvial.mds.iress.IMarketConnector#getExchange()
	 */
	public String getExchange() {
		return exchange;
	}

	/* (non-Javadoc)
	 * @see com.alluvial.mds.iress.IMarketConnector#getDataSource()
	 */
	public String getDataSource() {
		return dataSource;
	}

	/* (non-Javadoc)
	 * @see com.alluvial.mds.iress.IMarketConnector#getDataSourceBoard()
	 */
	public String getDataSourceBoard() {
		return dataSourceBoard;
	}

	public SocketChannel getSocket() {
		return socket;
	}
	
	/* (non-Javadoc)
	 * @see com.alluvial.mds.iress.IMarketConnector#getUsername()
	 */
	public String getUsername() {
		return username;
	}

	/* (non-Javadoc)
	 * @see com.alluvial.mds.iress.IMarketConnector#getPassword()
	 */
	public String getPassword() {
		return password;
	}

	/* (non-Javadoc)
	 * @see com.alluvial.mds.iress.IMarketConnector#getLoginRequestFlag()
	 */
	public short getLoginRequestFlag() {
		return loginRequestFlag;
	}
	
	boolean connected = false;

	//private DataDumper dataDumper = null;

	/* (non-Javadoc)
	 * @see com.alluvial.mds.iress.IMarketConnector#connect()
	 */
	public void connect() throws IOException {
		// Create socket connection handlers
		socket = SocketChannel.open();
		socket.configureBlocking(false);
		socket.connect(new InetSocketAddress(getHost(), getPort()));
	    while (!socket.finishConnect()){}
		
		//Create selector
		selector = Selector.open();
		socket.register(selector, SelectionKey.OP_READ);

	    System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: connected to " + getHost() + ":" + getPort());
		
		// run heart beating thread now
	    runHeartBeating();

	    connected = true;
	}
	
	public void disconnect() throws IOException {
		if (connected) {
			timerHeartBeat.cancel();
			socket.close();
			connected = false;
		}
	}

	public boolean login() throws MCException {
		if (!connected)
			return false;
		
		// 1. Wait server information packet
		Set<Byte> packetTypes = new HashSet<Byte>();
		packetTypes.add(IdfHelper.IressDataFeedPacketType.IDF_PKT_SERVER_INFORMATION);
		
		try {
			receiveData(
					packetTypes,
	
					new IRawDataHandler()
		    		{ 
		    			public void call(short packetLen, byte packetType, ByteBuffer bb) throws IOException
		    			{
		    				ServerInfo si = IdfHelper.parseServerInformationPacket(bb);
		    				System.out.println("[RECV] [T" + Thread.currentThread().getId() + "]: " + si);
		    			}
		    		});
		} catch (IOException ex) {
			throw new MCException("failed when receiving data with server information", ex);
		}

		// 2. Send credentials
		try {
			byte[] bytesToSend = IdfHelper.buildLoginPacket(getUsername(), getPassword(), getLoginRequestFlag());
			sendData(bytesToSend);
		} catch (IOException ex) {
			throw new MCException("failed when sending login data", ex);
		}

		// 3. Wait for login accepted/rejected packet
		//return wait_server_response(icc);
		packetTypes = new HashSet<Byte>();
		packetTypes.add(IdfHelper.IressDataFeedPacketType.IDF_PKT_SERVER_LOGIN_ACCEPTED); 
		packetTypes.add(IdfHelper.IressDataFeedPacketType.IDF_PKT_SERVER_LOGIN_REJECTED);
		
		try {
			receiveData(
					packetTypes,

					new IRawDataHandler() { 
						public void call(short packetLen, byte packetType, ByteBuffer bb) throws IOException
						{
							switch(packetType)
							{
								case IdfHelper.IressDataFeedPacketType.IDF_PKT_SERVER_LOGIN_ACCEPTED :
									System.out.println("[RECV] [T" + Thread.currentThread().getId() + "]: login accepted");
									loginPassed = true;
									break;
								
								case IdfHelper.IressDataFeedPacketType.IDF_PKT_SERVER_LOGIN_REJECTED :
									LoginRejectedPacketPayload pkt = IdfHelper.parseLoginRejectedPacket(bb);
									System.out.println("[RECV] [T" + Thread.currentThread().getId() + "]: login rejected with reason " + pkt.Reason);
									loginPassed = false;
									break;
							}
						}
					});
		} catch (IOException ex) {
			throw new MCException("failed when receiving data with login confirmation", ex);
		}
		
		return loginPassed;
	}
	
	/* (non-Javadoc)
	 * @see com.alluvial.mds.iress.IMarketConnector#subscribe()
	 */
	public boolean subscribe() throws MCException
	{
		// if it is not emulation, then return
		try {
			byte[] bytesToSend = IdfHelper.buildSubscriptionRequestPacket(getExchange(), getDataSource(), 
					getDataSourceBoard(), getSupplementPackets() != null ? getSupplementPackets().length : 0);
			sendData(bytesToSend);
		} catch (IOException ex) {
			throw new MCException("failed when sending data with subscription request", ex);
		}
		
		// receive market data
		Set<Byte> packetTypes = new HashSet<Byte>();
		packetTypes.add(IdfHelper.IressDataFeedPacketType.IDF_PKT_SERVER_FEEDDATA);

		try {
			receiveData(
				packetTypes,
				new IRawDataHandler()
				{ 
					@Override
					public void call(short packetLength, byte packetType, ByteBuffer bb) throws IOException {
			            FeedDataPacketPayload pktPayload = new FeedDataPacketPayload();
			            
			            if (!IdfHelper.parseServerDataFeedPacket(bb, pktPayload))
			            	throw new IllegalArgumentException("Invalid Idf Data Packet");
			            
			            SubscriptionConfirmationMessageContent scmc = 
			            	(SubscriptionConfirmationMessageContent)pktPayload.MsgContent;

			            System.out.println("[RECV] [T" + Thread.currentThread().getId() + "]: " + pktPayload);
			            
			            if (scmc.errorCode==SubscriptionConfirmationMessageContent.ErrorCode.SUBSCRIPTION_SUCCESS)
			            	subscribed = true;
			            else
			            	subscribed = false;
					}
				});
		} catch (IOException ex) {
			throw new MCException("failed when receiving data with subscription confirmation", ex);
		}
		
		return subscribed;
	}

	/**
	 * This method raises heart beating thread.
	 */
	void runHeartBeating()
	{
		timerHeartBeat = new Timer();

		final byte[] heart_beat_packet = IdfHelper.buildHeartBeatPacket();
		final ByteBuffer bb = ByteBuffer.wrap(heart_beat_packet);
		
		timerHeartBeat.schedule( new TimerTask() {
							            public void run()
							            {
							            	if(socket!=null && socket.isConnected())
												try {
													bb.position(0);
													while (socket.write(bb)!=0);
												} catch (IOException e) {
													System.out.println("[ERROR][T" + Thread.currentThread().getId() + "][" + IRESSChannelName + "]: error occurred while sending heartbeat to IRESS " + e);
													this.cancel();

													iressConnector.onHeartbeatSendingFailed();
												}
							            }
						    		},
						    		1000, 1000);		// heartbeat every second after a second.
	}

	private void sendData(byte[] bytesToSend) throws IOException {
		System.out.println("[SEND] [T" + Thread.currentThread().getId() + "]: sendData '" + JSMHexConverter.ByteArrayToHexString(bytesToSend, "") + "'");

		ByteBuffer bb = ByteBuffer.wrap(bytesToSend);
		
		do
			socket.write(bb);
		while (bb.remaining()!=0);
	}

	private void receiveData(Set<Byte> packetTypes, IRawDataHandler callback) throws MCException, IOException
	{
		while(true)
		{
			// 1) wait until data arrives from IRESS to socket
		    try {
		        selector.select();
		    } catch (IOException ex) {
				System.out.println("[DEBUG][T" + Thread.currentThread().getId() + "]: receiveData() - error occurred while waiting for socket event");
				throw new MCException("error occurred while waiting for socket event", ex);
		    }

			// 2) retrieve data from socket to memory and dump the data to file if dump mode is on
	        try {
			    // Get list of selection keys with pending events
			    Iterator<SelectionKey> it = selector.selectedKeys().iterator();
			    it.next();
	
		        // Remove it from the list to indicate that it is being processed
		        it.remove();

		        if (socket.read(inputBufer) == -1) {
		        	System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: receiveData() - IRESS disconnected");
		        	throw new MCException("IRESS disconnected us");
		        }
	        }
	        catch (ClosedSelectorException ex) {
	        	System.out.println("[DEBUG][T" + Thread.currentThread().getId() + "]: receiveData() - selector was closed");
	        	throw new MCException(ex);
	        }
	        catch (AsynchronousCloseException ex) {
	        	System.out.println("[DEBUG][T" + Thread.currentThread().getId() + "]: receiveData() - socket was closed from different thread");
	        	throw new MCException(ex);
	        }
	        catch (IOException ex) {
	        	// Connection may have been closed
	        	System.out.println("[DEBUG][T" + Thread.currentThread().getId() + "]: receiveData() - I/O error, " + ex);
	        	throw new MCException(ex);
	        }

			inputBufer.flip();

			// wait for the server information packet
			while (inputBufer.remaining()>=IdfHelper.PACKETLENGTH_FIELD_SIZE + IdfHelper.PACKETTYPE_SIZE)
			{
				short packetLen = inputBufer.getShort();
				
				if (packetLen > inputBufer.remaining())
			    {
			    	System.out.println("[DEBUG][T" + Thread.currentThread().getId() + "]: no data available");
			    	inputBufer.position(inputBufer.position() - 2);
			        break;                     // incomplete message, break the loop and wait for the rest of the packet comes in.
			    }
			   	else if (packetLen < 1)
			        throw new RuntimeException("Invalid Packet Length");

				byte recvPacketType = inputBufer.get();
				
				//PacketNo++;

				if (packetTypes.contains(recvPacketType))
				{
					callback.call(packetLen, recvPacketType, inputBufer);
					//System.out.println("[DEBUG]: after proc:  " + inputBufer.position() + " " + inputBufer.limit());
					inputBufer.compact();
					return;
				}
				else if (recvPacketType == IdfHelper.IressDataFeedPacketType.IDF_PKT_HEARTBEAT)
				{
					System.out.println("[RECV] [T" + Thread.currentThread().getId() + "]: HB");
				}
				else
				{
					System.err.println("[ERROR][T" + Thread.currentThread().getId() + "]: receiveData() - no handler found");
    				inputBufer.position(packetLen-1);
				}
			}
			
			inputBufer.compact();
		}
	}

	public TradeRecoveryRequestMessageContent[] getSupplementPackets() {
		// TODO Auto-generated method stub
		return null;
	}
}
