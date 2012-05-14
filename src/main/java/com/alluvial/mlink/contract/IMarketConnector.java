package com.alluvial.mlink.contract;

import com.alluvial.mds.common.MDSHelper;


/**
 * This is interface of IRESS market link. It is used by link users to send commands to market 
 * link and set listener in order to be called when IRESS events arrive.
 * @author erepekto
 */
public interface IMarketConnector {
	static final long Revision = MDSHelper.svnRevToLong("$Rev: 152 $");
	
	public abstract void connect() throws MCException;
	
	public abstract void login() throws MCException;

	public abstract void setListener(IMarketListener listener);
	
	public abstract void runDataReceiving() throws MCException;
	
	public abstract void waitDataReceivingFinished();

	public abstract void disconnect();
	
	// replay specific methods
	public abstract void addReplay(String date) throws MCException;
	public abstract void terminateReplays() throws MCException;
}