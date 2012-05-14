package com.alluvial.mdsclient;

import java.io.IOException;

import com.alluvial.mds.common.MDSHelper;

public class Main {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 158 $");

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) {
		long version = MDSHelper.getVersion();
		System.out.println("MDSClient (r" + version + ") is running.");
		
		MDSClient client = null;
		
		try {
			//String[] securities = new String[] {"DEMO", "DUMMY"};
			String[] securities = new String[] {"ASX", "RIO", "BHP"};
			//String[] securities = new String[] {"*"};

			client = new MDSClient();

			// replay
			client.subscribeForReplay("20111017");
			//client.subscribeForReplay("20111018");
		
			// live
			//client.requestDictionary();
			//client.subscribeForLive(securities);
			
			client.waitWhileReceiving();
			client.disconnect();
		}
		catch (Exception ex) {
			client.disconnect();
			ex.printStackTrace();
		}
		
		System.out.println("MDSClient has finished.");
	}
}
