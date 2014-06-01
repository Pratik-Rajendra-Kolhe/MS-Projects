/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

import javax.swing.text.html.HTMLDocument.Iterator;

import edu.buffalo.cse.ir.wikiindexer.SingleIndexerRunner;

/**
 * @author Pratik
 * This class is used to write an index to the disk
 * 
 */

public class IndexWriter implements Writeable,Serializable {

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	/**
	 * Constructor that assumes the underlying index is inverted
	 * Every index (inverted or forward), has a key field and the value field
	 * The key field is the field on which the postings are aggregated
	 * The value field is the field whose postings we are accumulating
	 * For term index for example:
	 * 	Key: Term (or term id) - referenced by TERM INDEXFIELD
	 * 	Value: Document (or document id) - referenced by LINK INDEXFIELD
	 * @param props: The Properties file
	 * @param keyField: The index field that is the key for this index
	 * @param valueField: The index field that is the value for this index
	 */
	protected Properties props;
	protected INDEXFIELD keyfld;
	protected INDEXFIELD valfld;
    protected	boolean isfwd;
    private int partno;
	
	protected Map<Integer,List<Integer[]>> index;
	protected Map<Integer,List<Integer[]>> authindex;
	protected Map<Integer,List<Integer[]>> termindex;
	protected Map<Integer,List<Integer[]>> categoryindex;
	protected Map<Integer,List<Integer[]>> linkindex;
	protected LocalDictionary localDict;
	
	
    public IndexWriter(Properties props, INDEXFIELD keyField, INDEXFIELD valueField) throws ClassNotFoundException, IOException {
		this(props, keyField, valueField, false);
        
	}
	
	/**
	 * Overloaded constructor that allows specifying the index type as
	 * inverted or forward
	 * Every index (inverted or forward), has a key field and the value field
	 * The key field is the field on which the postings are aggregated
	 * The value field is the field whose postings we are accumulating
	 * For term index for example:
	 * 	Key: Term (or term id) - referenced by TERM INDEXFIELD
	 * 	Value: Document (or document id) - referenced by LINK INDEXFIELD
	 * @param props: The Properties file
	 * @param keyField: The index field that is the key for this index
	 * @param valueField: The index field that is the value for this index
	 * @param isForward: true if the index is a forward index, false if inverted
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public IndexWriter(Properties props, INDEXFIELD keyField, INDEXFIELD valueField, boolean isForward) throws ClassNotFoundException, IOException {
		//TODO: Implement this method
		this.props=props;
		this.keyfld=keyField;
		this.valfld=valueField;
		this.isfwd=isForward;
		index=new HashMap<>();
		termindex=new HashMap<>();
		linkindex=new HashMap<>();
		categoryindex=new HashMap<>();
		authindex=new HashMap<>();
		localDict=new LocalDictionary(props,keyField);
		partno=0;
	}
	
	/**
	 * Method to make the writer self aware of the current partition it is handling
	 * Applicable only for distributed indexes.
	 * @param pnum: The partition number
	 */
	public  synchronized void setPartitionNumber(int pnum) {
		//TODO: Optionally implement this method
	}
	
	/**
	 * Method to add a given key - value mapping to the index
	 * @param keyId: The id for the key field, pre-converted
	 * @param valueId: The id for the value field, pre-converted
	 * @param numOccurances: Number of times the value field is referenced
	 *  by the key field. Ignore if a forward index
	 * @throws IndexerException: If any exception occurs while indexing
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public  synchronized void  addToIndex(int keyId, int valueId, int numOccurances) throws IndexerException, IOException, ClassNotFoundException {

    
		Integer[] doc={(Integer)valueId,(Integer)numOccurances};
	    
		List<Integer[]> d=new ArrayList<Integer[]>();

//-----------forward Indexing------
		
		if(isfwd)
		{
			this.partno=17;
			if(!index.containsKey(keyId))
			{
				    
				    d.add(doc);
					index.put(keyId, d);
			}		
			else
			{
				
				index.get(keyId).add(doc);
			}
			linkindex=index;
		}
		
//-------------inverted indexing-----------------------------
		
		
		if(!isfwd)
		{
		
			if(!index.containsKey(keyId))
			{
				    
				    d.add(doc);
					index.put(keyId, d);
			}		
			else
			{
				
				index.get(keyId).add(doc);
			}
		}
	}
	
	/**
	 * Method to add a given key - value mapping to the index
	 * @param keyId: The id for the key field, pre-converted
	 * @param value: The value for the value field
	 * @param numOccurances: Number of times the value field is referenced
	 *  by the key field. Ignore if a forward index
	 * @throws IndexerException: If any exception occurs while indexing
	 */
	public synchronized void addToIndex(int keyId, String value, int numOccurances) throws IndexerException {

	}
	
