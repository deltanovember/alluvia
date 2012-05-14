package com.alluvial.mds.contract;

import java.io.Serializable;

public class Quote implements Serializable {
	private static final long serialVersionUID = ContractHelper.svnRevToLong("$Rev: 167 $");
	
	public Quote(int securityId, char bidOrAsk, double price, long num,
			double volume, String dataSourceName, long updateTime, long UpdateTimeNS) {
		super();
		SecurityId = securityId;
		BidOrAsk = bidOrAsk;
		Price = price;
		Num = num;
		Volume = volume;
		DataSourceName = dataSourceName;
		this.UpdateTime = updateTime;
		this.UpdateTimeNS = UpdateTimeNS;
	}
	
	public int 		SecurityId;
	public String	Security;
	public char 	BidOrAsk;
	public double 	Price;
	public long 	Num;
	public double 	Volume;
	public String 	DataSourceName;
	public long 	UpdateTime;
    public long		UpdateTimeNS;

	@Override
	public String toString() {
		return "Quote [BidOrAsk=" + BidOrAsk + ", DataSourceName="
				+ DataSourceName + ", Num=" + Num + ", Price=" + Price
				+ ", Security=" + Security + ", SecurityId=" + SecurityId
				+ ", UpdateTimeNS=" + UpdateTimeNS + ", Volume=" + Volume
				+ ", updateTime=" + ContractHelper.dateFormat.format(UpdateTime) + "]";
	}
}
