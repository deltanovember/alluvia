package com.alluvial.mlink.contract;

import com.alluvial.mds.common.MDSHelper;

public class MCException extends Throwable {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");
	private static final long serialVersionUID = 1L;

	private String reason;
	private Exception underlyingException = null;
	
	public MCException(String reason) {
		this.reason = reason;
	}
	
	public MCException(Exception ex) {
		underlyingException = ex;
	}
	
	public MCException(String reason, Exception ex) {
		underlyingException = ex;
	}

	@Override
	public String getMessage() {
		return reason;
	}
	
	@Override
	public String toString() {
		return "MLinkException [reason=" + reason + ", underlyingException="
				+ underlyingException + "]";
	}
}
