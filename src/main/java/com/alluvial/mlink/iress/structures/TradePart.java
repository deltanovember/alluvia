package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class TradePart
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 164 $");

    public double 	LastPrice;    	// last Trade price.
    public long 	NumOfTrades;
    public long 	TradeTimeNS;
    public long 	TradeTime;

    public double 	MktValue;     	// char MktValue[16];
    public double 	MktVolume;
    public double 	CumValue;     	// Total value traded for the day.
    public double 	CumVolume;		// Total volume traded for the day
	
    @Override
	public String toString() {
		return "TradePart [CumValue=" + CumValue + ", CumVolume=" + CumVolume
				+ ", LastPrice=" + LastPrice + ", MktValue=" + MktValue
				+ ", MktVolume=" + MktVolume + ", NumOfTrades=" + NumOfTrades
				+ ", TradeTime=" + TradeTime + "]";
	}
}
