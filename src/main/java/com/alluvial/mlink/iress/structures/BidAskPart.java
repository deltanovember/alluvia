package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class BidAskPart
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");
	
    public double 	Price;
    public long 	Num;
    public double 	Volume;
    public String 	DataSourceName;
	
    @Override
	public String toString() {
		return "BidAskPart [DataSourceName=" + DataSourceName + ", Num=" + Num
				+ ", Price=" + Price + ", Volume=" + Volume + "]";
	}
}
