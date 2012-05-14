package com.alluvialtrading.strategies.straddle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import com.alluvialtrading.tools.TraderLib;
import com.alluvialtrading.vbroker.ASXBackTestingLib;
import com.alluvialtrading.vbroker.BackTestingLib;


public class Candidates {
	
	protected TraderLib lib = new TraderLib();
	protected BackTestingLib connector = new ASXBackTestingLib();	
	protected ArrayList<Spread> allSpreads = new ArrayList<Spread>();
	
	public static void main(String[] args) {
		new Candidates();
	}
	
	public Candidates() {
		String[] allStocks = lib.openFile("data", "lseSamplePrices.csv");
		for (String line : allStocks) {
			if (line.contains("arket") || line.length() < 20) {
				continue;
			}
			 String[] tokens = line.split(",");
			 String stock = tokens[1];
			 String segment = tokens[2];
			 String currency = tokens[3];
			 double price = Double.parseDouble(tokens[4]);
			 double tick = connector.getLSETickSize(segment, currency, price);
			 double value = Double.parseDouble(tokens[5]);
			 int volume = Integer.parseInt(tokens[6]);
			 int numberTrades = Integer.parseInt(tokens[7]);
			 double ask = Double.parseDouble(tokens[8]);
			 double bid = Double.parseDouble(tokens[9]);
			 allSpreads.add(new Spread(stock, price, tick, value,
					 volume, numberTrades, ask, bid));
			 
		}
		
		Collections.sort(allSpreads);
		
		for (int i=0; i<500; i++) {
			Spread current = allSpreads.get(i);
			if (current.getValue() > 100000 &&
					current.getSpread() > 0.3) {
				System.out.println(current.getSecurity() + "," + lib.round4(current.getSpread()) + "," + current.getTick() + ","
						+ current.getNumberTrades() + "," + current.getValue());
			}
			
		}
	}

}
