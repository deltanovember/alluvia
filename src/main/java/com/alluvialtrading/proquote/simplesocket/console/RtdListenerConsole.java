package com.alluvialtrading.proquote.simplesocket.console;

import com.alluvialtrading.proquote.rtdstocklistener.RtdListener;
import com.alluvialtrading.proquote.simplesocket.SocketHandler;

import org.w3c.dom.*;

import org.xml.sax.*;

import java.io.*;

import org.slf4j.Logger;

import javax.xml.parsers.*;
import javax.xml.xpath.*;

/**
 *
 *
 */
public class RtdListenerConsole {

    private ProquoteConnector connector = null;
    private Logger logger;
    private static RtdListener listener;

    RtdListenerConsole(ProquoteConnector connector, Logger logger) {
        this.connector = connector;
        this.logger = logger;
        run();
    }
    public void run() {

        logger.info("Loading socket config");

        int port = 1743;

        String format = "%StockCode%,%Function%,%NewValue%";

        try {
            Document doc = getConfigDocument("Config.xml");
            XPathFactory xpFactory = XPathFactory.newInstance();
            XPath xpath = xpFactory.newXPath();

            String configPort = xpath.evaluate("/Nini/Section[@Name='Socket']/Key[@Name='Port']/@Value", doc);
            port = (configPort.equals("")) ? 0 : java.lang.Integer.parseInt(configPort);

            String configFormat = xpath.evaluate("/Nini/Section[@Name='Socket']/Key[@Name='SendFormat']/@Value", doc);
            format = (configFormat.equals("")) ? "" : configFormat;

            SocketHandler socketHandler = new RtdListenerAlgorithmicTradingHandler(connector, format, logger);

            logger.info("Starting listener on port " + port + ".");

            listener = new RtdListener(socketHandler, logger, port);
            listener.start();

        }
        catch (XPathExpressionException e) {
            logger.error("There was an error retrieving a config value via XPath.", e);
        }
    }
    
    private enum Command {
        EXIT,
    }
    
    private Document getConfigDocument(String configFile) {
        Document doc = null;
        
        logger.info("Loading config document: " + configFile);
        
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder docBuilder = factory.newDocumentBuilder();
            doc = docBuilder.parse(configFile);
            
            return doc;
        }
        catch(IOException exception) {
            logger.error("There was an error reading the configuration file.", exception);
        }
        catch(ParserConfigurationException exception) {
            logger.error("Cannot parse configuration file.", exception);
        }
        catch(SAXException exception) {
            logger.error("There was a SAX error parsing the configuration file.", exception);
        }
        
        logger.info("Successfully loaded config document.");
        
        return doc;
    }
}