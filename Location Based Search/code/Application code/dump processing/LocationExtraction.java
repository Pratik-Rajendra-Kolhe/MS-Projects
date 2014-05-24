import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
 
import java.util.ArrayList;
import java.util.Iterator;


/**
 *  
 * @author pratik
 */

public class LocationExtraction {

	public static ArrayList<String[]> getLocation(String line) throws InvalidFormatException, IOException  {
		{
           
			ArrayList<String> place=new ArrayList<String>();
			ArrayList<String[]>ltlng=new ArrayList<String[]>();
				try {
			      
				   InputStream modelIn = new FileInputStream("C:\\Users\\Pratik\\workspace\\NLPTest\\en-token.bin");
				   TokenizerModel tokenModel = new TokenizerModel(modelIn);
				   Tokenizer tokenizer = new TokenizerME(tokenModel);
				   NameFinderME nameFinder =
				   new NameFinderME(new TokenNameFinderModel(new FileInputStream("C:\\Users\\Pratik\\workspace\\NLPTest\\en-ner-location.bin")));
				   String tokens[] = tokenizer.tokenize(line);
				   Span nameSpans[] = nameFinder.find(tokens);
				   if(nameSpans.length==0)
					   return ltlng;
				    String temp=new String();
				   for( int i = 0; i<nameSpans.length; i++) {
				      for(int j=nameSpans[i].getStart();j<nameSpans[i].getEnd();j++)
				      temp=temp.concat(" "+tokens[j]);
				      temp=temp.trim();
					
				   }
				   modelIn.close();
				}
				catch(Exception e) {
				   System.out.println(e.toString());
				}
				
          return ltlng;			
		}
	} 

	public static String[] getLatLong (String place) throws IOException
	{

	try{	
		String cordinates[]=new String[2];
		String url = "http://maps.googleapis.com/maps/api/geocode/xml?address="+place+"&sensor=true";
		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();
 		con.setRequestMethod("GET");
 		con.setRequestProperty("User-Agent", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\crome.exe");
 		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		
			while ((inputLine = in.readLine()) != null) {
			if(inputLine.contains("<lat>") || inputLine.contains("<lng>")) 
				if(inputLine.contains("<lat>"))
				{
                    
					cordinates[0]=inputLine.replaceAll("</*lat>","").trim();
				}      
				else
				{
					cordinates[1]=inputLine.replaceAll("</*lng>","").trim();
					break;
				}
		}
		in.close();

		return cordinates;
	}
	catch(Exception e)
	{
		e.printStackTrace();
		return null;
	}
	}
}
