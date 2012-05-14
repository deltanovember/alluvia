package com.alluvial.mlink.iress;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.FileChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.alluvial.mds.common.MDSHelper;
import com.alluvial.mlink.contract.IMarketConnector;
import com.alluvial.mlink.contract.IMarketListener;
import com.alluvial.mlink.contract.MCException;
import com.alluvial.mlink.iress.structures.FeedDataPacketPayload;

/**
 * This class implements IMarketConnector. It sends commands to IRESS and calls
 * back the IRESS link user over listener interface.
 * 
 * @author erepekto
 */
public class IressConnector implements IMarketConnector {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 155 $");
	
	private Selector iressSelector;
	private DataPieceProcessingQueue dataPieceProcessingQueue;
	
	// connections
	IressSocketConnection quoteConnectionHelper = null;
	IressSocketConnection depthConnectionHelper = null;
	IressSocketConnection tradeConnectionHelper = null;

	// continuous receiving
	private boolean dataParsingThreadActive;
	private boolean dataReceivingThreadActive;
	private Thread iressDataProcessingThread;
	private byte QUOTE_CHANNEL = 'Q';
	private byte TRADE_CHANNEL = 'T';
	private byte DEPTH_CHANNEL = 'D';

	// states of connector
	private boolean connected = false;
	private boolean loggedIn = false;
	private Boolean receiving = false;

	// 
	String host;
	int portQuote;
	int portDepth;
	int portTrade;
	String username;
	private String password;
	private String exchange;
	private String dataSource;
	private String dataSourceBoard;

	// emulation related fields
	private boolean emulationMode;
	private boolean dumpData;
	private String dumpDirectory;
	private static final String DEFUALT_DUMP_DIRECTORY = "dumps";
	private String nextReplayDate;
	private DataDumper allDataDumper = null;
	private boolean replayIsNotTerminated;
//	private DataDumper tradeDataDumper = null; // TODO later
//	private DataDumper quoteDataDumper = null;
//	private DataDumper depthDataDumper = null;

	private Object receivingPadLock = new Object();

	private boolean dataReplayingThreadActive;

	public IressConnector(String host, int portQuote, int portDepth,
			int portTrade, String username, String password, String exchange,
			String dataSource, String dataSourceBoard, boolean emulationMode, boolean dumpData,
			String dumpDirectory) {

		this.host = host;

		this.portQuote = portQuote;
		this.portDepth = portDepth;
		this.portTrade = portTrade;

		this.username = username;
		this.password = password;

		this.exchange = exchange;
		this.dataSource = dataSource;
		this.dataSourceBoard = dataSourceBoard;

		this.emulationMode = emulationMode;
		this.dumpData = dumpData;
		this.dumpDirectory = dumpDirectory==null || dumpDirectory.isEmpty()?DEFUALT_DUMP_DIRECTORY:dumpDirectory;
	}

	@Override
	public void connect() throws MCException {
		if (emulationMode) {
			return;
		}
		
		try {
			tradeConnectionHelper = new IressSocketConnection(this, "TRADE", host,
														portTrade, username, password, exchange, dataSource,
														dataSourceBoard, emulationMode, dumpData, dumpDirectory);

			depthConnectionHelper = new IressSocketConnection(this, "DEPTH", host,
														portDepth, username, password, exchange, dataSource,
														dataSourceBoard, emulationMode, dumpData, dumpDirectory);

			quoteConnectionHelper = new IressSocketConnection(this, "QUOTE", host,
														portQuote, username, password, exchange, dataSource,
														dataSourceBoard, emulationMode, dumpData, dumpDirectory);

			tradeConnectionHelper.connect();
			depthConnectionHelper.connect();
			quoteConnectionHelper.connect();
		} 
		catch (Exception ex) {
			disconnect();
			throw new MCException("the error occured while connecting", ex);
		}

		// dump file
		if (dumpData && !emulationMode) {
			allDataDumper = new DataDumper("ALL", dumpDirectory);
			allDataDumper.createFile();
		}
		
		connected = true;
	}
	
