package com.alluvial.mds.contract;

import java.io.Serializable;

public class QuoteMatch implements Serializable {
	private static final long serialVersionUID = ContractHelper.svnRevToLong("$Rev: 168 $");

	public String Security;
	public int 	  SecurityId;
	public double MatchVolume;
    public double SurplusVolume;	// The Surplus Volume indicates what quantity would be remaining in the market after the share opens at the Match Price. 
    public double IndicativePrice;	// Bid or offer price provided by way of information rather than as the level at which a trader is willing to Trade.
    public long	  UpdateTime;
    public long   UpdateTimeNS;

	public QuoteMatch(int securityId, double matchVolume,
			double surplusVolume, double indicativePrice,
			long updateTime, long UpdateTimeNS) {
		super();
		SecurityId = securityId;
		MatchVolume = matchVolume;
		SurplusVolume = surplusVolume;
		IndicativePrice = indicativePrice;
		this.UpdateTime = updateTime;
		this.UpdateTimeNS = UpdateTimeNS;
	}

	@Override
	public String toString() {
		return "QuoteMatch [IndicativePrice=" + IndicativePrice
				+ ", MatchVolume=" + MatchVolume + ", Security=" + Security
				+ ", SecurityId=" + SecurityId + ", SurplusVolume="
				+ SurplusVolume + ", UpdateTime=" + ContractHelper.dateFormat.format(UpdateTime)
				+ ", UpdateTimeNS=" + UpdateTimeNS + "]";
	}
}
