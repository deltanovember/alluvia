package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class FeedMessageHeader
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 164 $");

    public SecurityInstrumentSet SecInstrument;
    public long UpdateTime;
    public long UpdateTimeNS;
    public short SessionId;
	
    @Override
	public String toString() {
		return "FeedMessageHeader [SecInstrument=" + SecInstrument
				+ ", SessionId=" + SessionId + ", UpdateTime=" + UpdateTime
				+ "]";
	}
}
