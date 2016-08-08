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

	//The blocking queue used to process file content.
	@Resource
	private ArrayBlockingQueue<String> contentQueue;	
	
	//The thread pool executor to provide threads to process file content.
	@Autowired
	private ThreadPoolTaskExecutor queueExecutor;
	
	private Logger logger = LogManager.getLogger(FileTransformer.class);
	
	
	@Override
	public Object transformMessage(MuleMessage message, String outputEncoding) throws TransformerException {
			
		
		long startTime = System.currentTimeMillis();
		
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
    			new PutContentTask(contentQueue, inputStream);
    	queueExecutor.execute(putContentTask);  
    	
    	CountDownLatch threadSignal = new CountDownLatch(5);
    	
    	for(int i=0; i<5;i++)
    	{
    		String taskName = "ReadContentTask " + i;
    		ReadContentTask readContentTask =
    				new ReadContentTask(contentQueue, taskName, threadSignal);
    		queueExecutor.execute(readContentTask);
    	}
    	
    	try 
    	{
			threadSignal.await();			
			contentQueue.clear();
		} 
    	catch (Exception e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
		} 
    	
    	long endTime = System.currentTimeMillis();
    	long elaspeTime = endTime - startTime; 
    	
    	try {
    		muleContext.getRegistry().unregisterObject("processCompleteFlag");
			muleContext.getRegistry().registerObject("processCompleteFlag", "true");
			logger.info("Set Complete Flag To True");
		} catch (RegistrationException e) {
			logger.error(ExceptionUtils.getFullStackTrace(e));
		}
    	
    	
		
    	return "The elapse time is:" + elaspeTime;
	}

}
