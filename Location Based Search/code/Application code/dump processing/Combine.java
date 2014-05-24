import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 *  
 * @author pratik
 */

public class Combine {

public static void main(String args[]) throws IOException
{
   final File folder = new File("C:/Users/Pratik/workspace/IRProject-2/data/locationsXML");
   String s="<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
   FileWriter fw1 = new FileWriter("C:/Users/Pratik/workspace/IRProject-2/data/test.xml",true);
   BufferedWriter bw1 = new BufferedWriter(fw1);
   bw1.write(s);
   bw1.newLine();
   bw1.write("<docs>");
   bw1.close();
	
   listFilesForFolder(folder);
   
   FileWriter fw2 = new FileWriter("C:/Users/Pratik/workspace/IRProject-2/data/test.xml",true);
   BufferedWriter bw2 = new BufferedWriter(fw2);
   bw2.write("</docs>");
   bw2.close();

}	


public static void listFilesForFolder(final File folder) throws IOException {
    for (final File fileEntry : folder.listFiles()) {
        if (fileEntry.isDirectory()) {
            listFilesForFolder(fileEntry);
        } else {
            readFile(fileEntry.getName());
        }
    }
}

public static void readFile(String f) throws IOException
{
		
	 BufferedReader br = new BufferedReader(new FileReader("C:/Users/Pratik/workspace/IRProject-2/data/locationsXML/"+f));
		FileWriter fw = new FileWriter("C:/Users/Pratik/workspace/IRProject-2/data/test.xml",true);
    	   BufferedWriter bw = new BufferedWriter(fw);
          
	    try {
	            String line = br.readLine();
		       bw.newLine();
	            while (line != null) {
		        if(line.contains("?xml"))
		        {	
		        }else{	
		        	bw.write(line);
					bw.newLine();
		        }	
	        	    line=br.readLine();
	        	   
	        }
	       
	    } finally {
	        br.close();
	        bw.close();
	    }

}
	
}
