package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class MatchPart
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

    public double MatchVolume;
    public double SurplusVolume;	// The Surplus Volume indicates what quantity would be remaining in the market after the share opens at the Match Price. 
    public double IndicativePrice;	// Bid or offer price provided by way of information rather than as the level at which a trader is willing to Trade.
	
    @Override
	public String toString() {
		return "MatchPart [IndicativePrice=" + IndicativePrice
				+ ", MatchVolume=" + MatchVolume + ", SurplusVolume="
				+ SurplusVolume + "]";
	}
}
