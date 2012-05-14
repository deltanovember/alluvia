package com.alluvial.mlink.iress;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.HashMap;

import com.alluvial.mds.common.MDSHelper;
import com.alluvial.mds.contract.ConsolidatedOrder;
import com.alluvial.mds.contract.OffMktTrade;
import com.alluvial.mds.contract.Quote;
import com.alluvial.mds.contract.QuoteFull;
import com.alluvial.mds.contract.QuoteMatch;
import com.alluvial.mds.contract.SingleOrder;
import com.alluvial.mds.contract.Trade;
import com.alluvial.mlink.contract.IMarketListener;
import com.alluvial.mlink.iress.structures.BidAskPart;
import com.alluvial.mlink.iress.structures.DataResponseMessage;
import com.alluvial.mlink.iress.structures.DataSourceBoardInfoMessageContent;
import com.alluvial.mlink.iress.structures.DataSourceInfoMessageContent;
import com.alluvial.mlink.iress.structures.DepthBulkUpdateMsgBody;
import com.alluvial.mlink.iress.structures.DepthConsolidatedOrderMsgBody;
import com.alluvial.mlink.iress.structures.DepthSingleOrderMsgBody;
import com.alluvial.mlink.iress.structures.DepthSortKeyUpdateMsgBody;
import com.alluvial.mlink.iress.structures.ExchangeInfoMessageContent;
import com.alluvial.mlink.iress.structures.FeedDataPacketPayload;
import com.alluvial.mlink.iress.structures.FeedMessageHeader;
import com.alluvial.mlink.iress.structures.LoginRejectedPacketPayload;
import com.alluvial.mlink.iress.structures.MatchPart;
import com.alluvial.mlink.iress.structures.QuoteFullMsgBody;
import com.alluvial.mlink.iress.structures.SecurityInfoMessageContent;
import com.alluvial.mlink.iress.structures.SecurityInstrumentSet;
import com.alluvial.mlink.iress.structures.ServerInfo;
import com.alluvial.mlink.iress.structures.StatusPart;
import com.alluvial.mlink.iress.structures.SubscriptionConfirmationMessageContent;
import com.alluvial.mlink.iress.structures.TradeBrokerUpdateMsgBody;
import com.alluvial.mlink.iress.structures.TradeCancelMsgBody;
import com.alluvial.mlink.iress.structures.TradeMsgBody;
import com.alluvial.mlink.iress.structures.TradePart;
import com.alluvial.mlink.iress.structures.TradeRecoveryRequestMessageContent;
import com.alluvial.mlink.iress.util.JSMHexConverter;
import com.alluvial.mlink.iress.util.Sizeof;

/**
 * This class provides methods to parse and build IRESS messages.
 */
public class IdfHelper
{
	@SuppressWarnings("unused")
	private static final long Revision = MDSHelper.svnRevToLong("$Rev: 166 $");
	
	static final long millisec_offset_since_1600AD;

	static HashMap<String, IParser> handlers = null;
	static IMarketListener listener;
	
	static
	{
		// calculate offset in milliseconds since January 1st 1601
		Calendar calendar = Calendar.getInstance();
		calendar.set(1601, 0, 01, 00, 00, 00);
		calendar.set(Calendar.MILLISECOND, 0);
		millisec_offset_since_1600AD = calendar.getTimeInMillis();
		
		// register handlers to process data feed packets
		parseServerDataFeedPacketHandlerRegister();
	}
	
	// following enum definitions are cloned from the definitions within server module, namely
    // IressDataFeed.h in DataFeedServer project.

    static final short DEFAULT_DATASOURCEBOARD_ID = 1;
    static final short DEFAULT_DATASOURCE_ID = 1;

    static final short IDF_INVALID_SESSIONID = -1;

    // this is the type of IressFeedTCP packets. See IressFeedTCP doc for more information.
    public static class IressDataFeedPacketType
    {
        // used by both client and server:
    	static final byte IDF_PKT_DEBUG					= '+';
    	static final byte IDF_PKT_HEARTBEAT				= 'H';	// heart beat packet between server and client.

        // packets from the client:
    	static final byte IDF_PKT_CLIENT_LOGIN			= 'L';
    	static final byte IDF_PKT_CLIENT_CONTROL		= 'U';	// control packets to carry messages such as data request.

        // packets from the server to the client:
    	static final byte IDF_PKT_SERVER_INFORMATION	= 'I';	// the server information packet to be sent upon connection.
    	static final byte IDF_PKT_SERVER_LOGIN_ACCEPTED	= 'A';	// login accepted
    	static final byte IDF_PKT_SERVER_LOGIN_REJECTED	= 'J';	// login rejected

    	static final byte IDF_PKT_SERVER_FEEDDATA		= 'D';	// feed data packet carrying the feed data messages.
    }

    static final char LOGIN_REJECT_NOT_AUTHORIZED 		= 'A';
    static final char LOGIN_REJECT_SERVER_NOT_READY 	= 'F';

	// ----------------------------------------------------------------------------------------------
    // The types of IressDataFeed messages, i.e. the payload of IressFeedTCP data packets.

    // messages from clients:
	static final String IDF_MSG_SUBSCRIPTION_REQUEST        		= "SU";
	static final String IDF_MSG_SUBSCRIPTION_TRADE_RECOVERY_REQUEST	= "TR";

