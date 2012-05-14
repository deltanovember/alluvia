package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class TradeBrokerUpdateMsgBody
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 166 $");

    public char BidOrAsk;			// Buyer or seller.
	public short BrokerId;			// the id of the seller or buyer.
	public int TradeNo;			// the Trade no assigned to this trace cancellation transaction. must NOT be used as a recovery point.
	public long TradeTime;
	
	@Override
	public String toString() {
		return "TradeBrokerUpdateMsgBody [BidOrAsk=" + BidOrAsk + ", BrokerId="
				+ BrokerId + ", TradeNo=" + TradeNo + ", TradeTime="
				+ TradeTime + "]";
	}
}
