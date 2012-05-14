package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class ServerInfo {
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 142 $");

	public String ServerName;
    public byte ServerType;
    public String ServerVersion;
    
    @Override
	public String toString() {
		return "ServerInfo [ServerName=" + ServerName + ", ServerType="
				+ ServerType + ", ServerVersion=" + ServerVersion + "]";
	}
}
