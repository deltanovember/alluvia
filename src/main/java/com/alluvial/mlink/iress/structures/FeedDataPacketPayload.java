package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class FeedDataPacketPayload
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

    public String MsgType;
	public Object MsgContent;   // this is one of the general information messages (xxxxMessageContent), or a data response message (DataResponseMessage), as defined below.
	
	@Override
	public String toString() {
		return "FeedDataPacketPayload [T" + Thread.currentThread().getId() + "] [MsgContent=" + MsgContent + ", MsgType="
				+ MsgType + "]";
	}
}
