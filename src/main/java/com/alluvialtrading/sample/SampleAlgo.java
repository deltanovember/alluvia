package com.alluvialtrading.sample;
import com.alluvialtrading.algo.BaseAlgo;
import com.alluvialtrading.lib.DateIterator;
import com.alluvialtrading.data.Quote;
import com.alluvialtrading.data.Trade;
import com.alluvialtrading.sim.VirtualBroker;

import java.io.File;
import java.util.Date;
import java.util.Iterator;

public class SampleAlgo extends BaseAlgo {
	
	Date startDate = null;
	Date endDate = null;
	
	/**
	 * Every java program starts in main
	 * @param args
	 */
	public static void main(String[] args) {
		
		// this runs algo code
		new SampleAlgo(Market.LSE);
		
		// by here all trades have been dumped to CSV
		
		// below here is profit tracking related
		String[] files = new File("import").list();
		for (String file : files) {
			new VirtualBroker(file);
		}
	}
	
	public SampleAlgo(Market market) {
		// calls functionality from parent
		super(market);
	}

	
	@Override
	protected void init() {
		super.init();
		startDate = convertSmartsDateTime("1/3/2010 09:00:00.000");
		endDate = convertSmartsDateTime("31/3/2010 16:00:00.000");
	}
	
	public void algoStart() {
		// calls functionality from parent
		super.algoStart();
	}
	
	@Override
	public void algoBody() {

		// Start looping potential trades chronologically
	 	Iterator<Date> iterator = new DateIterator(startDate, endDate);
	 	while(iterator.hasNext()) {
	 		Date date = iterator.next();
	 		if (isTradingDate(dateToISODateString(date))) {

	 			String[] allStocks = getAllTradedSecurities(dateToISODateString(date));
	 			// loop through all stocks 
	 			for (String stock : allStocks) {
	 				setCurrentSecurity(stock);
	 				String todayDate = dateToISODateString(date);
	 				if (stock.equals("BLT.L")) {
	 					Quote quote = getQuote(todayDate, "13:00");
	 					//Quote preCloseMid = connector.getPrecloseQuote(todayDate, stock);
	 					System.out.println(todayDate + ", " + stock + " - 1pm price " + quote.getAsk() + " min spread " + getMinSpread(todayDate));
	 					//System.out.println(todayDate + ", " + stock + " - preclosing askBefore " + preCloseMid.getAsk());
				 		//String exitStrategy = Trade.EXIT_AGGRESSIVE;
				 		Date entry = quote.getDateTime();
				 		Date exit = new Date(60000 + entry.getTime());
			 			profitTrack(entry, stock, 1000, quote.getAsk(), 
			 					exit, Trade.EXIT_AGGRESSIVE);
	 				}
	 				else {
	 					//System.out.println(stock);
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
	}

	
	@Override
	protected String getCSVHeader() {
		return "Signal,Date,Stock,Overnight,Lower,Upper,IndexChange,Beta,RSquared";
	}



}