	@Override
	public synchronized void disconnect() {
		if (emulationMode) {
			stopDataReplaying();
			return;
		}
		
		try {
			if (connected) {
				stopDataReceiving();
				
				// now turn off heart beat timers and disconnect each socket
				quoteConnectionHelper.disconnect();
				depthConnectionHelper.disconnect();
				tradeConnectionHelper.disconnect();
				
				if (iressSelector!=null)
					iressSelector.close();
	
				if (allDataDumper!=null)
					allDataDumper.closeFile();
				
				connected = false;
				loggedIn = false;
			}
		} catch (Exception e) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId()
					+ "]: error occurred while disconnecting from feed");
			e.printStackTrace();
		}
	}

	@Override
	public void login() throws MCException {
		if (emulationMode)
			return;
		
		if (loggedIn)
			return;

		if (!connected) {
			throw new MCException(
					"login() is called before the connection is established");
		}

		if (!tradeConnectionHelper.login()) {
			disconnect();
			throw new MCException("login failed on trade channel");
		}
		
		if (!quoteConnectionHelper.login()) {
			disconnect();
			throw new MCException("login failed on quote channel");
		}

		if (!depthConnectionHelper.login()) {
			disconnect();
			throw new MCException("login failed on depth channel");
		}

		loggedIn = true;
	}

	@Override
	public void setListener(IMarketListener listener) {
		IdfHelper.listener = listener;
	}

	@Override
	public void runDataReceiving() throws MCException {
		if (emulationMode) {
			allDataDumper = new DataDumper("ALL", dumpDirectory);
			raiseContinuousDataReplayThread();
			
			// for debug
			//addReplay("20111017");
			
			return;
		}
		
		if (!tradeConnectionHelper.subscribe())
			throw new MCException("subscription failed on trade channel");

		if (!depthConnectionHelper.subscribe())
			throw new MCException("subscription failed on depth channel");
		
		if (!quoteConnectionHelper.subscribe())
			throw new MCException("subscription failed on quote channel");

		// start continuous receiving
//		tradeConnection.startListening();
//		depthConnection.startListening();
//		quoteConnection.startListening();

		try {
			raiseContinuousDataReadParseThreads();
		} catch (IOException e) {
			throw new MCException("error occurred while raising the receiving thread", e);
		}
	}

	/**
	 * This function starts two threads:
	 * 1) data receiving thread;
	 * 2) data processing thread.
	 * @throws IOException
	 */
	private void raiseContinuousDataReadParseThreads() throws IOException {
		iressSelector = Selector.open(); 
		tradeConnectionHelper.socket.register(iressSelector, SelectionKey.OP_READ, TRADE_CHANNEL);
		depthConnectionHelper.socket.register(iressSelector, SelectionKey.OP_READ, DEPTH_CHANNEL);
		quoteConnectionHelper.socket.register(iressSelector, SelectionKey.OP_READ, QUOTE_CHANNEL);

		receiving = true;
		selectorProcessingThread = new Thread( new Runnable() {
			@Override
			public void run() {
				// raising data queue processing thread
				dataParsingThreadActive = true;
				raiseDataParsingThread();

				// data receiving loop
				dataReceivingThreadActive = true;
				while (dataReceivingThreadActive) {
					Iterator<SelectionKey> it;
					try {
						iressSelector.select();

					    // Get list of selection keys with pending events
					    it = iressSelector.selectedKeys().iterator();
					}
					catch (ClosedSelectorException e) {
						e.printStackTrace();
						break;
					}
					catch (IOException e) {
						e.printStackTrace();
						break;
					}
		
					while (it.hasNext())
					{
					    // Get list of selection keys with pending events
					    SelectionKey selKey = (SelectionKey)it.next();
					    
						// 2) retrieve data from socket to memory and dump the data to file if dump mode is on
				        try {
					        // Remove it from the list to indicate that it is being processed
					        it.remove();

							if (!selKey.isValid())
								continue;
					        
					        // read data from sockets, the position should be memorized in order not to dump old data
					        IressDataPiece idp = IressDataPiece.get((Byte)selKey.attachment());
		
					        SocketChannel sc = (SocketChannel)selKey.channel();
					        
					        if ( sc.read(idp.bb)==-1) {
					        	System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: selectorProcessingThread - IRESS disconnected");
					        	dataReceivingThreadActive = false;
					        	break;
					        }
					        	
					        //System.out.println("[INFO] received " + (Byte)selKey.attachment()); // for debug
					        dataPieceProcessingQueue.add(idp);
				        }
				        catch (ClosedSelectorException ex) {
				        	System.out.println("[DEBUG][T" + Thread.currentThread().getId() + "]: selectorProcessingThread - selector was closed");
				        	dataReceivingThreadActive = false;
				        	break;
				        }
				        catch (AsynchronousCloseException ex) {
				        	System.out.println("[DEBUG][T" + Thread.currentThread().getId() + "]: selectorProcessingThread - socket was closed from different thread");
				        	dataReceivingThreadActive = false;
				        	break;
				        }
				        catch (IOException ex) {
				        	// Connection may have been closed
				        	System.out.println("[DEBUG][T" + Thread.currentThread().getId() + "]: selectorProcessingThread - I/O error, " + ex);
				        	dataReceivingThreadActive = false;
				        	break;
				        }
					}
				}

				stopDataProcessingThread();
				
				// then indicate that continuous receiving is over
				synchronized(receivingPadLock) {
					receiving = false;
					receivingPadLock.notifyAll();
				}
			}
		});
		
		selectorProcessingThread.setPriority(Thread.MAX_PRIORITY);
		selectorProcessingThread.start();
	}
	
	private void raiseDataParsingThread() {
		dataPieceProcessingQueue = new DataPieceProcessingQueue();

		iressDataProcessingThread = new Thread( new Runnable() {
			@Override
			public void run() {
				// this container holds remainders of received data and not fully processed (because last packet was cut off)
				HashMap<Byte, ByteBuffer> dataRemainders = new HashMap<Byte, ByteBuffer>(3);
				
				dataRemainders.put(QUOTE_CHANNEL, quoteConnectionHelper.inputBufer);
				dataRemainders.put(TRADE_CHANNEL, tradeConnectionHelper.inputBufer);
				dataRemainders.put(DEPTH_CHANNEL, depthConnectionHelper.inputBufer);

				// packet types
				final Set<Byte> packetTypes = new HashSet<Byte>();
				packetTypes.add(IdfHelper.IressDataFeedPacketType.IDF_PKT_SERVER_FEEDDATA);

				while (dataParsingThreadActive) {
					// merge just arrived data with possibly remaining in the buffer data
					IressDataPiece dp = dataPieceProcessingQueue.get();
					ByteBuffer dataRemainder = dataRemainders.get(dp.channel);
					dp.bb.flip();
					dataRemainder.put(dp.bb);
					dataRemainder.flip();

					// process data
					parseAndProcessReceivedData(
							packetTypes,
							new IRawDataHandler()
							{
								@Override
								public void call(short packetLength, byte packetType, ByteBuffer bb) throws IOException 
								{
					                FeedDataPacketPayload pktPayload = new FeedDataPacketPayload();
					                if (!IdfHelper.parseServerDataFeedPacket(bb, pktPayload))
					                	throw new IllegalArgumentException("Invalid Idf Data Packet");
				
					                // for debug
					                //System.out.println("[RECV] [T" + Thread.currentThread().getId() + "]: " + pktPayload);
					                IdfHelper.listener.countMessage();
								}
							},
							dataRemainder,
							dp.channel);
					
			        // dump to file
			        if (allDataDumper!=null)
			        	allDataDumper.dump(dataRemainder);

			        dataRemainder.compact();

			        // return data piece to cache
			        IressDataPiece.put(dp);
				}
			}
		});
		
		iressDataProcessingThread.setPriority(Thread.MAX_PRIORITY);
		iressDataProcessingThread.start();
	}
	
	private void stopDataProcessingThread() {
		// wait until data processing thread is alive
		dataParsingThreadActive = false;
		dataPieceProcessingQueue.wakeUp(QUOTE_CHANNEL);
		while (iressDataProcessingThread.isAlive())
			try {
				iressDataProcessingThread.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}

	private void stopDataReceiving() {
		if (receiving) {
			dataReceivingThreadActive = false;
			iressSelector.wakeup();
			
			while (selectorProcessingThread.isAlive())
				try {
					selectorProcessingThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			
			waitDataReceivingFinished();
		}
	}
	
	private void stopDataReplaying() {
		if (receiving) {
			dataReplayingThreadActive = false;
			
			// add fake replay to wake up queue
			try {
				addReplay("FAKE_REPLAY");
			} catch (MCException e1) {
				e1.printStackTrace();
			}
			
			while (replayThread.isAlive())
				try {
					replayThread.join();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			
			waitDataReceivingFinished();
		}
	}

	@Override
	public void waitDataReceivingFinished() {
		if (receiving)
			synchronized(receivingPadLock)
			{
				while (receiving)
					try {
						receivingPadLock.wait(1);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
			}
	}
	
	/**
	 * This method is called by connection when receiving is over normally or because of error.
	 */
	void onHeartbeatSendingFailed() {
		disconnect();
	}

	ArrayList<String> replayQueue = new ArrayList<String>();
	@Override
	public void addReplay(String replayDate) throws MCException {
		if (!emulationMode)
			throw new MCException("IRESS connector is in the live mode");
		
		synchronized(replayQueue)
		{
			replayQueue.add(replayDate);
			replayQueue.notify();
		}
	}

	/**
	 * This function puts all threads to the start line. Then checks if there are replays ordered.
	 * If replays are in the queue, then it notifies clients and releases threads. 
	 * @return
	 */
	public String getNextReplayDate() {
		synchronized(replayQueue)
		{
			while (replayQueue.size()==0)
				try {
					replayQueue.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}

			nextReplayDate = replayQueue.get(0);
			replayQueue.remove(0);
			replayQueue.notify();
		}
		
		return nextReplayDate;
	}

	@Override
	public void terminateReplays() throws MCException {
		synchronized(replayQueue)
		{
			replayQueue.clear();
			replayIsNotTerminated = false;
		}
	}
	
	static long PacketNo = 0;

	private Thread selectorProcessingThread;

	private Thread replayThread;
	void parseAndProcessReceivedData(Set<Byte> packetTypes, IRawDataHandler callback, ByteBuffer inputBufer, byte channel) {
		// process packets in buffer until the rest is lesser than three bytes
		while (inputBufer.remaining()>=IdfHelper.PACKETLENGTH_FIELD_SIZE + IdfHelper.PACKETTYPE_SIZE) 
		{
			int startPosForAssert = inputBufer.position();

			short packetLen = inputBufer.getShort();
			
		    if (packetLen > inputBufer.remaining())
		    {
		    	// incomplete message, break the loop and wait for the rest of the packet comes in.
		    	inputBufer.position(inputBufer.position() - IdfHelper.PACKETLENGTH_FIELD_SIZE);
		        break;
		    }
		   	else if (packetLen < 1)
		        throw new RuntimeException("Invalid Packet Length");

			byte recvPacketType = inputBufer.get();

			PacketNo++;
			
			if (packetTypes.contains(recvPacketType))
			{
				try {
					callback.call(packetLen, recvPacketType, inputBufer);
					//System.out.println("PacketNo=" + PacketNo);
				}
				catch(Exception ex)	{
					System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: parseAndProcessReceivedData() - error occurred while processing received packet");
					ex.printStackTrace();
					int adjstedPos = startPosForAssert + IdfHelper.PACKETLENGTH_FIELD_SIZE + packetLen;
					inputBufer.position(adjstedPos);
				}
			}
			else if (recvPacketType == IdfHelper.IressDataFeedPacketType.IDF_PKT_HEARTBEAT) {
				System.out.println("[RECV] [T" + Thread.currentThread().getId() + "]: " + (char)channel + "HB");
			}
			else {
				System.out.println("[WARN] [T" + Thread.currentThread().getId() + "]: parseAndProcessReceivedData() - no handler found, skipping packet");
				inputBufer.position(inputBufer.position() + packetLen - IdfHelper.PACKETTYPE_SIZE);
			}
			
			// TODO: assertion, the code below is to be removed
			if ((startPosForAssert + IdfHelper.PACKETLENGTH_FIELD_SIZE + packetLen)!=inputBufer.position() )
				System.err.println("[ERROR][T" + Thread.currentThread().getId() + "]: parseAndProcessReceivedData() - positions are inconsistent");
		}
	}

	/**
	 * Starts continuous data replaying thread.
	 */
	private void raiseContinuousDataReplayThread() {
		receiving = true;
		replayThread = new Thread( new Runnable() {
			@Override
			public void run() {
				System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: continuous replay thread started");
				replayDataContinuously();
				System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: continuous replay thread finished");
			}
		});
		
		replayThread.start();
	}
	
	private void replayDataContinuously()
	{
		dataReplayingThreadActive = true;
		while (dataReplayingThreadActive) {
			String replayDate = getNextReplayDate();
			
			if (!dataReplayingThreadActive)
				break;
			
			// notify about start of date replay
			IdfHelper.listener.onReplayDateStart(nextReplayDate);
			
			String[] dumpFiles = allDataDumper.getDumpFilesForDate(replayDate);

			System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: replayDataContinuously() - following files will be replayed " + Arrays.toString(dumpFiles));
			
			// this flag will be turned down if replay needs to be terminated
			replayIsNotTerminated = true;
			
			for (int i=0; i<dumpFiles.length; i++) {
				try {
					replayDumpFile(dumpFiles[i]);
				}
				catch (FileNotFoundException ex) {
					System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: replayDataContinuously() - dump file is not found, skipping " + ex);
				} catch (IOException ex) {
					System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: replayDataContinuously() - some error occurred while replaying '" + replayDate + "' " + ex);
				}
			}
			
			// notify about finish of date replay
			IdfHelper.listener.onReplayDateFinish(nextReplayDate);
		}

		// then indicate that continuous replaying is over
		synchronized(receivingPadLock) {
			receiving = false;
			receivingPadLock.notifyAll();
		}
		
		System.out.println("[DEBUG][T" + Thread.currentThread().getId() + "]: replayDataContinuously() has finished.");
	}
	
	private void replayDumpFile(String dumpFile) throws FileNotFoundException, IOException 
	{
		FileInputStream fis = new FileInputStream(dumpDirectory + File.separator + dumpFile);
		FileChannel fileChannel = fis.getChannel();
		
		boolean replayIsNotOver = true;
		
		int totallyBytesReplayed = 0;
		
		ByteBuffer inputBufer = ByteBuffer.allocateDirect(65536);
		inputBufer.order(ByteOrder.LITTLE_ENDIAN);
		inputBufer.clear();
		
		// packet types
		final Set<Byte> packetTypes = new HashSet<Byte>();
		packetTypes.add(IdfHelper.IressDataFeedPacketType.IDF_PKT_SERVER_FEEDDATA);
		
		while(replayIsNotOver && replayIsNotTerminated) {
			// 1) retrieve data dumped to file
			totallyBytesReplayed+=fileChannel.read(inputBufer);
		
		    if (fileChannel.read(inputBufer)==-1) {
		    	replayIsNotOver = false;
		    }
		    
			inputBufer.flip();

			// 2) parse and process received data 
			parseAndProcessReceivedData(packetTypes, 
										new IRawDataHandler()
										{
											@Override
											public void call(short packetLength, byte packetType, ByteBuffer bb) throws IOException 
											{
								                FeedDataPacketPayload pktPayload = new FeedDataPacketPayload();
								                if (!IdfHelper.parseServerDataFeedPacket(bb, pktPayload))
								                	throw new IllegalArgumentException("Invalid Idf Data Packet");
							
								                // for debug
								                //System.out.println("[RECV] [T" + Thread.currentThread().getId() + "]: " + pktPayload);
								                IdfHelper.listener.countMessage();
											}
										}, 
										inputBufer, (byte)'R');
		
			inputBufer.compact();
		}
		
		if (!replayIsNotOver)
			System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: IRESS dump file " + dumpFile + " is over");
		else if (!replayIsNotTerminated)
			System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: IRESS dump file " + dumpFile + " replay has been terminated");
		else
			System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: IRESS dump file " + dumpFile + " HERE");
		
		fileChannel.close();
		fis.close();
	}
	
}
