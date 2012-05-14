package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class ExchangeInfoMessageContent
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 164 $");

    public String   Exchange;	    // exchange name
    public String   DataSource;		// data SubscriptionSource name
    public String   Status;	    	// status of the exchange
    public long 	ExchangeTime;	// exchange time
    public short	ErrorCode;
	
    @Override
	public String toString() {
		return "ExchangeInfoMessageContent [DataSource=" + DataSource
				+ ", ErrorCode=" + ErrorCode + ", Exchange=" + Exchange
				+ ", ExchangeTime=" + ExchangeTime + ", Status=" + Status + "]";
	}				
}
