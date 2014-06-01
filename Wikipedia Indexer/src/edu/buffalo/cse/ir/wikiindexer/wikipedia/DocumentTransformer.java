/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.wikipedia;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import edu.buffalo.cse.ir.wikiindexer.indexer.INDEXFIELD;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.Tokenizer;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.wikipedia.WikipediaDocument.Section;

/**
 * A Callable document transformer that converts the given WikipediaDocument object
 * into an IndexableDocument object using the given Tokenizer
 * @author Pratik
 *
 */
public class DocumentTransformer implements Callable<IndexableDocument> {
	
	private Map<INDEXFIELD,Tokenizer> tknizerMap;
	private WikipediaDocument doc;
	
	/**
	 * Default constructor, DO NOT change
	 * @param tknizerMap: A map mapping a fully initialized tokenizer to a given field type
	 * @param doc: The WikipediaDocument to be processed
	 */
	public DocumentTransformer(Map<INDEXFIELD, Tokenizer> tknizerMap, WikipediaDocument doc) {
		
		this.tknizerMap=tknizerMap;
		this.doc=doc;
	}
	
	/**
	 * Method to trigger the transformation
	 * @throws TokenizerException Inc ase any tokenization error occurs
	 */
	public IndexableDocument call() throws TokenizerException {
		
		String author=doc.getAuthor();
		String title=doc.getTitle();
		Integer id=doc.getId();
		Date publishedDate=doc.getPublishDate();
		
		List<Section> sections=doc.getSections();
		Set<String> links=doc.getLinks();
		Map<String,String> langLinks=doc.getLangLinks();
		List<String> categories=doc.getCategories();
			
		IndexableDocument idxDoc= new IndexableDocument(title);	
		Tokenizer authorTokenizer=tknizerMap.get(INDEXFIELD.AUTHOR);
		Tokenizer termTokenizer=tknizerMap.get(INDEXFIELD.TERM);
		Tokenizer linkTokenizer=tknizerMap.get(INDEXFIELD.LINK);
		Tokenizer categoryTokenizer=tknizerMap.get(INDEXFIELD.CATEGORY);
		
//------Tokenize Author----------------------------------------------------------------------	
	    TokenStream	stream1=new TokenStream(author);
		authorTokenizer.tokenize(stream1);	
		idxDoc.addField(INDEXFIELD.AUTHOR, stream1);
		
//------Tokenize Sections and Text-----------------------------------------------------------------		
		Iterator<Section> si=sections.iterator();	   
		while(si.hasNext())
		{     
			Section s=si.next();	
			String sec=s.getTitle();
			String text=s.getText();
			   
			TokenStream stream2=new TokenStream(sec);
			stream2.append(text);
			termTokenizer.tokenize(stream2);
 			idxDoc.addField(INDEXFIELD.TERM, stream2);
		}
		 
//-------Tokenize Links-------------------------------------------------------------------		
		Iterator<String> li=links.iterator();
		while(li.hasNext())
		{
		String link=li.next();  
		TokenStream stream3=new TokenStream(link);   
		linkTokenizer.tokenize(stream3);	 		   
		idxDoc.addField(INDEXFIELD.LINK, stream3);
		}
			
//---------Tokenize Categories---------------------------------------------------------------------------------------------
		Iterator<String> ci=categories.iterator();
		while(ci.hasNext())
		{
			String category=ci.next();					   
			TokenStream stream4=new TokenStream(category); 
			
			categoryTokenizer.tokenize(stream4);
			idxDoc.addField(INDEXFIELD.CATEGORY, stream4);  
		}
				  
//-------------------------------------------------------------------------------------------------------         		
		return idxDoc;
	}
}