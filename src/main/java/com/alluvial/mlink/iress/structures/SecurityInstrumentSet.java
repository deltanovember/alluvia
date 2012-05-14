package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

// this can contain any of the four type of security codes, short/short with board/long/long with board.
public class SecurityInstrumentSet
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");
	
    public int SecurityId;
    public short DataSourceId;
    public short DataSourceBoardId;
	
    @Override
	public String toString() {
		return "SecurityInstrumentSet [DataSourceBoardId=" + DataSourceBoardId
				+ ", DataSourceId=" + DataSourceId + ", SecurityId="
				+ SecurityId + "]";
	}
}