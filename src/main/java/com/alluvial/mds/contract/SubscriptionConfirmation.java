package com.alluvial.mds.contract;

import java.io.Serializable;
import java.util.Arrays;

public class SubscriptionConfirmation implements Serializable  {
	private static final long serialVersionUID = ContractHelper.svnRevToLong("$Rev: 163 $");

	public byte[] retCodes;
	
	@Override
	public String toString() {
		return "SubscriptionConfirmation [retCodes="
				+ Arrays.toString(retCodes) + "]";
	}
}
