package com.alluvial.mds.contract;

import java.io.Serializable;

public class QuoteFull implements Serializable
{
	private static final long serialVersionUID = ContractHelper.svnRevToLong("$Rev: 167 $");

	public String 	Security;
	public int 	  	SecurityId;	   // TODO: this is to be removed, since the clients should not know IRESS security IDs
	
	public short 	Timezone;      // the GMT offset of the market, in minutes.
    public String 	QuotationBasis;
    public long 	SecurityType;
    
    public double 	OpenPrice;		// opening price
    public double 	HighPrice;		// today's high price
    public double 	LowPrice;		// today's low price
    public double 	ClosePrice;		// close price
    public String 	StatusNotes;
    
    public char 	SecurityStatus;
    
    // Bid part
    public double 	BidPrice;
    public long 	BidNum;
    public double 	BidVolume;
    public String 	BidDataSourceName;
    
    // Ask part
    public double 	AskPrice;
    public long 	AskNum;
    public double 	AskVolume;
    public String 	AskDataSourceName;
    
    // Trade part
    public double 	LastPrice;    	// last Trade price.
    public long 	NumOfTrades;
    public long 	TradeTime;
    public long 	TradeTimeNS;

    public double 	MktValue;     	// char MktValue[16];
    public double 	MktVolume;
    public double 	CumValue;     	// Total value traded for the day.
    public double 	CumVolume;		// Total volume traded for the day
    
    // Match part
    public double 	MatchVolume;
    public double 	SurplusVolume;		// The Surplus Volume indicates what quantity would be remaining in the market after the share opens at the Match Price. 
    public double 	IndicativePrice;	// Bid or offer price provided by way of information rather than as the level at which a trader is willing to Trade.
    
	public long 	UpdateTime;
    public long		UpdateTimeNS;
    
	public QuoteFull(int securityId, short timezone,
			String quotationBasis, long securityType, double openPrice,
			double highPrice, double lowPrice, double closePrice,
			String statusNotes, char securityStatus, double bidPrice,
			long bidNum, double bidVolume, String bidDataSourceName,
			double askPrice, long askNum, double askVolume,
			String askDataSourceName, double lastPrice, long numOfTrades,
			long tradeTime, long tradeTimeNS, double mktValue,
			double mktVolume, double cumValue, double cumVolume,
			double matchVolume, double surplusVolume, double indicativePrice,
			long updateTime, long updateTimeNS) {
		super();
		SecurityId = securityId;
		Timezone = timezone;
		QuotationBasis = quotationBasis;
		SecurityType = securityType;
		OpenPrice = openPrice;
		HighPrice = highPrice;
		LowPrice = lowPrice;
		ClosePrice = closePrice;
		StatusNotes = statusNotes;
		SecurityStatus = securityStatus;
		BidPrice = bidPrice;
		BidNum = bidNum;
		BidVolume = bidVolume;
		BidDataSourceName = bidDataSourceName;
		AskPrice = askPrice;
		AskNum = askNum;
		AskVolume = askVolume;
		AskDataSourceName = askDataSourceName;
		LastPrice = lastPrice;
		NumOfTrades = numOfTrades;
		TradeTime = tradeTime;
		TradeTimeNS = tradeTimeNS;
		MktValue = mktValue;
		MktVolume = mktVolume;
		CumValue = cumValue;
		CumVolume = cumVolume;
		MatchVolume = matchVolume;
		SurplusVolume = surplusVolume;
		IndicativePrice = indicativePrice;
		UpdateTime = updateTime;
		UpdateTimeNS = updateTimeNS;
	}

	@Override
	public String toString() {
		return "QuoteFull [AskDataSourceName=" + AskDataSourceName
				+ ", AskNum=" + AskNum + ", AskPrice=" + AskPrice
				+ ", AskVolume=" + AskVolume + ", BidDataSourceName="
				+ BidDataSourceName + ", BidNum=" + BidNum + ", BidPrice="
				+ BidPrice + ", BidVolume=" + BidVolume + ", ClosePrice="
				+ ClosePrice + ", CumValue=" + CumValue + ", CumVolume="
				+ CumVolume + ", HighPrice=" + HighPrice + ", IndicativePrice="
				+ IndicativePrice + ", LastPrice=" + LastPrice + ", LowPrice="
				+ LowPrice + ", MatchVolume=" + MatchVolume + ", MktValue="
				+ MktValue + ", MktVolume=" + MktVolume + ", NumOfTrades="
				+ NumOfTrades + ", OpenPrice=" + OpenPrice
				+ ", QuotationBasis=" + QuotationBasis + ", Security="
				+ Security + ", SecurityId=" + SecurityId + ", SecurityStatus="
				+ SecurityStatus + ", SecurityType=" + SecurityType
				+ ", StatusNotes=" + StatusNotes + ", SurplusVolume="
				+ SurplusVolume + ", Timezone=" + Timezone + ", TradeTime="
				+ TradeTime + ", TradeTimeNS=" + TradeTimeNS + ", UpdateTime="
				+ UpdateTime + ", UpdateTimeNS=" + UpdateTimeNS + "]";
	}
}
