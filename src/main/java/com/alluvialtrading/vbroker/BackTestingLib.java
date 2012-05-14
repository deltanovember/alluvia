package com.alluvialtrading.vbroker;

import com.alluvialtrading.data.Benchmark;
import com.alluvialtrading.data.Quote;
import com.alluvialtrading.data.Trade;
import com.alluvialtrading.tools.TraderLib;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;



public abstract class BackTestingLib {

	Statement statement = null;
	PreparedStatement preparedStatement = null;
	Connection connection = null;
	TraderLib lib = new TraderLib();


	// lse tick data
	private static final String LSE_PRICE_FORMAT = "LSE_PRICE_FORMAT";
	private static final String LSE_PRICE_THRESHOLDS = "LSE_PRICE_THRESHOLDS";

	// Trading days
	private static final String TRADING_DAYS = "TRADING_DAYS";

	public static void main(String[] args) {
		TraderLib lib = new TraderLib();
		BackTestingLib connector = new ASXBackTestingLib();

		//Quote quote = connector.getPrecloseQuote("2011-03-16", "BHP");

		//double mid = (quote.getBid() + quote.getAsk()) / 2;

		//System.out.println(connector.isTradingDate("ASX", "2011-02-07"));
		//Trade trade = connector.getClosingPrice("BHP", "2011-03-04");
		//System.out.println(connector.getTradingDate("3009-07-03", -1));
		//System.out.println(trade.getPrice());
		Date start = lib.convertISODateTimeString("2010-01-01 00:00:01.000");
		Date end = lib.convertISODateTimeString("2010-03-01 23:00:01.000");
		//connector.setCurrentSecurity("BHP");
		long time = System.currentTimeMillis();
		System.out.println(connector.getMinSpread("BHP.AX", "2010-03-01", "2010-03-02", true));
		System.out.println(System.currentTimeMillis() - time);

		//String[] traded = connector.getAllTradedSecurities("2010-03-04");
		//for (String security : traded) {
		//	System.out.println(security);
		//}
		connector.testAllEvents();
		//connector.setCurrentDateTime(lib.convertISODateTimeString("2010-03-05 13:00:01.000"));
		//System.out.println(connector.getVolume(start));
		//System.out.println(connector.getVolume());

	}

	public BackTestingLib() {
		init();
	}

	// Market design rules
	public abstract String getCloseTime();
	public abstract String getMarketName();

	/**
	 * Get all securities that traded on a given date. Uses
	 * raw trade table not benchmark table. Therefore slow
	 * @return   Array of String for all securities trading on that day
	 */
	public String[] getAllSecurities(String date) {
		
		String startDate = date + " 00:00:00";
		String endDate = date + " 23:59:59";
		
		String query = "SELECT DISTINCT(\"SECURITY\") FROM \"" + getMarketName() + "TRADES\" " + 
			"WHERE \"DATETIME\" > '" + startDate + "' AND " + 
			"\"DATETIME\" < '" + endDate + "' ORDER BY \"SECURITY\"";
 

		ArrayList<String> allSecurities = new ArrayList<String>();
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			while (result.next()) { // process results one row at a time
				allSecurities.add(result.getString(1));
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
		}
		
		String[] allArray = new String[allSecurities.size()];
		for (int i=0; i<allArray.length; i++) {
			allArray[i] = allSecurities.get(i);
		}
		return allArray;
		
	}

	
	/**
	 * Get all securities that traded on a given date
	 * @param date Date in ISO format
	 * @return
	 */
	public String[] getAllTradedSecurities(String date) {

		
		String query = "SELECT \"SECURITY\" FROM \"" + getMarketName() + "BENCHMARK\" " + 
			"WHERE \"DATE\" = '" + date + "'";
 

		ArrayList<String> allSecurities = new ArrayList<String>();
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			while (result.next()) { // process results one row at a time
				allSecurities.add(result.getString(1));
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
		}
		
		String[] allArray = new String[allSecurities.size()];
		for (int i=0; i<allArray.length; i++) {
			allArray[i] = allSecurities.get(i).trim();
		}
		return allArray;
		
	}
	
	private String getBenchmarkTable() {
		return getMarketName() + "BENCHMARK";
	}
	public Benchmark getBenchmarkData(String security, String date) {

		String getTcountQuery = "SELECT *" +
		" FROM \"" + getBenchmarkTable() + 
		"\" where \"SECURITY\"='" + security +  
		"' and \"DATE\"='" + date +
		"'";


		//System.out.println(getValueQuery);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(getTcountQuery);

			if (result.next()) { // process results one row at a time
				
				double open = result.getDouble(3);
				double close = result.getDouble(4);
				double preclose = result.getDouble(5);
				double high = result.getDouble(6);
				double low = result.getDouble(7);
				int tcount = result.getInt(8);
				int volume = result.getInt(9);
				double value = result.getDouble(10);
				
				return new Benchmark(security, date, open, close, preclose, 
						high, low, tcount, volume, value);

			}			

			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(getTcountQuery);
			
		}


