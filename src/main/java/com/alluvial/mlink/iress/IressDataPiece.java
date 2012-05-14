package com.alluvial.mlink.iress;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;

import com.alluvial.mds.common.MDSHelper;

public class IressDataPiece {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 155 $");
	
	byte channel; 
	ByteBuffer bb = ByteBuffer.allocateDirect(65536);
	
	private IressDataPiece(byte channel) {
		bb.order(ByteOrder.LITTLE_ENDIAN);
		this.channel = channel;
	}
	
	// === cache related functionality
	// cache is used in order not to allocate memory and have byte buffers already allocated
	private static ArrayList<IressDataPiece> cache = new ArrayList<IressDataPiece>();
	
	static IressDataPiece get(byte channel) {
		if (cache.size()!=0)
		{
			// if there is something in cache
			IressDataPiece dataPiece;

			synchronized(cache) {
				dataPiece = cache.get(0);
				cache.remove(0);
			}

			dataPiece.bb.clear();
			dataPiece.channel = channel;

			return dataPiece;
		}

		IressDataPiece newDataPiece = new IressDataPiece(channel);
		return newDataPiece;
	}
	
	static void put(IressDataPiece dp) {
		synchronized(cache) {
			cache.add(dp);
			dp.channel='N'; // TODO: to be removed
		}
	}
}
