package com.alluvial.mds.contract;

import java.io.Serializable;

public class SingleOrder implements Serializable
{
	private static final long serialVersionUID = ContractHelper.svnRevToLong("$Rev: 168 $");

	public int SecurityId;
	public String	Security;
    public char BidOrAsk;
    public long SortKey;
    public int SortSubKey;
    public char Action;
    public double Price;
    public double Volume;
    public int OrderType;
    public short BrokerNo;
    public long OrderNo;
    public long UpdateTime;
    public long UpdateTimeNS;

    @Override
	public String toString() {
		return "SingleOrder [Action=" + Action + ", BidOrAsk=" + BidOrAsk
				+ ", BrokerNo=" + BrokerNo + ", OrderNo=" + OrderNo
				+ ", OrderType=" + OrderType + ", Price=" + Price
				+ ", Security=" + Security + ", SecurityId=" + SecurityId
				+ ", SortKey=" + SortKey + ", SortSubKey=" + SortSubKey
				+ ", UpdateTime=" + ContractHelper.dateFormat.format(UpdateTime) + ", UpdateTimeNS="
				+ UpdateTimeNS + ", Volume=" + Volume + "]";
	}
}