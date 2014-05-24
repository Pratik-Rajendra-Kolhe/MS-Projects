
import org.geonames.*;
/**
 *  
 * @author pratik
 */

public class GeoName {

	public static void getData(String location) throws Exception {
	try{
		
		  WebService.setUserName("pratik");		 
		  ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
		  searchCriteria.setQ(location);
		  ToponymSearchResult searchResult = WebService.search(searchCriteria);
		  if(searchResult.getToponyms().size()!=0)
		  {         
		      String latlng=searchResult.getToponyms().get(0).getLatitude()+","+searchResult.getToponyms().get(0).getLongitude();
			  String country=searchResult.getToponyms().get(0).getCountryName();
			  WriterXML.processXML(country, location, latlng);
		  }
	}
	catch(Exception e)
	{
		
	}
	}

}