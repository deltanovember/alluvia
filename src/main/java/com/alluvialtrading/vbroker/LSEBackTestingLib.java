package com.alluvialtrading.vbroker;

public class LSEBackTestingLib extends BackTestingLib {
	
	@Override
	public String getCloseTime() {
		return "16:30:00.000";
	}
	
	@Override
	public String getMarketName() {
		return "lse";
	}

}
