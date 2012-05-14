package com.alluvialtrading.algo;

import java.util.ArrayList;
import java.util.Date;

import com.alluvialtrading.data.Quote;
import com.alluvialtrading.data.Regression;
import com.alluvialtrading.data.Trade;
import com.alluvialtrading.tools.MathLib;
import com.alluvialtrading.tools.TraderLib;
import com.alluvialtrading.vbroker.ASXBackTestingLib;
import com.alluvialtrading.vbroker.BackTestingLib;
import com.alluvialtrading.vbroker.LSEBackTestingLib;

/**
 * Baseline class to handle trading, cash management and so on.
 * The overall purpose of the class is to expose ALICE like functionality 
 * for algorithm designers to use.  The following principles should be kept in mind:
 * <ol>
 * <li>This class is meant to facilitate algorithm development in an easy, powerful and flexible manner.</li>
 * <li>Easy means that anything written within our framework should be no more complex than the ALICE equivalent</li>
 * <li>Powerful means that we expose all the functions ALICE has available at equivalent or faster speeds</li>
 * <li>Flexible means that we can add functions above and beyond what ALICE can provide.  Additionally we can connect to a number of different datasource ranging from historical data to live trading platforms</li>
 * </ol>
 * The most basic guideline is that algorithm developers should only be focusing on 
 * what to do not how to do things. As a concrete example, if an algorithm developer 
 * wishes to retrieve a quote, they should only need to call a getQuote method. 
 * They do not need to worry about implementation details such as whether the quote 
 * comes from a database or a live streaming feed. Details to the extent practicable 
 * should be completely abstracted away.
 * @author dnguyen
 *
 */
public abstract class BaseAlgo {	

	// For profit tracker
	private StringBuffer brokerTrades = null;

	private TraderLib lib = new TraderLib();
	private MathLib mathLib = new MathLib(); 
	private BackTestingLib connector = null;

	// Trading data
	private StringBuffer tradingRecord = new StringBuffer();
	private String market = "lse";

	// Universe of tradeable stocks
	protected ArrayList<String> allStocks = null;	

	// Implied variables
	private Date currentDateTime = null;
	private String currentSecurity = null;

	/**
	 * Maximum individual trade size in dollars
	 */
	protected int MAX_TRADE_VALUE = 100000;

	/**
	 * Minimum individual trade size in dollars
	 */
	protected int MIN_TRADE_VALUE = 5000;

	/**
	 * Enumeration representing all possible markets
	 *
	 */
	public enum Market {ASX, HKX, LSE};

	/**
	 * Current market for algo
	 */
	private Market currentMarket = null;

	public BaseAlgo(Market market) {

		allStocks = new ArrayList<String>();
		currentMarket = market;
		run();
	}

	protected void addTradingRecord(String record) {
		tradingRecord.append(record + "\r\n");
	}

	/**
	 * Main algorithm body where most computations are performed
	 */
	protected void algoBody() {

	}

	protected void algoEnd() {
		writeRecord();
	}

	protected void init() {
		brokerTrades = new StringBuffer();
		if (Market.ASX == currentMarket) {
			connector = new ASXBackTestingLib();
		}
		else if (Market.LSE == currentMarket) {
			connector = new LSEBackTestingLib();
		}
		market = connector.getMarketName();
	}

	/**
	 * Is this a valid trading date? In other words not a weekend or holiday
	 * @param date
	 * @return
	 */
	public boolean isTradingDate(Date date) {
		return connector.isTradingDate(market, date);
	}
	public boolean isTradingDate(String date) {
		return isTradingDate(lib.convertISODateTimeString(date));
	}
	
	public void loadStocks() {
		String[] stocks = lib.openFile("data", "allstocks.csv");
		for (String stock : stocks) {
			allStocks.add(stock.trim().split("\\.")[0]);
		}
	}

	/**
	 * Open file on hard disk and return as String array
	 * @param dirName directory name
	 * @param fileName file name
	 * @return String array one line per array element
	 */
	public String[] openFile(String dirName, String fileName) {
		return lib.openFile(dirName, fileName);
	}
	
