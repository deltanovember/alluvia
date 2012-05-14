/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alluvialtrading.proquote.simplesocket.console;

import com.alluvialtrading.proquote.simplesocket.SocketHandler;

import org.slf4j.Logger;

/**
 *
 *
 */
public class RtdListenerAlgorithmicTradingHandler implements SocketHandler {
    public ProquoteConnector tradingClass = null;
    private Logger logger;
    private String format;
    
    public RtdListenerAlgorithmicTradingHandler(String formatIn, Logger loggerIn) {
        this(null, formatIn, loggerIn);
    }
    
    public RtdListenerAlgorithmicTradingHandler(ProquoteConnector tradingClassIn, String formatIn, Logger loggerIn) {
        tradingClass = tradingClassIn;
        format = formatIn;
        logger = loggerIn;
    }
    
    public enum Function {
        P_VOLUME, 
        P_OPEN, 
        P_HIGH, 
        P_LOW, 
        P_CLOSE, 
        TURNOVER, 
        VOLUME, 
        OPEN, 
        HIGH,
        ASK,
        ASK_TIME,
        BID,
        BID_TIME,
        TRADE_TIME,
        NO_TRADES,
        LAST,
        UNCROSS_PRICE,
        UNCROSS_VOL,
        TRADE_PHASE,
        INC_VOL,
        NOVALUE,
        TRADEID,
        TRADE_TYPE;
        
        public static Function toFunction(String str) {
            try {
                return valueOf(str.toUpperCase().replaceAll(" ", "_"));
            } catch (Exception exception) {
                return NOVALUE;
            }
        }
    }
    
    @Override
    public String handleSocketRequest(String message) {
        //logger.info("Handling request: " + message);
        
        String[] messageParts = message.split(",");
        
        int stockCodePos = format.indexOf("%StockCode%");
        int functionPos = format.indexOf("%Function%");
        int valuePos = format.indexOf("%NewValue%");
        
        String stockCode = messageParts[getIndexForPosition(stockCodePos, functionPos, valuePos)];
        String function = messageParts[getIndexForPosition(functionPos, stockCodePos, valuePos)];
        String value = messageParts[getIndexForPosition(valuePos, stockCodePos, functionPos)];
        
        switch (Function.toFunction(function)) {

            case P_VOLUME:
                tradingClass.onPVolume(stockCode, Long.parseLong(value));
                break;
            case P_OPEN:
                tradingClass.onPOpen(stockCode, Float.parseFloat(value));
                break;
            case P_HIGH:
                tradingClass.onPHigh(stockCode, Float.parseFloat(value));
                break;
            case P_LOW:
                tradingClass.onPLow(stockCode, Float.parseFloat(value));
                break;
            case P_CLOSE:
                tradingClass.onPClose(stockCode, Float.parseFloat(value));
                break;
            case TURNOVER:
                tradingClass.onTurnover(stockCode, Long.parseLong(value));
                break;
            case VOLUME:
                tradingClass.onVolume(stockCode, Long.parseLong(value));
                break;
            case OPEN:
                tradingClass.onOpen(stockCode, Float.parseFloat(value));
                break;
            case HIGH:
                tradingClass.onHigh(stockCode, Float.parseFloat(value));
                break;
            case ASK:
                tradingClass.onAsk(stockCode, Float.parseFloat(value));
                break;
            case BID:
                tradingClass.onBid(stockCode, Float.parseFloat(value));
                break; 
            case TRADE_TIME:
                tradingClass.onTradeTime(stockCode, value);
	        break; 
            case LAST:
                tradingClass.onLast(stockCode, Float.parseFloat(value));
        	break;
            case UNCROSS_PRICE:
                tradingClass.onUncrossPrice(stockCode, Float.parseFloat(value));
        	break;
            case UNCROSS_VOL:
                tradingClass.onUncrossVol(stockCode, Integer.parseInt(value));
        	break;
            //case TRADE_PHASE:
            //    tradingClass.onTradePhase(stockCode, Float.parseFloat(value));
        	//break;
            case NO_TRADES:
                tradingClass.onNoTrades(stockCode, Integer.parseInt(value));
                break;
            case ASK_TIME:
                tradingClass.onAskTime(stockCode, value);
                break;   
            case BID_TIME:
                tradingClass.onBidTime(stockCode, value);
                break;
            case INC_VOL:
                tradingClass.onIncVol(stockCode, Integer.parseInt(value));
                break;
            case TRADE_TYPE:
                tradingClass.onTradeType(stockCode, value);
                break;
            case TRADEID:
                tradingClass.onTradeID(stockCode, value);
                break;
            default:
                break;
        }
        
        return message;
    }
    
    private int getIndexForPosition(int pos, int otherPos1, int otherPos2) {
        int index;
        
        if (pos > otherPos1 && pos > otherPos2) {
            index = 2;
        } else if (pos < otherPos1 && pos < otherPos2) {
            index = 0;
        } else {
            index = 1;
        }
        
        return index;
    }
}