package com.alluvialtrading.strategies.sunset;
import com.alluvialtrading.algo.BaseAlgo;
import com.alluvialtrading.lib.DateIterator;
import com.alluvialtrading.data.Quote;
import com.alluvialtrading.data.Trade;
import com.alluvialtrading.sim.VirtualBroker;
import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class Sunset extends BaseAlgo {
	
	String lastTime = null;
	Date dateStart = null;
	Date dateEnd = null;
	int benchmarkDays = 0;
	int benchmarkEvents = 0;
	String sunsetTime = "";
	
	// Thresholds
	double thresholdDailyTradesMin = 0;
	double thresholdDailyValueMin = 0;
	double thresholdLastSpreadMax = 0;
	double thresholdAuctionPriceMin = 0;
	double thresholdAuctionSpreadMin = 0;
	double thresholdAuctionValueMin = 0;
	double thresholdAuctionGapMin = 0;
	
	// Position sizes
	int positionSizeMax = 0;
	int positionSizeMin = 0;
	int positionSizeRelAuc = 0;
	
	/**
	 * Every java program starts in main
	 * @param args
	 */
	public static void main(String[] args) {
		
		// this runs algo code
		new Sunset(Market.ASX);
		
		// by here all trades have been dumped to CSV
		
		// below here is profit tracking related
		String[] files = new File("import").list();
		for (String file : files) {
			new VirtualBroker(file);
		}
	}
	
	public Sunset(Market market) {
		// calls functionality from parent
		super(market);
	}

	
	@Override
	protected void init() {

		super.init();
		
		// User params
		lastTime = getCloseTime();
		dateStart = getDateStart("2010-03-01");
		dateEnd = getDateEnd("2010-03-03");
		benchmarkDays = 5;
		benchmarkEvents = 20;
		sunsetTime = "15:30:00.000";
		
		// Position limits
		positionSizeMax = 50000; 			// $
		positionSizeMin = 10000; 			// $
		positionSizeRelAuc = 20;			// %
		
		// Thresholds
		thresholdDailyTradesMin = 70; 		// x
		thresholdDailyValueMin = 50000; 	// $
		thresholdLastSpreadMax = 5.0;		// %
		thresholdAuctionPriceMin = 0.4;		// %
		thresholdAuctionSpreadMin = 2; 		// x
		thresholdAuctionValueMin = 5000;	// $
		thresholdAuctionGapMin = 0.5;		// x
		
	}
	
	public void algoStart() {
		// calls functionality from parent
		super.algoStart();
	}
	
	@Override
	public void algoBody() {
		
		// Display
		System.out.println("\nSunset");
		System.out.println("\n======================================================\n\n");
		
		// Loop through trading days
	 	Iterator<Date> iterator = new DateIterator(dateStart, dateEnd);
	 	while(iterator.hasNext()) {
	 		Date date = iterator.next();
	 		if (isTradingDate(date)) {
	 			
	 			// Initialise variables
	 		    ArrayList<SunsetSignal> signal = new ArrayList<SunsetSignal>();
	 		   
	 			// Get stock list
	 			String[] allArray = getAllTradedSecurities(date);
	 			
 				// Set current time
 				setCurrentDateTime(combineDateTime(date,lastTime));
 				//Date currentDateTime = getCurrentDateTime();
	 			
	 			// Set current date
 				String currentDate = dateToISODateString(getCurrentDateTime());
 				
 				// Benchmarking period
 				String dateStart = getTradingDate(currentDate,-benchmarkDays);
 				String dateEnd = getTradingDate(currentDate,-1);
 				
	 			// Display
	 			System.out.println(currentDate);
 				
	 			// ====================================================================================
	 			// PRE-UNCROSSING MARKET SCAN
	 			// ====================================================================================
	 			
	 			// Loop through all stocks
	 			for (String stock : allArray) {
	 				
	 				// Current security
	 				stock = stock.trim();
	 				setCurrentSecurity(stock);
	 				System.out.println(stock);

	 				// Closing auction price change and liquidity check
	 				// --------------------------------------------------------------
	 				
	 				// Last quote
	 				Quote quoteLast = getQuote(currentDate, lastTime);
	 				if (null == quoteLast) {
	 					continue;
	 				}
	 				double askLast = quoteLast.getAsk();
	 				double bidLast = quoteLast.getBid();
	 				double midLast = quoteLast.getMid();
	 				if (0 == askLast || 0 == bidLast) {
	 					continue;
	 				}
	 				
	 				// Indicative uncrossing price and volume
	 				Trade tradeClose = getClosingTrade(currentDate);
	 				if (null == tradeClose) {
	 					continue;
	 				}
	 				double closePrice = tradeClose.getPrice();
	 				int closeVolume = tradeClose.getVolume();
	 				double closeValue = tradeClose.getValue();
	 				Date closeDateTime = tradeClose.getDate();
	 				
	 				double spreadLast = round4(askLast - bidLast);
	 				double spreadLastPercent = round2(100 * spreadLast / midLast);
	 				
	 				// Indicative uncrossing price change and value
	 				double auctionDelta = round2(100 * (closePrice - midLast) / midLast);
	 				
	 				// IF auction price change does not exceed transaction costs THEN
	 				if (Math.abs(auctionDelta) < thresholdAuctionPriceMin) {
	 					continue;
	 				}
	 				
	 				// IF auction price change is not unusual vis a vis the last spread THEN
	 				if (Math.abs(auctionDelta) < thresholdAuctionSpreadMin * spreadLastPercent || 
	 						spreadLastPercent > thresholdLastSpreadMax) {
	 					continue;
	 				}
	 				
	 				// IF auction price change has insufficient volume
	 				if (closeValue < thresholdAuctionValueMin) {
	 					continue;
	 				}
	 				
		 			// Sunset change based on events
		 			double maxPriceEvent = getMaxPrice(benchmarkEvents);
		 			double minPriceEvent = getMinPrice(benchmarkEvents);
		 			double priceRangeEvent = round4(maxPriceEvent - minPriceEvent);
		 			double priceLimitEventShort = round4(maxPriceEvent + (thresholdAuctionGapMin * priceRangeEvent));
		 			double priceLimitEventLong = round4(minPriceEvent - (thresholdAuctionGapMin * priceRangeEvent));
		 			double eventDelta = round4(2 * 100 * (maxPriceEvent - minPriceEvent) / (maxPriceEvent + minPriceEvent) + 0.0001);

		 			// IF close price is not unusual to pre-auction activity
	 				if ((auctionDelta > 0 && closePrice < priceLimitEventShort) ||
	 						(auctionDelta < 0 && closePrice > priceLimitEventLong)) {
	 					continue;
	 				}
		 			
		 			// Sunset change based on time
		 			double maxPriceTime = getMaxPrice(sunsetTime);
		 			double minPriceTime = getMinPrice(sunsetTime);
		 			double priceRangeTime = round4(maxPriceTime - minPriceTime);
		 			double priceLimitTimeShort = round4(maxPriceTime + (thresholdAuctionGapMin * priceRangeTime));
		 			double priceLimitTimeLong = round4(minPriceTime - (thresholdAuctionGapMin * priceRangeTime));
		 			double timeDelta = round4(2 * 100 * (maxPriceTime - minPriceTime) / (maxPriceTime + minPriceTime) + 0.0001);

		 			// IF close price is not unusual to pre-auction activity
	 				if ((auctionDelta > 0 && closePrice < priceLimitTimeShort) || 
	 						(auctionDelta < 0 && closePrice > priceLimitTimeLong)) {
	 					continue;
	 				}
		 			
			 		// Historical liquidity
	 				int dailyTrades = getTcount();
	 				double dailyValue = round2(getValue());
		 			double dailyTradesBenchmark = round2(getTcount(dateStart,dateEnd) / ((double)benchmarkDays));
			 		double dailyValueBenchmark = round2(getValue(dateStart,dateEnd) / ((double)benchmarkDays));
			 		
			 		// IF historically there is insufficient liquidity to get out tomorrow THEN
			 		if (dailyTrades < thresholdDailyTradesMin || 
			 				dailyTradesBenchmark < thresholdDailyTradesMin ||
			 				dailyValue < thresholdDailyValueMin || 
			 				dailyValueBenchmark < thresholdDailyValueMin) {
			 			continue;
			 		}
	 				
			 		// This is an opportunity to trade
			 		// ------------------------------------------------------------------------------

			 		// Add stock details to today's list of trading signals
			 		signal.add(new SunsetSignal(stock, 
			 			closeDateTime, 
				 		dailyTrades, 
				 		dailyTradesBenchmark, 
				 		dailyValue, 
				 		dailyValueBenchmark, 
				 		bidLast, 
				 		askLast, 
				 		midLast, 
				 		spreadLast, 
				 		spreadLastPercent, 
				 		closePrice, 
				 		auctionDelta, 
				 		priceLimitEventLong, 
				 		priceLimitEventShort, 
				 		maxPriceEvent, 
				 		minPriceEvent, 
				 		eventDelta, 
				 		priceLimitTimeLong, 
				 		priceLimitTimeShort, 
				 		maxPriceTime, 
				 		minPriceTime, 
				 		timeDelta, 
				 		closeVolume, 
				 		closeValue));

	 			} // END FOR all stocks


	 			// ====================================================================================
	 			// PRE-UNCROSSING ORDER ROUTING
	 			// ====================================================================================
	 			
	 			// Rank opportunities
	 			java.util.Collections.sort(signal);
	 			
	 			// Print sorted
	 			for (SunsetSignal current : signal) {
	 				
			 		// Display
			 		System.out.println("\n------------------------");
			 		System.out.println(current.getCloseDateTime());
			 		System.out.println(current.getStock());
			 		System.out.println("Trades: " + current.getDailyTrades() + " Benchmark: " + current.getDailyTradesBenchmark());
			 		System.out.println("Value: $" + current.getDailyValue() + " Benchmark: $" + current.getDailyValueBenchmark());
			 		System.out.println("Last bidBefore: $" + current.getBidLast() + " Last askBefore: $" + current.getAskLast() + " Last mid: $" + current.getMidLast());
			 		System.out.println("Last spread: $" + current.getSpreadLast() + "(" + current.getSpreadLastPercent() + "%)");
			 		System.out.println("Close price: $" + current.getClosePrice());
			 		System.out.println("Auction change: " + current.getAuctionDelta() + "%");
			 		System.out.println("Price limits Event: [" + current.getPriceLimitEventLong() + ":" + current.getPriceLimitEventShort() + "] ($" + current.getMaxPriceEvent() + "/$" + current.getMinPriceEvent() + " = " + current.getEventDelta() + "%)");
			 		System.out.println("Price limits Time: [" + current.getPriceLimitTimeLong() + ":" + current.getPriceLimitTimeShort() + "] ($" + current.getMaxPriceTime() + "/$" + current.getMinPriceTime() + " = " + current.getTimeDelta() + "%)");
			 		System.out.println("Close volume: " + current.getCloseVolume() + " Close value: $" + current.getCloseValue());
			 		Date exitDate = combineDateTime(getTradingDate(current.getCloseDateTime(), 1), sunsetTime);
			 		int sign = 1;
			 		if (current.getAuctionDelta() > 0) {
			 			sign = -1;
			 		}
			 		int volume = (int) (current.getCloseVolume() * positionSizeRelAuc / 100);
			 		if (volume * current.getClosePrice() > positionSizeMax) {
			 			volume = (int) (positionSizeMax / current.getClosePrice());
			 		}

			 		profitTrack(current.getCloseDateTime(), current.getStock(), (int) (sign * volume), current.getClosePrice(), 
			 				exitDate, Trade.EXIT_CLOSE);
	 				
	 			}
	 			
	 			/*
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
	 			*/
	 			
	 		} // END IF trading day
	 		
	 	} // END FOR all dates
	 	
	} // END algoBody()
		 			
	/*
		 				
	 				//if (stock.equals("BHP")) {
	 					
	 					//Trade closingTrade = connector.getClosingTrade(stock, todayDate);
	 					//System.out.println(todayDate + ", " + stock + " - closing price " + closingTrade.getPrice());
	 					
	 					//Quote quote = getQuote(todayDate, lastTime);
	 					//setCurrentDateTime(quote.getDateTime());

	 		 			//connector.setCurrentDateTime(quote.);
	 					
	 					
	 					//System.out.println("Close price:\t\t\t" + getClosePrice(date) + "\n");
	 					//System.out.println("Open price:\t\t\t" + getOpenPrice(date) + "\n");
	 					//System.out.println("Max price:\t\t\t" + getMaxPrice(date) + "\n");
	 					//System.out.println("Min price:\t\t\t" + getMinPrice(date) + "\n");
	 					

	 					System.out.println("getVWAP():\t\t\t" + getVWAP() + "\n");
	 					System.out.println("getVWAP(D-1):\t\t\t" + getVWAP(getTradingDate(currentDateTime,-1)) + "\n");
	 					System.out.println("getVWAP(D-5, D-1):\t\t" + getVWAP(getTradingDate(currentDateTime,-5),getDateEnd(getTradingDate(currentDateTime,-1))) + "\n");
	 					System.out.println("getVWAP(D-5, D-1, 0):\t\t" + getVWAP(getTradingDate(currentDateTime,-5),getDateEnd(getTradingDate(currentDateTime,-1)),0) + "\n");
	 					System.out.println("getVWAP(D-5, D-1, -100):\t" + getVWAP(getTradingDate(currentDateTime,-5),getDateEnd(getTradingDate(currentDateTime,-1)),-100) + "\n");
	 					System.out.println("getVWAP(D-5, D-1, 100):\t\t" + getVWAP(getTradingDate(currentDateTime,-5),getDateEnd(getTradingDate(currentDateTime,-1)),100) + "\n");
	 					System.out.println("getVWAP(D, -10):\t\t" + getVWAP(currentDateTime,-10) + "\n");
	 					System.out.println("getVWAP(D, Ta, Tb):\t\t" + getVWAP(currentDateTime,"15:00:00.000",lastTime) + "\n");
	 					System.out.println("getVWAP(T):\t\t\t" + getVWAP("15:00:00.000") + "\n");
	 					System.out.println("getVWAP(T, -10):\t\t" + getVWAP(lastTime,-10) + "\n");
	 					System.out.println("getVWAP(Ta, Tb):\t\t" + getVWAP("15:00:00.000",lastTime) + "\n");
	 					
	 					System.out.println("getTcount(D-5, D-1):\t\t" + getTcount(getTradingDate(currentDateTime,-5),getDateEnd(getTradingDate(currentDateTime,-1))) + "\n");
	 					System.out.println("getValue(D-5, D-1):\t\t" + getValue(getTradingDate(currentDateTime,-5),getDateEnd(getTradingDate(currentDateTime,-1)),0) + "\n");
	 					System.out.println("getVolume(D-5, D-1):\t\t" + getVolume(getTradingDate(currentDateTime,-5),getDateEnd(getTradingDate(currentDateTime,-1)),0) + "\n");

	 				}
		 			else {
		 				//System.out.println(dailyTradesBenchmark);
		 			}
	 			
	 			}
	 		}
	 		
	 	}
		
		// Build 30 day benchmarks
		
		// Read index data
		
	}
	
	*/
	
	@Override
	public void algoEnd() {
		super.algoEnd();
	}
	
	@Override
	protected String getCSVHeader() {
		return "Signal,Date,Stock,Overnight,Lower,Upper,IndexChange,Beta,RSquared";
	}
	
	private class SunsetSignal implements Comparable<SunsetSignal> {
		private String stock;
		private Date closeDateTime;
		private int dailyTrades;
		private double dailyTradesBenchmark;
		private double dailyValue;
		private double dailyValueBenchmark;
		private double bidLast;
		private double askLast;
		private double midLast;
		private double spreadLast;
		private double spreadLastPercent;
		private double closePrice;
		private double auctionDelta;
		private double priceLimitEventLong;
		private double priceLimitEventShort;
		private double maxPriceEvent;
 		private double minPriceEvent;
 		private double eventDelta;
 		private double priceLimitTimeLong;
 		private double priceLimitTimeShort;
 		private double maxPriceTime;
 		private double minPriceTime;
 		private double timeDelta;
 		private double closeVolume;
 		private double closeValue;

 		
		
		public SunsetSignal(String stock, Date closeDateTime, int dailyTrades,
				double dailyTradesBenchmark, double dailyValue,
				double dailyValueBenchmark, double bidLast, double askLast,
				double midLast, double spreadLast, double spreadLastPercent,
				double closePrice, double auctionDelta,
				double priceLimitEventLong, double priceLimitEventShort,
				double maxPriceEvent, double minPriceEvent, double eventDelta,
				double priceLimitTimeLong, double priceLimitTimeShort,
				double maxPriceTime, double minPriceTime, double timeDelta,
				double closeVolume, double closeValue) {
			super();
			this.stock = stock;
			this.closeDateTime = closeDateTime;
			this.dailyTrades = dailyTrades;
			this.dailyTradesBenchmark = dailyTradesBenchmark;
			this.dailyValue = dailyValue;
			this.dailyValueBenchmark = dailyValueBenchmark;
			this.bidLast = bidLast;
			this.askLast = askLast;
			this.midLast = midLast;
			this.spreadLast = spreadLast;
			this.spreadLastPercent = spreadLastPercent;
			this.closePrice = closePrice;
			this.auctionDelta = auctionDelta;
			this.priceLimitEventLong = priceLimitEventLong;
			this.priceLimitEventShort = priceLimitEventShort;
			this.maxPriceEvent = maxPriceEvent;
			this.minPriceEvent = minPriceEvent;
			this.eventDelta = eventDelta;
			this.priceLimitTimeLong = priceLimitTimeLong;
			this.priceLimitTimeShort = priceLimitTimeShort;
			this.maxPriceTime = maxPriceTime;
			this.minPriceTime = minPriceTime;
			this.timeDelta = timeDelta;
			this.closeVolume = closeVolume;
			this.closeValue = closeValue;
		}



		public String getStock() {
			return stock;
		}



		public void setStock(String stock) {
			this.stock = stock;
		}



		public Date getCloseDateTime() {
			return closeDateTime;
		}



		public void setCloseDateTime(Date closeDateTime) {
			this.closeDateTime = closeDateTime;
		}



		public int getDailyTrades() {
			return dailyTrades;
		}



		public void setDailyTrades(int dailyTrades) {
			this.dailyTrades = dailyTrades;
		}



		public double getDailyTradesBenchmark() {
			return dailyTradesBenchmark;
		}



		public void setDailyTradesBenchmark(double dailyTradesBenchmark) {
			this.dailyTradesBenchmark = dailyTradesBenchmark;
		}



		public double getDailyValue() {
			return dailyValue;
		}



		public void setDailyValue(double dailyValue) {
			this.dailyValue = dailyValue;
		}



		public double getDailyValueBenchmark() {
			return dailyValueBenchmark;
		}



		public void setDailyValueBenchmark(double dailyValueBenchmark) {
			this.dailyValueBenchmark = dailyValueBenchmark;
		}



		public double getBidLast() {
			return bidLast;
		}



		public void setBidLast(double bidLast) {
			this.bidLast = bidLast;
		}



		public double getAskLast() {
			return askLast;
		}



		public void setAskLast(double askLast) {
			this.askLast = askLast;
		}



		public double getMidLast() {
			return midLast;
		}



		public void setMidLast(double midLast) {
			this.midLast = midLast;
		}



		public double getSpreadLast() {
			return spreadLast;
		}



		public void setSpreadLast(double spreadLast) {
			this.spreadLast = spreadLast;
		}



		public double getSpreadLastPercent() {
			return spreadLastPercent;
		}



		public void setSpreadLastPercent(double spreadLastPercent) {
			this.spreadLastPercent = spreadLastPercent;
		}



		public double getClosePrice() {
			return closePrice;
		}



		public void setClosePrice(double closePrice) {
			this.closePrice = closePrice;
		}



		public double getAuctionDelta() {
			return auctionDelta;
		}



		public void setAuctionDelta(double auctionDelta) {
			this.auctionDelta = auctionDelta;
		}



		public double getPriceLimitEventLong() {
			return priceLimitEventLong;
		}



		public void setPriceLimitEventLong(double priceLimitEventLong) {
			this.priceLimitEventLong = priceLimitEventLong;
		}



		public double getPriceLimitEventShort() {
			return priceLimitEventShort;
		}



		public void setPriceLimitEventShort(double priceLimitEventShort) {
			this.priceLimitEventShort = priceLimitEventShort;
		}



		public double getMaxPriceEvent() {
			return maxPriceEvent;
		}



		public void setMaxPriceEvent(double maxPriceEvent) {
			this.maxPriceEvent = maxPriceEvent;
		}



		public double getMinPriceEvent() {
			return minPriceEvent;
		}



		public void setMinPriceEvent(double minPriceEvent) {
			this.minPriceEvent = minPriceEvent;
		}



		public double getEventDelta() {
			return eventDelta;
		}



		public void setEventDelta(double eventDelta) {
			this.eventDelta = eventDelta;
		}



		public double getPriceLimitTimeLong() {
			return priceLimitTimeLong;
		}



		public void setPriceLimitTimeLong(double priceLimitTimeLong) {
			this.priceLimitTimeLong = priceLimitTimeLong;
		}



		public double getPriceLimitTimeShort() {
			return priceLimitTimeShort;
		}



		public void setPriceLimitTimeShort(double priceLimitTimeShort) {
			this.priceLimitTimeShort = priceLimitTimeShort;
		}



		public double getMaxPriceTime() {
			return maxPriceTime;
		}



		public void setMaxPriceTime(double maxPriceTime) {
			this.maxPriceTime = maxPriceTime;
		}



		public double getMinPriceTime() {
			return minPriceTime;
		}



		public void setMinPriceTime(double minPriceTime) {
			this.minPriceTime = minPriceTime;
		}



		public double getTimeDelta() {
			return timeDelta;
		}



		public void setTimeDelta(double timeDelta) {
			this.timeDelta = timeDelta;
		}



		public double getCloseVolume() {
			return closeVolume;
		}



		public void setCloseVolume(double closeVolume) {
			this.closeVolume = closeVolume;
		}



		public double getCloseValue() {
			return closeValue;
		}



		public void setCloseValue(double closeValue) {
			this.closeValue = closeValue;
		}



		public int compareTo(SunsetSignal sd) {
			if (Math.abs(sd.getAuctionDelta()) > Math.abs(auctionDelta)) {
				return 1;
			}
			else if (Math.abs(sd.getAuctionDelta()) < Math.abs(auctionDelta)) {
				return -1;
			}
			else {
				return 0;
			}
		}
	}



}
