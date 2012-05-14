package com.alluvial.mds;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import com.alluvial.mds.common.MDSHelper;
import com.alluvial.mds.contract.ConsolidatedOrder;
import com.alluvial.mds.contract.OffMktTrade;
import com.alluvial.mds.contract.Quote;
import com.alluvial.mds.contract.QuoteFull;
import com.alluvial.mds.contract.QuoteMatch;
import com.alluvial.mds.contract.ReplayDateEnd;
import com.alluvial.mds.contract.ReplayDateStart;
import com.alluvial.mds.contract.SingleOrder;
import com.alluvial.mds.contract.Trade;
import com.alluvial.mlink.contract.IMarketConnector;
import com.alluvial.mlink.contract.IMarketListener;
import com.alluvial.mlink.contract.MCException;

/**
 * This class implements interface IMarketListener in order to be called back by IRESS market link.
 * @author erepekto
 */
public class MarketObserver implements IMarketListener {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 155 $");
	
	private HashMap<Integer, ArrayList<MDSSubscriber>> subscriptions = new HashMap<Integer, ArrayList<MDSSubscriber>>();
	
	private HashMap<String, Integer> dictionarySecCodeToId = new HashMap<String, Integer>();
	private HashMap<Integer, String> dictionaryIdToSecCode = new HashMap<Integer, String>();

	// this array contains subscribers subscribed for replay, these subscribers received updates for all instruments,
	// they don't subscribe for particular security
	private ArrayList<MDSSubscriber> replaySubscribers = new ArrayList<MDSSubscriber>();
	
	private long messageCounter;

	private IMarketConnector connector = null;

	MarketObserver(IMarketConnector connector) {
		this.connector = connector;
	}

	/**
	 * Addes subscriber.
	 * @param subscriber - subscriber itself
	 * @param securities - securities the subscriber to be subscribed for
	 * @param retCodes - indicates the result of subscription, 0 - OK, 1 - error.
	 */
	void addSubscribtion(MDSSubscriber subscriber, String[] securities, byte[] retCodes)
	{
		System.out.println("[RECV] [T" + Thread.currentThread().getId() + "]: received subscription from " + subscriber.getHost());
		
		for (int i=0; i<securities.length; i++)
		{
			String secCode = securities[i];

			Integer secID;
			synchronized(dictionarySecCodeToId)
			{
				secID = dictionarySecCodeToId.get(secCode);
			}
			
			if(secID==null) {
				retCodes[i] = 1;
				System.err.println("[WARN][T" + Thread.currentThread().getId() + "]: security " + secCode + " is not found in dictionary");
				continue;
			}
			
			HashMap<Integer, ArrayList<MDSSubscriber>> tempSubscriptions = 
				new HashMap<Integer, ArrayList<MDSSubscriber>>(subscriptions);
			ArrayList<MDSSubscriber> subscribersForInstr = tempSubscriptions.get(secID);
			
			Integer[] keys = new Integer[tempSubscriptions.keySet().size()];
			tempSubscriptions.keySet().toArray(keys);
			
			if (subscribersForInstr == null) {
				subscribersForInstr = new ArrayList<MDSSubscriber>();
				tempSubscriptions.put(secID, subscribersForInstr);
			}

			subscribersForInstr.add(subscriber);
			subscriptions = tempSubscriptions;
			
			retCodes[i] = 0;
		}
	}
	
	public void addReplaySubscription(MDSSubscriber subscriber, String date) throws IllegalArgumentException {
		if (!replaySubscribers.contains(subscriber)) {
			ArrayList<MDSSubscriber> newReplaySubscribers = new ArrayList<MDSSubscriber>(replaySubscribers);
			newReplaySubscribers.add(subscriber);
			replaySubscribers = newReplaySubscribers;
		}

		try {
			connector.addReplay(date);
			System.out.println("[RECV] [T" + Thread.currentThread().getId() + "]: accepted replay subscription from " + subscriber.getHost());
		} catch (MCException e) {
			removeSubscriber(subscriber);
			System.out.println("[RECV] [T" + Thread.currentThread().getId() + "]: rejected replay subscription from " + subscriber.getHost() + " because " + e.getMessage());
			throw new IllegalArgumentException(e.getMessage());
		}
	}

