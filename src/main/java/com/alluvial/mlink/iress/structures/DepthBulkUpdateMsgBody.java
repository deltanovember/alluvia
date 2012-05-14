package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class DepthBulkUpdateMsgBody
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

    public char BidOrAsk;
    public long StartSortKey;
    public int StartSortSubKey;
    public long EndSortKey;
    public int EndSortSubKey;
    public char Action;
    public char FieldFlag;				// 'P' – Updates to be applied to the Price field.
									    // 'O' – Updates to be applied to the Order Type field.
									    // ' ' – Field update values are not applicable.
    public double Price;
    public int OrderType;
	
    @Override
	public String toString() {
		return "DepthBulkUpdateMsgBody [Action=" + Action + ", BidOrAsk="
				+ BidOrAsk + ", EndSortKey=" + EndSortKey + ", EndSortSubKey="
				+ EndSortSubKey + ", FieldFlag=" + FieldFlag + ", OrderType="
				+ OrderType + ", Price=" + Price + ", StartSortKey="
				+ StartSortKey + ", StartSortSubKey=" + StartSortSubKey + "]";
	}
}
