package com.alluvialtrading.strategies.sunrise;

import com.alluvialtrading.algo.BaseAlgo;
import com.alluvialtrading.data.Regression;
import com.alluvialtrading.data.Trade;
import com.alluvialtrading.lib.DateIterator;
import com.alluvialtrading.sim.VirtualBroker;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.LinkedList;

public class Sunrise extends BaseAlgo {
	
	private boolean debug;
	
	// Valid trading dates Slow to search but there is not very much data
	private LinkedList<String> tradingDates = null;
		
	Date startDate = null;
	Date endDate = null;
	
	// Opening prices for current day
	
	// This takes a key in the following format: 03/02/2010/DJS.AX and 
	// stores the expected overnight change given THAT MORNING's change 
	// in the index
	private Hashtable<String, Double> overnightChanges = null;
	
	// Algo parameters
	private final static double MAX_OVERNIGHT_MOVE = 0.02;
	
	public static void main(String[] args) {
		new Sunrise(Market.ASX);
		String[] files = new File("import").list();
		for (String file : files) {
			new VirtualBroker(file);
		}
	}
	
	public Sunrise(Market market) {
		super(market);
	}
	
	public void algoStart() {
		super.algoStart();
	}
	
	@Override
	public void algoBody() {

		// Load trading dates
		loadTradingDates();
		
		loadOvernightChanges();
		
		// Start looping potential trades chronologically
	 	Iterator<Date> iterator = new DateIterator(startDate, endDate);
	 	while(iterator.hasNext())
	 	{
	 		Date date = iterator.next();
	 		if (isTradingDate(date)) {

                String[] stocks = getAllTradedSecurities(date);
	 			// loop through all stocks 
	 			for (String stock : stocks) {
	 				 
	 				String todayDate = dateToSmartsDateString(date);
	 				
	 				if (null != overnightChanges.get(todayDate + "/" + stock)) {
		 				// Get actual change
		 				double todayChange = overnightChanges.get(todayDate + "/" + stock);
		 				double todayIndex = overnightChanges.get(todayDate + "/AORD");
		 				ArrayList<Double> indexChanges = new ArrayList<Double>();
		 				ArrayList<Double> stockChanges = new ArrayList<Double>();
		 				
		 				int index = tradingDates.indexOf(todayDate);
		 				int numDays = 30;
		 				for (int i=index-1; i>=index-numDays; i--) {
		 					String currentDate = tradingDates.get(i);
		 					if (null != overnightChanges.get(currentDate + "/AORD") &&
		 							null != overnightChanges.get(currentDate + "/" + stock)) {
			 					indexChanges.add(overnightChanges.get(currentDate + "/AORD"));
			 					stockChanges.add(overnightChanges.get(currentDate + "/" + stock));
		 					}
		 					else {
		 						// guarantee 30 days
		 						//numDays++;
		 					}
		 					
		 					//System.out.println("regressing " + currentDate + "/" + stock);

		 				}
		 				
		 				//Double
		 				if (stockChanges.size() > 25) {
			 				Regression regression = getRegression(toDoubleArray(indexChanges), 
										toDoubleArray(stockChanges), 
															todayIndex, 
															0.9);
			 				System.out.println(debug);
			 				if (debug) {
			 					System.out.println("stock");
			 					for (double change : toDoubleArray(stockChanges)) {
			 						System.out.print(change * 100 + ",");
			 					}
			 					System.out.println("index");
			 					for (double change : toDoubleArray(indexChanges)) {
			 						System.out.print(change * 100 + ",");
			 					}
			 				}
			 				double lowerBound = regression.getLowerBound();
			 				double upperBound = regression.getUpperBound();
			 				String tradeInfo = todayDate + "," + stock + "," + todayChange + "," + 
			 								lowerBound + "," + upperBound + "," + todayIndex + "," + 
			 								regression.getSlope() + "," + regression.getrSquared();
			 				int sign = 0;
			 				Trade openTrade = getOpeningTrade(stock, dateToISODateString(date));
			 				
			 				if (null != openTrade) {
			 					
				 				double price = openTrade.getPrice();
				 				int volume = (int) (0.15 * openTrade.getVolume());
				 				
				 				boolean minVolume = price * Math.abs(volume) > MIN_TRADE_VALUE;
				 				boolean minRSquared = regression.getrSquared() > 0.3;
				 				String signal = "";
				 				String day = dateToDay(date);
				 				boolean monday = day.equals("Mon");
				 				boolean excessiveMovement = Math.abs(todayChange) > MAX_OVERNIGHT_MOVE;
				 				
				 				boolean trade = minVolume &&
						 						minRSquared &&
						 						!monday &&
						 						!excessiveMovement;
				 				
				 				if (price * Math.abs(volume) > MAX_TRADE_VALUE) {
				 					volume = (int) (MAX_TRADE_VALUE / price);
				 				}
				 				if (todayChange < lowerBound &&
				 						trade) {
				 					signal = "BUY";
				 					addTradingRecord(signal + "," + tradeInfo);
				 				}
				 				else if (todayChange > upperBound &&
				 							trade) {
				 					signal = "SELL";
				 					addTradingRecord(signal + "," + tradeInfo);
				 				}
				 				if (signal.equals("SELL")) {
				 					sign = -1;
				 				}
				 				else if (signal.equals("BUY")) {
				 					sign = 1;
				 				}
				 				volume = volume * sign;
				 				// For profit tracker
				 				if (0 != sign) {
					 				
				 					profitTrack(dateToISODateTimeString(openTrade.getDate()), 
		 									stock, volume
		 									 , price, "60", Trade.EXIT_VWAP);

				 				}
				 				
			 					
			 				}

		 				
		 				}

		 				
		 				
	 				} 				
	 				
	 			}
	 		}
	 		
	 	}
		
		// Build 30 day benchmarks
		
		// Read index data
		
	}
	
