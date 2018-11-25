
import java.io.BufferedReader;
import java.io.FileReader;

class ReadFile
{
	private final String path;
	
	public ReadFile(String file_path)
	{
		path = file_path;
	}
	
	public String[] OpenFile()
	{
		try
		{
			FileReader fr;
			fr = new FileReader(path);
			BufferedReader textReader = new BufferedReader(fr);
			
			int numberOfLines = readLines( );
			String[] textData = new String[numberOfLines];
			
			int i;
			for(i = 0; i < numberOfLines; i++)
			{
				textData[i] = textReader.readLine( );
			}
			textReader.close( );
			return textData;
		}
		catch(Exception e)
		{
			e.printStackTrace( );
			System.out.println("Failed retrieving file, Error in class ReadFile, in method OpenFile for file " + path);
			System.exit(1);
			return null;
		}
	}
	
	private int readLines()
	{
		try
		{
			FileReader file_to_read = new FileReader(path);
			BufferedReader bf = new BufferedReader(file_to_read);
			int numberOfLines = 0;
			while((bf.readLine( )) != null)
			{
				numberOfLines++;
			}
			bf.close( );
			return numberOfLines;
		}
		catch(Exception e)
		{
			e.printStackTrace( );
			System.out.println("Failed retrieving file, Error in class ReadFile, in method readLines for file " + path);
			System.exit(1);
			return 0;
		}
	}
}
