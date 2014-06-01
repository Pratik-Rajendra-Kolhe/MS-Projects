/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.PriorityQueue;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;


/**
 * @author Pratik
 * This class is used to introspect a given index
 * The expectation is the class should be able to read the index
 * and all associated dictionaries.
 */
public class IndexReader {
	/**
	 * Constructor to create an instance 
	 * @param props: The properties file
	 * @param field: The index field whose index is to be read
	 * 
	 */
	 
	private List<String> sDict;
	private List<String> lDict;
	private INDEXFIELD fld;
	private Properties props;
	
	public  IndexReader(Properties props, INDEXFIELD field) throws ClassNotFoundException, IOException {

		this.fld=field;
		this.props=props;
		 sDict=SharedDictionary.readSharedDictionary().sharedDict;
		 lDict=new ArrayList<String>();

			if(field==INDEXFIELD.LINK)
		
		       lDict=sDict;

			if(fld==INDEXFIELD.TERM)
				for(int i=0;i<Partitioner.partno;i++)
					lDict.addAll(IndexWriter.readIndex(i).localDict.localDict);
				
			if(fld==INDEXFIELD.AUTHOR)
		            lDict=IndexWriter.readIndex(15).localDict.localDict;
				
			if(fld==INDEXFIELD.CATEGORY)
		            lDict=IndexWriter.readIndex(16).localDict.localDict;
		} 
		
	
	
	/**
	 * Method to get the total number of terms in the key dictionary
	 * @return The total number of terms as above
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public synchronized int getTotalKeyTerms() throws ClassNotFoundException, IOException {

		return lDict.size();
	}
	
	/**
	 * Method to get the total number of terms in the value dictionary
	 * @return The total number of terms as above
	 */
	public synchronized int getTotalValueTerms() {

		return sDict.size();
	}
	
