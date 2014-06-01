/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * @author Pratik
 * An abstract class that represents a dictionary object for a given index
 */
public  abstract class Dictionary implements Writeable,Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	protected List<String> sharedDict;
	protected List<String> localDict;
		
	private INDEXFIELD fld;
	public Dictionary (Properties props, INDEXFIELD field) {

		
	 
		this.fld=field;
		sharedDict= new ArrayList<String>();
		localDict= new ArrayList<String>();
	}
	
	/* (non-Javadoc)
	 * @see edu.buffalo.cse.ir.wikiindexer.indexer.Writeable#writeToDisk()
	 */
	public synchronized void writeToDisk() throws IndexerException {
		// TODO Implement this method

	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.ir.wikiindexer.indexer.Writeable#cleanUp()
	 */
	public synchronized void cleanUp() {
		// TODO Implement this method

	}
	
	/**
	 * Method to check if the given value exists in the dictionary or not
	 * Unlike the subclassed lookup methods, it only checks if the value exists
	 * and does not change the underlying data structure
	 * @param value: The value to be looked up
	 * @return true if found, false otherwise
	 */
	public synchronized boolean exists(String value) {
		
		if(fld==INDEXFIELD.LINK)
			return sharedDict.contains(value);
        
		if(fld==INDEXFIELD.TERM)
      	    return localDict.contains(value);
                	
        if(fld==INDEXFIELD.AUTHOR)
    		return localDict.contains(value);
        
        if(fld==INDEXFIELD.CATEGORY)
    		return localDict.contains(value);
        
        return false;
	}
	
	/**
	 * MEthod to lookup a given string from the dictionary.
	 * The query string can be an exact match or have wild cards (* and ?)
	 * Must be implemented ONLY AS A BONUS
	 * @param queryStr: The query string to be searched
	 * @return A collection of ordered strings enumerating all matches if found
	 * null if no match is found
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public synchronized Collection<String> query(String queryStr) throws ClassNotFoundException, IOException {
		String s=null;
		int flag=0;
		List<String> l=new ArrayList<String>();
		List<String> o=new ArrayList<String>();
	

		if(fld==INDEXFIELD.LINK)
	
	       l=SharedDictionary.readSharedDictionary().sharedDict;

		if(fld==INDEXFIELD.TERM)
			for(int i=0;i<Partitioner.partno;i++)
				l.addAll(IndexWriter.readIndex(i).localDict.localDict);
			
		if(fld==INDEXFIELD.AUTHOR)
	            l=IndexWriter.readIndex(15).localDict.localDict;
			
		if(fld==INDEXFIELD.CATEGORY)
	            l=IndexWriter.readIndex(16).localDict.localDict;
		
		Iterator<String> i=l.iterator();
		
		
		if(queryStr.charAt(0)=='*')
		{
			s=queryStr.substring(1);
			flag=1;
			
		}	
		else if(queryStr.charAt(queryStr.length()-1)=='*')
		{	
			s=queryStr.substring(0,queryStr.length()-1);
		    flag=2;
		    
		}	
		else if(queryStr.contains("*"))
		{	
			s=queryStr;
			flag=3;
			
		}else
		{
			flag=4;
		}
		
		if(flag==1)
		{
		
		    	while(i.hasNext())
		    	{
		    		String temp=i.next();
		    		temp=temp.toLowerCase();
		    		
		    		if(temp.contains(s))
		    		{
		    		
		    			if(temp.substring(temp.length()-1-s.length()+1).compareToIgnoreCase(s)==0)
		    			{
		    				
		    				o.add(temp);
		    			}
		    		}
		    	}
		    	return o;
		}
		
		if(flag==2)
		{
		
			while(i.hasNext())
		    	{
		    		String temp=i.next();
		             temp=temp.toLowerCase();
		    		if(temp.contains(s))
		    		{
		    			
		    			
		    			if(temp.substring(0,s.length()).compareToIgnoreCase(s)==0)
		    			{
		    				o.add(temp);
		    			}
		    		}
		    	}
		    	return o;
		}
         
		if(flag==3)
		{
			String s1=s.split("\\*")[0];
			String s2=s.split("\\*")[1];
			
			s1=s1.toLowerCase();
			s2=s2.toLowerCase();
			
	    	while(i.hasNext())
	    	{
	    		String temp=i.next();
	    		temp=temp.toLowerCase();
	    		if(temp.contains(s1) && temp.contains(s2))
	    		{
	    			if(temp.substring(0,s1.length()).compareToIgnoreCase(s1)==0 && temp.substring(temp.length()-1-s2.length()+1).compareToIgnoreCase(s2)==0)
	    			{
	    				o.add(temp);
	    			}
	    		}
	    	}

			if(flag==4)
			{
		    	while(i.hasNext())
		    	{
		    		String temp=i.next();

		    		if(temp.compareToIgnoreCase(s)==0)
		    		
		    				o.add(temp);
		    	}		
		    		
		    	}

	    	return o;
		}

	return o;
	}
	
	/**
	 * Method to get the total number of terms in the dictionary
	 * @return The size of the dictionary
	 */
	public synchronized int getTotalTerms() {
		//TODO: Implement this method
		if(fld==INDEXFIELD.LINK)
			return sharedDict.size();
      	if(fld==INDEXFIELD.TERM)
			return localDict.size();
		if(fld==INDEXFIELD.AUTHOR)
			return localDict.size();
		if(fld==INDEXFIELD.CATEGORY)
			return localDict.size();

		return -1;
	}
	
	public synchronized static String getFileinfo() throws IOException
	{
	    
	    File currentFolder = new File(".");
        File workingFolder = new File(currentFolder, "serialized");
        if (!workingFolder.exists()) {
            workingFolder.mkdir();
            
        }
        File serializedF = new File(workingFolder.getAbsolutePath()+"\\Dict.ser");
        if(!serializedF.exists()) {
        	serializedF.createNewFile();
        } 
        
        return (workingFolder.getAbsolutePath()+"\\Dict.ser");

   	
	}
}