	/**
	 * This methods is called when subscriber is disconnected. It needs to be removed from subscriber list
	 * and all subscriptions of that subscriber are to be removed.
	 * 
	 * This method is synchronized since several threads can call it:
	 * 1) the MDS thread serving the incoming client connections and requests
	 * 2) IRESS channel related threads that send data over observer.on...() methods, if write() throws exception
	 * @param subscriber
	 */
	synchronized void removeSubscriber(MDSSubscriber subscriber)
	{
		boolean wasUnsibscribed = false;
		
		HashMap<Integer, ArrayList<MDSSubscriber>> newSubscriptions = 
			new HashMap<Integer, ArrayList<MDSSubscriber>>(subscriptions);
		
		Set<Integer> keys = newSubscriptions.keySet();
		Iterator<Integer> itr = keys.iterator();

		while(itr.hasNext())
		{
			Integer security = itr.next();
			ArrayList<MDSSubscriber> newSubscribers = new ArrayList<MDSSubscriber>(newSubscriptions.get(security));
			
			if (newSubscribers.remove(subscriber))
				wasUnsibscribed = true;

			newSubscriptions.put(security, newSubscribers);
		}
		
		subscriptions = newSubscriptions;

		// remove from replaying subscribers
		ArrayList<MDSSubscriber> newReplaySubscribers = new ArrayList<MDSSubscriber>(replaySubscribers);
		if (!wasUnsibscribed && newReplaySubscribers.contains(subscriber)) {
			newReplaySubscribers.remove(subscriber);
			replaySubscribers = newReplaySubscribers;
			wasUnsibscribed = true;

			// terminate all replays if all replayers have disconnected
			if (replaySubscribers.size()==0)
				try {
					connector.terminateReplays();
				} catch (MCException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
		if (wasUnsibscribed)
			System.out.println("[INFO] [T" + Thread.currentThread().getId() + "]: unsubscribed subscriber " + subscriber.getHost());
	}

	@Override
	public void onTrade(Trade trade) {
		ArrayList<MDSSubscriber> subscribers = subscriptions.get(trade.SecurityID);
		
		if (subscribers == null && replaySubscribers.size() == 0)
			return;

		trade.Security = dictionaryIdToSecCode.get(trade.SecurityID);

		// serializing
		ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(trade);
			oos.close();
		} catch (IOException e) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: onTrade - error occurred while serializing object");
			e.printStackTrace();
		}

		byte[] bytesToSend = bos.toByteArray();

		// send to subscribers of certain securities
		// TODO need to remove this check by having empty array for securities having no subscriptions
		if (subscribers!=null)
			for (int i=0; i<subscribers.size(); i++)
				subscribers.get(i).sendMessage(bytesToSend);
		
		// send to replay subscribers
		for (int i=0; i<replaySubscribers.size(); i++) {
			//subscribers.get(i).notifyOnTrade(trade);
			replaySubscribers.get(i).sendMessageSync(bytesToSend);
		}
	}

	@Override
	public void onUpdateDictionary(Integer SecurityId, String SecurityCode,
			String Exchange) {
		// TODO: clean old records, update subscriptions
		
		synchronized(dictionarySecCodeToId)
		{
			dictionarySecCodeToId.put(SecurityCode, SecurityId);
			dictionaryIdToSecCode.put(SecurityId, SecurityCode);
		}
	}

