package com.alluvia.sample

import com.alluvia.algo.BackTestingAlgo
import com.alluvialtrading.lib.DateIterator;

import com.alluvialtrading.data.Trade;



import java.util.Date;

abstract class SampleAlgo extends BackTestingAlgo {
  
  	val startDate = "2010-03-01" startOfDay
	  val endDate = "2010-03-31" endOfDay
	
	
	override def init {
		super.init
	}
	
	override def algoStart {
		// calls functionality from parent
		super.algoStart
	}
	

	override def algoBody {
	  // Start looping potential trades chronologically
	 	val iterator = new DateIterator(startDate, endDate);
	 	//get
	 	while(iterator.hasNext) {
	 		val date: java.util.Date = iterator.next();
	 		//val test = date toDateStr//
	 		if (isTradingDate(dateToISODateString(date))) {

	 			val allStocks = getAllTradedSecurities(dateToISODateString(date));
	 			// loop through all stocks 
	 			for (stock: String <- allStocks) {
	 			  //println(stock)
	 				setCurrentSecurity(stock);
	 				val todayDate = dateToISODateString(date);
	 				if (stock == "BHP.AX") {
	 					val quote = getQuote(todayDate, "13:00")
            // "blah".
	 					println(date.toDateStr + ", " + stock + " - 1pm price " + quote.getAsk + " min spread "
               + getMinSpread(todayDate));
             printcsv("record.csv", todayDate, stock, quote.getAsk, getMinSpread(todayDate), "matthew")
				 		val entry = quote.getDateTime
				 		val exit = new Date(60000 + entry.getTime)
			 			profitTrack(entry, stock, 1000, quote.getAsk, 
			 					exit, Trade.EXIT_AGGRESSIVE)
	 				}
	 				else {
	 					//System.out.println(stock);
	 				}
			
	 				
	 			}
	 		}
	 	}

	}
	
	override def algoEnd {
		super.algoEnd
	}

	
	override def getCSVHeader: String = {
		"Signal,Date,Stock,Overnight,Lower,Upper,IndexChange,Beta,RSquared"
	}



}