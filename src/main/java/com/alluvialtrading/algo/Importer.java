package com.alluvialtrading.algo;
import com.alluvialtrading.tools.TraderLib;

public class Importer {
	
	TraderLib lib = new TraderLib();
	
	
	/**
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
					info.setEventTime(lib.convertTime(info.getDate() + " " + data));

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
*/
}
