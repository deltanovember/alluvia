package com.alluvial.mds;

import com.alluvial.mds.common.MDSHelper;
import com.alluvial.mlink.contract.IMarketConnector;

/**
 * This class represents abstract state of MDS state machine.
 * It declares methods that should be implemented for state machine usage.
 * @author erepekto
 */
public abstract class AbsState {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");
	
	private MDSContext context = null;
	
	AbsState(MDSContext context) {
		this.context = context;
	}

	MDSContext getContext() {
		return context;
	}
	
	IMarketConnector getMarketConnector() {
		return context.getMarketConnector();
	}
	
	abstract void process();
	abstract String getStatus();
	abstract void stop();
}
