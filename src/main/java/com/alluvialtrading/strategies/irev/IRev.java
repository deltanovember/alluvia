/**
 * Intraday Reversion strategy
 */

package com.alluvialtrading.strategies.irev;

import com.alluvialtrading.algo.BaseAlgo;
import com.alluvialtrading.algo.Position;
import com.alluvialtrading.data.TradeInfo;
import com.alluvialtrading.tools.TraderLib;

import java.io.File;

import java.text.NumberFormat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Locale;

public class IRev extends BaseAlgo {
	
	TraderLib lib = new TraderLib();
	
	// Control trading strategies
	private boolean naive = false;
	
	
	// ************************************************************
	// Algo parameters
	
	// When are trades considered too close together? In minutes
	private static final int RECENT_TRADE_THRESHOLD = 10;
	
	// Maximum  high low volatility %
	private static final double MAX_VOLATILITY = 20;
	
	// Cap tradings size
	private static final double MAX_TRADE_VALUE = 100000;
	private static final double MIN_TRADE_VALUE = 10000;
	
	// Avoid 10 mins from close
	private final int END_OF_DAY_BUFFER = 10;
	
	// how many multiples of the spread
	private static final double SPREAD_THRESHOLD = 3;
		
	// absolute change in basis points - measure of price velocity
	private static final double BASIS_POINT_THRESHOLD = 50;
	
	// ************************************************************
	
	double cash = MAX_TRADE_VALUE;
	double shares = 0;
	Hashtable<String, Position> openPositions = new Hashtable<String, Position>();

	
	// ************************************************************
	// Data structures
	ArrayList<TradeInfo> allTrades = null;
	
	// Store trade counts by security code
	Hashtable<String, Integer> tradeCount = null;
	
	// Store last trade
	Hashtable<String, TradeInfo> lastTrade = null;	
	
	// Intra day high lows
	Hashtable<String, Double> intraDayLows = null;
	Hashtable<String, Double> intraDayHighs = null;
		
	// min trades before considering trade
	private static final int MIN_TRADES = 20;
	
	// ************************************************************
	
	// Constants
	private static final int BUY = 0;
	private static final int SELL = 1;
	private int ASK = -1;
	private int ASK_5 = -1;
	private int ASK_15 = -1;
	private int ASK_30 = -1;
	private int ASKVOL = -1;
	private int BID = -1;
	private int BID_5 = -1;
	private int BID_15 = -1;
	private int BID_30 = -1;
	private int BIDVOL = -1;
	private int DATE = -1;
	private int EVENT_ID = -1;
	private int EVENT_TIME = -1;
	private int LOW_PRICE = -1;
	private int HIGH_PRICE = -1;
	private int INSTRUMENT = -1;
	private int MARKET = -1;
	private int REF_PRICE = -1;
	private int REF_SPREAD = -1;
	private int REF_TCOUNT = -1;
	private int REF_VOLUME = -1;
	private int SECURITY = -1;
	
	// parameters
	private String OPEN_AUCTION_END = "10:00:00.0";
	
	private String CLOSE_AUCTION_END = "16:00:00.0";
	
	// Keep track of day changes
	private String lastDay = "";
	
	// For profit simulator
	StringBuffer simulator = new StringBuffer("market,date,event_time,security,volume,price,trade_leg\r\n");
	int totalTrades = 0;
	double totalProfit = 0;
	double turnover = 0;
	
	public static void main (String[] args) {
		String[] files = new File("data").list();
		for (String file : files) {
			new IRev(Market.ASX, file);
		}
	}
	