	/**
	 * Track profit of a particular trade/exit pair
	 * @param dateTime
	 * @param stock
	 * @param volume
	 * @param price
	 * @param exitTime
	 * @param closeStrategy
	 */
	protected void profitTrack(Date dateTime, String stock, int volume,
			double price, Date exitTime, String closeStrategy) {
		profitTrack(lib.dateToISODateTimeString(dateTime), stock, volume, 
				price, lib.dateToISODateTimeString(exitTime), closeStrategy);
	}

	/**
	 * Track profit of a particular trade/exit pair
	 * @param entryTime
	 * @param stock
	 * @param volume
	 * @param price
	 * @param exitTime
	 * @param closeStrategy Trade.CLOSE_AGGRESSIVE, Trade.CLOSE_MID, 
	 * Trade.CLOSE_PASSIVE, Trade.CLOSE_VWAP
	 */
	protected void profitTrack(String entryTime, String stock, int volume,
			double price, String exitTime, String closeStrategy) {
		brokerTrades.append(connector.getMarketName() + "," + 
				entryTime + 	"," + 
				stock + "," + volume
				+ "," + price + "," + 
				exitTime + "," + 
				closeStrategy + "\r\n");
	}

	private void run() {
		init();
		algoStart();
		algoBody();
		algoEnd();
	}

	/**
	 * This method is called at the start of the algorithm.  It should be used to perform initialisation
	 */
	protected void algoStart() {
		setCSVHeader(getCSVHeader());

	}

	/**
	 * Takes a date object and a time string in the 
	 * format HH:mm:ss.SSS and converts to a date object.
	 * date Date
	 * time String
	 * @return
	 */
	public Date combineDateTime(Date date, String time) {
		return lib.combineDateTime(date, time);
	}

	/**
	 * Takes a date string and a time string in the 
	 * format HH:mm:ss.SSS and converts to a date object.
	 * date String
	 * time String
	 * @return
	 */
	public Date combineDateTime(String date, String time) {
		return lib.combineDateTime(date, time);
	}

	/**
	 * Takes a Date in DD/MM/YYYY and converts to Date object 
	 * @param date
	 * @return
	 */
	public Date convertSmartsDate(String date) {
		return lib.convertSmartsDate(date);

	}

	/**
	 * Takes a time in the format 01/03/2010 10:35:29 format and converts to 
	 * Date object
	 * @param dateTime String in  01/03/2010 10:35:29.999 format
	 * @return
	 */
	public Date convertSmartsDateTime(String dateTime) {
		return lib.convertSmartsDateTime(dateTime);	
	}

	/**
	 * 
	 * @param date
	 * @return "Mon", "Tue" ...
	 */

	public String dateToDay(Date date) {
		return lib.dateToDay(date);
	}

	/**
	 * Takes a date object and converts to YYYY-MM-DD format
	 * @param date
	 * @return
	 */
	public String dateToISODateString(Date date) {
		return lib.dateToISODateString(date);
	}
	public String dateToISODateTimeString(Date date) {
		return lib.dateToISODateTimeString(date);
	}

	/**
	 * 
	 * @param date
	 * @return String in SMARTS format dd/MM/yyyy
	 */
	public String dateToSmartsDateString(Date date) {
		return lib.dateToSmartsDateString(date);
	}

	/**
	 * Adjust volume according to max trade parameters
	 * @param trade
	 * @return
	 */
	protected void adjustVolume(Trade trade) {
		double value = trade.getPrice() * trade.getVolume();
		if (value > MAX_TRADE_VALUE) {
			int volume = (int) (MAX_TRADE_VALUE / trade.getPrice());
			trade.setVolume(volume);
		}
	}


	/**
	 * Get all securities that traded on a given date
	 * @param date Date in ISO format
	 * @return
	 */
	public String[] getAllTradedSecurities(Date date) {
		return getAllTradedSecurities(lib.dateToISODateString(date));
	}
	public String[] getAllTradedSecurities(String date) {
		return connector.getAllTradedSecurities(date);
	}
	
	public double getClosePrice() {
		return getClosePrice(getCurrentDateTime());
	}
	public double getClosePrice(Date date) {
		return connector.getClosePrice(getCurrentSecurity(), date);
	}
	public double getClosePrice(String date) {
		return getClosePrice(lib.convertISODateTimeString(date));
	}

	/**
	 * Retrieve correct closing time for current market
	 * @return
	 */
	public String getCloseTime() {
		return connector.getCloseTime();
	}