    // messages to clients:

    // informational messages to help build up a dictionary about various types of Ids.
    static final String IDF_MSG_SUBSCRIPTION_CONFIRMATION	= "SC";		// the result of the subscription request.
    static final String IDF_MSG_EXCHANGE_INFO				= "XX";		// exchange information message
    static final String IDF_MSG_DATASOURCE_INFO			    = "XD";		// data SubscriptionSource information message
    static final String IDF_MSG_DTASOURCEBOARD_INFO		    = "XB";		// data SubscriptionSource board information message
    static final String IDF_MSG_SECURITY_INFO				= "IM";		// security information message

    // quote messages
    static final String IDF_MSG_QUOTE_FULL					= "QF";		// quote messages - full
    static final String IDF_MSG_QUOTE_BID					= "QB";		// quote messages - Bid only
    static final String IDF_MSG_QUOTE_ASK					= "QA";		// quote messages - Ask only
    static final String IDF_MSG_QUOTE_STATUS				= "QS";		// quote messages - Status only
    static final String IDF_MSG_QUOTE_TRADE			    	= "QT";		// quote messages - Trade only
    static final String IDF_MSG_QUOTE_MATCH			    	= "QM";		// quote messages - Match only

    // depth messages
    static final String IDF_MSG_DEPTH_SINGLE_ORDER			= "DS";		// Depth - Single Order
    static final String IDF_MSG_DEPTH_CONSOLIDATED_ORDER	= "DC";		// Depth - Consolidated Order
    static final String IDF_MSG_DEPTH_CLEAR				    = "DE";		// Depth - Clear whole depth
    static final String IDF_MSG_DEPTH_UPDATE_SORTKEY		= "DO";		// Depth - update sort key
    static final String IDF_MSG_DEPTH_BULKUPDATE			= "DU";		// Depth - bulk-update

    // Trade messages
    static final String IDF_MSG_TRADE      					= "TR";
    static final String IDF_MSG_TRADE_UPDATE_BROKER			= "TB";
    static final String IDF_MSG_TRADE_CANCEL				= "TC";

    // ----------------------------------------------------------------------------------------------
    // the sizes of common fields.
    static final int PACKETLENGTH_FIELD_SIZE = 2;
    static final int PACKETTYPE_SIZE = 1;
    static final int MSGTYPE_SIZE = 2;

    // The sizes of String type fields.
    static final int USERNAME_FIELD_SIZE = 6 + 1;
    static final int PASSWORD_FIELD_SIZE = 128 + 1;
    static final int REQUEST_FLAG_FIELD_SIZE = 2; // sizeof(short)

    static final int EXCHANGE_FIELD_SIZE = 16 + 1;
    static final int DATASOURCE_FIELD_SIZE = 8 + 1;
    static final int DATASOURCEBOARD_FIELD_SIZE = 8 + 1;

    static final int MESSAGETYPE_FIELD_SIZE = 2;

    static final int SERVERNAME_FIELD_SIZE = 20 + 1;
    static final int SERVERVERSION_FIELD_SIZE = 8 + 1;

    static final int EXCHANGESTATUS_FIELD_SIZE = 50 + 1;

    static final int SECURITYCODE_FIELD_SIZE = 32 + 1;

    static final int QUOTATIONBASIS_FIELD_SIZE = 11 + 1;
    static final int STATUSNOTES_FIELD_SIZE = 8 + 1;

    static final int TRADECONDITIONCODES_FIELD_SIZE = 8 + 1;
    // ----------------------------------------------------------------------------------------------

    static final char DEPTH_ORDER_BID = 'B';
    static final char DEPTH_ORDER_ASK = 'A';

    static final char DEPTH_ACTION_ADD = 'A';
    static final char DEPTH_ACTION_DELETE = 'D';
    static final char DEPTH_ACTION_MODIFY = 'M';

    static final char DEPTH_ACTION_BULK_SETVALUES = 'S';
    static final char DEPTH_ACTION_BULK_SET_BITS = 'I';
    static final char DEPTH_ACTION_BULK_UNSET_BITS = 'J';
    static final char DEPTH_ACTION_BULK_DELETEORDERS = 'E';

    static final char DEPTH_FIELDFLAG_PRICE = 'P';
    static final char DEPTH_FIELDFLAG_ORDERTYPE = 'O';
    static final char DEPTH_FIELDFLAG_NONE = ' ';

    // helper methods
    public static byte[] stringToByteArray(String str) throws UnsupportedEncodingException
    {
        return str.getBytes("UTF-8");  	// convert to UTF-8 according to C# sample provided by IRESS
    }

    // providing a default parameter value of 1024.
    public static String PrintBinaryInHex(byte[] barray)
    {
		return JSMHexConverter.ByteArrayToHexString(barray, "");
    }

    private static void BinaryWriteWithFilling(ByteArrayOutputStream binWriter, 
    										  byte[] subscriptionSource, int size) throws IOException
    {
        int BytesToFill = size - subscriptionSource.length;
        if (BytesToFill <= 0)
            binWriter.write(subscriptionSource, 0, size);
        else
        {
            binWriter.write(subscriptionSource);
            for (int i = 0; i < BytesToFill; i++)
                binWriter.write((byte)0);   // fill in ZERO. this filling has to be reviewed.
        }
    }

