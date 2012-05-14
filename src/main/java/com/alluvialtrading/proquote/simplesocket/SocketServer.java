/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alluvialtrading.proquote.simplesocket;

import java.net.*;
import java.nio.channels.*;
import java.util.concurrent.*;
import java.io.*;

import org.slf4j.Logger;

/**

 */
public class SocketServer implements Runnable {
    ServerSocketChannel serverSocketChannel = null;
    
    Executor executor = null;
    
    Logger logger = null;
    
    boolean running = false;
    
    int port = 1743;
    
    SocketHandler handler = null;
    
    public SocketServer(int portIn, SocketHandler handlerIn, Logger loggerIn) {
        port = portIn;
        
        handler = handlerIn;
        
        logger = loggerIn;
    }
    

    public void run() {
        running = false;
        
        try {
            serverSocketChannel = ServerSocketChannel.open();
            
            serverSocketChannel.configureBlocking(false);
            
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            
            executor = Executors.newCachedThreadPool();
            
            running = true;
            
            logger.info("Socket server started. Listening for socket requests.");
            
            acceptClientRequests();
            
            serverSocketChannel.close();
            
            logger.info("Socket server stopped.");
        }
        catch (IOException exception) {
            if (serverSocketChannel != null && !serverSocketChannel.isOpen()) {
                // This is fine.
            } else {
                try {
                    serverSocketChannel.close();
                } catch (IOException e) {
                }
            }
        }
        catch (InterruptedException exception) {
            //
        }
        finally {
            serverSocketChannel = null;
            
            running = false;
        }
    }
    
    private void acceptClientRequests() throws IOException, InterruptedException {
        while (running) {
            SocketChannel socketChannel = serverSocketChannel.accept();
            
            if (socketChannel == null) {
                Thread.sleep(500);
            } else {
                socketChannel.configureBlocking(false);
                
                logger.info("Socket request received. Starting server thread to handle it.");
                
                executor.execute(new SocketServerThread(socketChannel, handler, logger));
            }
        }
    }
}
