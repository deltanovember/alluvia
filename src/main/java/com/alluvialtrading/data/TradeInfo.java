package com.alluvialtrading.data;

import java.util.Date;

/**
 * Hack class to store derived trade info
 * @author dnguyen
 *
 */

public class TradeInfo extends Trade {
	
	private String date	= null;
	private String security	= null;
	private int instrument = -1;
	private String market = null;	
	private int eventId = -1;
	private Date eventTime = null;
	private double highPrice = -1;
	private double lowPrice = -1;
	private double refPrice = -1;
	private double refSpread = -1;
	private double refVolume = -1;
	private int refTcount = -1;
	private double bid = -1;
	private double ask = -1;
	private int bidVolume = -1;
	private int askVolume = -1;
	private double bidFive = -1;
	private double askFive = -1;
	private double bidFifteen = -1;
	private double askFifteen = -1;
	private double bidThirty = -1;
	private double askThirty = -1;
	private int type = -1;
	
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}

	public void setDate(String date) {
		this.date = date;
	}
	public String getSecurity() {
		return security;
	}
	public void setSecurity(String security) {
		this.security = security;
	}
	public int getInstrument() {
		return instrument;
	}
	public void setInstrument(int instrument) {
		this.instrument = instrument;
	}
	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		this.market = market;
	}
	public int getEventId() {
		return eventId;
	}
	public void setEventId(int eventId) {
		this.eventId = eventId;
	}
	public Date getEventTime() {
		return eventTime;
	}
	public void setEventTime(Date eventTime) {
		this.eventTime = eventTime;
	}
	public double getHighPrice() {
		return highPrice;
	}
	public void setHighPrice(double highPrice) {
		this.highPrice = highPrice;
	}
	public double getLowPrice() {
		return lowPrice;
	}
	public void setLowPrice(double lowPrice) {
		this.lowPrice = lowPrice;
	}
	public double getRefPrice() {
		return refPrice;
	}
	public void setRefPrice(double refPrice) {
		this.refPrice = refPrice;
	}
	public double getRefSpread() {
		return refSpread;
	}
	public void setRefSpread(double refSpread) {
		this.refSpread = refSpread;
	}
	public double getRefVolume() {
		return refVolume;
	}
	public void setRefVolume(double refVolume) {
		this.refVolume = refVolume;
	}
	public int getRefTcount() {
		return refTcount;
	}
	public void setRefTcount(int refTcount) {
		this.refTcount = refTcount;
	}
	public double getBid() {
		return bid;
	}
	public void setBid(double bid) {
		this.bid = bid;
	}
	
	public int compareTo(TradeInfo info) {
		if (info.getEventTime().after(eventTime)) {
			return -1;
		}
		else if (info.getEventTime().before(eventTime)) {
			return 1;
		}
		else {
			return 0;
		}

	}
	 
	 
	public double getAsk() {
		return ask;
	}
	public void setAsk(double ask) {
		this.ask = ask;
	}
	public int getBidVolume() {
		return bidVolume;
	}
	public void setBidVolume(int bidVolume) {
		this.bidVolume = bidVolume;
	}
	public int getAskVolume() {
		return askVolume;
	}
	public void setAskVolume(int askVolume) {
		this.askVolume = askVolume;
	}
	public double getBidFive() {
		return bidFive;
	}
	public void setBidFive(double bidFive) {
		this.bidFive = bidFive;
	}
	public double getAskFive() {
		return askFive;
	}
	public void setAskFive(double askFive) {
		this.askFive = askFive;
	}
	public double getBidFifteen() {
		return bidFifteen;
	}
	public void setBidFifteen(double bidFifteen) {
		this.bidFifteen = bidFifteen;
	}
	public double getAskFifteen() {
		return askFifteen;
	}
	public void setAskFifteen(double askFifteen) {
		this.askFifteen = askFifteen;
	}
	public double getBidThirty() {
		return bidThirty;
	}
	public void setBidThirty(double bidThirty) {
		this.bidThirty = bidThirty;
	}
	public double getAskThirty() {
		return askThirty;
	}
	public void setAskThirty(double askThirty) {
		this.askThirty = askThirty;
	}



}
