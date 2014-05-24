package Location;


import org.geonames.*;
public class GeoName {

	public static String getData(String location) throws Exception {
		String latlng=new String();
	try{
		WebService.setUserName("vivek"); // add your username here
		 
		  ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
		  searchCriteria.setQ(location);
		  ToponymSearchResult searchResult = WebService.search(searchCriteria);
		 
		  if(searchResult.getToponyms().size()!=0)
		  {         
		      latlng=searchResult.getToponyms().get(0).getLatitude()+","+searchResult.getToponyms().get(0).getLongitude();
			  //String country=searchResult.getToponyms().get(0).getCountryName();
			  //WriterXML.processXML(country, location, latlng);
		    
		  }
		  return latlng;
	}
	catch(Exception e)
	{

	}
	return latlng;
	}
	  
}