	/**
	 * Method to add a given key - value mapping to the index
	 * @param key: The key for the key field
	 * @param valueId: The id for the value field, pre-converted
	 * @param numOccurances: Number of times the value field is referenced
	 *  by the key field. Ignore if a forward index
	 * @throws IndexerException: If any exception occurs while indexing
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public synchronized void addToIndex(String key, int valueId, int numOccurances) throws IndexerException, ClassNotFoundException, IOException {

		int id=0;
		if(keyfld==INDEXFIELD.TERM)
		{	
			int part=Partitioner.getPartitionNumber(key);
			this.partno=part;
			id=localDict.lookup(key);
			addToIndex(id,valueId,numOccurances);
			termindex=index;
		}
		if(keyfld==INDEXFIELD.AUTHOR)
		{	
			this.partno=15;
			id=localDict.lookup(key);
			addToIndex(id,valueId,numOccurances);
			authindex=index;
		}
		
		if(keyfld==INDEXFIELD.CATEGORY)
		{	
			this.partno=16;
			id=localDict.lookup(key);
			addToIndex(id,valueId,numOccurances);
			categoryindex=index;
		}

	}
	
	/**
	 * Method to add a given key - value mapping to the index
	 * @param key: The key for the key field
	 * @param value: The value for the value field
	 * @param numOccurances: Number of times the value field is referenced
	 *  by the key field. Ignore if a forward index
	 * @throws IndexerException: If any exception occurs while indexing
	 */
	public  synchronized void addToIndex(String key, String value, int numOccurances) throws IndexerException {

	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.ir.wikiindexer.indexer.Writeable#writeToDisk()
	 */
	public  synchronized void writeToDisk() throws IndexerException {
		if(partno!=18)
		write(partno);
		 	}
	
	public synchronized  void write(int part)
	{
       
		String s="\\Index"+part+".ser";
		 try
	      {
	
			    File currentFolder = new File(".");
		        File workingFolder = new File(currentFolder, "serialized");
		        if (!workingFolder.exists()) {
		            workingFolder.mkdir();
		            
		        }
		        File serializedF = new File(workingFolder.getAbsolutePath()+s);
		        if(!serializedF.exists()) {
		        	serializedF.createNewFile();
		        } 
       
		    
	         FileOutputStream fileOut =new FileOutputStream(workingFolder.getAbsolutePath()+s);
	         ObjectOutputStream out = new ObjectOutputStream(fileOut);
	         out.writeObject(this);
	         out.close();
	         fileOut.close();
		  
		      }catch(IOException i)
	      {
	          i.printStackTrace();
	      }
		
	}

	/* (non-Javadoc)
	 * @see edu.buffalo.cse.ir.wikiindexer.indexer.Writeable#cleanUp()
	 */
	public  synchronized void cleanUp() {
		// TODO Implement this method
	     index.clear();
	     authindex.clear();
	     termindex.clear();
	     categoryindex.clear();
	     linkindex.clear();
		localDict=null;

	}
	
	public synchronized static String getFileinfo(int part) throws IOException
	{
		String s="\\Index"+part+".ser";
		
	     	
	    File currentFolder = new File(".");
        File workingFolder = new File(currentFolder, "serialized");
        if (!workingFolder.exists()) {
            workingFolder.mkdir();
            
        }
        File serializedF = new File(workingFolder.getAbsolutePath()+s);
        if(!serializedF.exists()) {
        	serializedF.createNewFile();
        } 
        
        return (workingFolder.getAbsolutePath()+s);
	        
	}
	
	public  synchronized static boolean isFile()
	{
	    File currentFolder = new File(".");
        File workingFolder = new File(currentFolder, "serialized");
        if (!workingFolder.exists()) 
            return false;
        else return true;
	}
	
		public synchronized  static IndexWriter readIndex(int part) throws IOException, ClassNotFoundException
	{
		
		try{
		String path=IndexWriter.getFileinfo(part); 
	  
        FileInputStream fileIn = new FileInputStream(path);
  	  
        ObjectInputStream in = new ObjectInputStream(fileIn);
        
    	
        IndexWriter i= (IndexWriter)in.readObject();
        in.close();
        fileIn.close();
        
        return i;
        }
        catch(Exception e)
        {
        	
        	return null;
        }
	}
}

