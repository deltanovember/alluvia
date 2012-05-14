package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class DataSourceInfoMessageContent
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");	
	
    public String Exchange;	    // exchange name
    public String DataSource;   // data SubscriptionSource name
    public short  DataSourceId;	// the id assigned to this data SubscriptionSource, unique within the exchange.
	
    @Override
	public String toString() {
		return "DataSourceInfoMessageContent [DataSource=" + DataSource
				+ ", DataSourceId=" + DataSourceId + ", Exchange=" + Exchange
				+ "]";
	}
}