	/**
	 * Method to retrieve the postings list for a given dictionary term
	 * @param key: The dictionary term to be queried
	 * @return The postings list with the value term as the key and the
	 * number of occurrences as value. An ordering is not expected on the map
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public synchronized Map<String, Integer> getPostings(String key) throws ClassNotFoundException, IOException {

		IndexWriter idx=new IndexWriter(null,null,null);
		int part=0;
		Map<Integer,List<Integer[]>> index=new HashMap<Integer,List<Integer[]>>();
		if(fld==INDEXFIELD.TERM)
		{
			
			part=Partitioner.getPartitionNumber(key);
			idx=IndexWriter.readIndex(part);
			lDict=idx.localDict.localDict;
			index=idx.termindex;
		}	
		
		if(fld==INDEXFIELD.AUTHOR)
		{
			
			
			idx=IndexWriter.readIndex(15);
			lDict=idx.localDict.localDict;
			index=idx.authindex;
		}	
		if(fld==INDEXFIELD.LINK)
		{
			idx=IndexWriter.readIndex(17);
			index=idx.linkindex;
		}	
		if(fld==INDEXFIELD.CATEGORY)
		{
			idx=IndexWriter.readIndex(16);
			lDict=idx.localDict.localDict;
			index=idx.categoryindex;
		}	

	
	
		Map<String, Integer> output=new HashMap<String,Integer>();
		
		int id;
		if(lDict.contains(key))
			 id=lDict.indexOf(key);
		else return output;
	
		List<Integer[]> pList=index.get(id);
		
		
		Iterator<Integer[]> i=pList.iterator();
		
		while(i.hasNext())
		{
			Integer[] val=i.next();
			
			String docID=sDict.get(val[0]);
			
			output.put(docID, val[1]);
		}
		
		return output;
	}
	
	/**
	 * Method to get the top k key terms from the given index
	 * The top here refers to the largest size of postings.
	 * @param k: The number of postings list requested
	 * @return An ordered collection of dictionary terms that satisfy the requirement
	 * If k is more than the total size of the index, return the full index and don't 
	 * pad the collection. Return null in case of an error or invalid inputs
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public synchronized Collection<String> getTopK(int k) throws ClassNotFoundException, IOException {

		Map<Integer,List<Integer[]>> index=new HashMap<Integer,List<Integer[]>>();
        List<String> output=new ArrayList<String>();
        Map<String,Integer> m=new HashMap<String,Integer>();
        
        if(fld==INDEXFIELD.LINK)
			
	    {	
              index=IndexWriter.readIndex(17).linkindex;
              Set<Integer> termID=index.keySet();
              List<String> term=new ArrayList<String>();
  
              
              Iterator<Integer> i=termID.iterator();
              
              while(i.hasNext())
              term.add(lDict.get((int) i.next()));	
              Collection<List<Integer[]>> post=index.values();
  
              
              Iterator<String> t= term.iterator();
              Iterator<List<Integer[]>> p=post.iterator();
              
               while(t.hasNext() && p.hasNext())
              	 m.put(t.next(),p.next().size());
               
               PriorityQueue<Entry<String, Integer>> pq = new PriorityQueue<Map.Entry<String,Integer>>(m.size(), new Comparator<Entry<String, Integer>>() {

            	    @Override
            	    public int compare(Entry<String, Integer> arg0,
            	            Entry<String, Integer> arg1) {
            	        return arg1.getValue().compareTo(arg0.getValue());
            	    }
            	});
            	pq.addAll(m.entrySet());
            	
            	while (!pq.isEmpty() && k>0) {
            		
            	    output.add(pq.poll().getKey());
            	    k--;
            	}
               
 
                   return output;
       
	   }
        else if(fld==INDEXFIELD.TERM)
        {
        	 
            	for(int j=0;j<Partitioner.partno;j++) 
            	{	
            	IndexWriter idx=IndexWriter.readIndex(j);
				
				
					index=idx.termindex;
				
            	lDict=idx.localDict.localDict;
            	
 			    Set<Integer> termID=index.keySet();
			    List<String> term=new ArrayList<String>();
			    
			    Iterator<Integer> i1=termID.iterator();
			    
			    while(i1.hasNext())
			    term.add(lDict.get((int) i1.next()));	
			    
			    
			    Collection<List<Integer[]>> post=index.values();
			    
			    Iterator<String> t= term.iterator();
			    Iterator<List<Integer[]>> p=post.iterator();
			     while(t.hasNext() && p.hasNext())
			    	 m.put(t.next(),p.next().size());
               }     
                PriorityQueue<Entry<String, Integer>> pq = new PriorityQueue<Map.Entry<String,Integer>>(m.size(), new Comparator<Entry<String, Integer>>() {

            	    @Override
            	    public int compare(Entry<String, Integer> arg0,
            	            Entry<String, Integer> arg1) {
            	        return arg1.getValue().compareTo(arg0.getValue());
            	    }
            	});
            	pq.addAll(m.entrySet());
            	
            	while (!pq.isEmpty() && k>0) {
            		
            	    output.add(pq.poll().getKey());
            	    k--;
            	}
                   return output;
            	
		 }   else if(fld==INDEXFIELD.AUTHOR)
				
		    {	
	              index=IndexWriter.readIndex(15).authindex;
	              Set<Integer> termID=index.keySet();
	              List<String> term=new ArrayList<String>();
	  
	              
	              Iterator<Integer> i=termID.iterator();
	              
	              while(i.hasNext())
	              term.add(lDict.get((int) i.next()));	
	              Collection<List<Integer[]>> post=index.values();
	  
	              
	              Iterator<String> t= term.iterator();
	              Iterator<List<Integer[]>> p=post.iterator();
	              
	               while(t.hasNext() && p.hasNext())
	              	 m.put(t.next(),p.next().size());
	               
	               PriorityQueue<Entry<String, Integer>> pq = new PriorityQueue<Map.Entry<String,Integer>>(m.size(), new Comparator<Entry<String, Integer>>() {

	            	    @Override
	            	    public int compare(Entry<String, Integer> arg0,
	            	            Entry<String, Integer> arg1) {
	            	        return arg1.getValue().compareTo(arg0.getValue());
	            	    }
	            	});
	            	pq.addAll(m.entrySet());
	            	
	            	while (!pq.isEmpty() && k>0) {
	            		
	            	    output.add(pq.poll().getKey());
	            	    k--;
	            	}
	               
	 
	                   return output;
	       
		   }else if(fld==INDEXFIELD.CATEGORY)
				
		    {	
	              index=IndexWriter.readIndex(16).categoryindex;
	              Set<Integer> termID=index.keySet();
	              List<String> term=new ArrayList<String>();
	  
	              
	              Iterator<Integer> i=termID.iterator();
	              
	              while(i.hasNext())
	              term.add(lDict.get((int) i.next()));	
	              Collection<List<Integer[]>> post=index.values();
	  
	              
	              Iterator<String> t= term.iterator();
	              Iterator<List<Integer[]>> p=post.iterator();
	              
	               while(t.hasNext() && p.hasNext())
	              	 m.put(t.next(),p.next().size());
	               
	               PriorityQueue<Entry<String, Integer>> pq = new PriorityQueue<Map.Entry<String,Integer>>(m.size(), new Comparator<Entry<String, Integer>>() {

	            	    @Override
	            	    public int compare(Entry<String, Integer> arg0,
	            	            Entry<String, Integer> arg1) {
	            	        return arg1.getValue().compareTo(arg0.getValue());
	            	    }
	            	});
	            	pq.addAll(m.entrySet());
	            	
	            	while (!pq.isEmpty() && k>0) {
	            		
	            	    output.add(pq.poll().getKey());
	            	    k--;
	            	}
	               
	 
	                   return output;
	       
		   }
        return output;
        
   	}
	/**
	 * Method to execute a boolean AND query on the index
	 * @param terms The terms to be queried on
	 * @return An ordered map containing the results of the query
	 * The key is the value field of the dictionary and the value
	 * is the sum of occurrences across the different postings.
	 * The value with the highest cumulative count should be the
	 * first entry in the map.
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	
	public synchronized Map<String, Integer> query(String... terms) throws ClassNotFoundException, IOException {

	
	
	List<Map<String,Integer>> l=new ArrayList<Map<String,Integer>>();
    String [] query=terms;
   Stemmer st=new Stemmer();    
   for(int i=0;i<query.length;i++){
    for (char c: query[i].toCharArray()) {
		st.add(c);
    }
    st.stem();
    query[i]=st.toString();
   }
    
    int flag=0;
    int cnt=0;
    Map<String,Integer> result=new HashMap<String,Integer>();
    if(query.length==1)
    	return getPostings(query[0]);
    
    for(int i=0;i<query.length;i++)
    	
	   l.add(getPostings(query[i]));
    
       Set<String> s=l.get(0).keySet();
       
       Iterator<String> j=s.iterator();
       
       while(j.hasNext())
         {
    	   String t=j.next();
    	   Iterator<Map<String,Integer>> il=l.iterator();
    	   il.next();
    	      while(il.hasNext())
    	        {
    	    	   Set<String> s2=il.next().keySet();
    	    	   if(s2.contains(t))
    	    	    	flag=1;
    	    	    else
    	    	    {  
    	    	    	flag=0;
    	    	    	break;
    	    	    }	
  	    	    
    	        }
    	  if(flag==1)
    	    {  
    		  for(int k=0;k<l.size();k++)
    		  {
    			  cnt=cnt+l.get(k).get(t);
    		  }
    		  result.put(t,cnt);
    	    } 
         }
       PriorityQueue<Entry<String, Integer>> pq = new PriorityQueue<Map.Entry<String,Integer>>(result.size(), new Comparator<Entry<String, Integer>>() {

   	    @Override
   	    public int compare(Entry<String, Integer> arg0,
   	            Entry<String, Integer> arg1) {
   	        return arg1.getValue().compareTo(arg0.getValue());
   	    }
   	});
   	pq.addAll(result.entrySet());
  
   	Map<String,Integer> temp=new LinkedHashMap<String,Integer>();
   	result.clear();
   	while (!pq.isEmpty()) {

   	
   	   temp.put(pq.poll().getKey(),pq.poll().getValue());
   	    
   	}  
	return temp;
	}
}
	
	


class Stemmer
{  private char[] b;
  private int i,     /* offset into b */
              i_end, /* offset to end of stemmed word */
              j, k;
  private static final int INC = 50;
                    /* unit of size whereby b is increased */
  public Stemmer()
  {  b = new char[INC];
  
     i = 0;
     i_end = 0;
  }

