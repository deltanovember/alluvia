package com.alluvialtrading.tools;

import org.apache.commons.math.distribution.TDistributionImpl;
import org.apache.commons.math.stat.descriptive.SummaryStatistics;
import org.apache.commons.math.stat.regression.SimpleRegression;

import com.alluvialtrading.data.Regression;

public class MathLib {

	public Regression getRegression(double[] x, double[] y, double xValue, double confidence) {
	
		TDistributionImpl dist = new TDistributionImpl(x.length-2);
		int extra = 1;
		
		double tStat = 0;
		try {
			tStat = dist.inverseCumulativeProbability(1-(1-confidence)/2);
			//System.out.println(tStat);
		}
		catch (Exception ex) {
			ex.printStackTrace();
		}
		SimpleRegression regression = new SimpleRegression();
		SummaryStatistics summary = new SummaryStatistics();
		for (int i=0; i<x.length; i++) {
			regression.addData(x[i], y[i]);
			summary.addValue(x[i]);
			if (xValue == x[i]) {
				extra = 0;
			}
		}
		
		double sum = 0;
		for (int i=0; i<x.length; i++) {
			sum += (x[i] - summary.getMean()) * (x[i] - summary.getMean());
		}
		
	
		double prediction = regression.predict(xValue);
		double square = (xValue-summary.getMean()) * (xValue-summary.getMean());
		double error = tStat * Math.sqrt(regression.getMeanSquareError() * (
				extra + 1.0/x.length + square/sum));
		//System.out.println( 1.0/x.length + square/sum);
		return new Regression(prediction, error, regression.getRSquare(), regression.getSlope());
	}

}
