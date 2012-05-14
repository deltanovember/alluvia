package com.alluvialtrading.data;

import com.alluvialtrading.tools.TraderLib;
import java.util.Date;


public class Quote {
	
	protected TraderLib lib = new TraderLib();
	
	private int bidVolume;
	private int askVolume;
	private double bid;
	private double ask;
	private Date dateTime;
	private String security;


	public Quote(Date dateTime, String security, int bidVolume, int askVolume,
			double bid, double ask) {
		super();
		this.dateTime = dateTime;
		this.bidVolume = bidVolume;
		this.askVolume = askVolume;
		this.bid = bid;
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
	
	public double getBid() {		
		return lib.round4(bid);
	}
	public void setBid(double bid) {
		this.bid = lib.round4(bid);
	}
	
	public double getAsk() {
		return lib.round4(ask);
	}
	public void setAsk(double ask) {
		this.ask = lib.round4(ask);
	}

	// Custom
	public double getBidValue() {
		return lib.round2(bid * bidVolume);
	}
	public double getAskValue() {
		return lib.round2(ask * askVolume);
	}
	public double getMid() {
		return lib.round4((bid + ask) / 2);
	}
	public double getSpread() {
		return lib.round4(ask - bid);
	}

	
	public Date getDateTime() {
		return dateTime;
	}
	public void setDateTime(Date dateTime) {
		this.dateTime = dateTime;
	}
	
	
	public String getSecurity() {
		return security;
	}

	public void setSecurity(String security) {
		this.security = security;
	}

	
}
