package smarts.rmi.contract;

import java.io.Serializable;

public class ControlArgs implements Serializable {
	private static final long serialVersionUID = 1L;

	public int transId;
	public int securityId;
	public String security;
	public int contrlStatus;
	public char level;
	public char hundredths;
	public String tagfields;

	public ControlArgs(int transId, int securityId, String security,
			int contrlStatus, char level, char hundredths, String tagfields) {
		this.transId = transId;
		this.securityId = securityId;
		this.security = security;
		this.contrlStatus = contrlStatus;
		this.level = level;
		this.hundredths = hundredths;
		this.tagfields = tagfields;
	}
}