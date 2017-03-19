import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class AppendToFile
{
	public String filePath;
	BufferedWriter bw;
	PrintWriter out;
	FileWriter fw;
	boolean fileWriterOpen = false;

	public AppendToFile(String file_path)
	{
		filePath = file_path;
		File file = new File(filePath);
		try 
		{
			file.createNewFile();
			System.out.println("File created for " + file_path);
	  	} 
		catch (IOException e) 
		{
			System.out.println("File already existed for " + file_path);
		}
	}

	public void openWriterAppend()
	{
		try
		{
			fw = new FileWriter(filePath, true);
			fileWriterOpen = true;
		}
		catch (IOException e)
		{
			System.out.println("Failed to open writer to " + filePath);
		}
	}
	
	public void closeWriterAppend()
	{
		try
		{
			fileWriterOpen = false;
			fw.close();
		}
		catch (IOException e)
		{
			System.out.println("Failed to close file writer for " + filePath);
		}
	}
	
	public void appendToFile(StringBuilder a)
	{
		try
		{
			if(fileWriterOpen==false)
			{
				fw = new FileWriter(filePath, true);
			}
			fw.append(a);
			if(fileWriterOpen==false)
			{
				fw.close();
			}
		} 
		catch (IOException e) 
		{
		   System.out.println("Failed to append to file " + filePath);
		}
	}
	
	
	public void cleanFile()
	{	
		try
		{
			FileWriter writer = new FileWriter(filePath, false);
			writer.write("");
			writer.close();
		} 
		catch (IOException e) 
		{
			System.out.println("Failed to clean file " + filePath);
		}
	}
}
