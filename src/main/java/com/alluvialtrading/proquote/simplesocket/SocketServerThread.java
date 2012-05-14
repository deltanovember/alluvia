/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alluvialtrading.proquote.simplesocket;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.nio.channels.*;

import org.slf4j.Logger;

/**
 *
 *
 */
public class SocketServerThread implements Runnable {
    Charset charset = Charset.forName("ISO-8859-1");
    CharsetDecoder decoder = charset.newDecoder();
    
    private SocketChannel socketChannel = null;
    
    private SocketHandler handler = null;
    
    private int numRead = 0;
    
    private SocketTextBuffer textBuffer = new SocketTextBuffer();
    
    private ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
    
    private Logger logger = null;
    
    private static String delimiter = "\r\n";
    
    public SocketServerThread(SocketChannel socketChannelIn, SocketHandler handlerIn, Logger loggerIn) {
        socketChannel = socketChannelIn;
        handler = handlerIn;
        logger = loggerIn;
    }
    
    public boolean readCommand(String delim) {
        if (!textBuffer.ready()) {
            return (!textBuffer.finished());
        }
        
        String line = textBuffer.next();
        
        handler.handleSocketRequest(line);
            
        numRead++;
        
        return readCommand(delim);
    }
    
    public void readInput() {
        boolean keepGoing = true;
        
        while (keepGoing)
        {
            try {
                buffer.clear();
                
                int numBytesRead = socketChannel.read(buffer);
                
                if (numBytesRead == -1) {
                    keepGoing = false;
                } else if (numBytesRead > 0) {
                    buffer.flip();

                    this.textBuffer.write(decoder.decode(buffer).toString());
                    
                    keepGoing = readCommand(delimiter);
                }
            } catch (IOException exception) {

            }
        }
    }
    

    public void run() {
        readInput();
        
        try {
           socketChannel.close(); 
        } catch (IOException exception) {
            //
        }
        logger.info("Socket server thread completed. Number of updates: " + numRead);
    }
}