  /**
   * Add a character to the word being stemmed.  When you are finished
   * adding characters, you can call stem(void) to stem the word.
   */

  public void add(char ch)
  {  if (i == b.length)
     {  char[] new_b = new char[i+INC];
        for (int c = 0; c < i; c++) new_b[c] = b[c];
        b = new_b;
     }
     b[i++] = ch;
  }


  /** Adds wLen characters to the word being stemmed contained in a portion
   * of a char[] array. This is like repeated calls of add(char ch), but
   * faster.
   */

  public void add(char[] w, int wLen)
  {  if (i+wLen >= b.length)
     {  char[] new_b = new char[i+wLen+INC];
        for (int c = 0; c < i; c++) new_b[c] = b[c];
        b = new_b;
     }
     for (int c = 0; c < wLen; c++) b[i++] = w[c];
  }

  /**
   * After a word has been stemmed, it can be retrieved by toString(),
   * or a reference to the internal buffer can be retrieved by getResultBuffer
   * and getResultLength (which is generally more efficient.)
   */
  public String toString() { return new String(b,0,i_end); }

  /**
   * Returns the length of the word resulting from the stemming process.
   */
  public int getResultLength() { return i_end; }

  /**
   * Returns a reference to a character buffer containing the results of
   * the stemming process.  You also need to consult getResultLength()
   * to determine the length of the result.
   */
  public char[] getResultBuffer() { return b; }

