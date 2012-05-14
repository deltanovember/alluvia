package com.alluvialtrading.sim;


import com.alluvialtrading.algo.Position;
import com.alluvialtrading.data.Quote;
import com.alluvialtrading.data.Trade;
import com.alluvialtrading.tools.TraderLib;
import com.alluvialtrading.vbroker.ASXBackTestingLib;
import com.alluvialtrading.vbroker.BackTestingLib;

import java.io.File;
import java.util.Collections;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;

/**
 * This class acts as a virtual broker.  It should perform the same functionality as a prime broker.  
 * This means it accepts a list of trades, accepts or rejects based on certain criteria and exits 
 * based on specified criteria.  The input is a CSV file with the columns defined as follows
 * <ol>
 * <li>Market name e.g. ASX</li>
 * <li>Trade date and time e.g. 2010-07-08 16:10:51.797</li>
 * <li>Security name e.g. BHP</li>
 * <li>Trade volume e.g. 4000, -1550. Buy or sell is implied by sign</li>
 * <li>Price e.g. 40.2</li>
 * <li>Exit date/time. If this a number e.g. 60, the broker will interpret this as the number 
 * of seconds from opening. If this is a date such as 2010-04-04 the broker will attempt to exit 
 * on that date.  This must be specified in conjunction with one of the following exit 
 * strategies: OPEN, CLOSE</li>
 * <li>Exit strategy e.g.  Trade.EXIT_AGGRESSIVE, Trade.EXIT_CLOSE, Trade.c, Trade.EXIT_MID,	
 * Trade.EXIT_PASSIVE, Trade.EXIT_VWAP, Trade.EXIT_OPEN</li>
 * </ol>
 * Certain exit strategies require only a date and certain others require both date and time.  
 * The following strategies only require a date: EXIT_CLOSE, EXIT_CLOSE, EXIT_OPEN.  The following
 *  strategies require both date and time: EXIT_MID, EXIT_PASSIVE, EXIT_AGGRESSIVE, EXIT_VWAP.
 * @author dnguyen
 *
 */

public class VirtualBroker {

	// If true manage bankroll limits
	private final boolean MANAGE_BANK = false;

	TraderLib lib = new TraderLib();
	static String importDir = "import";
	ArrayList<Trade> allTrades = new ArrayList<Trade>();
	BackTestingLib connector = new ASXBackTestingLib();
	
	// Trading data
	StringBuffer tradingRecord = new StringBuffer("TradeNo,Security,DateTime,Price,Volume,Brokerage,ReturnOnRisk,Profit,Cash,Shares,Assets\r\n");
	int totalTrades = 0;
	double totalProfit = 0;
	double turnover = 0;
	
	double cash = 100000;
	double shares = 0;
	
	Hashtable<String, Position> openPositions = new Hashtable<String, Position>();
	
	public static void main(String[] args) {
		String[] files = new File(importDir).list();
		for (String file : files) {
			new VirtualBroker(file);
		}
	}
	public VirtualBroker(String tradeData) {

		importTrades(tradeData);		
		lib.writeFile(".", "profit" + tradeData, tradingRecord.toString());
	}
	
	private void doTrade(Trade trade) {

		totalTrades++;

		// TradeNo,Security,Date,Time,Price,Volume,Brokerage,Volatility,ReturnOnRisk,Profit,Cash,Shares,Assets	
		tradingRecord.append(totalTrades + "," + 
				trade.getSecurity() + "," + 
				lib.dateToISODateTimeString(trade.getDate()) + ",");


		double profit = -1;
		
		double price = trade.getPrice();
		int volume = trade.getVolume();
		
		// grab price and volume if necessary
		if (price <= 0) {
			price = connector.getMarketPrice(trade);
		}
		
		tradingRecord.append(price + "," + volume + ",");
		double buyBrokerage = getBrokerage(trade);
		tradingRecord.append(buyBrokerage + ",");
		profit = - buyBrokerage;
		
		Position current = getPosition(trade.getSecurity());
		if (null != current) {
			double currentProfit = current.getPrice() * -volume + price * volume;
			
			// initial sell
			//if (current.getValue() < 0) {
				currentProfit = -currentProfit;
			//}
			profit += currentProfit;			
		}

		totalProfit += profit;
		turnover += Math.abs(price * volume);
		tradingRecord.append(100 * profit / (price * volume) + ",");
		tradingRecord.append(profit);
		
		// store trading position
		storePosition(trade, volume, price);
		
		double currentShares = lib.calculatePosition(openPositions);
		cash = cash + profit;
		double currentCash = cash - currentShares;
		tradingRecord.append("," + currentCash + "," + lib.calculatePosition(openPositions) + 
						"," + (currentCash + currentShares));
		
		if (profit < -30000) {
			//System.out.println("debug");
		}
		tradingRecord.append("\r\n");

	}

	
	private double getBrokerage(Trade trade) {
		
		double value = Math.abs(trade.getPrice() * trade.getVolume());
		if (getBrokerageRate() * value > getBrokerageMin()) {
			return getBrokerageRate() * value;
		}
		else {
			return getBrokerageMin();
		}
		//return 0;
	}
	
	
	public double getBrokerageRate() {
		return 0.001;
	}
	