	private static long convertTimestampToTime(long timestamp) {
    	// from http://stackoverflow.com/questions/5198021/java-convert-hex-to-time
		// convert 100 nanos to milliseconds
        timestamp=timestamp/10000;

        // convert to time offset from 1st Jan 1601 AD
        timestamp += millisec_offset_since_1600AD;

//        // TODO: the instance should be raised once, thus IdfHelper should be non-static. 
//        // Instantiation moved here here, because calendar is not thread safe, i.e. can't be used as static member.
//    	Calendar calendar = Calendar.getInstance();
//        
//        try {
//        	calendar.setTimeInMillis(timestamp);
//        }
//        catch (Exception ex) {
//        	calendar.setTimeInMillis(millisec_offset_since_1600AD);
//        	ex.printStackTrace();
//        }
//        
//        return calendar.getTime();
        return timestamp; 
	}

	public static LoginRejectedPacketPayload parseLoginRejectedPacket(ByteBuffer bb) throws IOException
	{
		LoginRejectedPacketPayload pkt = new LoginRejectedPacketPayload();
		pkt.Reason = (char)(bb.get());

		return pkt;
	}
	
	// --- Parse data feed packets --- //
	private interface IParser
	{
		Object parse(ByteBuffer binReader) throws IOException;
	}
	