  /* cons(i) is true <=> b[i] is a consonant. */

  private final boolean cons(int i)
  {  switch (b[i])
     {  case 'a': case 'e': case 'i': case 'o': case 'u': return false;
        case 'y': return (i==0) ? true : !cons(i-1);
        default: return true;
     }
  }

  /* m() measures the number of consonant sequences between 0 and j. if c is
     a consonant sequence and v a vowel sequence, and <..> indicates arbitrary
     presence,

        <c><v>       gives 0
        <c>vc<v>     gives 1
        <c>vcvc<v>   gives 2
        <c>vcvcvc<v> gives 3
        ....
  */

  private final int m()
  {  int n = 0;
     int i = 0;
     while(true)
     {  if (i > j) return n;
        if (! cons(i)) break; i++;
     }
     i++;
     while(true)
     {  while(true)
        {  if (i > j) return n;
              if (cons(i)) break;
              i++;
        }
        i++;
        n++;
        while(true)
        {  if (i > j) return n;
           if (! cons(i)) break;
           i++;
        }
        i++;
      }
  }

  /* vowelinstem() is true <=> 0,...j contains a vowel */

  private final boolean vowelinstem()
  {  int i; for (i = 0; i <= j; i++) if (! cons(i)) return true;
     return false;
  }

  /* doublec(j) is true <=> j,(j-1) contain a double consonant. */

  private final boolean doublec(int j)
  {  if (j < 1) return false;
     if (b[j] != b[j-1]) return false;
     return cons(j);
  }

  /* cvc(i) is true <=> i-2,i-1,i has the form consonant - vowel - consonant
     and also if the second c is not w,x or y. this is used when trying to
     restore an e at the end of a short word. e.g.

        cav(e), lov(e), hop(e), crim(e), but
        snow, box, tray.

  */

  private final boolean cvc(int i)
  {  if (i < 2 || !cons(i) || cons(i-1) || !cons(i-2)) return false;
     {  int ch = b[i];
        if (ch == 'w' || ch == 'x' || ch == 'y') return false;
     }
     return true;
  }

  private final boolean ends(String s)
  {  int l = s.length();
     int o = k-l+1;
     if (o < 0) return false;
     for (int i = 0; i < l; i++) if (b[o+i] != s.charAt(i)) return false;
     j = k-l;
     return true;
  }

  /* setto(s) sets (j+1),...k to the characters in the string s, readjusting
     k. */

  private final void setto(String s)
  {  int l = s.length();
     int o = j+1;
     for (int i = 0; i < l; i++) b[o+i] = s.charAt(i);
     k = j+l;
  }

  /* r(s) is used further down. */

  private final void r(String s) { if (m() > 0) setto(s); }

  /* step1() gets rid of plurals and -ed or -ing. e.g.

         caresses  ->  caress
         ponies    ->  poni
         ties      ->  ti
         caress    ->  caress
         cats      ->  cat

         feed      ->  feed
         agreed    ->  agree
         disabled  ->  disable

         matting   ->  mat
         mating    ->  mate
         meeting   ->  meet
         milling   ->  mill
         messing   ->  mess

         meetings  ->  meet

  */

  private final void step1()
  {  if (b[k] == 's')
     {  if (ends("sses")) k -= 2; else
        if (ends("ies")) setto("i"); else
        if (b[k-1] != 's') k--;
     }
     if (ends("eed")) { if (m() > 0) k--; } else
     if ((ends("ed") || ends("ing")) && vowelinstem())
     {  k = j;
        if (ends("at")) setto("ate"); else
        if (ends("bl")) setto("ble"); else
        if (ends("iz")) setto("ize"); else
        if (doublec(k))
        {  k--;
           {  int ch = b[k];
              if (ch == 'l' || ch == 's' || ch == 'z') k++;
           }
        }
        else if (m() == 1 && cvc(k)) setto("e");
    }
  }

