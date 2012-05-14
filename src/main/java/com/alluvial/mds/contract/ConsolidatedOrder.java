package com.alluvial.mds.contract;

import java.io.Serializable;
import java.util.Date;

public class ConsolidatedOrder  implements Serializable
{
	private static final long serialVersionUID = ContractHelper.svnRevToLong("$Rev: 165 $");

	public int SecurityID; // TODO remove this in all contract messages
	public String Security;
    public char BidOrAsk;
    public long SortKey;
    public int SortSubKey;
    public char Action;
    public double Price;
    public double Volume;
    public int OrderType;
    public int OrderCount;
    public long UpdateTime;
    public long UpdateTimeNS;

    @Override
	public String toString() {
		return "ConsolidatedOrder [Action=" + Action + ", BidOrAsk=" + BidOrAsk
				+ ", OrderCount=" + OrderCount + ", OrderType=" + OrderType
				+ ", Price=" + Price + ", Security=" + Security
				+ ", SecurityID=" + SecurityID + ", SortKey=" + SortKey
				+ ", SortSubKey=" + SortSubKey + ", UpdateTime=" + ContractHelper.dateFormat.format(UpdateTime)
				+ ", UpdateTimeNS=" + UpdateTimeNS + ", Volume=" + Volume + "]";
	}
}
