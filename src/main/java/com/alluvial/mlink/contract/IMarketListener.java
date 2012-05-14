package com.alluvial.mlink.contract;

import com.alluvial.mds.common.MDSHelper;
import com.alluvial.mds.contract.ConsolidatedOrder;
import com.alluvial.mds.contract.OffMktTrade;
import com.alluvial.mds.contract.Quote;
import com.alluvial.mds.contract.QuoteFull;
import com.alluvial.mds.contract.QuoteMatch;
import com.alluvial.mds.contract.SingleOrder;
import com.alluvial.mds.contract.Trade;

/**
 * This interface should be used by users of IRESS market link.
 * IRESS market link calls on...() methods for corresponding market events.
 * @author erepekto
 */
public interface IMarketListener {
	static final long Revision = MDSHelper.svnRevToLong("$Rev: 155 $");
	
	void onTrade(Trade trade);
	void onOffMktTrade(OffMktTrade trade);
	void onSingleOrder(SingleOrder order);
	void onConsolidatedOrder(ConsolidatedOrder order);
	void onUpdateDictionary(Integer SecurityId, String SecurityCode, String Exchange);
	void onQuote(Quote quote);
	void onQuoteMatch(QuoteMatch quote);
	void countMessage();
	
	// === replay-only related methods
	void onReplayDateStart(String replayDate);
	void onReplayDateFinish(String replayDate);
	void onQuoteFull(QuoteFull qf);
}
