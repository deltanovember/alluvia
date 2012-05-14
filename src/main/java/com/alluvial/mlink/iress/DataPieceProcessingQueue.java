package com.alluvial.mlink.iress;

import java.util.ArrayList;

import com.alluvial.mds.common.MDSHelper;

public class DataPieceProcessingQueue {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 166 $");
	
	ArrayList<IressDataPiece> queue = new ArrayList<IressDataPiece>();
	
	void add(IressDataPiece dataPiece) {
		synchronized(queue) {
			queue.add(dataPiece);
			queue.notify();
			//System.out.println(queue.size());
		}
	}
	
	IressDataPiece get() {
		synchronized(queue) {
			while (queue.size()==0)
				try {
					queue.wait(1);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				
			IressDataPiece dp = queue.get(0); // TODO: replace with IressDataPiece dp = queue.remove(0);
			queue.remove(0);
			return dp;
		}
	}
	
	void wakeUp(byte viaChannel) {
		synchronized(queue) {
			if (queue.size()==0) {
				queue.add(IressDataPiece.get(viaChannel));
				queue.notify();
			}
		}
	}
}
