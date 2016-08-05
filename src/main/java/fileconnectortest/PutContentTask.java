package fileconnectortest;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;
import java.util.concurrent.ArrayBlockingQueue;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This task is used to parse file input stream and put each line to blocking queue.
 * @author ShihuaWan
 *
 */
public class PutContentTask implements Runnable {
	//This blocking queue is used to contain each line of file.
	private ArrayBlockingQueue<String> contentQueue = null;
	//The file input stream.
	private InputStream inputStream;
	private final static Logger logger = LogManager.getLogger(PutContentTask.class);
	
	public PutContentTask(ArrayBlockingQueue<String> contentQueue, InputStream inputStream)
	{
		this.contentQueue = contentQueue;
		this.inputStream = inputStream;
	}
	
	@Override
	public void run() 
	{
			try 
			{						
				Scanner scanner = new Scanner(inputStream);
				while(scanner.hasNext())
				{
					String line = scanner.nextLine();
					contentQueue.put(line);				
				}
				//Put EOF string to line to notify read task that we reach the end of file.
				contentQueue.put("EOF");
				scanner.close();
			} catch (InterruptedException e) {
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