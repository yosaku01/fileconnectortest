package fileconnectortest;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.mule.api.MuleContext;
import org.mule.api.construct.FlowConstruct;
import org.mule.api.endpoint.InboundEndpoint;
import org.mule.api.lifecycle.CreateException;
import org.mule.api.registry.RegistrationException;
import org.mule.api.transport.Connector;
import org.mule.transport.file.FileMessageReceiver;
import org.mule.api.routing.filter.Filter;
import org.mule.transport.file.FileConnector;
import org.apache.commons.lang.exception.ExceptionUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;


/**
 * This class defined the file roll rule of file connector.
 * The file connector rolls one file once.
 *
 */
public class CustomFileMessageReceiver extends FileMessageReceiver {

	private Logger logger = LogManager.getLogger(CustomFileMessageReceiver.class);
	
	public CustomFileMessageReceiver(Connector connector, FlowConstruct flowConstruct, InboundEndpoint endpoint,
			String readDir, String moveDir, String moveToPattern, long frequency) throws CreateException {
		super(connector, flowConstruct, endpoint, readDir, moveDir, moveToPattern, frequency);
	}
	
	
	/**
	 * This method polling accesses the monitored file directory and return 
	 * the file list having specified number.
	 * @param the monitored file directory.
	 * @param the returned file list which has special number.
	 */
	@Override
	protected void basicListFiles(File currentDirectory, List<File> discoveredFiles)
    {	
		MuleContext muleContext = this.getFlowConstruct().getMuleContext();
		String processFlag = 
				muleContext.getRegistry().get("processCompleteFlag");
		
		if(processFlag.equals("false"))
		{
			logger.info("Not Processed");
			return;
		}
		
		logger.info("Process Start");
		
		File[] files;
        Filter filter = endpoint.getFilter(); 
      //Filter the file or directory according to setting filter conditions.
        if ( filter instanceof FileFilter)
        {
            files = currentDirectory.listFiles((FileFilter)filter);
        }
        else if(filter instanceof FilenameFilter)
        {
            files = currentDirectory.listFiles((FilenameFilter)filter);
        }
        else
        {        	
        	files = currentDirectory.listFiles();
        }

        // the listFiles calls above may actually return null (check the JDK code).
        if (files == null || files.length == 0)
        {    
        	try 
        	{
        		muleContext.getRegistry().unregisterObject("processCompleteFlag");
				muleContext.getRegistry().registerObject("processCompleteFlag", "true");
			} 
        	catch (RegistrationException e) 
        	{
        		logger.error(ExceptionUtils.getFullStackTrace(e));
			}
        	
        	logger.info("No Available Files");
        	logger.info("Process End");
            return;
        }       
        
        List<File> fileList =new ArrayList<File>();        
        scanFiles(currentDirectory, fileList);
        //Sort the scanned file list according to specified comparator.
        if(fileList.size() > 0)
        {
        	 //Compare all the scanned files.
    	    if(fileList.size() >1)
    	    {
    	       		Comparator<File> comparator = null;
    				try 
    				{
    					comparator = getComparator();
    					if (comparator != null)
    		            {
    		                 Collections.sort(fileList, comparator);
    		            }
    				} 
    				catch (Exception e) 
    				{
    					logger.error(ExceptionUtils.getFullStackTrace(e));
    				} 
    	       }
           
           discoveredFiles.clear();       
           //Add the required number's files.
           for(int i=0; i<1; i++)
           {
        	   discoveredFiles.add(fileList.get(i));
           }
        }
        else
        {
        	try 
        	{
        		muleContext.getRegistry().unregisterObject("processCompleteFlag");
				muleContext.getRegistry().registerObject("processCompleteFlag", "true");
			} 
        	catch (RegistrationException e) 
        	{
        		logger.error(ExceptionUtils.getFullStackTrace(e));
			}
        	logger.info("No Available Files");
        	logger.info("Process End");
        }
    }     
	
	/**
	 * This method accesses the directory recursively, gets all the files under this directory. 
	 * @param currentDirectory
	 * The special file directory.
	 * @param fileList
	 * The scanned file list.
	 */
	private void scanFiles(File currentDirectory, List<File> fileList)
	{
		File[] files;
		Filter filter = endpoint.getFilter();
		//Filter the file or directory according to setting filter conditions.
		if (filter instanceof FileFilter)
		{
			files = currentDirectory.listFiles((FileFilter)filter);
		}
		else if(filter instanceof FilenameFilter)
		{
			files = currentDirectory.listFiles((FilenameFilter)filter);
		}
		else
		{        	
			files = currentDirectory.listFiles();
		}
		
		if (files == null || files.length == 0)
		{
			return;
		}
	   
		for(File file:files)
		{
			if (!file.isDirectory())
			{
				fileList.add(file);                
			}
			else
			{
				if (((FileConnector)connector).isRecursive())
				{
					this.scanFiles(file, fileList);                   
				}
			}
		}  
    }
}
