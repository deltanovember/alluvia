package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class TradeMsgBody
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 164 $");

    public short SellerId;
    public long SellerOrderId;
    public short BuyerId;
    public long BuyerOrderId;
    public int TradeNo;
    public double TradeValue;
    public double TradeVolume;
    public double TradePrice;
    public long TradeTimeNS;
    public long TradeTime;
    public int ActionFlag;
    public String ConditionCodes;
	
    @Override
	public String toString() {
		return "TradeMsgBody [ActionFlag=" + ActionFlag + ", BuyerId="
				+ BuyerId + ", BuyerOrderId=" + BuyerOrderId
				+ ", ConditionCodes=" + ConditionCodes + ", SellerId="
				+ SellerId + ", SellerOrderId=" + SellerOrderId + ", TradeNo="
				+ TradeNo + ", TradePrice=" + TradePrice + ", TradeTime="
				+ TradeTime + ", TradeValue=" + TradeValue + ", TradeVolume="
				+ TradeVolume + "]";
	}
}