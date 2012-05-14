package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class DepthSortKeyUpdateMsgBody
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

    public char BidOrAsk;
    public long OldSortKey;
    public int OldSortSubKey;
    public long NewSortKey;
    public int NewSortSubKey;
	
    @Override
	public String toString() {
		return "DepthSortKeyUpdateMsgBody [BidOrAsk=" + BidOrAsk
				+ ", NewSortKey=" + NewSortKey + ", NewSortSubKey="
				+ NewSortSubKey + ", OldSortKey=" + OldSortKey
				+ ", OldSortSubKey=" + OldSortSubKey + "]";
	}
}
