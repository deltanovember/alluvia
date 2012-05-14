package com.alluvialtrading.vbroker;

/**
 *
 */
public class ASXBackTestingLib extends BackTestingLib {
	
	public ASXBackTestingLib() {
		super();
	}
	
	@Override
	public String getCloseTime() {
		return "16:00:00.000";
	}
	
	@Override
	public String getMarketName() {
		return "ASX";
	}
	


}