	/**
	 * Retrieve closing trade for specific security and date
	 * @param security
	 * @param date
	 * @return
	 */
	public Trade getClosingTrade(String date) {
		return connector.getClosingTrade(getCurrentSecurity(), date);
	}

	protected abstract String getCSVHeader();

	/**
	 * Return Date object capturing details of current
	 * quote or trade
	 * @return
	 */
	public Date getCurrentDateTime() {
		return currentDateTime;
	}

	/**
	 * Return String object capturing details of current
	 * quote or trade
	 * @return String representing security code
	 */
	public String getCurrentSecurity() {
		return currentSecurity;
	}

	/**
	 * Takes a date object and converts to a date object at day end.
	 * date Date
	 * @return
	 */
	public Date getDateEnd(Date date) {
		return lib.getDateEnd(date);
	}
	/**
	 * Takes a date string and converts to a date object at day end.
	 * date String
	 * @return
	 */
	public Date getDateEnd(String date) {
		return getDateEnd(lib.convertISODateTimeString(date));
	}
	
	/**
	 * Takes a date object and converts to a date object at day start.
	 * date Date
	 * @return
	 */
	public Date getDateStart(Date date) {
		return lib.getDateStart(date);
	}
	/**
	 * Takes a date string and converts to a date object at day start.
	 * date String
	 * @return
	 */
	public Date getDateStart(String date) {
		return getDateStart(lib.convertISODateTimeString(date));
	}

	/**
	 * Get first trade price
	 * @return
	 */
	public double getFirstPrice() {
		Date startDateTime = lib.getDateStart(getCurrentDateTime());
		return getFirstPrice(startDateTime, getCurrentDateTime());
	}
	public double getFirstPrice(Date dateStart) {
		return getFirstPrice(dateStart, getCurrentDateTime());
	}
	public double getFirstPrice(Date dateStart, Date dateEnd) {
		return connector.getFirstPrice(getCurrentSecurity(), dateStart, dateEnd);
	}
	public double getFirstPrice(String date) {
		return getFirstPrice(lib.convertISODateTimeString(date));
	}
	
	/**
	 * Get last trade price
	 * @return
	 */
	public double getLastPrice() {
		Date startDateTime = lib.getDateStart(getCurrentDateTime());
		return getLastPrice(startDateTime, getCurrentDateTime());
	}
	public double getLastPrice(Date dateStart) {
		return getLastPrice(dateStart, getCurrentDateTime());
	}
	public double getLastPrice(Date dateStart, Date dateEnd) {
		return connector.getLastPrice(getCurrentSecurity(), dateStart, dateEnd);
	}
	public double getLastPrice(String date) {
		return getLastPrice(lib.convertISODateTimeString(date));
	}
	
	/**
	 * Get maximum trade price
	 * @return
	 */
	public double getMaxPrice() {
		Date startDateTime = lib.getDateStart(getCurrentDateTime());
		return getMaxPrice(startDateTime, getCurrentDateTime());
	}
	public double getMaxPrice(Date dateStart) {
		return getMaxPrice(dateStart, getCurrentDateTime());
	}
	public double getMaxPrice(Date dateStart, Date dateEnd) {
		return connector.getMaxPrice(getCurrentSecurity(), dateStart, dateEnd);
	}
	public double getMaxPrice(int maxEvents) {
		return connector.getMaxPrice(getCurrentSecurity(), getCurrentDateTime(), maxEvents);
	}
	public double getMaxPrice(String startTime) {
		Date startDateTime = lib.combineDateTime(lib.dateToISODateString(getCurrentDateTime()), startTime);
		return getMaxPrice(startDateTime);
	}
	public double getMaxPrice(String startTime, String endTime) {
		String todayDate = lib.dateToISODateString(getCurrentDateTime());
		Date startDateTime = lib.combineDateTime(todayDate, startTime);
		Date endDateTime = lib.combineDateTime(todayDate, endTime);
		return getMaxPrice(startDateTime, endDateTime);
	}

	/**
	 * Adjust volume according to max trade parameters
	 * @return
	 */
	protected int getMaxVolume(double price, int volume) {
		int maxVolume = volume;
		double value = price * volume;
		if (value > MAX_TRADE_VALUE) {
			maxVolume = (int) (MAX_TRADE_VALUE / price);
		}
		return maxVolume;
	}
	
