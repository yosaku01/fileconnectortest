package fileconnectortest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mule.DefaultMuleMessage;
import org.mule.api.MuleContext;
import org.mule.api.MuleException;
import org.mule.api.client.MuleClient;

/**
 * This task is used to parse file input stream and put each line to blocking queue.
 * @author ShihuaWan
 *
 */
public class PutContentTask implements Runnable {
	//Current mule context.
	private MuleContext muleContext;
	
	//The file input stream.
	private InputStream inputStream;
	private final static Logger logger = LogManager.getLogger(PutContentTask.class);
	
	public PutContentTask(MuleContext muleContext, InputStream inputStream)
	{
		this.muleContext = muleContext;
		this.inputStream = inputStream;
	}
	
	@Override
	public void run() 
	{
			try 
			{					
				MuleClient muleClient = muleContext.getClient();
				Scanner scanner = new Scanner(inputStream);
				DefaultMuleMessage newMessage = null;
				while(scanner.hasNext())
				{
					String line = scanner.nextLine();
					//Wrapper each line content into mule message and send to vm queue.
					newMessage = new DefaultMuleMessage(line, muleContext);
					muleClient.send("vm://data", newMessage);				
				}
				//Wrapper EOF content into mule message to notify the reader component instance
				//the file has been parsed completely.
				DefaultMuleMessage endMessage = new DefaultMuleMessage("EOF", muleContext);
				muleClient.send("vm://data", endMessage);
				scanner.close();
			} catch (MuleException e) {
				logger.error(ExceptionUtils.getFullStackTrace(e));
			}
			finally
			{
				//Close the file input stream so that mule can auto delete the file.
				if(inputStream != null)
				{
					try {
						inputStream.close();
					} catch (IOException e) {
						logger.error(ExceptionUtils.getFullStackTrace(e));
					}
				}						
			}
		}
}