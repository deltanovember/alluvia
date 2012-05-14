package smarts.rmi.contract;

import java.io.Serializable;
import java.util.Arrays;

public class TradeArgs implements Serializable {
	private static final long serialVersionUID = 1L;

	int trans_id;
	int security_id;
	String security;
	Double id;

	Double price;
	Double volume;
	Double value;

	int[] amf;

	Double ask_id;
	int ask_littlefields;
	Double bid_id;
	int bid_littlefields;

	int littlefields;
	String[] tagfields;

	public TradeArgs(int transId, int securityId, String security, Double id,
			Double price, Double volume, Double value, int[] amf, Double askId,
			int askLittlefields, Double bidId, int bidLittlefields,
			int littlefields, String[] tagfields) {
		super();
		trans_id = transId;
		security_id = securityId;
		this.security = security;
		this.id = id;
		this.price = price;
		this.volume = volume;
		this.value = value;
		this.amf = amf;
		ask_id = askId;
		ask_littlefields = askLittlefields;
		bid_id = bidId;
		bid_littlefields = bidLittlefields;
		this.littlefields = littlefields;
		this.tagfields = tagfields;
	}

	public int getTrans_id() {
		return trans_id;
	}

	public int getSecurity_id() {
		return security_id;
	}

	public String getSecurity() {
		return security;
	}

	public Double getId() {
		return id;
	}

	public Double getPrice() {
		return price;
	}

	public Double getVolume() {
		return volume;
	}

	public Double getValue() {
		return value;
	}

	public int[] getAmf() {
		return amf;
	}

	public Double getAsk_id() {
		return ask_id;
	}

	public int getAsk_littlefields() {
		return ask_littlefields;
	}

	public Double getBid_id() {
		return bid_id;
	}

	public int getBid_littlefields() {
		return bid_littlefields;
	}

	public int getLittlefields() {
		return littlefields;
	}

	public String[] getTagfields() {
		return tagfields;
	}

	@Override
	public String toString() {
		return "TradeArgs [amf=" + Arrays.toString(amf) + ", ask_id=" + ask_id
				+ ", ask_littlefields=" + ask_littlefields + ", bid_id="
				+ bid_id + ", bid_littlefields=" + bid_littlefields + ", id="
				+ id + ", littlefields=" + littlefields + ", price=" + price
				+ ", security=" + security + ", security_id=" + security_id
				+ ", tagfields=" + Arrays.toString(tagfields) + ", trans_id="
				+ trans_id + ", value=" + value + ", volume=" + volume + "]";
	}
}
