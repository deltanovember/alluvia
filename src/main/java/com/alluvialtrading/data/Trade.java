package com.alluvialtrading.data;

import com.alluvialtrading.tools.TraderLib;
import java.util.Date;


public class Trade implements Comparable<Trade> {
	
	protected TraderLib lib = new TraderLib();
	
	private String market;
	private Date date;
	private String security;
	private int volume;
	private double price;
	
	/**
	 * YYYY-MM-DD assume exit on date
	 * int then assume exit in n seconds
	 */
	private String closeWait;
	private boolean closingTrade = false;
	private String closingStrategy = null;
	
	public final static String EXIT_AGGRESSIVE = "EXIT_AGGRESSIVE";
	public final static String EXIT_CLOSE = "EXIT_CLOSE";
	public final static String EXIT_LAST_TRADE = "EXIT_LAST_TRADE";
	public final static String EXIT_MID = "EXIT_MID";
	public final static String EXIT_PASSIVE = "EXIT_PASSIVE";
	public final static String EXIT_VWAP = "EXIT_VWAP";
	
	// Time constants
	public final static long ONE_DAY = 1000 * 60 * 60 * 24;
	
	public String getClosingStrategy() {
		return closingStrategy;
	}

	public void setClosingStrategy(String closingStrategy) {
		this.closingStrategy = closingStrategy;
	}

	public boolean isClosingTrade() {
		return closingTrade;
	}

	public void setClosingTrade(boolean closingTrade) {
		this.closingTrade = closingTrade;
	}

	public Trade() {
		
	}
	public Trade(String market, Date date, String security, int volume,
			double price, String closeWait, String closeStrategy) {
		super();
		this.market = market;
		this.date = date;
		this.security = security;
		this.volume = volume;
		this.price = price;
		this.closeWait = closeWait;
		this.closingStrategy = closeStrategy;
	}
	
	public int compareTo(Trade trade) {
		if (trade.getDate().after(date)) {
			return -1;
		}
		else if (trade.getDate().before(date)) {
			return 1;
		}
		else {
			return 0;
		}

	}

	public String getMarket() {
		return market;
	}
	public void setMarket(String market) {
		this.market = market;
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
	public int getVolume() {
		return volume;
	}
	public void setVolume(int volume) {
		this.volume = volume;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public String getCloseWait() {
		return closeWait;
	}
	public void setCloseWait(String closeWait) {
		this.closeWait = closeWait;
	}

	// Custom
	public double getValue() {
		return lib.round2(price * volume);
	}
	

}