	public IRev(Market market, String sourceFile) {
		super(market);
		init();
		readTradeData(sourceFile);
		Collections.sort(allTrades);
		//System.out.println(allTrades.size());
	
		// Run algo
		for (int i=0; i<allTrades.size(); i++) {
			TradeInfo current = allTrades.get(i);		
			Date effectiveClose = new Date(getCloseAuctionTime(current).getTime() - END_OF_DAY_BUFFER * 60 * 1000);
			if (isValidTradingTime(current)) {
				
				boolean recentTrade = hasTradedRecently(current);
				processTrade(current);	
				
				if (naive) {
					trade(current, SELL, true);
				}
				else {
					double ask = current.getAsk();
					double bid = current.getBid();
					
					// spread in basis points
					double currentSpread = 2 * 100 * 100 * (ask - bid) / (ask + bid);
					
					if (countTrades(current.getSecurity()) > MIN_TRADES) {
						
						double refSpread = current.getRefSpread();
						
						// 1 minute VWAP
						double refPrice = current.getRefPrice();
						double refValue = current.getRefPrice() * current.getRefVolume();
						int refTcount = current.getRefTcount();

						double mid = (bid + ask)/2;
						double bidValue = current.getBidVolume() * bid;
						double askValue = current.getAskVolume() * ask;
						
						// basis point changes - measure of price velocity
						double bidPchange = 100 * 100 * Math.log(bid / refPrice);
						double askPchange = 100 * 100 * Math.log(ask / refPrice);
						double midPchange = 100 * 100 * Math.log(mid / refPrice);
						
						// changes relative to spread
						double bidPchangeRelative = Math.abs(bidPchange/refSpread);
						double askPchangeRelative = Math.abs(askPchange/refSpread);


						if (recentTrade) {
							
						}
					
						else if (getVolatility(current) > MAX_VOLATILITY) {
							
						}
						
						else if (current.getEventTime().after(effectiveClose)) {
							
						}

						else {
							
							double orderBookThreshold = 2;			
														
							// Buy pressure
							if (bidPchange >= BASIS_POINT_THRESHOLD && bidPchangeRelative >= SPREAD_THRESHOLD) {
								
								if (current.getSecurity().equals("PDN.AX")) {
									System.out.println("debug");
								}
								
								// Respect order book pressure
								if (current.getBidVolume() > orderBookThreshold * current.getAskVolume()) {
									
								}
								// bank check
								else if (Math.abs(getPrice(current, SELL) * getVolume(current, SELL)) > getCurrentCash()) {
									System.out.println("rejected bank price: " + Math.abs(getPrice(current, SELL)) + " volume: " + getVolume(current, SELL)  + "cash: " + getCurrentCash());
								}
								// min trade size
								else if (Math.abs(getPrice(current, SELL) * getVolume(current, SELL)) < MIN_TRADE_VALUE) {
									
								}
								
								// is ref vol good?
								else if (current.getRefVolume() < 15 * Math.abs(getVolume(current, SELL))) {
									
								}
								else {
									// Contrarian
									trade(current, SELL, true);
								}
								
							}
							
							// Sell pressure
							if (askPchange <= -BASIS_POINT_THRESHOLD && askPchangeRelative >= SPREAD_THRESHOLD) {
								if (current.getSecurity().equals("PDN.AX")) {
									System.out.println("debug");
								}
								// Respect order book pressure
								if (current.getAskVolume() > orderBookThreshold * current.getBidVolume()) {
									
								}
								// bank check
								else if (Math.abs(getPrice(current, BUY) * getVolume(current, BUY)) > getCurrentCash()) {
									
								}
								// min threshold
								else if (Math.abs(getPrice(current, BUY) * getVolume(current, BUY)) < MIN_TRADE_VALUE) {
									//System.out.println(Math.abs(getPrice(current, BUY) * getVolume(current, BUY)));
								}
								// is ref vol good?
								else if (current.getRefVolume() < 15 * Math.abs(getVolume(current, BUY))) {
									
								}
								else {
									// Contrarian
									trade(current, BUY, true);
								}
								
							}	
						}
							

					
					}
				}
					
			}
		}
		
		// close trailing positions
		TradeInfo dummy = new TradeInfo();
		dummy.setEventTime(new Date());
		closePositions(dummy);
		
		lib.writeFile(".", "profit_sim_" + sourceFile, simulator.toString());
		System.out.println(sourceFile + " Trades: " + totalTrades + " Profit: " + formatProfit(totalProfit) + ", RoR: " + 100 * (totalProfit/turnover));

	}

	/**
	 * Checks all open positions. If any are older than 5 minutes, close 
	 * the positions at T + 5
	 * @param info
	 */
 	private void closePositions(TradeInfo info) {

		Enumeration<String> stockCodes = openPositions.keys();
		ArrayList<TradeInfo> trades = new ArrayList<TradeInfo>();
		while (stockCodes.hasMoreElements()) {
			String stockCode = stockCodes.nextElement();
			Position position = openPositions.get(stockCode);
			
			// Is the current trade more 5mins after open position?
			Date fiveAhead = new Date(position.getTrade().getDate().getTime() + 
									5 * 60 * 1000);
			if (info.getEventTime().after(fiveAhead)) {
				
				TradeInfo close = new TradeInfo();
				int type = BUY;
				if (position.getValue() > 0) {
					type = SELL;
				}
				
				// set to needed parameters
				close.setEventTime(fiveAhead);
				close.setAsk(close.getAskFive());
				close.setBid(close.getBidFive());
				
				int bidVolume = close.getBidVolume();
				close.setBidVolume(close.getAskVolume());
				close.setAskVolume(bidVolume);
				close.setType(type);
				trades.add(close);
				//trade(position.getTradeInfo(), type, false);
				
				// close
				//openPositions.remove(stockCode);
			}

		}
		
		Collections.sort(trades);
		for (TradeInfo close : trades) {
			trade(close, close.getType(), false);
		}
 		
 	}
	
