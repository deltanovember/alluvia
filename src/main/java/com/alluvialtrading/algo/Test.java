package com.alluvialtrading.algo;

import com.alluvialtrading.tools.TraderLib;
import com.alluvialtrading.data.Regression;
import java.util.Date;
import java.util.GregorianCalendar;

import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.stat.regression.SimpleRegression;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;;

public class Test {


	public static void main (String[] args) {
        java.util.GregorianCalendar.getInstance();

        System.exit(0);
		TraderLib lib = new TraderLib();
		System.out.println(lib.round4(.426));

		TDistributionImpl dist = new TDistributionImpl(25);
		try {
			//System.out.println(dist.inverseCumulativeProbability(0.975));
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		
		double[] x = {50,53,54,55,56,59,62,65,67,71,72,74,75,76,79,80,82,85,87,90,93,94,95,97,100};
		double[] y = {122,118,128,121,125,136,144,142,149,161,167,168,162,171,175,182,180,183,188,200,194,206,207,210,219};

		lib.reformatLSEPriceThresholds("data", "thresholds.csv");
		//lib.computeRegression(x, y);
		/**
		for (int year=1; year < 100; year++) {
			int magicNumber = ((int) (year * 1.25)) % 7;
			System.out.println(year + "=" + magicNumber);
		}
		*/
		//Date d2 = lib.convertTime("10:30:53.4");
		
	}

}
