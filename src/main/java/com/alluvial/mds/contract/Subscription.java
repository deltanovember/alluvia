package com.alluvial.mds.contract;

import java.io.Serializable;

public class Subscription implements Serializable {
	private static final long serialVersionUID = ContractHelper.svnRevToLong("$Rev: 163 $");

	public String[] securities;
}