		return null;
	}

	/**
	 * Get the market close price on the date specified
	 * @param date
	 * @param security
	 * @return
	 */
	public double getClosePrice(String security, Date date) {

		// Query initialisation
		String startString = lib.dateToISODateTimeString(lib.getDateStart(date));
		String endString = lib.dateToISODateTimeString(lib.getDateEnd(date));

		String query = "SELECT \"TRADEPRICE\"" +
		" FROM \"" + getTradeTable() + 
		"\" where \"DATETIME\">='" + startString +
		"' and \"DATETIME\"<='" + endString + 
		"' and \"SECURITY\"='" + security + "'" + 
		"  ORDER BY \"DATETIME\" DESC LIMIT 1"; 

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				return lib.round4(result.getDouble(1));
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		return -1;
	}

	/**
	 * 
	 * @param security
	 * @param date
	 * @return
	 */
	public Trade getClosingTrade(String security, String date) {

		String dateStart = date + " 16:00:00";
		String dateEnd = date + " 16:16:00";
		Trade trade = null;

		String query = "SELECT \"TRADEPRICE\", \"TRADEVOLUME\", \"DATETIME\"" +
		" FROM \"" + getTradeTable() + 
		"\" where \"DATETIME\"<='" + dateEnd + 
		"' and \"DATETIME\">='" + dateStart + 
		"' and \"SECURITY\"='" + security + "'" + 
		"  ORDER BY \"DATETIME\" DESC LIMIT 1"; 

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				trade = new Trade();
				trade.setPrice(lib.round4(result.getDouble(1)));
				//trade.setVolume(result.getInt(2));
				trade.setDate(result.getTimestamp(3));
				//System.out.println(result.getDouble(1));
				//
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		if (null != trade) {
			// find volume 
			query = "SELECT sum(\"TRADEVOLUME\")" +
			" FROM \"" + getTradeTable() + 
			"\" where \"DATETIME\"<='" + dateEnd + 
			"' and \"DATETIME\">='" + dateStart +
			"' and \"SECURITY\"='" + security +  
			"'"; 

			try {
				statement = connection.createStatement();
				ResultSet result = statement.executeQuery(query);
				if (result.next()) { // process results one row at a time
					trade.setVolume(result.getInt(1));
				}
				statement.close();

			} catch(SQLException ex) {
				System.err.println("SQLException: " + ex.getMessage());
				System.out.println(query);
				
			}
		}
		return trade;
	}



	public void init() {

		try {

			try {

				Class.forName("org.postgresql.Driver");

			} catch (ClassNotFoundException e) {

				System.out.println("Where is your PostgreSQL JDBC Driver? "
						+ "Include in your library path!");
				e.printStackTrace();
				return;

			}

			try {

				connection = DriverManager.getConnection(
						"jdbc:postgresql://192.168.16.23:5432/database", "owner",
				"t3stt3st");

			} catch (SQLException e) {

				System.out.println("Connection Failed! Check output console");
				e.printStackTrace();
				return;

			}

			if (connection != null) {
				//System.out.println("You made it, take control your database now!");
			} else {
				System.out.println("Failed to make connection!");
			}

		}

		catch (Exception ex) {
			ex.printStackTrace();
		}

	}
	
	/**
	 * Get the first price between two dates
	 * @param security    security to look up
	 * @param dateStart  start date
	 * @param dateEnd
	 * @return
	 */
	public double getFirstPrice(String security, Date dateStart, Date dateEnd) {

		// Query initialisation
		String startString = lib.dateToISODateTimeString(dateStart);
		String endString = lib.dateToISODateTimeString(dateEnd);

		String query = "SELECT \"TRADEPRICE\"" +
		" FROM \"" + getTradeTable() + 
		"\" where \"DATETIME\">='" + startString +
		"' and \"DATETIME\"<='" + endString + 
		"' and \"SECURITY\"='" + security + "'" + 
		"  ORDER BY \"DATETIME\" ASC LIMIT 1"; 

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				return lib.round4(result.getDouble(1));
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		return -1;
	}
	
	/**
	 * Get the date of first trade in database
	 * @return
	 */
	public Date getFirstTradeDate() {
		
		String query = "SELECT MIN(\"DATETIME\") FROM \"ASXTRADES\"";
 

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				return result.getTimestamp(1);
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
		}
		
		return null;
		
	}

	/**
	 * Get the last price between two dates
	 * @param security
	 * @param dateStart start date
	 * @param dateEnd   end date
	 * @return
	 */
	public double getLastPrice(String security, Date dateStart, Date dateEnd) {

		// Query initialisation
		String startString = lib.dateToISODateTimeString(dateStart);
		String endString = lib.dateToISODateTimeString(dateEnd);

		String query = "SELECT \"TRADEPRICE\"" +
		" FROM \"" + getTradeTable() + 
		"\" where \"DATETIME\">='" + startString +
		"' and \"DATETIME\"<='" + endString + 
		"' and \"SECURITY\"='" + security + "'" + 
		"  ORDER BY \"DATETIME\" DESC LIMIT 1"; 

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				return lib.round4(result.getDouble(1));
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		return -1;
	}
	
	/**
	 * 
	 * @param segment
	 * @param currency
	 * @param price
	 * @return tick size if found, -1
	 */
	public double getLSETickSize(String segment, String currency, double price) {

		double tickSize = -1;

		String query = "SELECT \"VALUE\"" +
		" FROM \"" + LSE_PRICE_FORMAT + 
		"\" where \"CODE\"=" + "(SELECT \"CODE\"" + 
		" FROM \"" + LSE_PRICE_THRESHOLDS +  
		"\" where \"SEGMENT\"='" + segment + 
		"' and \"CURRENCY\"='" + currency + 
		"' and \"MINPRICE\"<" + price + 
		" and \"MAXPRICE\">" + price + 
		" LIMIT 1)";
		//System.out.println(query);

		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) { // process results one row at a time

				return result.getDouble(1);

			}
			statement.close();
			//connection.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}


		return tickSize;

	}

	/**
	 * 
	 * @param security
	 * @param date
	 * @return
	 */
	public Trade getLastTrade(String security, String date) {

		String dateStart = date;
		String dateEnd = date + " 23:59:00";
		Trade trade = null;

		String query = "SELECT \"TRADEPRICE\", \"TRADEVOLUME\", \"DATETIME\"" +
		" FROM \"" + getTradeTable() + 
		"\" where \"DATETIME\"<='" + dateEnd + 
		"' and \"DATETIME\">='" + dateStart + 
		"' and \"SECURITY\"='" + security + "'" + 
		"  ORDER BY \"DATETIME\" DESC LIMIT 1"; 

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				trade = new Trade();
				trade.setPrice(lib.round4(result.getDouble(1)));
				trade.setDate(result.getTimestamp(3));
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		if (null != trade) {
			// find volume 
			query = "SELECT sum(\"TRADEVOLUME\")" +
			" FROM \"" + getTradeTable() + 
			"\" where \"DATETIME\"<='" + dateEnd + 
			"' and \"DATETIME\">='" + dateStart +
			"' and \"SECURITY\"='" + security +  
			"'"; 

			try {
				statement = connection.createStatement();
				ResultSet result = statement.executeQuery(query);
				if (result.next()) { // process results one row at a time
					trade.setVolume(result.getInt(1));
				}
				statement.close();

			} catch(SQLException ex) {
				System.err.println("SQLException: " + ex.getMessage());
				System.out.println(query);
				
			}
		}
		return trade;
	}
	
	/**
	 * Get the date of last trade in database
	 * @return
	 */
	public Date getLastTradeDate() {
		
		String query = "SELECT MAX(\"DATETIME\") FROM \"ASXTRADES\"";
 

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				return result.getTimestamp(1);
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
		}
		
		return null;
		
	}

	public double getMarketPrice(Trade trade) {


		String query = "SELECT \"BID\",\"ASK\"" +
		" FROM \"" + getQuoteTable() + "\" where \"DATETIME\"<='" + lib.dateToISODateTimeString(trade.getDate()) + 
		"' and \"SECURITY\"='" + trade.getSecurity() + 
		"'  ORDER BY \"DATETIME\" DESC LIMIT 1";

		//System.out.println(query);
		String closeStrategy = trade.getClosingStrategy();
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				double bid = result.getDouble(1);
				double ask = result.getDouble(2);
				double mid = (bid + ask) / 2;

				if (closeStrategy.equals(Trade.EXIT_MID)) {
					return mid;
				}
				else if (closeStrategy.equals(Trade.EXIT_AGGRESSIVE)) {

					// buy at askBefore
					if (trade.getVolume() > 0) {
						return ask;
					}
					else {
						return bid;
					}
				}
				else {
					// buy at bidBefore
					if (trade.getVolume() > 0) {
						return bid;
					}
					else {
						return ask;
					}
				}
			}
			statement.close();
			//connection.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}


		return 0;
	}

	public double getMaxPrice(String security, Date dateEnd, int maxEvents) {

		// Query initialisation
		String endString = lib.dateToISODateTimeString(dateEnd);

		String query = "SELECT MAX(\"TRADEPRICE\") " +
		" FROM (SELECT \"TRADEPRICE\" FROM \"" + getMarketName() + "TRADES\" " +
				" WHERE \"DATETIME\" <='" + endString + "' and \"SECURITY\"='" + security + "'  " +
				" ORDER BY \"DATETIME\" DESC LIMIT " + maxEvents + ") AS PRICEQUERY";

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				return lib.round4(result.getDouble(1));
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		return -1;
	}
	
	/**
	 * Get the maximum trade price between two dates
	 * @param security, String
	 * @param dateEnd, Date object
	 * @param dateStart, Date object
	 * @return price, double
	 */
	public double getMaxPrice(String security, Date dateStart, Date dateEnd) {

		// Query initialisation
		String startString = lib.dateToISODateTimeString(dateStart);
		String endString = lib.dateToISODateTimeString(dateEnd);

		String query = "SELECT \"TRADEPRICE\"" +
		" FROM \"" + getTradeTable() + 
		"\" where \"DATETIME\">='" + startString +
		"' and \"DATETIME\"<='" + endString + 
		"' and \"SECURITY\"='" + security + "'" + 
		"  ORDER BY \"TRADEPRICE\" DESC LIMIT 1"; 

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				return lib.round4(result.getDouble(1));
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		return -1;
	}
	
	public double getMinPrice(String security, Date dateEnd, int maxEvents) {

		// Query initialisation
		String endString = lib.dateToISODateTimeString(dateEnd);

		String query = "SELECT MIN(\"TRADEPRICE\") " +
		" FROM (SELECT \"TRADEPRICE\" FROM \"" + getMarketName() + "TRADES\" " +
				" WHERE \"DATETIME\" <='" + endString + "' and \"SECURITY\"='" + security + "'  " +
				" ORDER BY \"DATETIME\" DESC LIMIT " + maxEvents + ") AS PRICEQUERY";

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				return lib.round4(result.getDouble(1));
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		return -1;
	}

	/**
	 * Get the minimum trade price between two dates
	 * @param security, String
	 * @param dateEnd, Date object
	 * @param dateStart, Date object
	 * @return price, double
	 */
	public double getMinPrice(String security, Date dateStart, Date dateEnd) {

		// Query initialisation
		String startString = lib.dateToISODateTimeString(dateStart);
		String endString = lib.dateToISODateTimeString(dateEnd);

		String query = "SELECT \"TRADEPRICE\"" +
		" FROM \"" + getTradeTable() + 
		"\" where \"DATETIME\">='" + startString +
		"' and \"DATETIME\"<='" + endString + 
		"' and \"SECURITY\"='" + security + "'" + 
		"  ORDER BY \"TRADEPRICE\" ASC LIMIT 1"; 

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				return lib.round4(result.getDouble(1));
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		return -1;
	}
	
	public double getMinSpread(String security, String dateStart, String dateEnd, boolean useBenchmark) {

		// Query initialisation
		String startString = dateStart;
		String endString = dateEnd + " 23:59:59";

		String query = "SELECT \"DATETIME\", \"BID\", \"ASK\", MIN(\"ASK\" - \"BID\") AS DELTA " +
				" FROM \"" + getQuoteTable() + "\" WHERE " +
				" \"SECURITY\"='" + security + "' AND \"DATETIME\">'" + startString + "' AND " +
				" \"DATETIME\"<'" + endString + "' " +
				" GROUP BY \"DATETIME\", \"BID\", \"ASK\" " +
				" HAVING (\"ASK\" - \"BID\") > 0 " +
				" ORDER BY MIN(\"ASK\" - \"BID\") LIMIT 1";
		
		if (useBenchmark) {
			query = "SELECT \"MINSPREAD\"" +
					" FROM \"" + getBenchmarkTable() + "\" WHERE " +
					" \"SECURITY\"='" + security + "' AND " + 
					" \"DATE\"='" + dateStart + "'";
		}

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				if (useBenchmark) {
					return lib.round4(result.getDouble(1));
				}
				else
					return lib.round4(result.getDouble(4));
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		return -1;
	}

	/**
	 * Get first trade of the day
	 * @param security
	 * @param date
	 * @return
	 */
	public Trade getOpeningTrade(String security, String date) {

		String dateStart = date + " 07:30:00";
		String dateEnd = date + " 10:11:00";
		Trade trade = null;

		String query = "SELECT \"TRADEPRICE\", \"TRADEVOLUME\", \"DATETIME\"" +
		" FROM \"" + getTradeTable() + "\" where \"DATETIME\"<='" + dateEnd + 
		"' and \"DATETIME\">='" + dateStart + 
		"' and \"SECURITY\"='" + security + "'" + 
		"  ORDER BY \"DATETIME\" ASC LIMIT 1"; 

		System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				trade = new Trade();
				trade.setPrice(lib.round4(result.getDouble(1)));
				//trade.setVolume(result.getInt(2));
				trade.setDate(result.getTimestamp(3));
				//System.out.println(result.getDouble(1));
				//
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		if (null != trade) {
			// find volume 
			query = "SELECT sum(\"TRADEVOLUME\")" +
			" FROM \"" + getTradeTable() + "\" where \"DATETIME\"='" + 
			lib.dateToISODateTimeString(trade.getDate()) +
			"' and \"SECURITY\"='" + security +  
			"'"; 

			//System.out.println(query);
			try {
				statement = connection.createStatement();
				ResultSet result = statement.executeQuery(query);
				if (result.next()) { // process results one row at a time
					trade.setVolume(result.getInt(1));
				}
				statement.close();

			} catch(SQLException ex) {
				System.err.println("SQLException: " + ex.getMessage());
				System.out.println(query);
				
			}
		}
		return trade;
	}

	/**
	 * Get the market open price on the date specified
	 * @param date
	 * @param security
	 * @return
	 */
	public double getOpenPrice(String security, Date date) {

		// Note: currently this function actually returns the market open but the first trade price
		// Query initialisation
		String startString = lib.dateToISODateTimeString(lib.getDateStart(date));
		String endString = lib.dateToISODateTimeString(lib.getDateEnd(date));

		String query = "SELECT \"TRADEPRICE\"" +
		" FROM \"" + getTradeTable() + 
		"\" where \"DATETIME\">='" + startString +
		"' and \"DATETIME\"<='" + endString + 
		"' and \"SECURITY\"='" + security + "'" + 
		"  ORDER BY \"DATETIME\" ASC LIMIT 1"; 

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				return lib.round4(result.getDouble(1));
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		return -1;
	}

	/**
	 * 
	 * @param date
	 * @param time
	 * @param security
	 * @return
	 */
	public Quote getQuote(String security, String date, String time) {

		String startTime = date;
		String endTime = date + " " + time;

		String query = "SELECT \"BID\", \"ASK\", \"BIDVOLUME\", \"ASKVOLUME\", \"DATETIME\"" +
		" FROM \"" + getQuoteTable() + 
		"\" where \"DATETIME\">='" + startTime + 
		"' and \"DATETIME\"<='" + endTime + 
		"' and \"SECURITY\"='" + security + 
		"'  ORDER BY \"DATETIME\" DESC LIMIT 1";

		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);

			if (result.next()) { // process results one row at a time

				int bidVolume = result.getInt(3);
				int askVolume = result.getInt(4);

				double bid = result.getDouble(1);
				double ask = result.getDouble(2);

				String dateTime = result.getString(5);

				Quote quote = new Quote(lib.convertISODateTimeString(dateTime), security, bidVolume,askVolume,bid,ask);
				return quote;

			}
			statement.close();
			//connection.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}


		return null;
	}

	/**
	 * 
	 * @param date
	 * @param time
	 * @param security
	 * @return
	 */
	public Trade getTrade(String date, String time, String security) {

		String startTime = date;
		String endTime = date + " " + time;
		Trade trade = null;

		String query = "SELECT \"TRADEPRICE\", \"TRADEVOLUME\", \"DATETIME\"" +
		" FROM \"" + getTradeTable() + 
		"\" where \"DATETIME\">='" + startTime + 
		"' and \"DATETIME\"<='" + endTime + 
		"' and \"SECURITY\"='" + security + "'" + 
		"  ORDER BY \"DATETIME\" DESC LIMIT 1"; 

		//System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				trade = new Trade();
				trade.setPrice(lib.round4(result.getDouble(1)));
				trade.setDate(result.getTimestamp(3));
			}
			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		if (null != trade) {
			// find volume 
			query = "SELECT sum(\"TRADEVOLUME\")" +
			" FROM \"" + getTradeTable() + 
			"\" where \"DATETIME\">='" + startTime + 
			"' and \"DATETIME\"<='" + endTime + 
			"' and \"SECURITY\"='" + security +  
			"'"; 

			try {
				statement = connection.createStatement();
				ResultSet result = statement.executeQuery(query);
				if (result.next()) { // process results one row at a time
					trade.setVolume(result.getInt(1));
				}
				statement.close();

			} catch(SQLException ex) {
				System.err.println("SQLException: " + ex.getMessage());
				System.out.println(query);
				
			}
		}
		return trade;
	}

	/**
	 * Get a particular trading date
	 * @param date
	 * @param offset 2=2 days ahead, -5=5 days back
	 * @return
	 */
	public String getTradingDate(String date, int offset) {

		// Query initialisation
		//String dateString = lib.dateToISODateString(date);
		
		String sign = null;
		if (0 == offset) {
			return date;
		}
		else if (offset > 0) {
			sign = ">";
		}
		else {
			sign = "<";
		}

		// If nothing greater then out of range
		String query = "SELECT \"DATE\"" +
		" FROM \"" + TRADING_DAYS + 
		"\" where \"DATE\"" + ">" + "'" + date + 
		"' AND \"MARKET\"='" + getMarketName() + "'";
		int scroll = ResultSet.TYPE_SCROLL_INSENSITIVE;
		int update = ResultSet.CONCUR_UPDATABLE;
		try {
			preparedStatement = connection.prepareStatement(query, scroll, update);
			ResultSet result = preparedStatement.executeQuery();
			if (!result.next()) {
				return null;
			}
		}
		catch(SQLException ex) {
			ex.printStackTrace();
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		// If nothing less then out of range
		query = "SELECT \"DATE\"" +
		" FROM \"" + TRADING_DAYS + 
		"\" where \"DATE\"" + "<" + "'" + date + 
		"' AND \"MARKET\"='" + getMarketName() + "'";
		try {
			preparedStatement = connection.prepareStatement(query, scroll, update);
			ResultSet result = preparedStatement.executeQuery();
			if (!result.next()) {
				return null;
			}
		}
		catch(SQLException ex) {
			ex.printStackTrace();
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		query = "SELECT \"DATE\"" +
		" FROM \"" + TRADING_DAYS + 
		"\" where \"DATE\"" + sign + "'" + date + 
		"' AND \"MARKET\"='" + getMarketName() + "'";
		//System.out.println(query);
		try {
			preparedStatement = connection.prepareStatement(query, scroll, update);
			ResultSet result = preparedStatement.executeQuery();
			if (!result.next()) {
				return null;
			}
			result.absolute(offset);
			//return null;
			//return lib.convertISODateTimeString(result.getString(1));
			return result.getString(1);

		}

		catch(SQLException ex) {
			ex.printStackTrace();
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}


		return null;
	}
	
	/**
	 * Get transaction count between two dates inclusive
	 * @param security
	 * @param dateStart
	 * @param dateEnd
	 * @return
	 */
	public int getTcount(String security, String dateStart, String dateEnd) {

		String getTcountQuery = "SELECT SUM(\"TCOUNT\") " +
		" FROM \"" + getBenchmarkTable() + 
		"\" where \"SECURITY\"='" + security +  
		"' and \"DATE\">='" + dateStart +
		"' and \"DATE\"<='" + dateEnd + "'";

		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(getTcountQuery);

			if (result.next()) { // process results one row at a time
				return result.getInt(1);

			}			

			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(getTcountQuery);
			
		}


		return -1;
	}
	
	private String getQuoteTable() {
		return getMarketName() + "QUOTES";
	}

	/**
	 * Returns the number of on-market trades according to overloaded 
	 * getTcount function.
	 * @param dateStart
	 * @param dateEnd
	 * @return
	 */
	public int getTcount(String security, Date dateStart, Date dateEnd) {

		int totalTrades = 0;
		boolean sameDay = false;
		try {
			
			String endDateString = lib.dateToISODateString(dateEnd);
			String startDateString = lib.dateToISODateString(dateStart);
			if (startDateString.equals(endDateString)) {
				sameDay = true;
			}
			// Step 1 - count first day
			String startFirstDay = lib.dateToISODateTimeString(dateStart);
			
			// if multi day, count entire day
			String endFirstDay = lib.dateToISODateTimeString(lib.combineDateTime(dateStart, "23:59:59"));
			
			// otherwise terminate at specified time
			if (sameDay) {
				endFirstDay = lib.dateToISODateTimeString(dateEnd);
			}

			String getTcountQuery = "SELECT COUNT(\"TRADEVOLUME\") " + 
			" FROM \"" + getTradeTable() + 
			"\" where \"SECURITY\"='" + security +
			"' and \"DATETIME\">='" + startFirstDay +
			"' and \"DATETIME\"<='" + endFirstDay + "'";
			

			//System.out.println(getTcountQuery);
			
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(getTcountQuery);

			if (result.next()) { // process results one row at a time
				totalTrades += result.getInt(1);
				if (sameDay) {
					return totalTrades;
				}
			}
			
			// Step 2 use benchmark to obtain dates in between
			String currentDate = getTradingDate(startDateString, 1);
			
			while (!currentDate.equals(endDateString)) {
				Benchmark benchmark = getBenchmarkData(security, currentDate);
				if (null != benchmark) {
					totalTrades += benchmark.getTcount();
				}
				currentDate = getTradingDate(currentDate, 1);
			}
			
			// Step 3 - count last day
			String startLastDay = lib.dateToISODateTimeString(lib.combineDateTime(dateEnd, "00:00:00"));
			
			// if multi day, count entire day
			String endLastDay = lib.dateToISODateTimeString(dateEnd);

			getTcountQuery = "SELECT COUNT(\"TRADEVOLUME\") " + 
			" FROM \"" + getTradeTable() + 
			"\" where \"SECURITY\"='" + security +
			"' and \"DATETIME\">='" + startLastDay +
			"' and \"DATETIME\"<='" + endLastDay + "'";
			

			//System.out.println(getTcountQuery);
			
			statement = connection.createStatement();
			result = statement.executeQuery(getTcountQuery);

			if (result.next()) { // process results one row at a time
				totalTrades += result.getInt(1);
				return totalTrades;
			}
			

		} 
		catch (SQLException e ) {
			e.printStackTrace();
		} 

		finally {


			try {
				statement.close();

			} 
			catch (SQLException e ) {
				e.printStackTrace();
			} 

		}
		return -1;
	}

	/**
	 * Get value between two dates inclusive
	 * @param security
	 * @param dateStart
	 * @param dateEnd
	 * @return
	 */
	public double getValue(String security, String dateStart, String dateEnd) {

		String getValueQuery = "SELECT SUM(\"VALUE\") " +
		" FROM \"" + getBenchmarkTable() + 
		"\" where \"SECURITY\"='" + security +  
		"' and \"DATE\">='" + dateStart +
		"' and \"DATE\"<='" + dateEnd + "'";

		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(getValueQuery);

			if (result.next()) { // process results one row at a time
				return result.getDouble(1);

			}			

			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(getValueQuery);
			
		}


		return -1;
	}
	
	private String getTradeTable() {
		return getMarketName() + "TRADES";
	}
	
	/**
	 * Returns the value of trades according to overloaded 
	 * getValue function.
	 * @param dateStart  start date
	 * @param dateEnd    end date
	 * @return
	 */
	public double getValue(String security, Date dateStart, Date dateEnd, int maxEvents) {

		double totalValue = 0;
		boolean sameDay = false;
		try {
			
			String endDateString = lib.dateToISODateString(dateEnd);
			String startDateString = lib.dateToISODateString(dateStart);
			if (startDateString.equals(endDateString)) {
				sameDay = true;
			}
			// Step 1 - count first day
			String startFirstDay = lib.dateToISODateTimeString(dateStart);
			
			// if multi day, count entire day
			String endFirstDay = lib.dateToISODateTimeString(lib.combineDateTime(dateStart, "23:59:59"));
			
			// otherwise terminate at specified time
			if (sameDay) {
				endFirstDay = lib.dateToISODateTimeString(dateEnd);
			}

			String getValueQuery = "SELECT SUM(\"TRADEPRICE\"* \"TRADEVOLUME\")" +
			" FROM \"" + getTradeTable() + 
			"\" where \"SECURITY\"='" + security +  
			"' and \"DATETIME\">='" + startFirstDay +
			"' and \"DATETIME\"<='" + endFirstDay + "'";
			

			//System.out.println(getTcountQuery);
			
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(getValueQuery);

			if (result.next()) { // process results one row at a time
				totalValue += result.getDouble(1);
				if (sameDay) {
					return totalValue;
				}
			}
			
			// Step 2 use benchmark to obtain dates in between
			String currentDate = getTradingDate(startDateString, 1);
			
			while (!currentDate.equals(endDateString)) {
				Benchmark benchmark = getBenchmarkData(security, currentDate);
				if (null != benchmark) {
					totalValue += benchmark.getValue();
				}
				currentDate = getTradingDate(currentDate, 1);
			}
			
			// Step 3 - count last day
			String startLastDay = lib.dateToISODateTimeString(lib.combineDateTime(dateEnd, "00:00:00"));
			
			// if multi day, count entire day
			String endLastDay = lib.dateToISODateTimeString(dateEnd);

			getValueQuery = "SELECT (\"TRADEPRICE\"* \"TRADEVOLUME\")" +
			" FROM \"" + getTradeTable() + 
			"\" where \"SECURITY\"='" + security +  
			"' and \"DATETIME\">='" + startLastDay +
			"' and \"DATETIME\"<='" + endLastDay + "'";
			

			//System.out.println(getTcountQuery);
			
			statement = connection.createStatement();
			result = statement.executeQuery(getValueQuery);

			if (result.next()) { // process results one row at a time
				totalValue += result.getDouble(1);
				return totalValue;
			}
			

		} 
		catch (SQLException e ) {
			e.printStackTrace();
		} 

		finally {


			try {
				statement.close();

			} 
			catch (SQLException e ) {
				e.printStackTrace();
			} 

		}
		return -1;

	}

	/**
	 * Returns the volume of on-market trades according to overloaded 
	 * getVolume function.
	 * @param dateStart
	 * @param dateEnd
	 * @return
	 */
	public int getVolume(String security, Date dateStart, Date dateEnd, int maxEvents) {

		try {

			// Query initialisation
			String startString = lib.dateToISODateTimeString(dateStart);
			String endString = lib.dateToISODateTimeString(dateEnd);

			String getVolumeQuery = "SELECT SUM(\"TRADEVOLUME\") " + 
			" FROM \"" + getTradeTable() + 
			"\" where \"DATETIME\">='" + startString +
			"' and \"DATETIME\"<='" + endString + 
			"' and \"SECURITY\"='" + security + "'";

			// System.out.println(getVolume.toString());
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(getVolumeQuery);

			if (result.next()) { // process results one row at a time
				return result.getInt(1);
			}

		} 
		catch (SQLException e ) {
			e.printStackTrace();
		} 

		finally {


			try {
				statement.close();

			} 
			catch (SQLException e ) {
				e.printStackTrace();
			} 

		}
		return -1;
	}

	/**
	 * Calculates VWAP according to overloaded getVWAP function.
	 * @param dateStart
	 * @param dateEnd
	 * @param maxEvents
	 * @return
	 */
	public double getVWAP(String security, Date dateStart, Date dateEnd, int maxEvents) {

		// Query initialisation
		String startString = lib.dateToISODateTimeString(dateStart);
		String endString = lib.dateToISODateTimeString(dateEnd);

		//System.out.println(security + " " + startString + " " + endString + " " + maxEvents);

		String getVWAPQuery = "SELECT \"TRADEPRICE\", \"TRADEVOLUME\"" +
		" FROM \"" + getTradeTable() + 
		"\" where \"DATETIME\">='" + startString +
		"' and \"DATETIME\"<='" + endString + 
		"' and \"SECURITY\"='" + security + "'" + 
		" ORDER BY \"DATETIME\" ";
		if (maxEvents > 0) {
			getVWAPQuery += "ASC LIMIT '" + Math.abs(maxEvents) + "'";
		} else if (maxEvents < 0) {
			getVWAPQuery += "DESC LIMIT '" + Math.abs(maxEvents) + "'";
		} else {
			getVWAPQuery += "DESC";
		}

		//System.out.println(getVWAPQuery);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(getVWAPQuery);
			double totalVolume = 0;
			double totalValue = 0;
			while (result.next()) { // process results one row at a time
				double currentPrice = result.getDouble(1);
				double currentVolume = result.getDouble(2);
				double currentValue = currentPrice * currentVolume;

				totalVolume += currentVolume;
				totalValue += currentValue;

			}			

			statement.close();

			// VWAP
			return lib.round4(totalValue / totalVolume);


		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(getVWAPQuery);
			
		}


		return 0;
	}

	/**
	 * Exit strategy assuming we capture VWAP
	 * @param trade
	 * @return
	 */
	public double getVWAPPrice(Trade trade) {

		String query = "SELECT \"TRADEPRICE\",\"TRADEVOLUME\"" +
		" FROM \"" + getTradeTable() + "\" where \"DATETIME\">='" + lib.dateToISODateTimeString(trade.getDate()) + 
		"' and \"SECURITY\"='" + trade.getSecurity() + "'" + 
		"  ORDER BY \"DATETIME\" ASC LIMIT 1000";

		System.out.println(query);
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			double tradeVolume = Math.abs(trade.getVolume());
			double totalVolume = 0;
			double totalValue = 0;
			while (result.next()) { // process results one row at a time
				double currentPrice = result.getDouble(1);
				double currentVolume = result.getDouble(2);
				double currentValue = currentPrice * currentVolume;

				totalVolume += currentVolume;
				totalValue += currentValue;

				if (totalVolume > 5 * tradeVolume) {
					break;
				}

			}			

			statement.close();

			// VWAP
			return totalValue / totalVolume;


		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}


		return 0;
	}
	

	
	public void insertQuote(Quote quote) {
		String insertString;

		insertString = "insert into \"" + getQuoteTable() + "\" values(" +
		//message.getTransactionId() + "," +
		"'" + lib.dateToISODateTimeString(quote.getDateTime()) + "'," +
		"'" + quote.getSecurity() + "'," +
		quote.getBid() + "," +
		quote.getAsk() + "," +
		quote.getBidVolume() + "," +
		quote.getAskVolume() + 
		")";



		try {
			statement = connection.createStatement();
			statement.executeUpdate(insertString);
			statement.close();
			//connection.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(insertString);
			
		}
	}
	
	public void insertBenchmark(String date, String security, double open, 
			double close, double preclose, 
			double high, double low, int tcount,
			int volume, double value, double minSpread) {
		String insertString;

		insertString = "INSERT into \"" + getMarketName() + "BENCHMARK\" values(" +
		"'" + date + "'," + 
		"'" + security + "'," + 
		open + "," + 
		close + "," + 
		preclose + "," + 
		high + "," + 
		low + "," + 
		tcount + "," + 
		volume + "," + 
		value + "," + 
		minSpread + 
		")";



		try {
			statement = connection.createStatement();
			statement.executeUpdate(insertString);
			statement.close();
			//connection.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(insertString);
			
		}
	}

	public boolean isTradingDate(String market, Date date) {
		
		boolean tradingDate = false;
		
		// Query initialisation
		String dateString = lib.dateToISODateString(date);

		String query = "SELECT \"MARKET\",\"DATE\"" +
		" FROM \"" + TRADING_DAYS + 
		"\" where \"MARKET\"='" + market + 
		"' and \"DATE\"='" + dateString + 
		"'";

		//System.out.println(query);
		//String closeStrategy = trade.getClosingStrategy();
		try {
			statement = connection.createStatement();
			ResultSet result = statement.executeQuery(query);
			if (result.next()) { // process results one row at a time
				return true;
			}
			statement.close();
			//connection.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}

		return tradingDate;
	}

	public void testAllEvents() {

		String query = "SELECT * " +
		" FROM \"" + getTradeTable() +   
		"\" WHERE \"DATETIME\">='" + "2010-03-01" +
		"' and \"DATETIME\"<='" + "2010-03-31'";
		System.out.println(query);
		
		System.out.println(new Date());
		try {
			connection.setAutoCommit(false);
			statement = connection.createStatement();
			statement.setFetchSize(5000);
			ResultSet result = statement.executeQuery(query);
			int counter = 0;
			while (result.next()) { // process results one row at a time
				//return result.getInt(1);
				counter++;
				//if (counter % 1000 == 0) {
				//	System.out.println(counter);
				//}

			}			

			statement.close();

		} catch(SQLException ex) {
			System.err.println("SQLException: " + ex.getMessage());
			System.out.println(query);
			
		}
		System.out.println(new Date());
	}


}
