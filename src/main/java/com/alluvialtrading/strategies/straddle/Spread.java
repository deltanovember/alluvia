package com.alluvialtrading.strategies.straddle;

import java.util.Date;


public class Spread implements Comparable<Spread> {
	
	private String security;
	private double price;
	private double tick;
	private double value;
	private int volume;
	private int numberTrades;
	private double ask;
	private double bid;

	public int compareTo(Spread spread) {
		
		if (numberTrades > spread.getNumberTrades()) {
			return -1;
		}
		else if (numberTrades < spread.getNumberTrades()) {
			return 1;
		}
		else {
			return 0;
		}

	}
	
	public double getSpread() {
		return (ask - bid) / bid * 100;
	}

	public String getSecurity() {
		return security;
	}

	public void setSecurity(String security) {
		this.security = security;
	}

	public double getPrice() {
		return price;
	}

	public void setPrice(double price) {
		this.price = price;
	}

	public double getTick() {
		return tick;
	}

	public void setTick(double tick) {
		this.tick = tick;
	}

	public double getValue() {
		return value;
	}

	public void setValue(double value) {
		this.value = value;
	}

	public int getVolume() {
		return volume;
	}

	public void setVolume(int volume) {
		this.volume = volume;
	}

	public int getNumberTrades() {
		return numberTrades;
	}

	public void setNumberTrades(int numberTrades) {
		this.numberTrades = numberTrades;
	}

	public double getAsk() {
		return ask;
	}

	public void setAsk(double ask) {
		this.ask = ask;
	}

	public double getBid() {
		return bid;
	}

	public void setBid(double bid) {
		this.bid = bid;
	}

	public Spread(String security, double price, double tick, double value,
			int volume, int numberTrades, double ask, double bid) {
		super();
		this.security = security;
		this.price = price;
		this.tick = tick;
		this.value = value;
		this.volume = volume;
		this.numberTrades = numberTrades;
		this.ask = ask;
		this.bid = bid;
	}


	
	

}

