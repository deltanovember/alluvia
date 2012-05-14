package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class DataSourceBoardInfoMessageContent
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");
	
	public String 	Exchange;		    // exchange name
    public short 	DataSourceId;		// the id assigned to this data SubscriptionSource, unique within the exchange.
    public String 	DataSourceBoard;	// data SubscriptionSource board name
    public short 	DataSourceBoardId;	// the id assigned to this data SubscriptionSource, unique within the exchange and data SubscriptionSource.
    
    @Override
	public String toString() {
		return "DataSourceBoardInfoMessageContent [DataSourceBoard="
				+ DataSourceBoard + ", DataSourceBoardId=" + DataSourceBoardId
				+ ", DataSourceId=" + DataSourceId + ", Exchange=" + Exchange
				+ "]";
	}
}
