package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class SourceInstrumentSet
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

    public String Exchange;
    public String Datasource;
    public String Datasourceboard;
	
    @Override
	public String toString() {
		return "SourceInstrumentSet [Datasource=" + Datasource
				+ ", Datasourceboard=" + Datasourceboard + ", Exchange="
				+ Exchange + "]";
	}
}
