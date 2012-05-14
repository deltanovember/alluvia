package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class QuoteFullMsgBody
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

    public short Timezone;      // the GMT offset of the market, in minutes.
    public String QuotationBasis;
    public long SecurityType;

    public double OpenPrice;	// opening price
    public double HighPrice;	// today's high price
    public double LowPrice;		// today's low price
    public double ClosePrice;	// close price
    public String StatusNotes;

    public StatusPart SecurityStatus;
    public BidAskPart Bid;
    public BidAskPart Ask;
    public TradePart Trade;
    public MatchPart Match;
	
    @Override
	public String toString() {
		return "QuoteFullMsgBody [Ask=" + Ask + ", Bid=" + Bid
				+ ", ClosePrice=" + ClosePrice + ", HighPrice=" + HighPrice
				+ ", LowPrice=" + LowPrice + ", Match=" + Match
				+ ", OpenPrice=" + OpenPrice + ", QuotationBasis="
				+ QuotationBasis + ", SecurityStatus=" + SecurityStatus
				+ ", SecurityType=" + SecurityType + ", StatusNotes="
				+ StatusNotes + ", Timezone=" + Timezone + ", Trade=" + Trade
				+ "]";
	}
}
