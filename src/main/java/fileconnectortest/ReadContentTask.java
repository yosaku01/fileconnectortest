package fileconnectortest;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CountDownLatch;

import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ReadContentTask implements Runnable {
	//This blocking queue is used to contain each line of file.
	private ArrayBlockingQueue<String> contentQueue = null;	
	//The task name of read content task(It identifies each read task).
	private String taskName;	
	//The synchronous signal variable for each read task.
	private CountDownLatch signal = null;	
	
	private Logger logger = LogManager.getLogger(ReadContentTask.class);
	
	public ReadContentTask(ArrayBlockingQueue<String> contentQueue, String taskName, CountDownLatch signal)
	{
		this.contentQueue = contentQueue;
		this.taskName = taskName;
		this.signal = signal;
	}	
	
	
	@Override
	public void run() 
	{
		synchronized (this)
		{
			while(true)
			{				
				if(!contentQueue.isEmpty())
				{
					try 
					{
						String line = contentQueue.take();
						if(line!=null && !line.equals("") && !line.equals("EOF"))
						{
							//process each line.
							System.out.println(taskName + " Line is:" + line);
						}
						else
						{
							//Notify other read task.
							contentQueue.put("EOF");
							break;
						}
					} catch (InterruptedException e) {
							logger.error(ExceptionUtils.getFullStackTrace(e));
					}
				}
			}
			signal.countDown();
		}
	}
}
