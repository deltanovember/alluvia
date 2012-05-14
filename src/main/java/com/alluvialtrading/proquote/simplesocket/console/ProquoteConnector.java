package com.alluvialtrading.proquote.simplesocket.console;

import com.alluvial.mds.contract.Quote;
import com.alluvial.mds.contract.QuoteMatch;
import com.alluvial.mds.contract.Trade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;
import java.util.Hashtable;

public class ProquoteConnector {
    private Logger logger = null;

    Hashtable<String, Float> uncrossPrice = new Hashtable<String, Float>();
    Hashtable<String, String> tradeIDs = new Hashtable<String, String>();
    Hashtable<String, Float> tradePrices = new Hashtable<String, Float>();
    Hashtable<String, Integer> tradeVolumes = new Hashtable<String, Integer>();
    // TradeID => Boolean
    Hashtable<String, Boolean> alreadyTraded = new Hashtable<String, Boolean>();

    public static void main(String[] args) {
        new ProquoteConnector();
    }

    public ProquoteConnector() {
        logger = LoggerFactory.getLogger(RtdListenerConsole.class);
        new RtdListenerConsole(this, logger).run();

    }

    protected void onEvent(Object obj) {
        //System.out.println("onEvent: " + obj);
    }

    public void onPVolume(String code, long data) {
        //logger.info("P Volume: " + code + ", " + data);
    }

    public void onPOpen(String code, float data) {
        //logger.info("P Open: " + code + ", " + data);
    }

    public void onPHigh(String code, float data) {
        //logger.info("P High: " + code + ", " + data);
    }

    public void onPLow(String code, float data) {
        //logger.info("P Low: " + code + ", " + data);
    }

    public void onPClose(String code, float data) {
        //logger.info("P Close: " + code + ", " + data);
    }

    public void onTurnover(String code, long data) {
        //logger.info("Turnover: " + code + ", " + data);
    }

    public void onVolume(String code, long data) {
        //logger.info("Volume: " + code + ", " + data);
    }

    public void onOpen(String code, float data) {
        //logger.info("Open: " + code + ", " + data);
    }

    public void onHigh(String code, float data) {
        //logger.info("High: " + code + ", " + data);
    }

    public void onIncVol(String code, int data) {
       tradeVolumes.put(code, data);
    }

    public void onAsk(String code, float data) {
        Quote quote = new Quote(0, 'A', data, 0, 0.0, "", (new Date()).getTime(), 0);
        quote.Security = code;
        onEvent(quote);
    }

    public void onAskTime(String code, String data) {
        //logger.info("AskTime: " + code + ", " + data);
    }

    public void onBid(String code, float data) {
        Quote quote = new Quote(0, 'B', data, 0, 0.0, "", (new Date()).getTime(), 0);
        quote.Security = code;
        onEvent(quote);
    }

    public void onBidTime(String code, String data) {
        //logger.info("BidTime: " + code + ", " + data);
    }

    public void onTradeTime(String code, String data) {
        //logger.info("TradeTime: " + code + ", " + data);
    }

    public void onTradeID(String code, String data) {
        if (code.equals("RIO.L"))
            System.out.println("ID" + code + " " + data);
    }

    public void onTradeType(String code, String data) {
        short zeroShort = 0;
        if (data.equals("AT")) {
            Trade trade = new Trade(new Integer(0), zeroShort, 0L, zeroShort, 0, 0, 0.0, tradeVolumes.get(code), new Double(data),
                    (new Date()).getTime(), 0, "", (new Date()).getTime(), 0L);
            trade.Security = code;
            onEvent(trade);
        }
        alreadyTraded.put(tradeIDs.get(code), true);
    }

    public void onNoTrades(String code, int data) {
        // logger.info("NoTrades: " + code + ", " + data);
    }

    public void onLast(String code, float data) {

    }

    public void onUncrossPrice(String code, float data) {
        //System.out.println("uncross");
        uncrossPrice.put(code, data);
    }

    public void onUncrossVol(String code, int data) {
        if (!uncrossPrice.containsKey(code)) {
            //System.out.println("blah0");
            return;
        }
        QuoteMatch quote = new QuoteMatch(0, data, 0.0, uncrossPrice.get(code), (new Date()).getTime(), 0L);
        quote.Security = code;
        onEvent(quote);
    }

}