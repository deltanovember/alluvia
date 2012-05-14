package com.alluvial.mds;

import java.util.Timer;
import java.util.TimerTask;

import com.alluvial.mds.common.MDSHelper;
import com.alluvial.mlink.contract.MCException;

/**
 * This class represents receiving state. 
 * In this state:
 * 1) the MDS receives data from IRESS continuously, 
 *    for this purpose the separate threads are raised in IRESS market link;
 * 2) MDS opens accepting socket for algos connections;
 * 3) processes algos requests (dictionary request and subscription request);
 * 4) runs intensity measurer thread;
 * 
 * @author erepekto
 */
public class RecevingState extends AbsState {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 152 $");
	
	Timer intensityMeasurerTimer;
	
	RecevingState(MDSContext context) {
		super(context);
	}

	@Override
	public String getStatus() {
		return "receiving data from IRESS";
	}

	@Override
	void process() {
		// subscribe for instrument
		try {
			getMarketConnector().runDataReceiving();
		}
		catch (MCException ex) {
			System.out.println("[ERROR][T" + Thread.currentThread().getId() + "]: error occured in market connector while running continious receiving, turning to disconnection state " + ex);
			getContext().changeState(new DisconnectionState(getContext()));
			return;
		}

		// for research purposes, to see IRESS message intensity 
		runIntesityMeasurer();
		
		// run server for incoming clients (algos)
		getContext().runServer();
	
		getMarketConnector().waitDataReceivingFinished();

		// when market connector finished all started instances should be shut down
		getContext().stopServer();
	
		stopIntesityMeasurer();
		
		getContext().changeState(new DisconnectionState(getContext()));
	}
	
	@Override
	public void stop() {
		getMarketConnector().disconnect();
	}
	
	private void runIntesityMeasurer() {
		// intensity measurer
		intensityMeasurerTimer = new Timer();

		intensityMeasurerTimer.schedule( new TimerTask() {
							            public void run() {
							            	System.out.println("[DEBUG][T" + Thread.currentThread().getId() + "]: mps: " + getContext().getObserver().getMessageCount()/(float)5);
							            	getContext().getObserver().resetMessageCount();
							            }
						    		},
						    		5000, 5000);
	}
	
	private void stopIntesityMeasurer() {
		intensityMeasurerTimer.cancel();
	}
}
