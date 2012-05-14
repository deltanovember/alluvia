package com.alluvial.mds;

import com.alluvial.mds.common.MDSHelper;
import com.alluvial.mlink.contract.IMarketConnector;

/**
 * This class represents disconnection state. The only place from the state machine turns to
 * this state is LogonState in case if login was unsuccessful.
 * @author erepekto
 */
public class DisconnectionState extends AbsState {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

	DisconnectionState(MDSContext context) {
		super(context);
	}

	@Override
	String getStatus() {
		return "disconnecting state";
	}

	@Override
	void process() {
		IMarketConnector mconn = getMarketConnector();
		mconn.disconnect();
		getContext().changeState(null);
	}

	@Override
	void stop() {
	}
}
