package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

// login rejected packet. PacketType = IDF_PKT_SERVER_LOGIN_REJECTED
public class LoginRejectedPacketPayload
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

	public char Reason;		// one of the values defined below.
}
