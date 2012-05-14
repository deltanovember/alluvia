package com.alluvialtrading.consulting;

import java.io.File;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.util.PDFTextStripper;

public class JournalExtractor {
	
	String pdf = "data\\1.pdf";
	
    public static void main( String[] args ) throws Exception
    {
    	new JournalExtractor();

    }
	public JournalExtractor() {
		
		try {
			String[] files = new File("papers").list();
							PDDocument doc = PDDocument.load(new java.io.File("temp.pdf"));
				PDFTextStripper stripper = new PDFTextStripper();
				stripper.setEndPage(1);
				//stripper.
				String firstPage = stripper.getText(doc);
				String[] tokens = firstPage.split("\r\n");
				for (String line : tokens) {
					System.out.println(line);
				}
				doc.close();
			
			//
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}

		
	}
}