	/**
	 * @param security
	 * @return
	 */
	private int countTrades(String security) {
		if (null == tradeCount.get(security)) {
			return 0;
		}
		else {
			return tradeCount.get(security);
		}
	}
	
	private String formatProfit(double profit) {

	    NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US); 
	    String s = n.format(profit);
	    return s;
	}


	private Date getCloseAuctionTime(TradeInfo info) {
		return lib.convertSmartsDateTime(info.getDate() + " " + CLOSE_AUCTION_END);
	}
	
	@Override
	protected String getCSVHeader() {
		return "TradeNo,Security,Date,Time,Price,Volume,Brokerage,Volatility,ReturnOnRisk,Profit,Cash,Shares,Assets\r\n";
	}
	
	private double getCurrentCash() {
		return cash - lib.calculatePosition(openPositions);
	}
	
	private Date getOpenAuctionTime(TradeInfo info) {
		return lib.convertSmartsDateTime(info.getDate() + " " + OPEN_AUCTION_END);
	}
	
	/**
	 * Get position in particular stock
	 * @param security
	 * @return
	 */
	private Position getPosition(String security) {
		
		return openPositions.get(security);
		
	}
	
	private double getPrice(TradeInfo info, int type) {
		
		double price = -1;
		if (BUY == type) {
			price = info.getAsk();
		}
		else {
			price = info.getBid();
		}
		
		
		return price;
	}

	
	private double getVolatility(TradeInfo info) {

		double high = info.getHighPrice();
		double low = info.getLowPrice();

		double intraChange = 100 * (high - low) / low;
		return intraChange;
	}
	
	private int getVolume(TradeInfo info, int type) {
		
		int volume = -1;
		if (BUY == type) {
			volume = info.getAskVolume();
		}
		else {
			volume = -info.getBidVolume();
		}
		
		// If we have existing opposite positions, allow them to be closed out
		int extraVolume = 0;
		int rawExtraVolume = 0;
		if (null != openPositions.get(info.getSecurity()) &&
				openPositions.get(info.getSecurity()).getVolume() * volume < 0) {
			extraVolume = Math.abs(openPositions.get(info.getSecurity()).getVolume());
			rawExtraVolume = openPositions.get(info.getSecurity()).getVolume();
		}
		
		double price = getPrice(info, type);
		double maxPosition = MAX_TRADE_VALUE;
		if (getCurrentCash() < MAX_TRADE_VALUE) {
			maxPosition = getCurrentCash();
		}
		
		if (totalProfit < 0) {
			//maxPosition += totalProfit;
		}
		if (Math.abs(price * volume) > maxPosition) {
			if (BUY == type) {
				return (int) ((maxPosition / price));
			}
			else {
				return (int) ((-maxPosition / price));
			}
			//return volume;m
		}
		else {
			return volume;
		}
	}
	
	private boolean hasTradedRecently(TradeInfo info) {
		
		if (null == lastTrade.get(info.getSecurity())) {
			return false;
		}
		else {

			Date timeAhead = new Date(lastTrade.get(info.getSecurity()).getEventTime().getTime() + 
					RECENT_TRADE_THRESHOLD * 60 * 1000);
			boolean recent = timeAhead.after(info.getEventTime());
			if (recent) {
				//System.out.println("text");
			}
			return recent;
			
		}
	}
	
	protected void init() {
		tradeCount = new Hashtable<String, Integer>();
		lastTrade = new Hashtable<String, TradeInfo>();
		allTrades = new ArrayList<TradeInfo>();
	}
	
	private void initDay() {
		intraDayLows = new Hashtable<String, Double>();
		intraDayHighs = new Hashtable<String, Double>();
	}
	
	/**
	 * Filter dodgy trades
	 * @param info
	 * @return
	 */
	private boolean isValidTrade(TradeInfo info) {
		return info.getBidFive() > 0 &&
				info.getAskFive() > 0 &&
				info.getAsk() > info.getBid();
	}
	
	private boolean isValidTradingTime(TradeInfo info) {

		return info.getEventTime().after(getOpenAuctionTime(info)) &&
			info.getEventTime().before(getCloseAuctionTime(info));
	}
	
	/**
	 * Store trade statistics etc
	 * @param info
	 */
	private void processTrade(TradeInfo info) {
		
		// count trades by security
		String security = info.getSecurity();
		
		if (null == tradeCount.get(security)) {
			tradeCount.put(security, 1);
		}
		else {
			tradeCount.put(security, tradeCount.get(security) + 1);
		}
		
		// high and lows
		double mid = (info.getAsk() + info.getBid())/2;
		if (null == intraDayHighs.get(security)) {
			intraDayHighs.put(security, mid);
		}
		else if (mid > intraDayHighs.get(security)) {
			intraDayHighs.put(security, mid);
		}
		
		if (null == intraDayLows.get(security)) {
			intraDayLows.put(security, mid);
		}
		else if (mid < intraDayLows.get(security)) {
			intraDayLows.put(security, mid);
		}
		
	}
	
	private void readTradeData(String sourceFile) {
		
		long start = System.currentTimeMillis();
		// Read raw CSV files
		String[] rawData = lib.openFile("data", sourceFile);
		
		
		// Convert to TradeInfo objects
		for (int i=0; i<rawData.length; i++) {
			
			TradeInfo info = new TradeInfo();
			
			String[] splitLine = rawData[i].split(",");
			
			for (int column=0; column<splitLine.length; column++) {
				
				String data = splitLine[column];
				// process headers
				if (0 == i) {
					if (data.equalsIgnoreCase("date")) {
						DATE = column;
					}
					else if (data.equalsIgnoreCase("security")) {
						SECURITY = column;
					}
					else if (data.equalsIgnoreCase("instrument")) {
						INSTRUMENT = column;
					}
					else if (data.equalsIgnoreCase("market")) {
						MARKET = column;
					}
					else if (data.equalsIgnoreCase("event_id")) {
						EVENT_ID = column;
					}
					else if (data.equalsIgnoreCase("event_time")) {
						EVENT_TIME = column;
					}
					else if (data.equalsIgnoreCase("high_price")) {
						HIGH_PRICE = column;
					}
					else if (data.equalsIgnoreCase("low_price")) {
						LOW_PRICE = column;
					}
					else if (data.equalsIgnoreCase("ref_price")) {
						REF_PRICE = column;
					}
					else if (data.equalsIgnoreCase("ref_spread")) {
						REF_SPREAD = column;
					}
					else if (data.equalsIgnoreCase("ref_tcount")) {
						REF_TCOUNT = column;
					}
					else if (data.equalsIgnoreCase("ref_volume")) {
						REF_VOLUME = column;
					}
					else if (data.equalsIgnoreCase("bid_delayed")) {
						BID = column;
					}
					else if (data.equalsIgnoreCase("ask_delayed")) {
						ASK = column;
					}
					else if (data.equalsIgnoreCase("bidvol")) {
						BIDVOL = column;
					}
					else if (data.equalsIgnoreCase("askvol")) {
						ASKVOL = column;
					}
					else if (data.equalsIgnoreCase("bid_5")) {
						BID_5 = column;
					}
					else if (data.equalsIgnoreCase("ask_5")) {
						ASK_5 = column;
					}
					else if (data.equalsIgnoreCase("bid_15")) {
						BID_15 = column;
					}
					else if (data.equalsIgnoreCase("ask_15")) {
						ASK_15 = column;
					}
					else if (data.equalsIgnoreCase("bid_30")) {
						BID_30 = column;
					}
					else if (data.equalsIgnoreCase("ask_30")) {
						ASK_30 = column;
					}
				}
				else if (data.length() < 1) {
					// ignore
				}
				else if (DATE == column) {
					if (!data.equals(lastDay)) {
						lastDay = data;
						initDay();
					}
					info.setDate(data);
				}
				else if (SECURITY == column) {
					info.setSecurity(data);
				}
				else if (INSTRUMENT == column) {
					info.setInstrument(new Integer(data));
				}
				else if (MARKET == column) {
					info.setMarket(data);
				}
				else if (EVENT_ID == column) {
					info.setEventId(new Integer(data));
				}
				else if (EVENT_TIME == column) {				
					info.setEventTime(lib.convertSmartsDateTime(info.getDate() + " " + data));

				}
				else if (HIGH_PRICE == column) {
					info.setHighPrice(new Double(data));
				}
				else if (LOW_PRICE == column) {
					info.setLowPrice(new Double(data));
				}
				else if (REF_PRICE == column) {
					info.setRefPrice(new Double(data));
				}
				else if (REF_SPREAD == column) {
					info.setRefSpread(new Double(data));
				}
				else if (REF_VOLUME == column) {
					info.setRefVolume(new Integer(data));
				}
				else if (REF_TCOUNT == column) {
					info.setRefTcount(new Integer(data));
				}				
				else if (BID == column) {
					info.setBid(new Double(data));
				}
				else if (ASK == column) {
					info.setAsk(new Double(data));
				}
				else if (BIDVOL == column) {
					info.setBidVolume(new Integer(data));
				}
				else if (ASKVOL == column) {
					info.setAskVolume(new Integer(data));
				}
				else if (BID_5 == column) {
					info.setBidFive(new Double(data));
				}
				else if (ASK_5 == column) {
					info.setAskFive(new Double(data));
				}
				else if (BID_15 == column) {
					info.setBidFifteen(new Double(data));
				}
				else if (ASK_15 == column) {
					info.setAskFifteen(new Double(data));
				}
				else if (BID_30 == column) {
					info.setBidThirty(new Double(data));
				}
				else if (ASK_30 == column) {
					info.setAskThirty(new Double(data));
				}
			}
			
			if (null != info.getDate()) {
				if (isValidTrade(info)) {
					allTrades.add(info);
				}
				else {
					//System.out.println(info.getSecurity() + "," + info.getEventTime());
					//System.exit(0);
				}
				
			}

		}
		long finish = System.currentTimeMillis();
		long elapsed = (finish - start) / 1000;
		//System.out.println("read took: " + elapsed  + "s");
	}
	
	/**
	 * Store the net position in each stock for bankroll management
	 * @param info
	 * @param type
	 */
	private void storePosition(TradeInfo info, int type, int volume, double price) {	
		
		// store net position in stock
		double stockPosition = volume * price;
		if (SELL == type) {
			stockPosition = -1 * stockPosition;
		}
		
		Position currentPosition = openPositions.get(info.getSecurity());
		if (null == currentPosition) {
			// new position
			openPositions.put(info.getSecurity(), new Position(info, volume, price));
		}
		else {
			currentPosition.addToPosition(volume, price);
			if (0 == currentPosition.getVolume()) {
				openPositions.remove(info.getSecurity());
			}
		}
		

		
	}

	/**
	 * Simulate trade. Should be error checked by now
	 * @param info
	 * @param type BUY or SELL
	 * @param checkClose true for normal trades, false for closing
	 * trades
	 */
	
	private void trade(TradeInfo info, int type, boolean checkClose) {
		

		// do we need to close out any positions?
		if (checkClose) {
			closePositions(info);
		}
		totalTrades++;
		
		// store last trade
		lastTrade.put(info.getSecurity(), info);
		
		// TradeNo,Security,Date,Time,Price,Volume,Brokerage,Volatility,ReturnOnRisk,Profit,Cash,Shares,Assets	
		addTradingRecord(totalTrades + "," + 
								info.getSecurity() + "," + 
								info.getDate() + "," + 
								lib.dateToTimeString(info.getEventTime()) + ",");
		
		// market, date, time, security, volume, price, trade_leg
		simulator.append("HKX," + 
							info.getDate() + "," + 
							lib.dateToTimeMillis(info.getEventTime()) + ","+ 
							info.getSecurity() + ",");

		double profit = -1;
		
		double price = getPrice(info, type);
		int volume = getVolume(info, type);
		
		String tradeType = "open";
		if (!checkClose) {
			tradeType = "close";
		}
		String tradeSign = "";
		if (volume > 0) {
			tradeSign = "+";
		}
		simulator.append(tradeSign + volume + "," + price + "," + tradeType);	

		addTradingRecord(price + "," + volume + ",");
		double buyBrokerage = 0;//getBrokerage(price * volume);
		addTradingRecord(buyBrokerage + ",");
		addTradingRecord(getVolatility(info) + ",");
		profit = - buyBrokerage;
		if (!checkClose) {
			Position current = getPosition(info.getSecurity());
			double currentProfit = current.getPrice() * -volume + price * volume;
			
			// initial sell
			//if (current.getValue() < 0) {
				currentProfit = -currentProfit;
			//}
			profit += currentProfit;
			
		}
		totalProfit += profit;
		turnover += Math.abs(price * volume);
		addTradingRecord(100 * profit / (price * volume) + ",");
		addTradingRecord(profit + "");
		
		// store trading position
		storePosition(info, type, volume, price);
		
		double currentShares = lib.calculatePosition(openPositions);
		cash = cash + profit;
		double currentCash = cash - currentShares;
		addTradingRecord("," + currentCash + "," + lib.calculatePosition(openPositions) + 
						"," + (currentCash + currentShares));
		
		if (profit < -30000) {
			//System.out.println("debug");
		}
		addTradingRecord("\r\n");
		simulator.append("\r\n");
		
	}

	

}