	/**
	 * This function registers handlers for parsing data feed packets.
	 */
	public static void parseServerDataFeedPacketHandlerRegister()
    {
		handlers = new HashMap<String, IParser>(19);  // 19 is the number of handlers, it is set for better efficiency
		
    	handlers.put(IdfHelper.IDF_MSG_SUBSCRIPTION_CONFIRMATION, 
					 new IParser() {
						 	@Override
							public Object parse(ByteBuffer binReader) throws IOException {
								return IdfHelper.parseSubscriptionConfirmation(binReader);
							}
				     });

    	handlers.put(IdfHelper.IDF_MSG_EXCHANGE_INFO, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
							return IdfHelper.parseExchangeInformation(binReader);
						}
			     });
    	
    	handlers.put(IdfHelper.IDF_MSG_DATASOURCE_INFO, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
							return IdfHelper.parseDataSourceInfo(binReader);
						}
			     });

    	handlers.put(IdfHelper.IDF_MSG_DTASOURCEBOARD_INFO, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
							return IdfHelper.parseDataSourceBoardInfo(binReader);
						}
			     });

    	handlers.put(IdfHelper.IDF_MSG_SECURITY_INFO, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
					 		SecurityInfoMessageContent simc = IdfHelper.parseSecurityInfo(binReader);
					 		listener.onUpdateDictionary(simc.SecurityId, simc.SecurityCode, simc.Exchange);
							return simc;
						}
			     });
    	
        // quote messages
    	handlers.put(IdfHelper.IDF_MSG_QUOTE_FULL, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				            DataResponseMessage msg = new DataResponseMessage();
				            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
				            
				            QuoteFullMsgBody qfmb = IdfHelper.parseQuoteFullPacket(binReader);
				            msg.DataMsgBody = qfmb;

				            // for external usage
				            QuoteFull qf = new QuoteFull(msg.DataMsgHeader.SecInstrument.SecurityId, qfmb.Timezone,
			            		qfmb.QuotationBasis, qfmb.SecurityType, qfmb.OpenPrice,
			            		qfmb.HighPrice, qfmb.LowPrice, qfmb.ClosePrice,
			            		qfmb.StatusNotes, qfmb.SecurityStatus.SecurityStatus, qfmb.Bid.Price,
			            		qfmb.Bid.Num, qfmb.Bid.Volume, qfmb.Bid.DataSourceName,
			            		qfmb.Ask.Price, qfmb.Ask.Num, qfmb.Ask.Volume,
			            		qfmb.Ask.DataSourceName, qfmb.Trade.LastPrice, qfmb.Trade.NumOfTrades,
			            		qfmb.Trade.TradeTime, qfmb.Trade.TradeTimeNS, qfmb.Trade.MktValue,
			            		qfmb.Trade.MktVolume, qfmb.Trade.CumValue, qfmb.Trade.CumVolume,
			            		qfmb.Match.MatchVolume, qfmb.Match.SurplusVolume, qfmb.Match.IndicativePrice,
        						msg.DataMsgHeader.UpdateTime, msg.DataMsgHeader.UpdateTimeNS);
				            
				            listener.onQuoteFull(qf);

				            return msg;
						}
			     });
    	
    	handlers.put(IdfHelper.IDF_MSG_QUOTE_BID, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				        	DataResponseMessage msg = new DataResponseMessage();
				            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
				            msg.DataMsgBody = IdfHelper.parseBidAskPart(binReader);
				            
				            // external
				            BidAskPart bap = (BidAskPart)msg.DataMsgBody;
				            Quote quote = new Quote(msg.DataMsgHeader.SecInstrument.SecurityId,
				            						'B', bap.Price, bap.Num, bap.Volume, bap.DataSourceName,
				            						msg.DataMsgHeader.UpdateTime, msg.DataMsgHeader.UpdateTimeNS);
				            listener.onQuote(quote);
				            
				            return msg;
						}
			     });

    	handlers.put(IdfHelper.IDF_MSG_QUOTE_ASK, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				        	DataResponseMessage msg = new DataResponseMessage();
				            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
				            msg.DataMsgBody = IdfHelper.parseBidAskPart(binReader);
				            
				            // external
				            BidAskPart bap = (BidAskPart)msg.DataMsgBody;
				            Quote quote = new Quote(msg.DataMsgHeader.SecInstrument.SecurityId,
				            						'A', bap.Price, bap.Num, bap.Volume, bap.DataSourceName,
				            						 msg.DataMsgHeader.UpdateTime, msg.DataMsgHeader.UpdateTimeNS); 
				            listener.onQuote(quote);
				            
				            return msg;
						}
			     });

    	handlers.put(IdfHelper.IDF_MSG_QUOTE_MATCH, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				        	DataResponseMessage msg = new DataResponseMessage();
				            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
				            msg.DataMsgBody = IdfHelper.parseMatchPart(binReader);
				            
				            // external usage
				            MatchPart mp = (MatchPart)msg.DataMsgBody;
				            QuoteMatch qm = new QuoteMatch(msg.DataMsgHeader.SecInstrument.SecurityId, 
				            							mp.MatchVolume, mp.SurplusVolume, mp.IndicativePrice,
				            							msg.DataMsgHeader.UpdateTime, msg.DataMsgHeader.UpdateTimeNS);
				            listener.onQuoteMatch(qm); 
				            
				            return msg;
						}
			     });
    	
    	handlers.put(IdfHelper.IDF_MSG_QUOTE_STATUS, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				        	DataResponseMessage msg = new DataResponseMessage();
				            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
				            msg.DataMsgBody = IdfHelper.parseStatusPart(binReader);
				            return msg;
						}
			     });

    	handlers.put(IdfHelper.IDF_MSG_QUOTE_TRADE, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				        	DataResponseMessage msg = new DataResponseMessage();
				            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
				            
				            TradePart tradePart = IdfHelper.parseTradePart(binReader);
				            msg.DataMsgBody = tradePart;
				            
//				            listener.onTrade(msg.DataMsgHeader.SecInstrument.SecurityId, 
//				            				 tradePart.LastPrice, 
//				            				 tradePart.CumVolume);
				            return msg;
						}
			     });
    	
        // depth messages
    	handlers.put(IdfHelper.IDF_MSG_DEPTH_SINGLE_ORDER, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				        	DataResponseMessage msg = new DataResponseMessage();
				            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
				            msg.DataMsgBody = IdfHelper.parseDepthSingleOrder(msg.DataMsgHeader,
				            												  binReader);
				            return msg;
						}
			     });
    	
    	handlers.put(IdfHelper.IDF_MSG_DEPTH_CONSOLIDATED_ORDER, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				        	DataResponseMessage msg = new DataResponseMessage();
				            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
				            msg.DataMsgBody = IdfHelper.parseDepthConsolidatedOrder(
				            		msg.DataMsgHeader, binReader);
				            return msg;
						}
			     });

    	handlers.put(IdfHelper.IDF_MSG_DEPTH_BULKUPDATE, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				        	DataResponseMessage msg = new DataResponseMessage();
				            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
				            msg.DataMsgBody = IdfHelper.parseDepthBulkUpdateMsg(binReader);
				            return msg;
						}
			     });
    	
    	handlers.put(IdfHelper.IDF_MSG_DEPTH_UPDATE_SORTKEY, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				        	DataResponseMessage msg = new DataResponseMessage();
				            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
				            msg.DataMsgBody = IdfHelper.parseDepthSortKeyUpdate(binReader);
				            return msg;
						}
			     });

    	handlers.put(IdfHelper.IDF_MSG_DEPTH_CLEAR, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				        	DataResponseMessage msg = new DataResponseMessage();
				            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
							msg.DataMsgBody = null; // TODO ???
				            return msg;
						}
			     });

        // Trade message
    	handlers.put(IdfHelper.IDF_MSG_TRADE, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				        	DataResponseMessage msg = new DataResponseMessage();
                            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
				            TradeMsgBody tradeMsgBody = IdfHelper.parseTradeMessage(binReader);
				            msg.DataMsgBody = tradeMsgBody;
				            
				            if ( (tradeMsgBody.ActionFlag & TradeActionFlags.ON_MARKET_TRADE) != 0) {
					            // external usage
					            Trade trade = new Trade(msg.DataMsgHeader.SecInstrument.SecurityId,
					            		tradeMsgBody.SellerId,
					            		tradeMsgBody.SellerOrderId,
					            		tradeMsgBody.BuyerId,
					            		tradeMsgBody.BuyerOrderId,
					            		tradeMsgBody.TradeNo,
					            		tradeMsgBody.TradeValue,
					            		tradeMsgBody.TradeVolume,
					            		tradeMsgBody.TradePrice,
					            		tradeMsgBody.TradeTime,
					            		tradeMsgBody.ActionFlag,
					            		tradeMsgBody.ConditionCodes,
					            		msg.DataMsgHeader.UpdateTime,
					            		msg.DataMsgHeader.UpdateTimeNS);
					            
					            listener.onTrade(trade);
				            }
				            else {
					            OffMktTrade trade = new OffMktTrade(msg.DataMsgHeader.SecInstrument.SecurityId,
					            		tradeMsgBody.SellerId,
					            		tradeMsgBody.BuyerId,
					            		tradeMsgBody.TradeNo,
					            		tradeMsgBody.TradeValue,
					            		tradeMsgBody.TradeVolume,
					            		tradeMsgBody.TradePrice,
					            		tradeMsgBody.TradeTime,
					            		tradeMsgBody.ConditionCodes,
					            		msg.DataMsgHeader.UpdateTime,
					            		msg.DataMsgHeader.UpdateTimeNS);

					            listener.onOffMktTrade(trade);
				            }
				            
				            return msg;
						}
			     });

    	handlers.put(IdfHelper.IDF_MSG_TRADE_UPDATE_BROKER, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				        	DataResponseMessage msg = new DataResponseMessage();
				            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
				            msg.DataMsgBody = IdfHelper.parseTradeBrokerUpdateMessage(binReader);
				            return msg;
						}
			     });
    	
    	handlers.put(IdfHelper.IDF_MSG_TRADE_CANCEL, 
				 new IParser() {
					 	@Override
						public Object parse(ByteBuffer binReader) throws IOException {
				        	DataResponseMessage msg = new DataResponseMessage();
				            msg.DataMsgHeader = IdfHelper.parseMsgHeader(binReader);
				            msg.DataMsgBody = IdfHelper.parseTradeCancelMessage(binReader);
				            return msg;
						}
			     });
    }
	
    private static String byteArrayToAsciiString(ByteBuffer reader, int exchangeFieldSize) throws UnsupportedEncodingException {
    	byte[] array = new byte[exchangeFieldSize];
    	reader.get(array);
    	String result = new String(array, "ASCII");
    	int termIndex = result.indexOf('\0'); 					// TODO: can be done more efficiently?
		return result.substring(0, termIndex!=-1?termIndex:0);
	}
	
	public static boolean parseServerDataFeedPacket(ByteBuffer binReader, FeedDataPacketPayload pktPayload) throws IOException
    {
		pktPayload.MsgType = IdfHelper.getMessageType(binReader);
		IParser handler = handlers.get(pktPayload.MsgType);
		
		if (handler!=null)
		{
			pktPayload.MsgContent = handler.parse(binReader);
			return true;
		}
		
		return false;
    }

    public static SubscriptionConfirmationMessageContent parseSubscriptionConfirmation(ByteBuffer reader) throws UnsupportedEncodingException
    {
        SubscriptionConfirmationMessageContent SubConfirmMsg = new SubscriptionConfirmationMessageContent();

        SubConfirmMsg.SubscriptionSource.Exchange = byteArrayToAsciiString(reader, EXCHANGE_FIELD_SIZE);
        SubConfirmMsg.SubscriptionSource.Datasource = byteArrayToAsciiString(reader, DATASOURCE_FIELD_SIZE);
        SubConfirmMsg.SubscriptionSource.Datasourceboard = byteArrayToAsciiString(reader, DATASOURCEBOARD_FIELD_SIZE);
        short errCode = reader.getShort();
        SubConfirmMsg.errorCode = SubscriptionConfirmationMessageContent.ErrorCode.convert(errCode);

        return SubConfirmMsg;
    }

	public static ExchangeInfoMessageContent parseExchangeInformation(ByteBuffer reader) throws UnsupportedEncodingException
    {
        ExchangeInfoMessageContent ExgInfo = new ExchangeInfoMessageContent();

        ExgInfo.Exchange = byteArrayToAsciiString(reader, EXCHANGE_FIELD_SIZE);
        ExgInfo.DataSource = byteArrayToAsciiString(reader, DATASOURCE_FIELD_SIZE);
        ExgInfo.Status = byteArrayToAsciiString(reader, EXCHANGESTATUS_FIELD_SIZE);
        ExgInfo.ExchangeTime = reader.getLong();
        ExgInfo.ErrorCode = reader.getShort();

        return ExgInfo;
    }
    
    public static DataSourceInfoMessageContent parseDataSourceInfo(ByteBuffer reader) throws UnsupportedEncodingException
    {
        DataSourceInfoMessageContent DsInfo = new DataSourceInfoMessageContent();

        DsInfo.Exchange = byteArrayToAsciiString(reader, EXCHANGE_FIELD_SIZE);
        DsInfo.DataSource = byteArrayToAsciiString(reader, DATASOURCE_FIELD_SIZE);
        DsInfo.DataSourceId = reader.getShort();

        return DsInfo;
    }

    public static DataSourceBoardInfoMessageContent parseDataSourceBoardInfo(ByteBuffer reader) throws UnsupportedEncodingException
    {
        DataSourceBoardInfoMessageContent DsbInfo = new DataSourceBoardInfoMessageContent();

        DsbInfo.Exchange = byteArrayToAsciiString(reader, EXCHANGE_FIELD_SIZE);
        DsbInfo.DataSourceId = reader.getShort();
        DsbInfo.DataSourceBoard = byteArrayToAsciiString(reader, DATASOURCEBOARD_FIELD_SIZE);
        DsbInfo.DataSourceBoardId = reader.getShort();

        return DsbInfo;
    }

    public static SecurityInfoMessageContent parseSecurityInfo(ByteBuffer reader) throws UnsupportedEncodingException
    {
        SecurityInfoMessageContent secInfo = new SecurityInfoMessageContent();

        secInfo.SecurityId = reader.getInt();
        secInfo.SecurityCode = byteArrayToAsciiString(reader, SECURITYCODE_FIELD_SIZE);
        secInfo.Exchange = byteArrayToAsciiString(reader, EXCHANGE_FIELD_SIZE);

        return secInfo;
    }

    public static StatusPart parseStatusPart(ByteBuffer reader)
    {
        StatusPart status = new StatusPart();
        status.SecurityStatus = (char)(reader.get());
        return status;
    }

    public static BidAskPart parseBidAskPart(ByteBuffer reader) throws UnsupportedEncodingException
    {
        BidAskPart msgBody = new BidAskPart();
        msgBody.Price = reader.getDouble();
        msgBody.Num = reader.getInt();
        msgBody.Volume = reader.getDouble();
        msgBody.DataSourceName = byteArrayToAsciiString(reader, DATASOURCE_FIELD_SIZE);
        return msgBody;
    }

    public static TradePart parseTradePart(ByteBuffer reader)
    {
        TradePart msgBody = new TradePart();
        msgBody.LastPrice = reader.getDouble();
        msgBody.NumOfTrades = reader.getInt();

        msgBody.TradeTimeNS = reader.getLong();
        if (msgBody.TradeTimeNS != 0)
            msgBody.TradeTime = convertTimestampToTime(msgBody.TradeTimeNS);

        msgBody.MktValue = reader.getDouble();
        msgBody.MktVolume = reader.getDouble();

        msgBody.CumValue = reader.getDouble();
        msgBody.CumVolume = reader.getDouble();

        return msgBody;
    }

    public static MatchPart parseMatchPart(ByteBuffer reader)
    {
        MatchPart msgBody = new MatchPart();
        msgBody.MatchVolume = reader.getDouble();
        msgBody.SurplusVolume = reader.getDouble();
        msgBody.IndicativePrice = reader.getDouble();
        return msgBody;
    }

    public static QuoteFullMsgBody parseQuoteFullPacket(ByteBuffer reader) throws UnsupportedEncodingException
    {
        QuoteFullMsgBody msgBody = new QuoteFullMsgBody();

        // misc info
        msgBody.Timezone = reader.getShort();
        msgBody.QuotationBasis = byteArrayToAsciiString(reader, QUOTATIONBASIS_FIELD_SIZE);
        msgBody.SecurityType = reader.getInt();

        msgBody.OpenPrice = reader.getDouble();
        msgBody.HighPrice = reader.getDouble();
        msgBody.LowPrice = reader.getDouble();
        msgBody.ClosePrice = reader.getDouble();

        msgBody.StatusNotes = byteArrayToAsciiString(reader, STATUSNOTES_FIELD_SIZE);

        msgBody.SecurityStatus = parseStatusPart(reader);
        msgBody.Bid = parseBidAskPart(reader);
        msgBody.Ask = parseBidAskPart(reader);
        msgBody.Trade = parseTradePart(reader);
        msgBody.Match = parseMatchPart(reader);

        return msgBody;
    }

    public static DepthSingleOrderMsgBody parseDepthSingleOrder(FeedMessageHeader header, ByteBuffer reader)
    {
        DepthSingleOrderMsgBody msgBody = new DepthSingleOrderMsgBody();
        msgBody.BidOrAsk = (char)(reader.get());
        msgBody.SortKey = reader.getLong();
        msgBody.SortSubKey = reader.getInt();
        msgBody.Action = (char)(reader.get());
        msgBody.Price = reader.getDouble();
        msgBody.Volume = reader.getDouble();
        msgBody.OrderType = reader.getInt();
        msgBody.BrokerNo = reader.getShort();
        msgBody.OrderNo = reader.getLong();
        
        // for external usage
        SingleOrder so = new SingleOrder();
        so.SecurityId = header.SecInstrument.SecurityId;
        so.BidOrAsk = msgBody.BidOrAsk;
        so.SortKey = msgBody.SortKey;
        so.SortSubKey = msgBody.SortSubKey;
        so.Action = msgBody.Action;
        so.Price = msgBody.Price;
        so.Volume = msgBody.Volume;
        so.OrderType = msgBody.OrderType;
        so.BrokerNo = msgBody.BrokerNo;
        so.OrderNo = msgBody.OrderNo;
        so.UpdateTime = header.UpdateTime;
        so.UpdateTimeNS = header.UpdateTimeNS;
        listener.onSingleOrder(so);
        
        return msgBody;
    }

    public static DepthConsolidatedOrderMsgBody parseDepthConsolidatedOrder(FeedMessageHeader header, ByteBuffer reader)
    {
        DepthConsolidatedOrderMsgBody msgBody = new DepthConsolidatedOrderMsgBody();
        msgBody.BidOrAsk = (char)(reader.get());
        msgBody.SortKey = reader.getLong();
        msgBody.SortSubKey = reader.getInt();
        msgBody.Action = (char)(reader.get());
        msgBody.Price = reader.getDouble();
        msgBody.Volume = reader.getDouble();
        msgBody.OrderType = reader.getInt();
        msgBody.OrderCount = reader.getInt();
        
        // for external usage
        ConsolidatedOrder co = new ConsolidatedOrder();
        co.SecurityID = header.SecInstrument.SecurityId;
        co.BidOrAsk = msgBody.BidOrAsk;
        co.SortKey = msgBody.SortKey;
        co.SortSubKey = msgBody.SortSubKey;
        co.Action = msgBody.Action;
        co.Price = msgBody.Price;
        co.Volume = msgBody.Volume;
        co.OrderType = msgBody.OrderType;
        co.OrderCount = msgBody.OrderCount;
        co.UpdateTime = header.UpdateTime;
        co.UpdateTimeNS = header.UpdateTimeNS;
        listener.onConsolidatedOrder(co);
        
        return msgBody;
    }

    public static DepthBulkUpdateMsgBody parseDepthBulkUpdateMsg(ByteBuffer reader)
    {
        DepthBulkUpdateMsgBody msgBody = new DepthBulkUpdateMsgBody();
        msgBody.BidOrAsk = (char)(reader.get());
        msgBody.StartSortKey = reader.getLong();
        msgBody.StartSortSubKey = reader.getInt();
        msgBody.EndSortKey = reader.getLong();
        msgBody.EndSortSubKey = reader.getInt();
        msgBody.Action = (char)(reader.get());
        msgBody.FieldFlag = (char)(reader.get());
        msgBody.Price = reader.getDouble();
        msgBody.OrderType = reader.getInt();
        return msgBody;
    }

    public static DepthSortKeyUpdateMsgBody parseDepthSortKeyUpdate(ByteBuffer reader)
    {
        DepthSortKeyUpdateMsgBody msgBody = new DepthSortKeyUpdateMsgBody();
        msgBody.BidOrAsk = (char)(reader.get());
        msgBody.OldSortKey = reader.getLong();
        msgBody.OldSortSubKey = reader.getInt();
        msgBody.NewSortKey = reader.getLong();
        msgBody.NewSortSubKey = reader.getInt();
        return msgBody;
    }

    public static TradeMsgBody parseTradeMessage(ByteBuffer reader) throws UnsupportedEncodingException
    {
        TradeMsgBody msgBody = new TradeMsgBody();
        msgBody.SellerId = reader.getShort();
        msgBody.SellerOrderId = reader.getLong();
        msgBody.BuyerId = reader.getShort();
        msgBody.BuyerOrderId = reader.getLong();
        msgBody.TradeNo = reader.getInt();
        msgBody.TradeValue = reader.getDouble();
        msgBody.TradeVolume = reader.getDouble();
        msgBody.TradePrice = reader.getDouble();
        msgBody.TradeTimeNS = reader.getLong();
        msgBody.TradeTime = convertTimestampToTime(msgBody.TradeTimeNS);
        msgBody.ActionFlag = reader.getInt();
        msgBody.ConditionCodes = byteArrayToAsciiString(reader, TRADECONDITIONCODES_FIELD_SIZE);
        return msgBody;
    }

    public static TradeCancelMsgBody parseTradeCancelMessage(ByteBuffer reader) throws UnsupportedEncodingException
    {
        TradeCancelMsgBody msgBody = new TradeCancelMsgBody();
        msgBody.tradeCancelled = parseTradeMessage(reader);
        msgBody.CancelTradeNo = reader.getInt();
        return msgBody;
    }

    public static TradeBrokerUpdateMsgBody parseTradeBrokerUpdateMessage(ByteBuffer reader)
    {
        TradeBrokerUpdateMsgBody msgBody = new TradeBrokerUpdateMsgBody();
        msgBody.TradeNo = reader.getInt();
		msgBody.BrokerId = reader.getShort();
		msgBody.BidOrAsk = (char)(reader.get());
		msgBody.TradeTime = reader.getLong();
		return msgBody;
    }

    // login packet
    public static byte[] buildLoginPacket(String username, String password, short RequestFlag) throws UnsupportedEncodingException, IOException
    {
        int PacketSize = PACKETLENGTH_FIELD_SIZE + PACKETTYPE_SIZE + USERNAME_FIELD_SIZE + PASSWORD_FIELD_SIZE + REQUEST_FLAG_FIELD_SIZE;  // user name, password, request flag

        ByteArrayOutputStream memStream = new ByteArrayOutputStream(PacketSize);

        memStream.write(reverseShort((short)(PacketSize - PACKETLENGTH_FIELD_SIZE)));    // packet payload length
        memStream.write((byte)IressDataFeedPacketType.IDF_PKT_CLIENT_LOGIN);           // packet type

        BinaryWriteWithFilling(memStream, stringToByteArray(username), USERNAME_FIELD_SIZE);  // user name
        BinaryWriteWithFilling(memStream, stringToByteArray(password), PASSWORD_FIELD_SIZE); // password
        memStream.write(reverseShort((short)RequestFlag));      // request flag

        return memStream.toByteArray();
    }

    private static byte[] reverseShort(short n)
    {
    	byte[] ret = {(byte)(n & 0xff), (byte)((n >> 8) & 0xff)};
    	return ret;
    }
    
    private static byte[] reverseInt(int n)
    {
    	byte[] ret = {(byte)(n & 0xff), (byte)((n >> 8) & 0xff), (byte)((n >> 16) & 0xff), (byte)((n >> 24) & 0xff)};
    	return ret;
    }
    
    public static byte[] buildHeartBeatPacket()
    {
        int PacketSize = PACKETLENGTH_FIELD_SIZE + PACKETTYPE_SIZE;

        ByteArrayOutputStream baos = new ByteArrayOutputStream(PacketSize);
        byte[] packet_length_content=IdfHelper.reverseShort((short)1); 		// 
		baos.write(packet_length_content, 0, packet_length_content.length);	// packet payload length
        baos.write(IressDataFeedPacketType.IDF_PKT_HEARTBEAT);  			// packet type

        return baos.toByteArray();
    }

    // data request packet (exchange, datasource)
	public static byte[] buildSubscriptionRequestPacket(String exchange,
			String dataSource, String dataSourceBoard, int supplementPacketNum) throws UnsupportedEncodingException, IOException 
	{
        int PktSize = PACKETLENGTH_FIELD_SIZE + PACKETTYPE_SIZE + MSGTYPE_SIZE 
        + EXCHANGE_FIELD_SIZE + DATASOURCE_FIELD_SIZE + DATASOURCEBOARD_FIELD_SIZE + Sizeof.sizeof(new Integer(0)); // msg type, exchange, data SubscriptionSource, board, supplement packet num

        ByteArrayOutputStream bs = new ByteArrayOutputStream(PktSize);
	
        bs.write(reverseShort((short)(PktSize - PACKETLENGTH_FIELD_SIZE)));     // packet payload length
        bs.write(IressDataFeedPacketType.IDF_PKT_CLIENT_CONTROL); // packet type
	
        bs.write(stringToByteArray(IDF_MSG_SUBSCRIPTION_REQUEST));       // message type
	
	    // SubscriptionSource instrument
	    BinaryWriteWithFilling(bs, stringToByteArray(exchange), EXCHANGE_FIELD_SIZE);
	    BinaryWriteWithFilling(bs, stringToByteArray(dataSource), DATASOURCE_FIELD_SIZE);
	    BinaryWriteWithFilling(bs, stringToByteArray(dataSourceBoard), DATASOURCEBOARD_FIELD_SIZE);

	    // number of supplement packets
	    bs.write(reverseInt(supplementPacketNum));
	
	    return bs.toByteArray();
	}

	public static byte[] buildTradeRecoveryRequestPacket(TradeRecoveryRequestMessageContent recovery) throws UnsupportedEncodingException
    {
		int PktSize = PACKETLENGTH_FIELD_SIZE + PACKETTYPE_SIZE + MSGTYPE_SIZE
			+ Sizeof.sizeof(new Integer(0)) + Sizeof.sizeof(new Short((short)0)) + Sizeof.sizeof(new Integer(0));		// security id + session id + trade no.
		
		byte[] result = new byte[PktSize];
		ByteBuffer binWriter = ByteBuffer.wrap(result);

		binWriter.putShort((short)(PktSize - PACKETLENGTH_FIELD_SIZE));     // packet payload length
        binWriter.put((byte)IressDataFeedPacketType.IDF_PKT_CLIENT_CONTROL); // packet type

        binWriter.put(stringToByteArray(IDF_MSG_SUBSCRIPTION_TRADE_RECOVERY_REQUEST));       // message type

        // SubscriptionSource instrument
		binWriter.putInt(recovery.SecurityId);
		binWriter.putShort(recovery.SessionId);
		binWriter.putInt(recovery.TradeNo);
        
        return result;
    }

	public static String getMessageType(ByteBuffer bb) throws IOException
    {
		byte[] type = new byte[MSGTYPE_SIZE];
		bb.get(type);
        return new String(type, "ASCII");
    }

    public static SecurityInstrumentSet parseSecInstrument(ByteBuffer reader)
    {
        SecurityInstrumentSet SecInstrument = new SecurityInstrumentSet();

        SecInstrument.SecurityId = reader.getInt();
        SecInstrument.DataSourceId = reader.getShort();
        SecInstrument.DataSourceBoardId = reader.getShort();

        return SecInstrument;
    }

    public static FeedMessageHeader parseMsgHeader(ByteBuffer binReader)
    {
        FeedMessageHeader msgHeader = new FeedMessageHeader();
        msgHeader.SecInstrument = parseSecInstrument(binReader);
        
        long timestampNS = binReader.getLong();
        msgHeader.UpdateTime = convertTimestampToTime(timestampNS);
        msgHeader.UpdateTimeNS = timestampNS;

        msgHeader.SessionId = binReader.getShort();
        return msgHeader;
    }

	private static int getByteArray(ByteBuffer src, byte[] dst)
	{
		try
		{
			src.get(dst);
			return dst.length;
		}
		catch(BufferUnderflowException ex)
		{
			return -1;
		}
	}
    
	public static ServerInfo parseServerInformationPacket(ByteBuffer bb) throws IOException {
        ServerInfo srvInfo = new ServerInfo();
        byte[] data = new byte[20 + 1];

        if (getByteArray(bb, data)==21)  {
        	srvInfo.ServerName = new String(data, "ASCII");

        	int index = srvInfo.ServerName.indexOf('\0');
        	srvInfo.ServerName = srvInfo.ServerName.substring(0, index!=-1?index:0);
        }
        else
        	srvInfo.ServerName = "not read";
        
        srvInfo.ServerType = (byte)bb.get();

        data = new byte[8 + 1];
        if (getByteArray(bb, data)==9)
        	srvInfo.ServerVersion = new String(data, "ASCII");
        else
        	srvInfo.ServerVersion = "not read";

        return srvInfo;
	}
}