package com.alluvialtrading.spp;

import java.io.File;
import java.util.Date;

import com.alluvialtrading.algo.BaseAlgo;
import com.alluvialtrading.data.Trade;
import com.alluvialtrading.sim.VirtualBroker;
import com.alluvialtrading.vbroker.ASXBackTestingLib;
import com.alluvialtrading.vbroker.BackTestingLib;



public class SPP extends BaseAlgo {
	
	/**
	 * Every java program starts in main
	 * @param args
	 */
	public static void main(String[] args) {
		
		// this runs algo code
		new SPP(Market.ASX);
		
		// by here all trades have been dumped to CSV
		
		// below here is profit tracking related
		String[] files = new File("import").list();
		for (String file : files) {
				
			new VirtualBroker(file);
		}
	}
	
	public SPP(Market market) {
		// calls functionality from parent
		super(market);
	}

	
	@Override
	protected void init() {
		super.init();
	}
	
	public void algoStart() {
		// calls functionality from parent
		super.algoStart();
	}
	
	@Override
	public void algoBody() {
		
		// Load trade data from CSV
		String[] tradeData = openFile("data", "spp.csv");

	 	for (String line : tradeData) {

	 		if (line.length() <= 5 ||
	 				line.contains("tock")) {
	 			continue;
	 		}
	 		String[] tokens = line.split(",");
	 		String stock = tokens[0];
	 		String date = tokens[1];
	 		double percent = 1;
	 		if (tokens.length > 2) {
	 			//percent = Double.parseDouble(tokens[2]);
	 		}
	 		
	 		// Convert date
	 		Date dateObject = convertSmartsDate(date);
	 		date = dateToISODateString(dateObject);
	 		String previousDate = getTradingDate(date, -1);
	 		

	 		if (null != previousDate) {
		 		Trade closingTrade = getClosingTrade(previousDate);
		 		if (null != closingTrade &&
		 				percent > 0.5) {
			 		String dateTime = dateToISODateTimeString(closingTrade.getDate());
			 		int volume = (int) (0.2 * closingTrade.getVolume());			 		
			 		double price = closingTrade.getPrice();
			 		volume = getMaxVolume(price, volume);
			 		String exitWait = getTradingDate(date, 3);
			 		String exitStrategy = Trade.EXIT_LAST_TRADE;
		 			profitTrack(dateTime, stock, -volume, price, exitWait, exitStrategy);	
		 		}
		 		else {
		 			System.out.println("no closing " + stock + date);
		 		}

	 		}
	 		

	 		//System.out.println(stock + "," + date + "," + previousDate);
	 	}

		
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
