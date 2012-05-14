package com.alluvialtrading.data;

public class Benchmark {

	private String security = null;
	private String date = null;
	private double open = 0;
	private double close = 0;
	private double preclose = 0;
	private double high = 0;
	private double low = 0;
	private int tcount = 0;
	private int volume = 0;
	private double value = 0;
	public Benchmark(String security, String date, double open, double close,
			double preclose, double high, double low, int tcount, int volume,
			double value) {
		super();
		this.security = security;
		this.date = date;
		this.open = open;
		this.close = close;
		this.preclose = preclose;
		this.high = high;
		this.low = low;
		this.tcount = tcount;
		this.volume = volume;
		this.value = value;
	}
	public String getSecurity() {
		return security;
	}
	public void setSecurity(String security) {
		this.security = security;
	}
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
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
	public double getPreclose() {
		return preclose;
	}
	public void setPreclose(double preclose) {
		this.preclose = preclose;
	}
	public double getHigh() {
		return high;
	}
	public void setHigh(double high) {
		this.high = high;
	}
	public double getLow() {
		return low;
	}
	public void setLow(double low) {
		this.low = low;
	}
	public int getTcount() {
		return tcount;
	}
	public void setTcount(int tcount) {
		this.tcount = tcount;
	}
	public int getVolume() {
		return volume;
	}
	public void setVolume(int volume) {
		this.volume = volume;
	}
	public double getValue() {
		return value;
	}
	public void setValue(double value) {
		this.value = value;
	}
	
	

}