  /* step2() turns terminal y to i when there is another vowel in the stem. */

  private final void step2() { if (ends("y") && vowelinstem()) b[k] = 'i'; }

  /* step3() maps double suffices to single ones. so -ization ( = -ize plus
     -ation) maps to -ize etc. note that the string before the suffix must give
     m() > 0. */

  private final void step3() { if (k == 0) return; /* For Bug 1 */ switch (b[k-1])
  {
      case 'a': if (ends("ational")) { r("ate"); break; }
                if (ends("tional")) { r("tion"); break; }
                break;
      case 'c': if (ends("enci")) { r("ence"); break; }
                if (ends("anci")) { r("ance"); break; }
                break;
      case 'e': if (ends("izer")) { r("ize"); break; }
                break;
      case 'l': if (ends("bli")) { r("ble"); break; }
                if (ends("alli")) { r("al"); break; }
                if (ends("entli")) { r("ent"); break; }
                if (ends("eli")) { r("e"); break; }
                if (ends("ousli")) { r("ous"); break; }
                break;
      case 'o': if (ends("ization")) { r("ize"); break; }
                if (ends("ation")) { r("ate"); break; }
                if (ends("ator")) { r("ate"); break; }
                break;
      case 's': if (ends("alism")) { r("al"); break; }
                if (ends("iveness")) { r("ive"); break; }
                if (ends("fulness")) { r("ful"); break; }
                if (ends("ousness")) { r("ous"); break; }
                break;
      case 't': if (ends("aliti")) { r("al"); break; }
                if (ends("iviti")) { r("ive"); break; }
                if (ends("biliti")) { r("ble"); break; }
                break;
      case 'g': if (ends("logi")) { r("log"); break; }
  } }

  /* step4() deals with -ic-, -full, -ness etc. similar strategy to step3. */

  private final void step4() { switch (b[k])
  {
      case 'e': if (ends("icate")) { r("ic"); break; }
                if (ends("ative")) { r(""); break; }
                if (ends("alize")) { r("al"); break; }
                break;
      case 'i': if (ends("iciti")) { r("ic"); break; }
                break;
      case 'l': if (ends("ical")) { r("ic"); break; }
                if (ends("ful")) { r(""); break; }
                break;
      case 's': if (ends("ness")) { r(""); break; }
                break;
  } }

  /* step5() takes off -ant, -ence etc., in context <c>vcvc<v>. */

  private final void step5()
  {   if (k == 0) return; /* for Bug 1 */ switch (b[k-1])
      {  case 'a': if (ends("al")) break; return;
         case 'c': if (ends("ance")) break;
                   if (ends("ence")) break; return;
         case 'e': if (ends("er")) break; return;
         case 'i': if (ends("ic")) break; return;
         case 'l': if (ends("able")) break;
                   if (ends("ible")) break; return;
         case 'n': if (ends("ant")) break;
                   if (ends("ement")) break;
                   if (ends("ment")) break;
                   /* element etc. not stripped before the m */
                   if (ends("ent")) break; return;
         case 'o': if (ends("ion") && j >= 0 && (b[j] == 's' || b[j] == 't')) break;
                                   /* j >= 0 fixes Bug 2 */
                   if (ends("ou")) break; return;
                   /* takes care of -ous */
         case 's': if (ends("ism")) break; return;
         case 't': if (ends("ate")) break;
                   if (ends("iti")) break; return;
         case 'u': if (ends("ous")) break; return;
         case 'v': if (ends("ive")) break; return;
         case 'z': if (ends("ize")) break; return;
         default: return;
      }
      if (m() > 1) k = j;
  }

  /* step6() removes a final -e if m() > 1. */

  private final void step6()
  {  j = k;
     if (b[k] == 'e')
     {  int a = m();
        if (a > 1 || a == 1 && !cvc(k-1)) k--;
     }
     if (b[k] == 'l' && doublec(k) && m() > 1) k--;
  }

  /** Stem the word placed into the Stemmer buffer through calls to add().
   * Returns true if the stemming process resulted in a word different
   * from the input.  You can retrieve the result with
   * getResultLength()/getResultBuffer() or toString().
   */
  public void stem()
  {  k = i - 1;
     if (k > 1) { step1(); step2(); step3(); step4(); step5(); step6(); }
     i_end = k+1; i = 0;
  }  
}

   