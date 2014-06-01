/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.tokenizer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

/**
 * This class represents a stream of tokens as the name suggests.
 * It wraps the token stream and provides utility methods to manipulate it
 *
 */
public class TokenStream implements Iterator<String>{
	
	/**
	 * Default constructor
	 * @param bldr: THe stringbuilder to seed the stream
	 */
	private List<String> stream;
	private String inputStream;
	private ListIterator<String> itr;
	
	public TokenStream(StringBuilder bldr) {
		//TODO: Implement this method
	    this.inputStream=bldr.toString(); 
		stream=new ArrayList<String>();
		if(this.inputStream!=null && this.inputStream.length()!=0)
			stream.add(this.inputStream);
			
		itr=stream.listIterator();
	}
	
	/**
	 * Overloaded constructor
	 * @param bldr: THe stringbuilder to seed the stream
	 */
	public TokenStream(String string) {
		//TODO: Implement this method
		this.inputStream=string; 
		stream=new ArrayList<String>();
		if(this.inputStream!=null && this.inputStream.length()!=0)
			stream.add(this.inputStream);
		
		itr=stream.listIterator();
	}
	
	/**
	 * Method to append tokens to the stream
	 * @param tokens: The tokens to be appended
	 */
	public void append(String... tokens) {

		if(tokens!=null)
		 {	
			for(int i=0;i<tokens.length;i++)
			{
				if(tokens[i]!=null && tokens[i]!="")
				{
					stream.add(tokens[i]);
				}	
			}	
		 }
		itr=stream.listIterator();		
	}
	
	/**
	 * Method to retrieve a map of token to count mapping
	 * This map should contain the unique set of tokens as keys
	 * The values should be the number of occurrences of the token in the given stream
	 * @return The map as described above, no restrictions on ordering applicable
	 */
	public Map<String, Integer> getTokenMap() {

		Map<String,Integer> map=new HashMap<String,Integer>();
		if(stream==null)
			return null;
		if(stream.isEmpty())
			return null;
		else
		{
			String v=new String();
			int cnt=0;
			for(int i=0;i<stream.size();i++)
			{
				v=stream.get(i);
				cnt=this.query(v);
				map.put(v, cnt);
			}
			return map;
		}
	}
	
	/**
	 * Method to get the underlying token stream as a collection of tokens
	 * @return A collection containing the ordered tokens as wrapped by this stream
	 * Each token must be a separate element within the collection.
	 * Operations on the returned collection should NOT affect the token stream
	 */
	public Collection<String> getAllTokens() {

		if(stream.isEmpty())
			return null;
		else			
		return stream;	
	}
	
	/**
	 * Method to query for the given token within the stream
	 * @param token: The token to be queried
	 * @return: THe number of times it occurs within the stream, 0 if not found
	 */
	public int query(String token) {

		int count=0;
		if(stream==null)
			return 0;
		else if(stream.isEmpty())
			return 0;
		else 
		{
			for(int i=0;i<stream.size();i++)
			{
				if(stream.get(i).compareTo(token)==0)
					count++;
			}
			return count;
		}
	}
	
	/**
	 * Iterator method: Method to check if the stream has any more tokens
	 * @return true if a token exists to iterate over, false otherwise
	 */
	public boolean hasNext() {
		// TODO: Implement this method
		if(stream.isEmpty())
			return false;
		return itr.hasNext();
	}
	
	/**
	 * Iterator method: Method to check if the stream has any more tokens
	 * @return true if a token exists to iterate over, false otherwise
	 */
	public boolean hasPrevious() {

		if(stream.isEmpty())
			return false;
		return itr.hasPrevious();
	}
	
	/**
	 * Iterator method: Method to get the next token from the stream
	 * Callers must call the set method to modify the token, changing the value
	 * of the token returned by this method must not alter the stream
	 * @return The next token from the stream, null if at the end
	 */
	public String next() {

		   if(itr.hasNext())
			   return itr.next();
		   else
			   return null;
	}
	
	/**
	 * Iterator method: Method to get the previous token from the stream
	 * Callers must call the set method to modify the token, changing the value
	 * of the token returned by this method must not alter the stream
	 * @return The next token from the stream, null if at the end
	 */
	public String previous() {

		   if(itr.hasPrevious())
			   return itr.previous();
		   else
			   return null;
	}
	
	/**
	 * Iterator method: Method to remove the current token from the stream
	 */
	public void remove() {

	  if(inputStream==null)
	   {
		// [Do nothing]
	   }
	  else if(stream.isEmpty())
	  {
		  // [Do nothing]
	  }
	  else if(!itr.hasNext())
	  {
		  // [Do nothing]
	  }
	  else
	  {	 	
		 itr.next();
		 itr.remove();
	  }
	}	
	
