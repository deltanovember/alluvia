package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class DataResponseMessage
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");
	
    public FeedMessageHeader DataMsgHeader;
    public Object DataMsgBody;  // this is one of the message body/parts defined below.
	
    @Override
	public String toString() {
		return "DataResponseMessage [DataMsgBody=" + DataMsgBody + ", DataMsgHeader=" + DataMsgHeader + "]";
	}
}