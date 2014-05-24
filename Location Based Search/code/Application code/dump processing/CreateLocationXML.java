import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;


public class CreateLocationXML {
	public static void main(String[] args) throws Exception {
		 BufferedReader br = new BufferedReader(new FileReader("C:/Users/Pratik/workspace/NLPTest/locations/processedloc.txt"));

		    try {
		       
		        String line = br.readLine();
			        while (line != null) {
		        	         line=line.trim();
		        	         line=line.replaceAll(" +"," ");
		        	         line=line.replaceAll(" ","_");
		        	         GeoNa.getData(line);
		        	         line=br.readLine();
		        	   
		        }
		       
		    } finally {
		        br.close();
		    }

	}

}