	@Override
	public void onConsolidatedOrder(ConsolidatedOrder order) {
		ArrayList<MDSSubscriber> subscribers = subscriptions.get(order.SecurityID);
		
		if (subscribers == null && replaySubscribers.size() == 0)
			return;
		
		order.Security = dictionaryIdToSecCode.get(order.SecurityID);

		// serializing
		ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(order);
			oos.close();
		} catch (IOException e) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: onConsolidatedOrder - error occurred while serializing object");
			e.printStackTrace();
		}

		byte[] bytesToSend = bos.toByteArray();
		
		// send to subscribers of certain securities
		// TODO need to remove this check by having empty array for securities having no subscriptions
		if (subscribers!=null)
			for (int i=0; i<subscribers.size(); i++)
				subscribers.get(i).sendMessage(bytesToSend);
		
		// send to replay subscribers
		for (int i=0; i<replaySubscribers.size(); i++)
			replaySubscribers.get(i).sendMessageSync(bytesToSend);
	}

	@Override
	public void onSingleOrder(SingleOrder order) {
		ArrayList<MDSSubscriber> subscribers = subscriptions.get(order.SecurityId);
		
		if (subscribers == null && replaySubscribers.size() == 0)
			return;
		
		order.Security = dictionaryIdToSecCode.get(order.SecurityId);
		
		// serializing
		ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(order);
			oos.flush();
			oos.close();
		} catch (IOException e) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: onSingleOrder - error occurred while serializing object");
			e.printStackTrace();
		}

		byte[] bytesToSend = bos.toByteArray();
		
		// send to subscribers of certain securities
		if (subscribers != null)
			for (int i=0; i<subscribers.size(); i++)
				subscribers.get(i).sendMessage(bytesToSend);
		
		// send to replay subscribers
		for (int i=0; i<replaySubscribers.size(); i++)
			replaySubscribers.get(i).sendMessageSync(bytesToSend);

	}

	@Override
	public void onQuote(Quote quote) {
		ArrayList<MDSSubscriber> subscribers = subscriptions.get(quote.SecurityId);
		
		if (subscribers == null && replaySubscribers.size() == 0)
			return;
		
		quote.Security = dictionaryIdToSecCode.get(quote.SecurityId);
		
		// serializing
		ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(quote);
			oos.close();
		} catch (IOException e) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: onQuote - error occurred while serializing object");
			e.printStackTrace();
		}
		byte[] bytesToSend = bos.toByteArray();
		
		// send to subscribers of certain securities
		if (subscribers != null)
			for (int i=0; i<subscribers.size(); i++)
				subscribers.get(i).sendMessage(bytesToSend);

		
		// send to replay subscribers
		for (int i=0; i<replaySubscribers.size(); i++)
			replaySubscribers.get(i).sendMessageSync(bytesToSend);
	}

	@Override
	public void onQuoteMatch(QuoteMatch qm) {
		ArrayList<MDSSubscriber> subscribers = subscriptions.get(qm.SecurityId);
		
		if (subscribers == null && replaySubscribers.size() == 0)
			return;
		
		qm.Security = dictionaryIdToSecCode.get(qm.SecurityId);
		
		byte[] bytesToSend = serializeIt(qm);
		
		if (bytesToSend==null)
			return;
		
		// send to subscribers of certain securities
		if (subscribers != null)
			for (int i=0; i<subscribers.size(); i++)
				subscribers.get(i).sendMessage(bytesToSend);
		
		// send to replay subscribers
		for (int i=0; i<replaySubscribers.size(); i++)
			replaySubscribers.get(i).sendMessageSync(bytesToSend);
	}

	private byte[] serializeIt(Object qm) {
		// serializing
		ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(qm);
			oos.close();
		} catch (IOException e) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: serializeIt - error occurred while serializing object");
			e.printStackTrace();
			return null;
		}
		byte[] bytesToSend = bos.toByteArray();
		return bytesToSend;
	}

	public HashMap<String, Integer> getDictionary() {
		HashMap<String, Integer> r;
		synchronized(dictionarySecCodeToId)
		{
			r = new HashMap<String, Integer>(dictionarySecCodeToId);
			dictionarySecCodeToId.notify();
		}
		
		return r;
	}

	@Override
	public void countMessage() {
		messageCounter++;
	}

	public long getMessageCount() {
		return messageCounter;
	}
	
	public void resetMessageCount() {
		messageCounter = 0;
	}

	@Override
	public void onReplayDateStart(String replayDate) {
		System.out.println("[DEBUG][T" + Thread.currentThread().getId() + "]: onReplayDateStart - starting replaying date " + replayDate);
		
		// clear dictionaries before start
		dictionarySecCodeToId.clear();
		dictionaryIdToSecCode.clear();
		
		ReplayDateStart rds = new ReplayDateStart();
		rds.date = replayDate;

		byte[] bytesToSend = serializeIt(rds);
		
		if (bytesToSend==null)
			return;
		
		// send to replay subscribers
		for (int i=0; i<replaySubscribers.size(); i++)
			replaySubscribers.get(i).sendMessageSync(bytesToSend);
	}

	@Override
	public void onReplayDateFinish(String replayDate) {
		System.out.println("[DEBUG][T" + Thread.currentThread().getId() + "]: onReplayDateFinish - has replayed date " + replayDate);		
		
		ReplayDateEnd rde = new ReplayDateEnd();
		rde.date = replayDate;

		byte[] bytesToSend = serializeIt(rde);
		
		if (bytesToSend==null)
			return;
		
		// send to replay subscribers
		for (int i=0; i<replaySubscribers.size(); i++)
			replaySubscribers.get(i).sendMessageSyncWithFlush(bytesToSend);
	}

	@Override
	public void onQuoteFull(QuoteFull qf) {
		ArrayList<MDSSubscriber> subscribers = subscriptions.get(qf.SecurityId);

		if (subscribers == null && replaySubscribers.size() == 0)
			return;

		qf.Security = dictionaryIdToSecCode.get(qf.SecurityId);

		byte[] bytesToSend = serializeIt(qf);
		
		if (bytesToSend==null)
			return;
		
		// send to subscribers of certain securities
		if (subscribers != null)
			for (int i=0; i<subscribers.size(); i++)
				subscribers.get(i).sendMessage(bytesToSend);
		
		// send to replay subscribers
		for (int i=0; i<replaySubscribers.size(); i++)
			replaySubscribers.get(i).sendMessageSync(bytesToSend);
	}

	@Override
	public void onOffMktTrade(OffMktTrade trade) {
		ArrayList<MDSSubscriber> subscribers = subscriptions.get(trade.SecurityID);
		
		if (subscribers == null && replaySubscribers.size() == 0)
			return;

		trade.Security = dictionaryIdToSecCode.get(trade.SecurityID);

		// serializing
		ByteArrayOutputStream bos = new ByteArrayOutputStream() ;
		ObjectOutputStream oos;
		try {
			oos = new ObjectOutputStream(bos);
			oos.writeObject(trade);
			oos.close();
		} catch (IOException e) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: onOffMktTrade - error occurred while serializing object");
			e.printStackTrace();
		}

		byte[] bytesToSend = bos.toByteArray();

		// send to subscribers of certain securities
		// TODO need to remove this check by having empty array for securities having no subscriptions
		if (subscribers!=null)
			for (int i=0; i<subscribers.size(); i++)
				subscribers.get(i).sendMessage(bytesToSend);
		
		// send to replay subscribers
		for (int i=0; i<replaySubscribers.size(); i++) {
			//subscribers.get(i).notifyOnTrade(trade);
			replaySubscribers.get(i).sendMessageSync(bytesToSend);
		}		
	}
}
