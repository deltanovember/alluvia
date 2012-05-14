package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class TradeCancelMsgBody 
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

    public TradeMsgBody tradeCancelled; // the Trade that has been cancelled.
    public int CancelTradeNo;	// the Trade no assigned to this trace cancellation transaction. must NOT be used as a recovery point.
	
    @Override
	public String toString() {
		return "TradeCancelMsgBody [CancelTradeNo=" + CancelTradeNo
				+ ", tradeCancelled=" + tradeCancelled + "]";
	}
}