	public double getBrokerageMin() {
		return 10;
	}
	
	
	/**
	 * Get position in particular stock
	 * @param security
	 * @return
	 */
	private Position getPosition(String security) {
		
		return openPositions.get(security);
		
	}
	
	private void importTrades(String tradeData) {
		
		if (tradeData.charAt(0) == '.') {
			return;
		}
		// Read raw CSV files
		String[] rawData = lib.openFile(importDir, tradeData);	
		int start = 0;
		if (rawData[0].contains("arket")) {
			start = 1;
		}
		// Convert to TradeInfo objects
		for (int i=start; i<rawData.length; i++) {
			
			if (!rawData[i].contains(",")) {
				continue;
			}
			String[] splitLine = rawData[i].split(",");

			String market = splitLine[0];
			Date date = lib.convertISODateTimeString(splitLine[1]);
			String security = splitLine[2];
			int volume = Integer.parseInt(splitLine[3]);
			double price = Double.parseDouble(splitLine[4]);
			String closeWait = splitLine[5];
			String closeStrategy = splitLine[6];
			
			Trade trade = new Trade(market, date, security,
					volume, price, closeWait, closeStrategy);
			allTrades.add(trade);
			
		}
		
		// Go through all the trades and 
		// inject the closing trades when necessary
		int stop = allTrades.size();
		for (int i=0; i<stop ; i++) {
			
			Trade trade = allTrades.get(i);
			// set future time
			if (trade.getCloseWait().length() >= 0) {
				Date newDate = null;
				
				if (trade.getClosingStrategy().equals(Trade.EXIT_VWAP)) {
					
					// calculate VWAP just after
					newDate = new Date(1000 * Integer.parseInt(trade.getCloseWait()) + 
							trade.getDate().getTime());
				}
				else if (trade.getCloseWait().contains("-")) {
					newDate = lib.convertISODateTimeString(trade.getCloseWait());
				}
				else {
					newDate = new Date(1000 * Integer.parseInt(trade.getCloseWait()) + 
							trade.getDate().getTime());
				}
				
				
				Trade close = new Trade(trade.getMarket(), newDate, trade.getSecurity(),
						-trade.getVolume(), 0, "0", trade.getClosingStrategy());
				double exitPrice = 0;
				
				if (trade.getClosingStrategy().equals(Trade.EXIT_VWAP)) {
					exitPrice = connector.getVWAPPrice(close);
				}
				else if (trade.getClosingStrategy().equals(Trade.EXIT_LAST_TRADE)) {
					
					String tradeTime = lib.dateToTimeString(trade.getDate());
					String dateString = lib.dateToISODateString(trade.getDate());
					// If the initial trade occurred after the market closed for 
					// continuous trading, the exit must occur on the subsequent trading day
					if (tradeTime.compareTo(connector.getCloseTime()) > 0) {
						String exitDay = connector.getTradingDate(dateString, 1);
						if (exitDay.contains("-")) {
							exitDay = trade.getCloseWait();
						}
						Trade lastTrade = connector.getLastTrade(trade.getSecurity(), exitDay);
						if (null == lastTrade) {
							trade.setClosingStrategy(Trade.EXIT_AGGRESSIVE);
							Quote lastQuote = connector.getQuote(trade.getSecurity(), exitDay, connector.getCloseTime());
							if (null != lastQuote) {
								exitPrice = connector.getMarketPrice(trade);
								close.setDate(lastQuote.getDateTime());
							}
							else {
								exitPrice = trade.getPrice();
								close.setDate(trade.getDate());
							}
						}
						else {
							exitPrice = lastTrade.getPrice();
							close.setDate(lastTrade.getDate());
						}
						
						
					}
					
					// otherwise exit same day
					else {
						exitPrice = connector.getClosePrice(trade.getSecurity(), lib.convertISODateTimeString(dateString));
					}
					
					
				}
				else {
					exitPrice = connector.getMarketPrice(close);
				}

				
				//System.out.println(exitPrice);
				close.setPrice(exitPrice);
				close.setClosingTrade(true);
				allTrades.add(close);
			}
		}
		
		Collections.sort(allTrades);
		for (Trade trade : allTrades) {
			doTrade(trade);
		}
			
	}
	
	/**
	 * Store the net position in each stock for bankroll management
	 * @param info
	 * @param type
	 */
	private void storePosition(Trade trade, int volume, double price) {	
		
		// store net position in stock
		double stockPosition = volume * price;
		if (volume < 0) {
			stockPosition = -1 * stockPosition;
		}
		
		Position currentPosition = openPositions.get(trade.getSecurity());
		if (null == currentPosition) {
			// new position
			openPositions.put(trade.getSecurity(), new Position(trade, volume, price));
		}
		else {
			currentPosition.addToPosition(volume, price);
			if (0 == currentPosition.getVolume()) {
				openPositions.remove(trade.getSecurity());
			}
		}
		

		
	}
}
