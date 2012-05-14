package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class SecurityInfoMessageContent
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

    public int	SecurityId;		    // the security id, unique within the exchange.
    public String SecurityCode;	    // the readable security code.
    public String Exchange;		    // exchange.
	
    @Override
	public String toString() {
		return "SecurityInfoMessageContent [Exchange=" + Exchange
				+ ", SecurityCode=" + SecurityCode + ", SecurityId="
				+ SecurityId + "]";
	}
}
