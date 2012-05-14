package com.alluvial.mds.contract;

import java.io.Serializable;

public class ReplayReject implements Serializable {
	private static final long serialVersionUID = ContractHelper.svnRevToLong("$Rev: 163 $");

	public String date;
	public String reason;

	public ReplayReject(String date, String reason) {
		super();
		this.date = date;
		this.reason = reason;
	}

	@Override
	public String toString() {
		return "ReplayReject [date=" + date + ", reason=" + reason + "]";
	}
}