	/**
	 * Method to merge the current token with the previous token, assumes whitespace
	 * separator between tokens when merged. The token iterator should now point
	 * to the newly merged token (i.e. the previous one)
	 * @return true if the merge succeeded, false otherwise
	 */
	public boolean mergeWithPrevious() {

		if (stream.isEmpty())
			return false;
		else if(!itr.hasPrevious())
				return false;
		else if(itr.hasNext())
		{
			String s1=itr.next();
			itr.remove();				
			String s2=itr.previous();
			String s3=s2+" "+s1;
			itr.set(s3);
			return true;
		}
		else 
			return false;
	}
	
	/**
	 * Method to merge the current token with the next token, assumes whitespace
	 * separator between tokens when merged. The token iterator should now point
	 * to the newly merged token (i.e. the current one)
	 * @return true if the merge succeeded, false otherwise
	 */
	public boolean mergeWithNext() {

		ListIterator<String> tempItr=itr;
		
		if (stream.isEmpty())
		return false;
		else if(!tempItr.hasNext())
			return false;
		else
		{
			String s1=tempItr.next();
			if(!itr.hasNext())
				return false;
			String s2=tempItr.next();
			String s3=s1+" "+s2;

			tempItr.previous();
			tempItr.remove();
			tempItr.previous();
			tempItr.set(s3);
			itr=tempItr;
			return true;
		}			
	}
	
	/**
	 * Method to replace the current token with the given tokens
	 * The stream should be manipulated accordingly based upon the number of tokens set
	 * It is expected that remove will be called to delete a token instead of passing
	 * null or an empty string here.
	 * The iterator should point to the last set token, i.e, last token in the passed array.
	 * @param newValue: The array of new values with every new token as a separate element within the array
	 */
	public void set(String... newValue) {
		List<String> temp=new ArrayList<>();
		ListIterator<String> tempItr=stream.listIterator();
		tempItr=itr;
		int idx=0;
		if(stream==null)
			{
				// [Do nothing]
			}	
			else if(stream.isEmpty())
			{
				// [Do nothing]
			}
			else
			{
			    if(newValue[0]==null)
			    {
					// [Do nothing]
			    }
			    else if(newValue[0].length()==0)
			    {
					// [Do nothing]
			    }
				else if(!tempItr.hasNext())
				{
					if(stream.size()==1)
					{
						tempItr.previous();
						tempItr.remove();
		        	    tempItr.add(newValue[0]);
					}
					else
					{
						// [Do nothing]
					}
					// [Do nothing]
				}
		        else if(!tempItr.hasPrevious())
		        {
		        	for(int i=0;i<newValue.length;i++)
		        	{
		        		temp.add(newValue[i]);  
		        	}
		        	  tempItr.next();		        	  
		        	  tempItr.remove();
		        	  temp.addAll(stream);
		        	  stream=temp;
		        	  tempItr=stream.listIterator();
		        	  for(int i=0;i<newValue.length-1;i++)
		        		  tempItr.next();
		        	}
		        else
		        {
                     tempItr.remove();
                     if(!tempItr.hasNext())
                    	 idx=tempItr.nextIndex();
                     else
                      idx=tempItr.nextIndex();  
                      
		        	 for(int i=0;i<idx;i++)
		        	 {
		        		 temp.add(stream.get(i)); 
		        	 }
		        	  
		        	 for(int i=0;i<newValue.length;i++)
		        	 {
		        		 temp.add(newValue[i]);
		        	 }
		        	  
		        	 for(int i=idx;i<stream.size();i++)
		        	 {
		        		 temp.add(stream.get(i));
		        	 }
		        	  
		        	 stream=temp;
		        	 tempItr=stream.listIterator();
		        	 for(int i=0;i<idx+newValue.length-1;i++)
		        		 tempItr.next();
		        }
			}
	itr=tempItr;
	}
	
	/**
	 * Iterator method: Method to reset the iterator to the start of the stream
	 * next must be called to get a token
	 */
	public void reset() {

		itr=stream.listIterator();
	}
	
	/**
	 * Iterator method: Method to set the iterator to beyond the last token in the stream
	 * previous must be called to get a token
	 */
	public void seekEnd() {
		while(itr.hasNext())
			itr.next();
	}
	
	/**
	 * Method to merge this stream with another stream
	 * @param other: The stream to be merged
	 */
	public void merge(TokenStream other) {

		if(other==null)
		{
			// [Do nothing]
		}
		else if(!other.stream.isEmpty())
		{	   
	       Object []s=other.getAllTokens().toArray();
		   for(int i=0;i<s.length;i++)
		      {
			     stream.add(s[i].toString());
		      }
		}
	 itr=stream.listIterator();
	}
}