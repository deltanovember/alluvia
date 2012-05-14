/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alluvialtrading.proquote.rtdstocklistener;

import com.alluvialtrading.proquote.simplesocket.SocketHandler;
import com.alluvialtrading.proquote.simplesocket.SocketServer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 *
 */
public class RtdListener {

    Logger logger;
    SocketServer server;
    SocketHandler handler;
    
    int port;
    
    public RtdListener(SocketHandler handlerIn) {
        this(handlerIn, LoggerFactory.getLogger(RtdListener.class));
    }
    
    public RtdListener(SocketHandler handlerIn, Logger loggerIn) {
        this(handlerIn, loggerIn, 1743);
    }
    
    
    
    public RtdListener(SocketHandler handlerIn, Logger loggerIn, int portIn) {
        logger = loggerIn;
        
        handler = handlerIn;
        
        port = portIn;
        
        server = new SocketServer(port, handler, logger);
    }
    
    public void start() {
        new Thread(server).start();
    }
}
