package com.alluvial.mds;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.StreamCorruptedException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousCloseException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

import com.alluvial.mds.common.MDSHelper;
import com.alluvial.mds.contract.ContractHelper;
import com.alluvial.mds.contract.DictionaryRequest;
import com.alluvial.mds.contract.DictionaryResponse;
import com.alluvial.mds.contract.MDSInfo;
import com.alluvial.mds.contract.ReplayConfirmation;
import com.alluvial.mds.contract.ReplayReject;
import com.alluvial.mds.contract.ReplayRequest;
import com.alluvial.mds.contract.Subscription;
import com.alluvial.mds.contract.SubscriptionConfirmation;

public class MDSSubscriber {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 166 $");
	
	private ByteBuffer buffer = ByteBuffer.allocate(65535);
	private SocketChannel sChannel;
	private SelectionKey key;
	private MarketObserver observer;
	private String host;
	private Selector selector;
	private boolean connected;

	public MDSSubscriber(SelectionKey key, MarketObserver observer) throws IOException {
		this.key = key;
		this.sChannel = (SocketChannel) key.channel();
		this.host = sChannel.socket().getInetAddress().getHostAddress();
		this.observer = observer;
		this.connected = true;
		selector = Selector.open();
		sChannel.register(selector, SelectionKey.OP_WRITE);
		
		System.out.println("[RECV] [T" + Thread.currentThread().getId() + "]: connection from " + getHost());
	}

	public String getHost() {
		return host;
	}

	public void processRequest() throws IOException {
		// it can be disconnection
	    try {
	    	int bytesRead = sChannel.read(buffer);

	    	// if disconnected
	    	if (bytesRead==-1) {
    			connected = false;
	    		observer.removeSubscriber(this); // TODO: this call should be asynchronous

	    		synchronized(sChannel)
	    		{
					selector.wakeup();
	    			key.cancel();
					sChannel.close();
					sChannel.notify();
					
	    		}

				return;
	    	}
	    	
	    	connected = true;
	    }
	    catch (IOException e) {
			connected = false;

	    	// The remote forcibly closed the connection, cancel, the selection key and close the channel.
			observer.removeSubscriber(this);
			
			synchronized(sChannel)
    		{
				selector.wakeup();
    			key.cancel();
				sChannel.close();
				sChannel.notify();
    		}
		
			return;
	    }

	    // actual buffer processing
    	buffer.flip();

    	int posBeforeRead = buffer.position();
    	byte[] readBytes = new byte[buffer.limit()];
    	buffer.get(readBytes);
    	buffer.position(posBeforeRead);

    	ByteArrayInputStream bis = new ByteArrayInputStream(readBytes, 0, readBytes.length);

    	while (bis.available()!=0)
    	{
    		Object obj;

	    	try
	    	{
	    		ObjectInputStream ois = new ObjectInputStream(bis);
	    		obj = ois.readObject();
	    		buffer.position(buffer.limit() - bis.available());
	    	}
	    	catch (StreamCorruptedException ex)
	    	{
	    		break;
	    	}
	    	catch (Exception ex)
	    	{
	    		ex.printStackTrace();
	    		break;
	    	}

    		if (obj.getClass()==Subscription.class) {
    			Subscription s = (Subscription)obj;
    			SubscriptionConfirmation sc = new SubscriptionConfirmation();

    			String[] securitiesToBeSubscribed;

    			// subscribe to all
    			if (s.securities.length==1 && s.securities[0].equals("*")) {
    				securitiesToBeSubscribed = new String[observer.getDictionary().size()];
    				observer.getDictionary().keySet().toArray(securitiesToBeSubscribed);
    			}
    			else
    				securitiesToBeSubscribed = s.securities;

    			sc.retCodes = new byte[securitiesToBeSubscribed.length];
    			observer.addSubscribtion(this, securitiesToBeSubscribed, sc.retCodes);

    			sendObjectSync(sc);
    		}
    		else if(obj.getClass()==DictionaryRequest.class) {
    			DictionaryResponse dr = new DictionaryResponse();
    			dr.securities = new String[observer.getDictionary().size()];
    			observer.getDictionary().keySet().toArray(dr.securities);
    			sendDictionarySync(dr);
    		}
    		else if (obj.getClass()==ReplayRequest.class) {
    			ReplayRequest rr = (ReplayRequest)obj;
    			
    			// try to add replay subscription
    			try {
    				observer.addReplaySubscription(this, rr.date);
    			
	    			// create confirmation
	    			ReplayConfirmation rc = new ReplayConfirmation();
	    			rc.date = rr.date;
	    			
	    			sendObjectSync(rc);
    			}
    			catch (IllegalArgumentException ex) {
    				ReplayReject rej = new ReplayReject(rr.date, ex.getMessage());
	    			sendObjectSync(rej);
		    		
	    			synchronized(sChannel)
		    		{
		    			key.cancel();
						sChannel.close();
						sChannel.notify();
		    		}
    			}
    		}
    		else {
    			System.out.println("[WARN] [T" + Thread.currentThread().getId() + "]: client request type is unknown " + obj.getClass());
    		}
    	}
    	
		buffer.compact();
	}
	
