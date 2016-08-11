package fileconnectortest;

import java.io.InputStream;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import org.mule.api.MuleMessage;
import org.mule.api.registry.RegistrationException;
import org.mule.api.transformer.TransformerException;
import org.mule.transformer.AbstractMessageTransformer;
import org.mule.util.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileTransformer extends AbstractMessageTransformer {


	//The thread pool executor to provide threads to process file content.
	@Autowired
	private ThreadPoolTaskExecutor queueExecutor;
	
	private Logger logger = LogManager.getLogger(FileTransformer.class);
	
	
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
		
		logger.info("Process File");		
		try 
		{
			muleContext.getRegistry().unregisterObject("processCompleteFlag");
			muleContext.getRegistry().registerObject("processCompleteFlag", "false");
		} 
		catch (RegistrationException e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
		}		
		
		
		//The file input stream sent by file connector.
		InputStream inputStream = 
				(InputStream)message.getPayload();
		
		PutContentTask putContentTask =
    			new PutContentTask(muleContext, inputStream);
    	queueExecutor.execute(putContentTask);      	
		
    	return null;
	}

}
