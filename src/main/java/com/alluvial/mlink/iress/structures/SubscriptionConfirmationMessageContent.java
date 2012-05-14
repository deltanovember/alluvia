package com.alluvial.mlink.iress.structures;

import com.alluvial.mds.common.MDSHelper;

public class SubscriptionConfirmationMessageContent
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 148 $");

    public SubscriptionConfirmationMessageContent()
    {
        SubscriptionSource = new SourceInstrumentSet();
    }
    public SourceInstrumentSet SubscriptionSource;		// the SubscriptionSource to which the error code is in response to previous subscription request sequence.
    public ErrorCode errorCode;				                // One of the values defined in ESubscriptionRequestErrorCodes below.

    public enum ErrorCode
    {
        SUBSCRIPTION_FAILURE_SERVER_NOT_READY ((short)-3),		// server not ready, try again later
        SUBSCRIPTION_FAILURE_PKTCOUNT_MISMATCH((short)-2),		// the actual number of received subscription supplement packets does not Match with specified packet number in subscription request message.
        SUBSCRIPTION_FAILURE_UNSPECIFIED ((short)-1),			// unspecified server error
        SUBSCRIPTION_SUCCESS ((short)0);				        // OK.
        
        ErrorCode(short c)
        {
        	code = c;
        }
        
        short code;
        
        public static ErrorCode convert(short value)
        {
        	for (int i=0; i<ErrorCode.class.getEnumConstants().length; i++)
        		if (ErrorCode.class.getEnumConstants()[i].code==value)
        			return ErrorCode.class.getEnumConstants()[i];
        			
        	throw new RuntimeException("supplied error code is not found " + value);
        }
    }
    
	@Override
	public String toString() {
		return "SubscriptionConfirmationMessageContent [ErrorCode=" + errorCode
				+ ", SubscriptionSource=" + SubscriptionSource + "]";
	}
}
