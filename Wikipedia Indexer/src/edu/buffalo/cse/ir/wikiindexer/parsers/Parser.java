/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.parsers;

import java.io.File;
import java.util.Collection;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.buffalo.cse.ir.wikiindexer.wikipedia.WikipediaDocument;
import edu.buffalo.cse.ir.wikiindexer.wikipedia.WikipediaParser;

/**
 * @author Pratik
 *
 */
public class Parser {
	/* */
	private final Properties props;
	
	/**
	 * 
	 * @param idxConfig
	 * @param parser
	 */
	 
	public Parser(Properties idxProps) {
			props = idxProps;
	}
	

	/**
	 * 
	 * @param filename
	 * @param docs
	 */
	public void parse(String filename, Collection<WikipediaDocument> docs) {	
		try{
			if(filename == null)
			{
				
			}
			else if(filename == "")
			{
				
			}		
			else if(new File(filename).createNewFile())
			{
				new File(filename).delete();
			}
			else
			{
				File xmlFile = new File(filename);
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document xml = dBuilder.parse(xmlFile);
 
				xml.getDocumentElement().normalize();
	
				NodeList nListPage = xml.getElementsByTagName("page");
				NodeList nListRevision=xml.getElementsByTagName("revision");
				NodeList nListContributor=xml.getElementsByTagName("contributor");
 	 
			for (int i=0,j=0,k=0; i < nListPage.getLength() && j < nListRevision.getLength() && k < nListContributor.getLength(); i++,j++,k++)
			{
				int idFromXml=0;
				String timestampFromXml=new String();
				String authorFromXml=new String();
				String ttl=new String();
				String textfromXml=new String();
		
				Node nNodePage = nListPage.item(i);
				Node nNodeRevision=nListRevision.item(j);
				Node nNodeContributor=nListContributor.item(k);

				if (nNodePage.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element e = (Element) nNodePage;
					if(e.getElementsByTagName("title").item(0)!=null)
						ttl=e.getElementsByTagName("title").item(0).getTextContent();
					if(e.getElementsByTagName("id").item(0)!=null)
						idFromXml=Integer.parseInt(e.getElementsByTagName("id").item(0).getTextContent());    
				}
				
				if (nNodeRevision.getNodeType() == Node.ELEMENT_NODE) 
				{
					Element e = (Element) nNodeRevision;
					if(e.getElementsByTagName("timestamp").item(0)!=null)
						timestampFromXml=e.getElementsByTagName("timestamp").item(0).getTextContent();
					if(e.getElementsByTagName("text").item(0)!=null)
						textfromXml=e.getElementsByTagName("text").item(0).getTextContent();
				}
	
				if (nNodeContributor.getNodeType() == Node.ELEMENT_NODE) 
				{	
					Element e = (Element) nNodeContributor;
					if(e.getElementsByTagName("username").item(0)!=null)
						authorFromXml=e.getElementsByTagName("username").item(0).getTextContent();
					else if(e.getElementsByTagName("ip").item(0)!=null)
						authorFromXml=e.getElementsByTagName("ip").item(0).getTextContent();
				}

				WikipediaDocument wikiDoc=new WikipediaDocument(idFromXml,timestampFromXml,authorFromXml,ttl);

				//To Parse Wiki Pages---
				wikiDoc=WikipediaParser.parseWiki(textfromXml,wikiDoc);
				add(wikiDoc, docs);
			}
		}
	}
	catch(Exception e)
	{
		
	}	
}

	/**
	 * Method to add the given document to the collection.
	 * @param doc: The WikipediaDocument to be added
	 * @param documents: The collection of WikipediaDocuments to be added to
	 */
	private synchronized void add(WikipediaDocument doc, Collection<WikipediaDocument> documents) {
		documents.add(doc);
	}
}