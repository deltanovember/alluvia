package com.alluvial.mds.contract;

import java.io.Serializable;

public class DictionaryResponse implements Serializable {
//	@Override
//	public String toString() {
//		StringBuffer sb = new StringBuffer();
//		
//		for (int i=0; i<securities.length; i++)
//			sb.append(codes[i]+ ":" + securities[i] + " ");
//		
//		return "DictionaryResponse [securities=" + sb.toString() + "]";
//	}

	private static final long serialVersionUID = ContractHelper.svnRevToLong("$Rev: 163 $");

	//public Integer[] codes;
	public String[] securities;
}
