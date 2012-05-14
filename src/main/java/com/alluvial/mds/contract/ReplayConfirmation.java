package com.alluvial.mds.contract;

import java.io.Serializable;

public class ReplayConfirmation implements Serializable {
	private static final long serialVersionUID = ContractHelper.svnRevToLong("$Rev: 163 $");

	public String date;
	
	@Override
	public String toString() {
		return "ReplayConfirmation [date=" + date + "]";
	}
}