	@Override
	public void algoEnd() {
		super.algoEnd();
		//writeFile("import", "brokertrades.csv", brokerTrades.toString());
	}

	@Override
	protected String getCSVHeader() {
		return "Signal,Date,Stock,Overnight,Lower,Upper,IndexChange,Beta,RSquared";
	}
	
	@Override
	protected void init() {
		super.init();
		debug = false;
		tradingDates = new LinkedList<String>();
		startDate = convertSmartsDateTime("1/3/2010 09:00:00.000");
		endDate = convertSmartsDateTime("31/3/2010 16:00:00.000");
		overnightChanges = new Hashtable<String, Double>();
		
		//brokerTrades = new StringBuffer("Market,Date,Security,Volume,Price,CloseWait,CloseStrategy\r\n");

	}
	
	public boolean isValidTradingDate(Date date) {
		return tradingDates.indexOf(dateToSmartsDateString(date)) > 0;
	}
	
	public void loadStocks() {
		String[] stocks = openFile("data", "allstocks.csv");
		for (String stock : stocks) {
			allStocks.add(stock.trim().split("\\.")[0]);
		}
	}
	
	public void loadOvernightChanges() {
		String[] overnight = openFile("data", "openclose.csv");
		for (String price : overnight) {
			String[] split = price.split(",");
			if (split[2].length() > 0 && split[3].length() > 0) {
				String date = split[0];
				String security = split[1].trim();
				double yesterdayClose = Double.parseDouble(split[2]);
				double todayOpen = Double.parseDouble(split[3]);
				double delta = (todayOpen - yesterdayClose) / yesterdayClose;
				overnightChanges.put(date + "/" + security, delta);

			}

		}
		
		// load index as a special stock
		String[] index = openFile("data", "index.csv");
		for (String price : index) {
			String[] split = price.split(",");
			String date = split[0];
			String security = "AORD";
			double yesterdayClose = Double.parseDouble(split[1]);
			double todayOpen = Double.parseDouble(split[2]);
			double delta = (todayOpen - yesterdayClose) / yesterdayClose;
			overnightChanges.put(date + "/" + security, delta);
		}
	}
	
	public void loadTradingDates() {
		String[] dates = openFile("data", "index.csv");
		for (String date : dates) {
			tradingDates.add(date.split(",")[0]);
		}
	}
	

}