	/**
	 * Get minimum bidBefore price
	 * @return
	 */
	/*
	public double getMinBid() {
		Date startDateTime = lib.getDateStart(getCurrentDateTime());
		return getMinBid(startDateTime);
	}
	public double getMinBid(Date dateStart) {
		return getMinBid(dateStart, getCurrentDateTime());
	}
	public double getMinBid(Date dateStart, Date dateEnd) {
		return connector.getQuoteLimit(getCurrentSecurity(), dateStart, dateEnd, maxEvents);
	}
	public double getMinBid(int maxEvents) {
		return connector.getQuoteLimit(getCurrentSecurity(), getCurrentDateTime(), maxEvents);
	}
	public double getMinBid(String startTime) {
		Date startDateTime = lib.combineDateTime(lib.dateToISODateString(getCurrentDateTime()), startTime);
		return getMinBid(startDateTime);
	}
	public double getMinBid(String startTime, String endTime) {
		String todayDate = lib.dateToISODateString(getCurrentDateTime());
		Date startDateTime = lib.combineDateTime(todayDate, startTime);
		Date endDateTime = lib.combineDateTime(todayDate, endTime);
		return getMinBid(startDateTime, endDateTime);
	}
	*/
	
	/**
	 * Get minimum trade price
	 * @return
	 */
	public double getMinPrice() {
		Date startDateTime = lib.getDateStart(getCurrentDateTime());
		return getMinPrice(startDateTime, getCurrentDateTime());
	}
	public double getMinPrice(Date dateStart) {
		return getMinPrice(dateStart, getCurrentDateTime());
	}
	public double getMinPrice(Date dateStart, Date dateEnd) {
		return connector.getMinPrice(getCurrentSecurity(), dateStart, dateEnd);
	}
	public double getMinPrice(int maxEvents) {
		return connector.getMinPrice(getCurrentSecurity(), getCurrentDateTime(), maxEvents);
	}
	public double getMinPrice(String startTime) {
		Date startDateTime = lib.combineDateTime(lib.dateToISODateString(getCurrentDateTime()), startTime);
		return getMinPrice(startDateTime);
	}
	public double getMinPrice(String startTime, String endTime) {
		String todayDate = lib.dateToISODateString(getCurrentDateTime());
		Date startDateTime = lib.combineDateTime(todayDate, startTime);
		Date endDateTime = lib.combineDateTime(todayDate, endTime);
		return getMinPrice(startDateTime, endDateTime);
	}
	
	/**
	 * Get minimum spread for given date
	 * @param date
	 * @return
	 */
	public double getMinSpread(String date) {
		return connector.getMinSpread(getCurrentSecurity(), date, date, true);
	}

	/**
	 * Get opening price
	 * @return
	 */
	public double getOpenPrice() {
		return getOpenPrice(getCurrentDateTime());
	}
	public double getOpenPrice(Date date) {
		return connector.getOpenPrice(getCurrentSecurity(), date);
	}
	public double getOpenPrice(String date) {
		return getOpenPrice(lib.convertISODateTimeString(date));
	}

	public Trade getOpeningTrade(String security, String date) {
		return connector.getOpeningTrade(security, date);
	}

	/**
	 * Retrieve quote for current date and time
	 * @param date Date in YYYY-MM-DD format
	 * @param time Time in HH:mm:ss.SSS format
	 * @return Quote at specified date and time
	 */
	public Quote getQuote(String date, String time) {
		return connector.getQuote(getCurrentSecurity(), date, time);
	}

	public Regression getRegression(double[] x, double[] y, double xValue, double confidence) {
		return mathLib.getRegression(x, y, xValue, confidence);
	}


	// ***************************************************************
	// TCOUNT FUNCTIONS
	// ***************************************************************

	/**
	 * Get transaction count between two dates (inclusive).
	 * @return int
	 */
	public int getTcount() {
		Date startDateTime = lib.getDateStart(getCurrentDateTime());
		return getTcount(startDateTime, getCurrentDateTime());
	}
	
	/**
	 * Get transaction count between two dates (inclusive).
	 */
	public int getTcount(String dateStart, String dateEnd) {
		return connector.getTcount(getCurrentSecurity(), dateStart, dateEnd);
	}
	/**
	 * Get transaction count between two dates (inclusive).
	 * @param startDateTime
	 * @param endDateTime
	 * @param maxEvents
	 * @return int
	 */
	public int getTcount(Date startDateTime, Date endDateTime) {
		return connector.getTcount(getCurrentSecurity(), startDateTime, endDateTime);
	}

