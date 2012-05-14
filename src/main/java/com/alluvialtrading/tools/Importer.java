package com.alluvialtrading.tools;

import com.alluvialtrading.vbroker.ASXBackTestingLib;
import com.alluvialtrading.vbroker.BackTestingLib;
import com.alluvialtrading.vbroker.Message;

import java.io.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.Date;
import java.util.regex.*;

public abstract class Importer {
	
	File csv = null;
	TraderLib lib = new TraderLib();
	BackTestingLib connector = new ASXBackTestingLib();
	
	 // Charset and decoder for ISO-8859-15
    private static Charset charset = Charset.forName("ISO-8859-15");
    private static CharsetDecoder decoder = charset.newDecoder();
    
 // Pattern used to parse lines
    private static Pattern linePattern = Pattern.compile(".*\r?\n");
    
    public static void main(String[] args) {
    	System.out.println(new java.util.Date());
    	//new Importer();
    	System.out.println(new java.util.Date());
    }
	
	public Importer() {
		
		try {
			csv = getFile();
			// Open the file and then get a channel from the stream
			FileInputStream fis = new FileInputStream(csv);
			FileChannel fc = fis.getChannel();
	
			// Get the file's size and then map it into memory
			int sz = (int)fc.size();
			MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, sz);
	
			// Decode the file into a char buffer
			CharBuffer cb = decoder.decode(bb);
	
			Matcher lm = linePattern.matcher(cb);	// Line matcher
	
			int lines = 0;
			while (lm.find()) {
			    lines++;
			    CharSequence cs = lm.group(); 	// The current line
			  
			    processLine(cs.toString());
			    if (lm.end() == cb.limit())
			    	break;
			}
	
			// Close the channel and the stream
			fc.close();
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
	}
	
	protected abstract File getFile();
	protected abstract void processLine(String line);

}
