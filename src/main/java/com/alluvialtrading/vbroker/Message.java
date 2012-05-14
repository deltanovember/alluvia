package com.alluvialtrading.vbroker;

import java.util.Date;

public class Message {
	
	private int transactionId;
	private String type;
	private Date date;
	private String security;
	private double bid;
	private double ask;
	private int bidVolume;
	private int askVolume;
	private double tradePrice;
	private int tradeVolume;
	public int getTransactionId() {
		return transactionId;
	}
	public void setTransactionId(int transactionId) {
		this.transactionId = transactionId;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public String getSecurity() {
		return security;
	}
	public void setSecurity(String security) {
		this.security = security;
	}
	public double getBid() {
		return bid;
	}
	public void setBid(double bid) {
		this.bid = bid;
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
	public double getTradePrice() {
		return tradePrice;
	}
	public void setTradePrice(double tradePrice) {
		this.tradePrice = tradePrice;
	}
	public int getTradeVolume() {
		return tradeVolume;
	}
	public void setTradeVolume(int tradeVolume) {
		this.tradeVolume = tradeVolume;
	}
	public Message(int transactionId, String type, Date date, String security,
			double bid, double ask, int bidVolume, int askVolume,
			double tradePrice, int tradeVolume) {
		super();
		this.transactionId = transactionId;
		this.type = type;
		this.date = date;
		this.security = security;
		this.bid = bid;
		this.ask = ask;
		this.bidVolume = bidVolume;
		this.askVolume = askVolume;
		this.tradePrice = tradePrice;
		this.tradeVolume = tradeVolume;
	}
	

}
