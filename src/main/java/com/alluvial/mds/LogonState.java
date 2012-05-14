package com.alluvial.mds;

import com.alluvial.mds.common.MDSHelper;
import com.alluvial.mlink.contract.MCException;

/**
 * This class represents logon state. MDS state machine turns to this state from connection state if
 * connection to IRESS was successful.
 * @author erepekto
 */
public class LogonState extends AbsState {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");
	
	AbsState nextState;
	
	LogonState(MDSContext context) {
		super(context);
	}

	@Override
	public void process() {
		try {
			getMarketConnector().login();
			getContext().changeState(new RecevingState(getContext()));
		}
		catch(MCException ex) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId()
					+ "]: error occurred in Logon state " + ex);
			getContext().changeState(new DisconnectionState(getContext()));			
		}
	}

	@Override
	public void stop() {
		throw new RuntimeException("LoginState::stop() is not implemented");
	}
	
	@Override
	public String getStatus() {
		return "logging in to IRESS";
	}
}
