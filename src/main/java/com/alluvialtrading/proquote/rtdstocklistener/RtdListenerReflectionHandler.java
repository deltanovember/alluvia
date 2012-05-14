/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alluvialtrading.proquote.rtdstocklistener;

import com.alluvialtrading.proquote.simplesocket.SocketHandler;

import org.slf4j.Logger;

import java.lang.reflect.*;

/**
 *
 *
 */
public class RtdListenerReflectionHandler implements SocketHandler {
    public Object tradingClass = null;
    
    private Logger logger;
    
    private String format;
    
    public RtdListenerReflectionHandler(String formatIn, Logger loggerIn) {
        this(null, formatIn, loggerIn);
    }
    
    public RtdListenerReflectionHandler(Object tradingClassIn, String formatIn, Logger loggerIn) {
        tradingClass = tradingClassIn;
        
        format = formatIn;
        
        logger = loggerIn;
    }
    
    @Override
    public String handleSocketRequest(String message) {
        String[] messageParts = message.split(",");
        
        int stockCodePos = format.indexOf("%StockCode%");
        int functionPos = format.indexOf("%Function%");
        int valuePos = format.indexOf("%NewValue%");
        
        String stockCode = messageParts[getIndexForPosition(stockCodePos, functionPos, valuePos)];
        String function = messageParts[getIndexForPosition(functionPos, stockCodePos, valuePos)];
        float value = Float.parseFloat(messageParts[getIndexForPosition(valuePos, stockCodePos, functionPos)]);
        
        if (tradingClass != null) {
            callFunction(stockCode, function, value);
        }
        
        return message;
    }
    
    private void callFunction(String stockCode, String function, float value) {
        Class c = tradingClass.getClass();
            
        Class[] methodArgs = new Class[2];
        methodArgs[0] = String.class;
        methodArgs[1] = float.class;
        
        try {
            Method m = c.getDeclaredMethod("on" + function.replaceAll(" ", ""), methodArgs);
            
            if (m != null) {
                Object[] invokeArgs = new Object[2];
                invokeArgs[0] = stockCode;
                invokeArgs[1] = value;
                m.invoke(tradingClass, invokeArgs);
            }
        } catch (NoSuchMethodException exception) {
            logger.error("No such method was found to call.", exception);
        } catch (IllegalAccessException exception) {
            logger.error("Unable to access the method to call.", exception);
        } catch (InvocationTargetException exception) {
            logger.error("There was an error invoking the target method.", exception);
        }
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
