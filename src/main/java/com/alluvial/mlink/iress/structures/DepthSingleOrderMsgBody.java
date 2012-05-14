package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class DepthSingleOrderMsgBody
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

    public char BidOrAsk;
    public long SortKey;
    public int SortSubKey;
    public char Action;
    public double Price;
    public double Volume;
    public int OrderType;
    public short BrokerNo;
    public long OrderNo;
	
    @Override
	public String toString() {
		return "DepthSingleOrderMsgBody [Action=" + Action + ", BidOrAsk="
				+ BidOrAsk + ", BrokerNo=" + BrokerNo + ", OrderNo=" + OrderNo
				+ ", OrderType=" + OrderType + ", Price=" + Price
				+ ", SortKey=" + SortKey + ", SortSubKey=" + SortSubKey
				+ ", Volume=" + Volume + "]";
	}
}
