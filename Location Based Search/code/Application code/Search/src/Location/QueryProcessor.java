package Location;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.tokenize.Tokenizer;
import opennlp.tools.tokenize.TokenizerME;
import opennlp.tools.tokenize.TokenizerModel;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.Span;

public class QueryProcessor extends News{
	public static Map<String,News> results=null; // Map to store strings
	public static int rank=1;
	public static int d=500;
	public static void main(String args[])
	{
		try {
			getResults("tea in Assam");
		} catch (InvalidFormatException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	public static Map<Integer,News> getResults(String q) throws InvalidFormatException, IOException
	{
		
		results=new HashMap<String,News>();
		rank=0;
		List<Place>locations=new ArrayList<Place>(); // list for storing nearby locations
        Map<Integer,News> rankedResults=new TreeMap<Integer,News>();
       
	    for(int i=0;i<q.split(" ").length;i++)  // getting distance
	    {
	    	if(isInteger(q.split(" ")[i]))
	    		d=Integer.parseInt(q.split(" ")[i]);
	    }
     
	    
	    String querylocation=getLocationName(q); // getting location names from the query
	    System.out.println(querylocation.length());
		if(querylocation!=null && querylocation.length()!=0)
		{
		q=q.replaceAll(querylocation," ");
		    
	    String latlng=getLatLng(querylocation.replaceAll(" ","+"));// get lat-lng of location
		if(latlng==null || latlng.length()==0)
			try {
				latlng=GeoName.getData(querylocation.replaceAll(" ","+"));
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	    if(latlng!=null && latlng.length()!=0)
				{	
			      locations=getLocations(latlng);   // get near by locations
			      getNews(locations,q);   //getting news
				}
	    else getNews(q.replaceAll(" *","+"));
	    
		}else getNews(q.replaceAll(" *","+"));
	    
	    Set<String> s=results.keySet();
	   
	    Iterator<String> i=s.iterator();
	    
	    while(i.hasNext())
	    {
	    	News t=results.get(i.next());
	    	rankedResults.put(t.rank,t);
	 	 }
	    
	    Set<Integer> r=rankedResults.keySet();
	    Iterator ri=r.iterator();
	    
	    while(ri.hasNext())
	    {
	    	News t=rankedResults.get(ri.next());
            System.out.println(t.rank);
            System.out.println(t.time);
            System.out.println(t.type);
            System.out.println(t.latlng);
            System.out.println(t.title);
            System.out.println(t.summary);
            System.out.println("-----------------------------------------------------");
	 	 }
	    return rankedResults;
	    
	}
	public static void getNews(List<Place> locations,String query)
	{
	
		getRelatedNews(locations,query);
		getLocationNews(locations,query);
	}	
	
	
	public static void getRelatedNews(List<Place>locations,String query)
	{	
		Iterator<Place> i=locations.iterator();
		String title=new String();
		String time=new String();
		String summary=new String();
		String type="R";
		String latlng=new String();
		try
				{
		    	while(i.hasNext())
		    	{	
		        	 
		         
		    		Place loc=i.next();
		    		if(loc.name.length()<1)
		    			loc=i.next();
					//	String url = "http://localhost:8080/solr/wikinews/select?q="+loc.name.concat(" "+query).trim().replaceAll(" ","+")+"&df=text&wt=xml&indent=true";
			            String url="http://localhost:8080/solr/wikinews/select?q="+loc.name.concat(" AND "+query).trim().replaceAll(" ","+")+"&defType=edismax&df=categories&fl=title,summary,timestamp&qf=categories^5.0+title^10.0+text^0.3&bq=(categories:"+loc.name.replaceAll(" ","%20")+")^3&bq=(title:"+query.replaceAll(" ","%20")+")^2&sort=score%20desc,%20timestamp%20desc&wt=xml&indent=tr";
			            URL obj = new URL(url);
						HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				 		con.setRequestMethod("GET");
				 		con.setRequestProperty("User-Agent", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\crome.exe");
				 		BufferedReader in = new BufferedReader(
					    new InputStreamReader(con.getInputStream()));
						String inputLine;
						int count=0;
							while ((inputLine = in.readLine()) != null) {
				            if(count==3)
				            	break;
				         	
								if(inputLine.contains("\"timestamp\""))
						          {
							    	String temp=inputLine.replaceAll(" *","").replaceAll("<.*\">","").replaceAll("<.*>",""); 
							    	time=dateFormat(temp);
                           
						          }else if(inputLine.contains("\"summary\""))
						          {
						        	  String temp=inputLine.replaceAll("<.*\">","").replaceAll("<.*>","").trim();
						        	  summary=temp;

						          }
								else if(inputLine.contains("\"title\""))
						          {
						        	 
								     String temp=inputLine.replaceAll("<.*\">","").replaceAll("<.*>","").trim();
						        	  title=temp;
		                              latlng=loc.latlng;
		                              count ++; 
		                              if(title.trim()!="")
		                              results.put(title,new News(title,time,type,latlng,summary,rank++));
						          }
						        	  
			
						 	}
		    			in.close();
		    	  }			
					}
		     catch(Exception e)
				{
					e.printStackTrace();
				
				}

	}

	public static void getLocationNews(List<Place>locations,String query)
	{	
		Iterator<Place> i=locations.iterator();
		String title=new String();
		String time=new String();
		String summary=new String();
		String type="L";
		String latlng=new String();
		    try
				{
		    	while(i.hasNext())
		    	{
		    		    Place loc=i.next();
		    		    if(loc.name.length()<1)
			    			loc=i.next();
						String url = "http://localhost:8080/solr/wikinews/select?q="+loc.name.trim().replaceAll(" ","+")+"&defType=edismax&df=categories&fl=title,summary,timestamp&qf=categories^5.0+title^10.0+text^0.3&wt=xml&indent=true";
						URL obj = new URL(url);
						HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				 		con.setRequestMethod("GET");
				 		con.setRequestProperty("User-Agent", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\crome.exe");
				 		BufferedReader in = new BufferedReader(
					    new InputStreamReader(con.getInputStream()));
						String inputLine;
					    int count=0;	
							while ((inputLine = in.readLine()) != null) {
							    if(count==3)
							    	break;
							    
								if(inputLine.contains("\"timestamp\""))
						          {
									String temp=inputLine.replaceAll(" *","").replaceAll("<.*\">","").replaceAll("<.*>","");
							        time=dateFormat(temp);
						          }else if(inputLine.contains("\"summary\""))
						          {
						        	  String temp=inputLine.replaceAll("<.*\">","").replaceAll("<.*>","").trim();
						        	  summary=temp;
						        	}
								else if(inputLine.contains("\"title\""))
						          {
						        	   
						        	  String temp=inputLine.replaceAll("<.*\">","").replaceAll("<.*>","").trim();
						        	  title=temp;
		                              latlng=loc.latlng;
		                              count++;
		                              if(title.trim()!="") 
		                             results.put(title,new News(title,time,type,latlng,summary,rank++));
						          }
						        	  
			
						 	}
						in.close();
		    	}
					}
		     catch(Exception e)
				{
					e.printStackTrace();
				
				}

	}
		
	
	
	public static List<Place> getLocations(String latlng)
	{
	    try
			{
	                List<Place> locations=new ArrayList<Place>();
					
					
	                String url = "http://localhost:8080/solr/locations/select?wt=xml&indent=true&fl=location,country,latlng&q=*:*&fq={!geofilt}&sfield=latlng&pt="+latlng+"&d="+d+"&sort=geodist()%20asc";
					URL obj = new URL(url);
					HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			 		con.setRequestMethod("GET");
			 		con.setRequestProperty("User-Agent", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\crome.exe");
			 		BufferedReader in = new BufferedReader(
				    new InputStreamReader(con.getInputStream()));
					String inputLine;
					String l=new String();
					String c=new String();
					
						while ((inputLine = in.readLine()) != null) {
						    
							if(inputLine.contains("location"))
					          {
						    	
						    	String temp=inputLine.replaceAll("<.*\">","").replaceAll("<.*>","").trim();
					             if(!temp.contains("location"))
					             l=temp;
					            
					          }
						    else if(inputLine.contains("latlng"))
							   {
					             String temp=inputLine.replaceAll("<.*\">","").replaceAll("<.*>","").trim();
					             if(!temp.contains("latlng"))
					             c=temp;
					          
							    locations.add(new Place(l,c));	
							    
							   }
						    	
		
					 	}
					in.close();
					
					return locations;
				}
	     catch(Exception e)
			{
				e.printStackTrace();
				return null;
			}
		
	}
	public static String getLatLng(String location)
	{
    try
		{
                String latlng=new String();
				String url = "http://localhost:8080/solr/locations/select?q="+location+"&df=location&wt=xml&indent=true";
				URL obj = new URL(url);
				HttpURLConnection con = (HttpURLConnection) obj.openConnection();
		 		con.setRequestMethod("GET");
		 		con.setRequestProperty("User-Agent", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\crome.exe");
		 		BufferedReader in = new BufferedReader(
			    new InputStreamReader(con.getInputStream()));
				String inputLine;
				
					while ((inputLine = in.readLine()) != null) {
				    if(inputLine.contains("latlng"))
				          {
				             latlng=inputLine.replaceAll(" *","").replaceAll("<.*\">","").replaceAll("<.*>","");
				             return(latlng);
				          }
					}
				in.close();
				return latlng;
			}
     catch(Exception e)
		{
			e.printStackTrace();
			return null;
		}
	}
	public static String getLocationName(String q)
	{
		String location=new String();
		q=q.replaceAll(" +"," ");
		String []temp=q.split(" ");
		
		for(int i=0;i<temp.length;i++)
		{
			if(Character.isUpperCase(temp[i].charAt(0)))
			{
				location=location.concat(" "+temp[i]);
			}
		}
		
		return (location.trim());
	}
	
	public static String dateFormat(String time)
	{
		SimpleDateFormat d1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
		SimpleDateFormat d2 = new SimpleDateFormat("MMM dd, yyyy");
		try {
			time=d2.format(d1.parse(time));
					} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return time;
	}
	public static boolean isInteger(String s) {
	    try { 
	        Integer.parseInt(s); 
	    } catch(NumberFormatException e) { 
	        return false; 
	    }
	    // only got here if we didn't return false
	    return true;
	}
	
	public static void getNews(String query)
	{	
	
		String title=new String();
		String time=new String();
		String summary=new String();
		String type="R";
		String latlng=new String();
		try
				{
		        	 	//	String url = "http://localhost:8080/solr/wikinews/select?q="+loc.name.concat(" "+query).trim().replaceAll(" ","+")+"&df=text&wt=xml&indent=true";
			            String url="http://localhost:8080/solr/wikinews/select?q="+query+"&defType=edismax&df=categories&fl=title,summary,timestamp&qf=categories^10+title^5.0+text^0.3&sort=score%20desc,%20timestamp%20desc&wt=xml&indent=tr";
			            URL obj = new URL(url);
						HttpURLConnection con = (HttpURLConnection) obj.openConnection();
				 		con.setRequestMethod("GET");
				 		con.setRequestProperty("User-Agent", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\crome.exe");
				 		BufferedReader in = new BufferedReader(
					    new InputStreamReader(con.getInputStream()));
						String inputLine;
						int count=0;
							while ((inputLine = in.readLine()) != null) {
				            if(count==10)
				            	break;
				         	
								if(inputLine.contains("\"timestamp\""))
						          {
							    	String temp=inputLine.replaceAll(" *","").replaceAll("<.*\">","").replaceAll("<.*>",""); 
							    	time=dateFormat(temp);
                           
						          }else if(inputLine.contains("\"summary\""))
						          {
						        	  String temp=inputLine.replaceAll("<.*\">","").replaceAll("<.*>","").trim();
						        	  summary=temp;

						          }
								else if(inputLine.contains("\"title\""))
						          {
						        	 
								     String temp=inputLine.replaceAll("<.*\">","").replaceAll("<.*>","").trim();
						        	  title=temp;
		                              latlng="";
		                              count ++; 
		                              if(title.trim()!="")
		                              results.put(title,new News(title,time,type,latlng,summary,rank++));
						          }
						        	  
			
						 	}
		    			in.close();
				
					}
		     catch(Exception e)
				{
					e.printStackTrace();
				
				}

	}

}


