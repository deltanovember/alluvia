package com.alluvial.mds.contract;

import java.io.Serializable;

public class OffMktTrade implements Serializable {
	private static final long serialVersionUID = ContractHelper.svnRevToLong("$Rev: 167 $");

	public Integer SecurityID; 
	public String  Security;
    public short SellerId;
    public short BuyerId;
    public int TradeNo;
    public double TradeValue;
    public double TradeVolume;
    public double TradePrice;
    public long TradeTime;
    public String ConditionCodes;
    public long UpdateTime;
    public long UpdateTimeNS;

    public OffMktTrade(Integer securityID, short sellerId,
			short buyerId, int tradeNo, double tradeValue, double tradeVolume,
			double tradePrice, long tradeTime, String conditionCodes,
			long updateTime, long updateTimeNS) {
		super();
		SecurityID = securityID;
		SellerId = sellerId;
		BuyerId = buyerId;
		TradeNo = tradeNo;
		TradeValue = tradeValue;
		TradeVolume = tradeVolume;
		TradePrice = tradePrice;
		TradeTime = tradeTime;
		ConditionCodes = conditionCodes;
		UpdateTime = updateTime;
		UpdateTimeNS = updateTimeNS;
	}

	@Override
	public String toString() {
		return "OffMktTrade [BuyerId=" + BuyerId + ", ConditionCodes="
				+ ConditionCodes + ", Security=" + Security + ", SecurityID="
				+ SecurityID + ", SellerId=" + SellerId + ", TradeNo="
				+ TradeNo + ", TradePrice=" + TradePrice + ", TradeTime="
				+ TradeTime + ", TradeValue=" + TradeValue + ", TradeVolume="
				+ TradeVolume + ", UpdateTime=" + ContractHelper.dateFormat.format(UpdateTime)
				+ ", UpdateTimeNS=" + UpdateTimeNS + "]";
	}
}
