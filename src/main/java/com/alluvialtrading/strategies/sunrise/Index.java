package com.alluvialtrading.strategies.sunrise;

import java.util.Date;

public class Index {
	
	private Date date;
	private double open;
	private double close;
	public Index(Date date, double open, double close) {
		super();
		this.date = date;
		this.open = open;
		this.close = close;
	}
	public Date getDate() {
		return date;
	}
	public void setDate(Date date) {
		this.date = date;
	}
	public double getOpen() {
		return open;
	}
	public void setOpen(double open) {
		this.open = open;
	}
	public double getClose() {
		return close;
	}
	public void setClose(double close) {
		this.close = close;
	}

}
