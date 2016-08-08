package fileconnectortest;

import java.io.File;
import java.util.Comparator;

public class FileNameSeparator implements Comparator<File> 
{	
	public int compare(File file1, File file2) 
	{		
        String fileName1 = file1.getName();        
        String fileName2 = file2.getName();       
        return fileName1.compareToIgnoreCase(fileName2);
	}
}