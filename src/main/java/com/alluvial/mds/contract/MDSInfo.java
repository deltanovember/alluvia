package com.alluvial.mds.contract;

import java.io.Serializable;

/**
 * This is the first message sent by MDS to the client.
 * It contains the contract version that MDS exposes.
 * @author erepekto
*/
public class MDSInfo implements Serializable {
	private static final long serialVersionUID = ContractHelper.svnRevToLong("$Rev: 163 $");

	public long contractVersion;

	@Override
	public String toString() {
		return "MDSInfo [contractVersion=" + contractVersion + "]";
	}
}
