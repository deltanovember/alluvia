package com.alluvialtrading.data;

public class Regression {

	double prediction;
	double error;
	double rSquared;

	double slope;
	
	public double getrSquared() {
		return rSquared;
	}
	public void setrSquared(double rSquared) {
		this.rSquared = rSquared;
	}
	

	public Regression(double prediction, double error, double rSquared,
			double slope) {
		super();
		this.prediction = prediction;
		this.error = error;
		this.rSquared = rSquared;
		this.slope = slope;
	}
	public double getPrediction() {
		return prediction;
	}
	public void setPrediction(double prediction) {
		this.prediction = prediction;
	}
	public double getLowerBound() {
		return prediction - error;
	}
	public void setError(double error) {
		this.error = error;
	}
	public double getUpperBound() {
		return prediction + error;
	}
	
	public double getSlope() {
		return slope;
	}


	
	
}
