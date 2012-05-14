package com.alluvialtrading.tools;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.PrintWriter;
import java.math.RoundingMode;

import java.util.ArrayList;
import java.util.Date;
import java.util.Enumeration;
import java.util.Hashtable;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;


import com.alluvialtrading.algo.Position;

/**
 * This class should contain generic library functions that could be expected to 
 * be used in a wide variety of contexts.  This includes reading files, writing 
 * files, rounding numbers and converting dates.  As a guideline, this class 
 * should only contain non-sensitive, non-proprietary code
 * 
 * This class should not require any market specific subclasses
 * @author dnguyen
 *
 */


public class TraderLib {
	
	// Transaction costs
	//private static final double BROKERAGE = 0.001;
	//private static final double MIN_DOLLAR_COST = 10;
	
	
	public double calculatePosition(Hashtable<String, Position> openPositions) {
		
		double position = 0;
		Enumeration<String> stockCodes = openPositions.keys();
		while (stockCodes.hasMoreElements()) {
			String stockCode = stockCodes.nextElement();
			position += Math.abs(openPositions.get(stockCode).getValue());
		}
		
		return position;
		
	}
	
	/**
	 * Takes a date object and a time string in the 
	 * format HH:mm:ss.SSS and converts to a date object.
	 * date Date
	 * time String
	 * @return
	 */
	public Date combineDateTime(Date date, String time) {
		
		String dateTime = dateToISODateString(date) + " " + time;

		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		if (!time.contains(".")) {
			parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		}
		try {
			return parser.parse(dateTime);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	/**
	 * Takes a date string and a time string in the 
	 * format HH:mm:ss.SSS and converts to a date object.
	 * date String
	 * time String
	 * @return
	 */
	public Date combineDateTime(String date, String time) {
		
		String dateTime = date + " " + time;

		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        if (!time.contains("."))
            parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			return parser.parse(dateTime);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}
	
	public void computeRegression(double[] x, double[] y) {

        // first pass: read in data, compute xbar and ybar
        double sumx = 0.0, sumy = 0.0, sumx2 = 0.0;
        for (int i=0; i< x.length; i++) {

            sumx  += x[i];
            sumx2 += x[i] * x[i];
            sumy  += y[i];

        }
        int n = x.length;
        double xbar = sumx / n;
        double ybar = sumy / n;

        // second pass: compute summary statistics
        double xxbar = 0.0, yybar = 0.0, xybar = 0.0;
        for (int i = 0; i < n; i++) {
            xxbar += (x[i] - xbar) * (x[i] - xbar);
            yybar += (y[i] - ybar) * (y[i] - ybar);
            xybar += (x[i] - xbar) * (y[i] - ybar);
        }
        double beta1 = xybar / xxbar;
        double beta0 = ybar - beta1 * xbar;

        // print results
        System.out.println("y   = " + beta1 + " * x + " + beta0);

        // analyze results
        int df = n - 2;
        double rss = 0.0;      // residual sum of squares
        double ssr = 0.0;      // regression sum of squares
        for (int i = 0; i < n; i++) {
            double fit = beta1*x[i] + beta0;
            rss += (fit - y[i]) * (fit - y[i]);
            ssr += (fit - ybar) * (fit - ybar);
        }
        double R2    = ssr / yybar;
        double svar  = rss / df;
        double svar1 = svar / xxbar;
        double svar0 = svar/n + xbar*xbar*svar1;
        System.out.println("R^2                 = " + R2);
        System.out.println("std error of beta_1 = " + Math.sqrt(svar1));
        System.out.println("std error of beta_0 = " + Math.sqrt(svar0));
        svar0 = svar * sumx2 / (n * xxbar);
        System.out.println("std error of beta_0 = " + Math.sqrt(svar0));

        System.out.println("SSTO = " + yybar);
        System.out.println("SSE  = " + rss);
        System.out.println("SSR  = " + ssr);
	}
	
	/**
	 * Takes a date in the format 31/03/2010 and converts to 
	 * Date object
	 * @return
	 */
	public Date convertSmartsDate(String date) {
		
		SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
		try {
			return parser.parse(date);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		

	}

	/**
	 * If white space method assumes yyyy-MM-dd HH:mm:ss.SSS 
	 * otherwise assumes yyyy-MM-dd
	 * @param date
	 * @return
	 */
	public Date convertISODateTimeString(String date) {
		
		SimpleDateFormat parser = null;
		if (date.contains(" ")) {
			if (date.contains(".")) {
				parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
			} else {
				parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			}
		}
		else {
			parser = new SimpleDateFormat("yyyy-MM-dd");
		}
		
		try {
			return parser.parse(date);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		

	}
	

	/**
	 * Takes a time in the format 01/03/2010 10:35:29 format and converts to 
	 * Date object
	 * @param dateTime String in  01/03/2010 10:35:29.999 format
	 * @return
	 */
	public Date convertSmartsDateTime(String dateTime) {
		
		SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss.SSS");
		try {
			return parser.parse(dateTime);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		

	}
	
	/**
	 * 
	 * @param date
	 * @return String in SMARTS format dd/MM/yyyy
	 */
	public String dateToSmartsDateString(Date date) {
		
		//StringBuffer formatted = new StringBuffer();
		SimpleDateFormat parser = new SimpleDateFormat("dd/MM/yyyy");
		try {
			return parser.format(date);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		

	}
	
	/**
	 * 
	 * @param date
	 * @return "Mon", "Tue" ...
	 */
	
	public String dateToDay(Date date) {
		
		//StringBuffer formatted = new StringBuffer();
		SimpleDateFormat parser = new SimpleDateFormat("E");
		try {
			return parser.format(date);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		

	}
	
	public String dateToISODateString(Date date) {
		
		//StringBuffer formatted = new StringBuffer();
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd");
		try {
			return parser.format(date);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		

	}
	
	public String dateToISODateTimeString(Date date) {
		
		//StringBuffer formatted = new StringBuffer();
		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		try {
			return parser.format(date);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		

	}
	
	
	/**
	 * 
	 * @return
	 */
	public String dateToTimeMillis(Date date) {
		
		//StringBuffer formatted = new StringBuffer();
		SimpleDateFormat parser = new SimpleDateFormat("HH:mm:ss.SSS");
		try {
			String formatted = parser.format(date);
			return formatted;
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		

	}
	
	/**
	 * 
	 * @return
	 */
	public String dateToTimeString(Date date) {
		
		//StringBuffer formatted = new StringBuffer();
		SimpleDateFormat parser = new SimpleDateFormat("HH:mm:ss");
		try {
			return parser.format(date);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		

	}

	/**
	 * Takes a date object and converts to a date object at day end.
	 * date Date
	 * @return
	 */
	public Date getDateEnd(Date date) {
		
		String dateTime = dateToISODateString(date) + " 23:59:59.999";

		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		try {
			return parser.parse(dateTime);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}

	/**
	 * Takes a date object and converts to a date object at day start.
	 * date String
	 * @return
	 */
	public Date getDateStart(Date date) {
		
		String dateTime = dateToISODateString(date) + " 00:00:00.000";

		SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
		try {
			return parser.parse(dateTime);
		}
		catch (Exception ex) {
			ex.printStackTrace();
			return null;
		}
		
	}

	/**
	 * Open a file
	 * 
	 * @param dirName
	 *            directory name
	 * @param fileName
	 *            file same
	 * @return A string array, with each element of the array representing a
	 *         line in the file
	 */
	public String[] openFile(String dirName, String fileName) {
        // System.out.println(new File(".").getAbsoluteFile());
		ArrayList<String> allText = new ArrayList<String>();
		BufferedReader reader = null;
		String[] textPieces = null;
		String fullPath = dirName + System.getProperty("file.separator")
				+ fileName;
		try {

			if ((new File(fullPath)).exists()) {
				reader = new BufferedReader(new FileReader(fullPath));
				String currentLine;
				while ((currentLine = reader.readLine()) != null) {
					// if (currentLine.length() > 0)
					allText.add(currentLine);
				}

				reader.close();
			}
		}

		catch (Exception ex) {
			System.err.println("Error opening file: " + fullPath);
			ex.printStackTrace();
		}
		textPieces = new String[allText.size()];
		for (int i = 0; i < allText.size(); i++) {
			textPieces[i] = allText.get(i).toString();
		}
		return textPieces;

	}

	
	/**
	 * Take lse threshold and flatten out so each column has only
	 * one value
	 * @param dirName directory name
	 * @param fileName
	 */
	public void reformatLSEPriceThresholds(String dirName, String fileName) {
		
		StringBuffer output = new StringBuffer();
		String[] lines = openFile(dirName, fileName);
		
		for (String line : lines) {
			String[] tokens = line.split(",");
			String segment = tokens[0];
			String currency = tokens[1];
			String minPrice = tokens[2];
			String maxPrice = tokens[3];
			String format = tokens[4];
			
			String[] allSegments = segment.split("/");
			String[] allCurrencies = currency.split("/");
			
			for (String currentSegment : allSegments) {
				
				for (String currentCurrency : allCurrencies) {
					output.append(currentSegment.trim() + "," + 
									currentCurrency.trim() + "," + 
									minPrice + "," + 
									maxPrice + "," + 
									format + "\r\n");
				}
			}
		}
		
		writeFile(dirName, "newthresholds.csv", output.toString());
	}

    public double round0(double unrounded) {
		DecimalFormat df = new DecimalFormat("#");
		df.setRoundingMode(RoundingMode.HALF_UP);
		return Double.parseDouble(df.format(unrounded));
	}
	public double round1(double unrounded) {
		DecimalFormat df = new DecimalFormat("#.#");
		df.setRoundingMode(RoundingMode.HALF_UP);
		return Double.parseDouble(df.format(unrounded));
	}
	public double round2(double unrounded) {
		DecimalFormat df = new DecimalFormat("#.##");
		df.setRoundingMode(RoundingMode.HALF_UP);
		return Double.parseDouble(df.format(unrounded));
	}
	public double round3(double unrounded) {
		DecimalFormat df = new DecimalFormat("#.###");
		df.setRoundingMode(RoundingMode.HALF_UP);
		return Double.parseDouble(df.format(unrounded));
	}
	public double round4(double unrounded) {
		DecimalFormat df = new DecimalFormat("#.####");
		df.setRoundingMode(RoundingMode.HALF_UP);
		return Double.parseDouble(df.format(unrounded));
	}
	

	
	public double[] toDoubleArray(ArrayList<Double> list) {
		double[] array = new double[list.size()];
		for (int i=0; i<list.size(); i++) {
			array[i] = list.get(i);
		}
		return array;
	}
	
	/**
	 * Writes a file to the disk
	 * 
	 * @param dirName
	 *            java.lang.String representing the directory name
	 * @param fileName
	 *            java.lang.String representing the file name
	 * @param data
	 *            java.lang.String representing the data to print
	 */
	public void writeFile(String dirName, String fileName, String data) {

		// createDirectory(dirName);
		PrintWriter csv = null;
		
		if (!new File(dirName).exists()) {
			new File(dirName).mkdir();
		}

		// Now write to file
		try {
			csv = new PrintWriter(new FileOutputStream(dirName
					+ System.getProperty("file.separator") + fileName));
			csv.println(data);
		}

		catch (Exception ex) {
			System.out.println("Error writing the following file: " + fileName);
			ex.printStackTrace();
		}

		finally {
			if (null != csv) {
				csv.flush();
				csv.close();
			}

		}

	} // method writeFile
	

}
