package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class DepthConsolidatedOrderMsgBody
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
    public int OrderCount;	// number of orders at this price level

    @Override
	public String toString() {
		return "DepthConsolidatedOrderMsgBody [Action=" + Action
				+ ", BidOrAsk=" + BidOrAsk + ", OrderCount=" + OrderCount
				+ ", OrderType=" + OrderType + ", Price=" + Price
				+ ", SortKey=" + SortKey + ", SortSubKey=" + SortSubKey
				+ ", Volume=" + Volume + "]";
	}
}
