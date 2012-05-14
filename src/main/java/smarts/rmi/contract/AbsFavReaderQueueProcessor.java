package smarts.rmi.contract;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;

public abstract class AbsFavReaderQueueProcessor extends UnicastRemoteObject 
												 implements IEventQueueProcessor  {
	private static final long serialVersionUID = 1L;
	
	// Hash table containing callers. Initial capacity is set to 7 - for the number of callers at the moment
	transient HashMap<String, IEventMethodCaller> callers = new HashMap<String, IEventMethodCaller>(7);

	public AbsFavReaderQueueProcessor() throws RemoteException
	{
		callers.put(DayStartArgs.class.toString(), 
				new IEventMethodCaller() 
				{
					@Override
					public void process(Object args) {
						onDayStart((DayStartArgs)args);
					}
				});
		
		callers.put(DayEndArgs.class.toString(), 
				new IEventMethodCaller() 
				{
					@Override
					public void process(Object args) {
						onDayEnd((DayEndArgs)args);
					}
				});
		
		callers.put(StartArgs.class.toString(), 
				new IEventMethodCaller() 
				{
					@Override
					public void process(Object args) {
						onStart((StartArgs)args);
					}
				});
		
		callers.put(EndArgs.class.toString(), 
				new IEventMethodCaller() 
				{
					@Override
					public void process(Object args) {
						onEnd((EndArgs)args);
					}
				});
		
		callers.put(QuoteArgs.class.toString(), 
				new IEventMethodCaller() 
				{
					@Override
					public void process(Object args) {
						onQuote((QuoteArgs)args);
					}
				});
		
		callers.put(TradeArgs.class.toString(), 
				new IEventMethodCaller() 
				{
					@Override
					public void process(Object args) {
						onTrade((TradeArgs)args);
					}
				});
		
		callers.put(ControlArgs.class.toString(), 
				new IEventMethodCaller() 
				{
					@Override
					public void process(Object args) {
						onControl((ControlArgs)args);
					}
				});
	}
	
	public abstract void onDayStart(DayStartArgs args);

	public abstract void onDayEnd(DayEndArgs args);

	public abstract void onStart(StartArgs args);

	public abstract void onEnd(EndArgs args);
	
    public abstract void onQuote(QuoteArgs args);
	
	public abstract void onTrade(TradeArgs args);
	
    public abstract void onControl(ControlArgs args);

    // this interface is to call methods for certain type of events
    private abstract interface IEventMethodCaller
    {
    	void process(Object args);
    }
    
    // == IEventQueueProcessor
	@Override
	public void process(Object[] queue) throws RemoteException
	{
		for (int i=0; i<queue.length; i++)
			callers.get(queue[i].getClass().toString()).process(queue[i]);
		
//		Iterator<Object> queueIt = queue.iterator();
//		
//		while(queueIt.hasNext())
//		{
//			Object event = queueIt.next();
//			callers.get(event.getClass().toString()).process(event);
//		}
	}
}