/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.wikipedia;

import edu.buffalo.cse.ir.wikiindexer.indexer.INDEXFIELD;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;

import java.util.HashMap;
import java.util.Map;

/**
 * A simple map based token view of the transformed document
 * @author Pratik
 *
 */
public class IndexableDocument {
	
	private Map<INDEXFIELD,TokenStream> idxMap;
	private String docID;
	
	/**
	 * Default constructor
	 */
	public IndexableDocument() {
		
		this(null);
	}
	
	public IndexableDocument(String docID)
	{
		this.idxMap=new HashMap<INDEXFIELD,TokenStream>();
		this.docID=docID;
	}
	
	
	/**
	 * MEthod to add a field and stream to the map
	 * If the field already exists in the map, the streams should be merged
	 * @param field: The field to be added
	 * @param stream: The stream to be added.
	 */
	public void addField(INDEXFIELD field, TokenStream stream) {
		//TODO: Implement this method
		if(field==INDEXFIELD.AUTHOR)
	    {
			if(idxMap.containsKey(field))
	    	{
	    		idxMap.get(field).merge(stream);
	    	}
	    	else
	    	{
	    		idxMap.put(INDEXFIELD.AUTHOR,stream);
	    	}
	    }
	    if(field==INDEXFIELD.TERM)
	    {
	    	if(idxMap.containsKey(field))
	    	{
	    		idxMap.get(field).merge(stream);
	    	}
	    	else
	    	{
	    		idxMap.put(INDEXFIELD.TERM,stream);
	    	}
	    }
	    if(field==INDEXFIELD.LINK)
	    {
	    	if(idxMap.containsKey(field))
	    	{
	    		idxMap.get(field).merge(stream);
	    	}
	    	else
	    	{
	    		idxMap.put(INDEXFIELD.LINK,stream);
	    	}  	
	    }
	    if(field==INDEXFIELD.CATEGORY)
	    {
	    	if(idxMap.containsKey(field))
	    	{
	    		idxMap.get(field).merge(stream);
	    	}
	    	else
	    	{
	    		idxMap.put(INDEXFIELD.CATEGORY,stream);
	    	}
	    }
	}
	
	/**
	 * Method to return the stream for a given field
	 * @param key: The field for which the stream is requested
	 * @return The underlying stream if the key exists, null otherwise
	 */
	public TokenStream getStream(INDEXFIELD key) {
		//TODO: Implement this method
		if(key==null)
			return null;
		else
			return idxMap.get(key);
	}
	
	/**
	 * Method to return a unique identifier for the given document.
	 * It is left to the student to identify what this must be
	 * But also look at how it is referenced in the indexing process
	 * @return A unique identifier for the given document
	 */
	public String getDocumentIdentifier() {
		//TODO: Implement this method
		if(docID==null)
			return null;
		else
			return docID;
	}
}
