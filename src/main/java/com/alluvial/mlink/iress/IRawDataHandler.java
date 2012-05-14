package com.alluvial.mlink.iress;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.alluvial.mds.common.MDSHelper;

/**
 * This interface defines callback that is used when using receiveData() functions.
 * It is called for specified packet type and receives the buffer containing the packet contents.
 * @author erepekto
 */
public interface IRawDataHandler {
	static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

	void call(short packetLength, byte packetType, ByteBuffer stream) throws IOException;
}
