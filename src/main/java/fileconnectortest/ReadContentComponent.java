package fileconnectortest;

import org.mule.api.MuleContext;
import org.mule.api.MuleEventContext;
import org.mule.api.MuleMessage;
import org.mule.api.lifecycle.Callable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReadContentComponent implements Callable {
	
	private Logger logger = LogManager.getLogger(ReadContentComponent.class);
	
	@Override
	public Object onCall(MuleEventContext eventContext) throws Exception {
		MuleMessage muleMessage = eventContext.getMessage();
		MuleContext muleContext = eventContext.getMuleContext();
		String threadName = Thread.currentThread().getName();
		if(muleMessage !=null && muleMessage.getPayload() != null)
		{
				String message = muleMessage.getPayloadAsString();	
				if(message.equals("EOF"))
				{					
					muleContext.getRegistry().unregisterObject("processCompleteFlag");
					muleContext.getRegistry().registerObject("processCompleteFlag", "true");
					logger.info("Set Complete Flag True");		
				}
				else
				{
					logger.debug(threadName + " Line is:" + message);
				}
		}
		return eventContext;
	}
}
