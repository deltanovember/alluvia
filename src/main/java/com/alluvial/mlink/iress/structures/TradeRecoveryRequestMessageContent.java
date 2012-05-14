package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class TradeRecoveryRequestMessageContent {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

	public int SecurityId;	// the unique security id, specified by SecurityInfoMessage in previous session. Should we use security code instead?
	public short SessionId;	// the session id the following trade no belongs to.
	public int TradeNo;		// the largest trade no of trade messages the client has ever received, specifying the recovery point.
}
