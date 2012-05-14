package smarts.rmi.contract;

import java.io.Serializable;

public class QuoteArgs implements Serializable {
	private static final long serialVersionUID = 1L;
	
	public int transId;
	public int securityId;
	public String security;
	public Double bidPrice;
	public Double askPrice;
	public Double bidVolume;
	public Double askVolume;

	public QuoteArgs(int transId, int securityId, String security,
			Double bidPrice, Double askPrice, Double bidVolume, Double askVolume) {
		this.transId = transId;
		this.securityId = securityId;
		this.security = security;
		this.bidPrice = bidPrice;
		this.askPrice = askPrice;
		this.bidVolume = bidVolume;
		this.askVolume = askVolume;
	}
	
	@Override
	public String toString() {
		return "QuoteArgs [askPrice=" + askPrice + ", askVolume=" + askVolume
				+ ", bidPrice=" + bidPrice + ", bidVolume=" + bidVolume
				+ ", security=" + security + ", securityId=" + securityId
				+ ", transId=" + transId + "]";
	}
}