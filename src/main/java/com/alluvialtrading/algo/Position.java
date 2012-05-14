package com.alluvialtrading.algo;

import com.alluvialtrading.data.Trade;

public class Position {

	private Trade trade = null;
	private int volume;
	private double price;
	public Trade getTrade() {
		return trade;
	}
	public void setTradeInfo(Trade trade) {
		this.trade = trade;
	}
	
	public double getValue() {
		return volume * price;
	}
	public int getVolume() {
		return volume;
	}
	public void setVolume(int volume) {
		this.volume = volume;
	}
	public void addToPosition(int volume, double price) {
		
		// opposite directions?
		boolean opposite = volume * this.volume < 0;
		if (opposite) {
			
			if (Math.abs(volume) >= Math.abs(this.volume)) {
				// existing position is wiped out
				this.price = price;
			}
			else {
				// existing position is maintained no price change
				
			}
		}
		else {
			// calculate average price
			double totalValue = volume * price + this.volume * this.price;
			this.price = totalValue / (volume + this.volume);
		}
		
		this.volume += volume;
	}
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price = price;
	}
	public Position(Trade trade, int volume, double price) {
		super();
		this.trade = trade;
		this.volume = volume;
		this.price = price;
	}
	
	
	
}
