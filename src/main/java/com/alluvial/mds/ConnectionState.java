package com.alluvial.mds;

import com.alluvial.mds.common.MDSHelper;
import com.alluvial.mlink.contract.IMarketConnector;
import com.alluvial.mlink.contract.MCException;

/**
 * The class represents the connection state of MDS.
 * In this state MDS connects to IRESS using connect() method exposed by IRESS market link.
 * @author erepekto
 *
 */
public class ConnectionState extends AbsState {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");
	
	ConnectionState(MDSContext context) {
		super(context);
	}

	@Override
	public void process() {
		IMarketConnector mconn = getMarketConnector();

		try {
			mconn.connect();
			getContext().changeState(new LogonState(getContext()));
		}
		catch(MCException ex) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: error occurred while connecting to feed");
			ex.printStackTrace();
			getContext().changeState(null);
		}
	}
	
	@Override
	public void stop() {
		throw new RuntimeException("ConnectionState::stop() is not implemented");
	}
	
	@Override
	public String getStatus() {
		return "connecting to IRESS";
	}
}
