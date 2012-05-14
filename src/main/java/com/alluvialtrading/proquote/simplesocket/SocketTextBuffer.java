/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.alluvialtrading.proquote.simplesocket;

import java.util.*;

/**
 *
 *
 */
public class SocketTextBuffer {
    private String textBuffer = "";
    
    private String delim = "\r\n";
    
    private boolean isFinished = false;
    
    private String eof = "<EOF>";
    
    private ArrayList items = new ArrayList();
    
    public String ltrim(String item) {
        return item.replaceAll("^("+delim+")+", "");
    }
    
    public String rtrim(String item) {
        return item.replaceAll("("+delim+")+$", "");
    }
    
    public String trim(String item) {
        return this.rtrim(this.ltrim(item));
    }
    
    public boolean ready() {
        return (items.size() > 0);
    }
    
    public boolean finished() {
        return this.isFinished;
    }
    
    public String next() {
        if (items.isEmpty()) {
            return "";
        }
        
        String val = (String)items.get(0);
        
        items.remove(0);
        
        return val;
    }
    
    public void write(String input) {
        if (input.isEmpty()) {
            return;
        }
        
        if (!this.textBuffer.isEmpty()) {
            input = this.textBuffer + input;
            this.textBuffer = "";
        }
        
        if (input.startsWith(this.eof)) {
            this.isFinished = true;
            return;
        }
        
        if (!input.contains(delim)) {
            this.textBuffer = input;
            return;
        }
        
        int index = input.indexOf(delim);
        
        this.items.add(this.trim(input.substring(0, index)));
        
        this.write(this.ltrim(input.substring(index)));
    }
}
