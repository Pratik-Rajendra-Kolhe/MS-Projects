import java.io.IOException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import opennlp.tools.util.InvalidFormatException;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class ReadXML {
	public static void main(String[] args) {	
		try{
						
			String filename="C:/Users/Pratik/workspace/IRProject-2/wikinews/data/wikinews.xml";
			SAXParserFactory factory = SAXParserFactory.newInstance();
						SAXParser saxParser = factory.newSAXParser();
					 
					DefaultHandler handler = new DefaultHandler() {
					StringBuilder str=new StringBuilder();
					boolean title = false;
					boolean id=false;
				    boolean time=false;
				    boolean auth=false;
				    boolean text=false;
				    int idFromXml=0;
					String timestampFromXml=new String();
					String authorFromXml=new String();
					String ttl=new String();
					String textfromXml=new String();
	
					public void startElement(String uri, String localName,String qName, 
				                Attributes attributes) throws SAXException {
				        if (qName.equalsIgnoreCase("TITLE")) {
							title = true;
						}
						if (qName.equalsIgnoreCase("TIMESTAMP")) {
							time = true;
						}
						if (qName.equalsIgnoreCase("TEXT")) {
							text = true;
						}
						if (qName.equalsIgnoreCase("PARENTID")) {
							id = true;
						}
						if (qName.equalsIgnoreCase("USERNAME")) {
							auth = true;
						}
	
					}
				 
					public void endElement(String uri, String localName,
						String qName) throws SAXException {
						 
						if (qName.equalsIgnoreCase("TITLE")){
								title = false;
								
								
						}
				 
						if (qName.equalsIgnoreCase("USERNAME")){
							
							auth = false;
						
						}
						if (qName.equalsIgnoreCase("PARENTID")){
							
							id = false;
							
						}
						if (qName.equalsIgnoreCase("TIMESTAMP")){
							
							time = false;
							
						}
						if (qName.equalsIgnoreCase("TEXT")){
						
							text = false;

						 str=new StringBuilder();
						 if(!ttl.contains("Wiki") && !ttl.contains("Category") &&!ttl.contains("Template") && !ttl.contains("Help") && !ttl.contains("File") && !ttl.contains("Thread") && !ttl.contains("Comments"))
							try {
								WriterXML.processXML(ttl, timestampFromXml, idFromXml, authorFromXml, textfromXml);
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

//						 
						}
					}
				 
					
					public void characters(char ch[], int start, int length) throws SAXException {
						
						
						if (title){
				         ttl=new String(ch, start, length);
							
						}
				 
						if (auth){
							authorFromXml=new String(ch, start, length);
							
						}
						if (id){
	
							idFromXml=Integer.parseInt(new String(ch, start, length));
							
						}
						if (time){
							timestampFromXml=new String(ch, start, length);
						}
						if (text){
						   for(int i=start;i<start+length;i++)
							   str.append(ch[i]);
							textfromXml=str.toString();
						
						}
				 
					}
				 
					
				 
				 };
				 
				       saxParser.parse(filename, handler);
				
			  } 
			catch (Exception e) {
			e.printStackTrace();
			}
			

	}
}