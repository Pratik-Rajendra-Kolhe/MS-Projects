/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Map;
import java.util.Properties;

/**
 * @author Pratik
 * This class represents a subclass of a Dictionary class that is
 * shared by multiple threads. All methods in this class are
 * synchronized for the same reason.
 */
public class SharedDictionary extends Dictionary {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -2571975712894803121L;

	/**
	 * Public default constructor
	 * @param props: The properties file
	 * @param field: The field being indexed by this dictionary
	 */

	
	public SharedDictionary(Properties props, INDEXFIELD field) {
		super(props, field);

				
	}
	
	
	public synchronized static String getFileinfo() throws IOException
	{
	    
	    File currentFolder = new File(".");
        File workingFolder = new File(currentFolder, "serialized");
        if (!workingFolder.exists()) {
            workingFolder.mkdir();
            
        }
        File serializedF = new File(workingFolder.getAbsolutePath()+"\\SharedDict.ser");
        if(!serializedF.exists()) {
        	serializedF.createNewFile();
        } 
        
        return (workingFolder.getAbsolutePath()+"\\SharedDict.ser");

   	
	}
	
	public synchronized void cleanUp()
	{
		sharedDict.clear();
	}
	
	public synchronized void writeToDisk() throws IndexerException
	{
		 try
	      {
			    
			    File currentFolder = new File(".");
		        File workingFolder = new File(currentFolder, "serialized");
		        if (!workingFolder.exists()) {
		            workingFolder.mkdir();
		            
		        }
		        File serializedF = new File(workingFolder.getAbsolutePath()+"\\SharedDict.ser");
		        if(!serializedF.exists()) {
		        	serializedF.createNewFile();
		        } 
      
		    
	         FileOutputStream fileOut =new FileOutputStream(workingFolder.getAbsolutePath()+"\\SharedDict.ser");
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(this);
	         out.close();
	         fileOut.close();
	         
	      }catch(IOException i)
	      {
	          i.printStackTrace();
	      }
		
	}

	
	public synchronized static SharedDictionary readSharedDictionary() throws IOException, ClassNotFoundException
	{
		
		try{
		String path=SharedDictionary.getFileinfo(); 
	
        FileInputStream fileIn = new FileInputStream(path);
        ObjectInputStream in = new ObjectInputStream(fileIn);
    	
    	
        SharedDictionary i= (SharedDictionary)in.readObject();
        in.close();
        fileIn.close();
        return i;
        }
        catch(Exception e)
        {
        	
        	return null;
        }
  
		
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
		 if (super.exists(value))
	      return sharedDict.indexOf(value);
	     else
	    	 sharedDict.add(value);
	
		return sharedDict.indexOf(value);
	}

}
