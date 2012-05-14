package com.alluvial.mds.contract;

import java.io.Serializable;

public class Trade implements Serializable {
	private static final long serialVersionUID = ContractHelper.svnRevToLong("$Rev: 167 $");

	public Integer SecurityID; 
	public String  Security;

    public short SellerId;
    public long SellerOrderId;
    public short BuyerId;
    public long BuyerOrderId;
    public int TradeNo;
    public double TradeValue;
    public double TradeVolume;
    public double TradePrice;
    public long TradeTime;
    public int ActionFlag;
    public String ConditionCodes;
    public long UpdateTime;
    public long UpdateTimeNS;

	public Trade(Integer securityID, short sellerId,
			long sellerOrderId, short buyerId, long buyerOrderId, int tradeNo,
			double tradeValue, double tradeVolume, double tradePrice,
			long tradeTime, int actionFlag, String conditionCodes,
			long UpdateTime, long UpdateTimeNS) {
		super();
		SecurityID = securityID;
		SellerId = sellerId;
		SellerOrderId = sellerOrderId;
		BuyerId = buyerId;
		BuyerOrderId = buyerOrderId;
		TradeNo = tradeNo;
		TradeValue = tradeValue;
		TradeVolume = tradeVolume;
		TradePrice = tradePrice;
		TradeTime = tradeTime;
		ActionFlag = actionFlag;
		ConditionCodes = conditionCodes;
		this.UpdateTime = UpdateTime;
		this.UpdateTimeNS = UpdateTimeNS;
	}

	@Override
	public String toString() {
		return "Trade [ActionFlag=" + ActionFlag + ", BuyerId=" + BuyerId
				+ ", BuyerOrderId=" + BuyerOrderId + ", ConditionCodes="
				+ ConditionCodes + ", Security=" + Security + ", SecurityID="
				+ SecurityID + ", SellerId=" + SellerId + ", SellerOrderId="
				+ SellerOrderId + ", TradeNo=" + TradeNo + ", TradePrice="
				+ TradePrice + ", TradeTime=" + ContractHelper.dateFormat.format(TradeTime) + ", TradeValue="
				+ TradeValue + ", TradeVolume=" + TradeVolume + ", UpdateTime="
				+ ContractHelper.dateFormat.format(UpdateTime) + ", UpdateTimeNS=" + UpdateTimeNS + "]";
	}
}