	/**
	 * for debug
	 * for tests purposes
	 */
	public void processTestRequest() {
		final MDSSubscriber this1=this;
		new Thread( new Runnable() {
			
			@Override
			public void run() {
				ReplayRequest rr = new ReplayRequest();
				rr.date = "20110812";
				observer.addReplaySubscription(this1, rr.date);
				rr.date = "20110812";
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				observer.addReplaySubscription(this1, rr.date);
				rr.date = "20110812";
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				observer.addReplaySubscription(this1, rr.date);
				rr.date = "20110812";
				try {
					Thread.sleep(10000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				observer.addReplaySubscription(this1, rr.date);
				
			}
		}).start();
	}

	private void sendDictionarySync(DictionaryResponse dict) {
		try {
			sendObjectSync(dict);
			System.out.println("[SENT] [T" + Thread.currentThread().getId() + "]: dictionary to " + this.getHost());
		} catch (IOException e) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: sendDictionarySync - error occurred while sending dictionary");
			e.printStackTrace();
		}
	}

	ByteBuffer bb = ByteBuffer.allocate(1024*1024);
	
	private void sendObjectSync(Object obj) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
		ObjectOutputStream oos = new ObjectOutputStream(bos);
		oos.writeObject(obj);
		oos.close();
		
		byte[] bytesToSend = bos.toByteArray();
		sendMessageSyncWithFlush(bytesToSend);
	}

	/**
	 * This message blocks executing thread until all bytes from buffer has been sent, but only when buffer is full.
	 * @param bytesToSend
	 * @throws IOException
	 */
	void sendMessageSync(byte[] bytesToSend) {
		synchronized(bb)
		{
			if (bb.remaining()<bytesToSend.length) {
				sendEntireBuffer();
			}

			if (!connected)
				return;
			
			// put data to byte buffer
			try {
				bb.put(bytesToSend);
			}
			catch(BufferOverflowException ex) {
				bb.notify();
				observer.removeSubscriber(this);
	
				synchronized(sChannel)
	    		{
					try {
						sChannel.close();
						connected = false;
					} catch (IOException e) {
						e.printStackTrace();
					}
					sChannel.notify();
	    		}

				System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: client " + getHost() + " is disconnected, because of overflown buffer, remaining: " + bb.remaining() + ", bytes to be sent: "+ bytesToSend.length + ".");
				return;
			}
			
			//System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: 244 /synchronized(bb)");
		}
	}
	
	void sendMessageSyncWithFlush(byte[] bytesToSend) {
		synchronized(bb)
		{
			sendMessageSync(bytesToSend);
			sendEntireBuffer();
		}
	}
	
	private void sendEntireBuffer() {
		bb.flip();
		
		while(bb.remaining()>0 && connected)
		{
		    try {
		        // Wait for an event
		    	//System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: selector.select() start");
		        selector.select();
		        //System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: selector.select() done");
		    } catch (IOException e) {
		        // Handle error with selector
		        break;
		    }
			
		    try {
    			sChannel.write(bb);
			}
			catch(AsynchronousCloseException ex) {
				System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: socket closed from another thread: " + ex);
				break;
			}
			catch(IOException ex)
			{
				System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: sChannel.write(bb) " + ex);
				break;
			}
		}
	
		bb.compact();
	}
	
	/**
	 * This messages sends data in non-blocking mode, it puts data to networkd buffer and returns.
	 * If network buffer is not empty enough for the passed amount of data, then remaining bytes
	 * are left in ByteBuffer (see field bb) and will be sent in next sessions.
	 *   
	 * @param bytesToSend
	 * @throws IOException
	 */
	void sendMessage(byte[] bytesToSend) {
		synchronized(bb)
		{
			// put data to byte buffer
			try {
				bb.put(bytesToSend);
			}
			catch(BufferOverflowException ex) {
				if (connected)
				{
					connected = false;
					observer.removeSubscriber(this);
					
					synchronized(sChannel)
		    		{
						try {
							sChannel.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
						sChannel.notify();
		    		}
					
					System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: client " + getHost() + " is disconnected, because of overflown buffer, remaining: " + bb.remaining() + ", bytes to be sent: "+ bytesToSend.length + ".");
				}

				return;
			}
		}

		try
		{
			synchronized(bb)
			{
				bb.flip();
				sChannel.write(bb);  
				bb.compact();
			}
		
//				if (bytesSent!=bytesInBuffer)
//					System.out.println("[WARN] [T" + Thread.currentThread().getId() + "]: bytes sent " + bytesSent + ", while bytes in buffer " + bytesInBuffer);
		}
		catch(AsynchronousCloseException ex) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: socket closed from another thread: " + ex);
		}
		catch(IOException ex) {
			if (connected)
			{
				System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: " + ex);
				ex.printStackTrace();
			}
//				try {
//					// wait, connection should being closed by another thread
//					sChannel.notify();
//					sChannel.wait(3000);
//				} catch (InterruptedException e) {
//					e.printStackTrace();
//				}
//				
//				// if connection is still not closed then throw exception
//				if (sChannel.isOpen())
//					throw ex;
		}
	}

	public void sendMDSInfo() throws IOException {
		MDSInfo mi = new MDSInfo();
		mi.contractVersion = ContractHelper.getVersion();
		sendObjectSync(mi);
	}
}
