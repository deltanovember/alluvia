package com.alluvialtrading.tools;

import com.alluvialtrading.data.Quote;
//import com.alluvialtrading.database.ASXBackTestingLib;
import com.alluvialtrading.vbroker.BackTestingLib;
import com.alluvialtrading.vbroker.LSEBackTestingLib;

import java.util.Date;

public class BenchmarkGenerator {

	private BackTestingLib connector = null;
	private TraderLib lib = new TraderLib();
	
	public static void main(String[] args) {
		new BenchmarkGenerator();
	}
	
	public BenchmarkGenerator() {
		connector = new LSEBackTestingLib();
		Date start = connector.getFirstTradeDate();
		Date end = connector.getLastTradeDate();
		String currentDate = lib.dateToISODateString(start);
		
		String endString = lib.dateToISODateString(end);
		Date date = null;
		while (!currentDate.equals(endString)) {
			//
			//System.out.println(currentDate);
			String[] allSecurities = connector.getAllSecurities(currentDate);
			for (String security : allSecurities) {
				security = security.trim();
				date = lib.convertISODateTimeString(currentDate);
				Date endOfDay = lib.combineDateTime(date, "23:59:59.000");
				double open = connector.getOpenPrice(security, date);
				double close = connector.getClosePrice(security, date);
				if (security.equals("BHP")) {
					System.out.println("*" + currentDate + " " + security);
				}
				Quote precloseQuote = connector.getQuote(security, currentDate, connector.getCloseTime());
				double preclose = 0;
				if (null != precloseQuote) {
					preclose = precloseQuote.getMid();
				}
				double high = connector.getMaxPrice(security, date, endOfDay);
				double low = connector.getMinPrice(security, date, endOfDay);
				int tcount = connector.getTcount(security, date, endOfDay);
				int volume = connector.getVolume(security, date, endOfDay, 0);
				double value = connector.getValue(security, date, endOfDay, 0);
				double minSpread = connector.getMinSpread(security, currentDate, currentDate, false);
				System.out.println(currentDate + " " + security);
				connector.insertBenchmark(currentDate, security, open, close, preclose, high, low, tcount, volume, value, minSpread);
				
			}
			currentDate = connector.getTradingDate(currentDate, 1);
		}
		
	}
}