	/**
	 * Return a trading date with specified offset
	 * @param date Date as date object
	 * @param offset 2 means 2 trading days ahead, -1 means 1 trading day back
	 * @return date as Date
	 */
	public String getTradingDate(Date date, int offset) {
		return getTradingDate(lib.dateToISODateString(date), offset);
	}
	/**
	 * Return a trading date with specified offset
	 * @param date as String
	 * @param offset 2 means 2 trading days ahead, -1 means 1 trading day back
	 * @return date as String
	 */
	public String getTradingDate(String date, int offset) {
		return connector.getTradingDate(date, offset);
	}
	

	// ***************************************************************
	// VALUE FUNCTIONS
	// ***************************************************************

	/**
	 * Get transaction dollar value between two dates (inclusive).
	 * @return double rounded to 4 decimal places
	 */
	public double getValue() {
		Date startDateTime = lib.getDateStart(getCurrentDateTime());
		return getValue(startDateTime, getCurrentDateTime(),0);
	}
	
	/**
	 * Get transaction dollar value between two dates (inclusive).
	 */
	public double getValue(String dateStart, String dateEnd) {
		return connector.getValue(getCurrentSecurity(), dateStart, dateEnd);
	}
	
	/**
	 * Calculates value between two dates (inclusive). Limit results to maxEvents.
	 * Negative (positive) maxEvents rolls backwards (forwards) in time.
	 * @param startDateTime
	 * @param endDateTime
	 * @param maxEvents
	 * @return double rounded to 4 decimal places
	 */
	public double getValue(Date startDateTime, Date endDateTime, int maxEvents) {
		return connector.getValue(getCurrentSecurity(), startDateTime, endDateTime, maxEvents);
	}


	// ***************************************************************
	// VOLUME FUNCTIONS
	// ***************************************************************

	/**
	 * Get transaction volume between two dates (inclusive).
	 * @return int
	 */
	public int getVolume() {
		Date startDateTime = lib.getDateStart(getCurrentDateTime());
		return getVolume(startDateTime, getCurrentDateTime(),0);
	}
	
	/**
	 * Calculates volume between two dates (inclusive). Limit results to maxEvents.
	 * Negative (positive) maxEvents rolls backwards (forwards) in time.
	 * @param startDateTime
	 * @param endDateTime
	 * @param maxEvents
	 * @return int
	 */
	public int getVolume(Date startDateTime, Date endDateTime, int maxEvents) {
		return connector.getVolume(getCurrentSecurity(), startDateTime, endDateTime, maxEvents);
	}


	// ***************************************************************
	// VWAP FUNCTIONS
	// ***************************************************************

	/**
	 * Calculates VWAP between start of today and now.
	 * @return double rounded to 4 decimal places
	 */
	public double getVWAP() {
		Date startDateTime = lib.getDateStart(getCurrentDateTime());
		return getVWAP(startDateTime, getCurrentDateTime());
	}

	/**
	 * Calculates VWAP between start date and now (inclusive).
	 * @param startDateTime, a date object
	 * @return double rounded to 4 decimal places
	 */
	public double getVWAP(Date startDateTime) {
		return getVWAP(startDateTime, getCurrentDateTime());
	}

	/**
	 * Calculates VWAP between two dates (inclusive).
	 * @param startDate
	 * @param endDate
	 * @return double rounded to 4 decimal places
	 */
	public double getVWAP(Date startDateTime, Date endDateTime) {
		return getVWAP(startDateTime, endDateTime, 0);
	}

	/**
	 * Calculates VWAP between two dates (inclusive). Limit results to maxEvents.
	 * Negative (positive) maxEvents rolls backwards (forwards) in time.
	 * @param startDateTime
	 * @param endDateTime
	 * @param maxEvents
	 * @return double rounded to 4 decimal places
	 */
	public double getVWAP(Date startDateTime, Date endDateTime, int maxEvents) {
		return connector.getVWAP(getCurrentSecurity(), startDateTime, endDateTime, maxEvents);
	}

