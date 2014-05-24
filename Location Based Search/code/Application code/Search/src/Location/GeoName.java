package Location;


import org.geonames.*;
/**
 *  
 * @author pratik
 */
public class GeoName {

	public static String getData(String location) throws Exception {
		String latlng=new String();
	try{
		
		WebService.setUserName("pratik"); 
		 
		  ToponymSearchCriteria searchCriteria = new ToponymSearchCriteria();
		  searchCriteria.setQ(location);
		  ToponymSearchResult searchResult = WebService.search(searchCriteria);
		 
		  if(searchResult.getToponyms().size()!=0)
		  {         
		      latlng=searchResult.getToponyms().get(0).getLatitude()+","+searchResult.getToponyms().get(0).getLongitude();
		  }
		  return latlng;
	}
	catch(Exception e)
	{
		e.printstacktrace();
	}
	return latlng;
	}
	  
}