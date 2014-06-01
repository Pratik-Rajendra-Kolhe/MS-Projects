/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

import java.util.Properties;

/**
 * @author Pratik
 * This class represents a subclass of a Dictionary class that is
 * local to a single thread. All methods in this class are
 * assumed thread safe for the same reason.
 */
public class LocalDictionary extends Dictionary {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Public default constructor
	 * @param props: The properties file
	 * @param field: The field being indexed by this dictionary
	 */
	private INDEXFIELD fld;
	public LocalDictionary(Properties props, INDEXFIELD field) {
		super(props, field);

		this.fld=field;
	}
	
	/**
	 * Method to lookup and possibly add a mapping for the given value
	 * in the dictionary. The class should first try and find the given
	 * value within its dictionary. If found, it should return its
	 * id (Or hash value). If not found, it should create an entry and
	 * return the newly created id.
	 * @param value: The value to be looked up
	 * @return The id as explained above.
	 */
	public synchronized int lookup(String value) {

		if(fld==INDEXFIELD.LINK)
		{
			
			if(super.exists(value))
				return sharedDict.indexOf(value);
			else
			{	
				
				sharedDict.add(value);
			    return sharedDict.indexOf(value);
			}
		}
		else
		{
			
			if(super.exists(value))
				return localDict.indexOf(value);
			else
			{	
				
				localDict.add(value);
			    return localDict.indexOf(value);
			}
		}
	
	
		
	}
	
	
}