	/**
	 * Calculates VWAP starting at a specified date. Limit results to maxEvents.
	 * Negative (positive) maxEvents rolls backwards (forwards) in time.
	 * @param date, a Date
	 * @param maxEvents
	 * @return double rounded to 4 decimal places
	 */
	public double getVWAP(Date date, int maxEvents) {

		Date startDateTime = null;
		Date endDateTime = null;

		if (maxEvents > 0) {
			startDateTime = date;
			endDateTime = lib.getDateEnd(date);
		} else if (maxEvents < 0) {
			startDateTime = lib.getDateStart(date);
			endDateTime = getCurrentDateTime();
		} else {
			startDateTime = date;
			endDateTime = getCurrentDateTime();
		}

		return getVWAP(startDateTime, endDateTime, maxEvents);
	}

	/**
	 * Calculates VWAP between two times on a given date.
	 * @param date, a Date object
	 * @param startTime, a String
	 * @param endTime, a String
	 * @return double rounded to 4 decimal places
	 */
	public double getVWAP(Date date, String startTime, String endTime) {
		Date startDateTime = lib.combineDateTime(date, startTime);
		Date endDateTime = lib.combineDateTime(date, endTime);
		return getVWAP(startDateTime, endDateTime);
	}

	/**
	 * Calculates VWAP between time and now.
	 * @param startTime, a String
	 * @return double rounded to 4 decimal places
	 */
	public double getVWAP(String startTime) {
		Date startDateTime = lib.combineDateTime(lib.dateToISODateString(getCurrentDateTime()), startTime);
		return getVWAP(startDateTime, getCurrentDateTime());
	}

	/**
	 * Calculates VWAP between two times.
	 * @param startTime, a String
	 * @param endTime, a String
	 * @return double rounded to 4 decimal places
	 */
	public double getVWAP(String startTime, String endTime) {
		String todayDate = lib.dateToISODateString(getCurrentDateTime());
		Date startDateTime = lib.combineDateTime(todayDate, startTime);
		Date endDateTime = lib.combineDateTime(todayDate, endTime);
		return getVWAP(startDateTime, endDateTime);
	}

	/**
	 * Calculates VWAP starting at a specified time. Limit results to maxEvents.
	 * Negative (positive) maxEvents rolls backwards (forwards) in time.
	 * @param time, a String
	 * @param maxEvents
	 * @return double rounded to 4 decimal places
	 */
	public double getVWAP(String time, int maxEvents) {

		Date startDateTime = null;
		Date endDateTime = null;
		String todayDate = lib.dateToISODateString(getCurrentDateTime());

		if (maxEvents > 0) {
			startDateTime = lib.combineDateTime(todayDate, time);
			endDateTime = getDateEnd(todayDate);
		} else if (maxEvents < 0) {
			startDateTime = getDateStart(todayDate);
			endDateTime = lib.combineDateTime(todayDate, time);
		} else {
			startDateTime = getDateStart(todayDate);
			endDateTime = getCurrentDateTime();
		}

		return getVWAP(startDateTime, endDateTime, maxEvents);
	}


	// ***************************************************************

	/**
	 * Round numbers
	 * @param double
	 */
	protected double round1(Double unrounded) {
		return lib.round1(unrounded);
	}	
	protected double round2(Double unrounded) {
		return lib.round2(unrounded);
	}	
	protected double round3(Double unrounded) {
		return lib.round3(unrounded);
	}	
	protected double round4(Double unrounded) {
		return lib.round4(unrounded);
	}	
	
	protected void setCSVHeader(String header) {
		tradingRecord = new StringBuffer(header + "\r\n");
	}


	/**
	 * Set the current time of the quote, order or trade
	 * the algo is processing
	 * @param date should contain time and date
	 */
	public void setCurrentDateTime(Date date) {
		currentDateTime = date;
	}

	/**
	 * Set the current security of the quote, order or trade
	 * the algo is processing
	 * @param security String representation of security
	 */
	public void setCurrentSecurity(String security) {
		currentSecurity = security;
	}

	/**
	 * Convert Double ArrayList to double array
	 * @param list
	 * @return
	 */
	public double[] toDoubleArray(ArrayList<Double> list) {
		return lib.toDoubleArray(list);
	}

	protected void writeRecord() {
		lib.writeFile(".", "trades.csv", tradingRecord.toString());
		lib.writeFile("import", "brokertrades.csv", brokerTrades.toString());
	}

}
