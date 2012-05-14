package com.alluvial.mlink.iress;

import com.alluvial.mds.common.MDSHelper;

public class TradeActionFlags {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 166 $");
	
//	0x00000001; // This trade affects the Open, High, Low, and Last prices.
	static int ON_MARKET_TRADE = 0x00000002; // This trade affect the market volume and value. Without this flag set, the trade is off-market.
//	0x00000004; // This trade contains the accumulation of all traded volume of current session.
//	0x00000008; // Reserved
//	0x00000010; // Reserved
//	0x00000020; // This trade is a re-transmit of an existing trade. Ignore if same trade has been received previously.
	static int ON_CANCEL_TRADE = 0x00000040; // This is a trade cancelling operation. Only trade cancelling message carries this flag.
//	0x00000080; // Reserved
//	0x00000100; // The bid side is the aggressor
//	0x00000200; // The ask side is the aggressor
//	0x00000800; // This trade is an anonymous bid
//	0x00001000; // This trade is an anonymous ask
//	0x00002000; // This trade affects only the Open, High, and Low prices, not the Last.
//	0x00004000; // Reserved
}
