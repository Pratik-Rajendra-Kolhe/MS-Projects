import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Characters;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import opennlp.tools.util.InvalidFormatException;


public class WriterXML {
	  public static void processXML(String country, String location, String latlng) throws InvalidFormatException, IOException {
	        String fileName = "C:/Users/Pratik/workspace/NLPTest/locationsXML/"+location+".xml";
	        String rootElement = "place";
	        WriterXML xmlWriter = new WriterXML();
	        Map<String,String> elementsMap = new HashMap<String, String>();
	        elementsMap.put("location",location);
	        elementsMap.put("latlng", latlng);
	        elementsMap.put("country",country);
	        
	        xmlWriter.writeXML(fileName, rootElement, elementsMap);
	         
	    }
	  public static void processXML(String ttl,String timestampFromXml,int idFromXml,String authorFromXml,String textfromXml) throws InvalidFormatException, IOException {
	        String fileName = "C:/Users/Pratik/workspace/NLPTest/wikinewsXML/"+authorFromXml+".xml";
	        String rootElement = "page";
	        WriterXML xmlWriter = new WriterXML();
	        Map<String,String> elementsMap = new HashMap<String, String>();
	        elementsMap.put("timestamp",timestampFromXml);
	        elementsMap.put("text", textfromXml);
	        elementsMap.put("username",authorFromXml);
	        elementsMap.put("title",ttl);
	        xmlWriter.writeXML(fileName, rootElement, elementsMap);
	         
	    }
	     
	    public void writeXML(String fileName, String rootElement, Map<String, String> elementsMap){
	        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
	        try {
	            XMLEventWriter xmlEventWriter = xmlOutputFactory
	                    .createXMLEventWriter(new FileOutputStream(fileName), "UTF-8");
	            XMLEventFactory eventFactory = XMLEventFactory.newInstance();
	            XMLEvent end = eventFactory.createDTD("\n");
	            StartDocument startDocument = eventFactory.createStartDocument();
	            xmlEventWriter.add(startDocument);
	            xmlEventWriter.add(end);
	            StartElement configStartElement = eventFactory.createStartElement("",
	                "", rootElement);
	            xmlEventWriter.add(configStartElement);
	            xmlEventWriter.add(end);
	            // Write the element nodes
	            Set<String> elementNodes = elementsMap.keySet();
	            for(String key : elementNodes){
	                createNode(xmlEventWriter, key, elementsMap.get(key));
	            }
	             
	            xmlEventWriter.add(eventFactory.createEndElement("", "", rootElement));
	            xmlEventWriter.add(end);
	            xmlEventWriter.add(eventFactory.createEndDocument());
	            xmlEventWriter.close();
	 
	        } catch (FileNotFoundException | XMLStreamException e) {
	            e.printStackTrace();
	        }
	    }
	     
	    private static void createNode(XMLEventWriter eventWriter, String element,
	            String value) throws XMLStreamException {
	          XMLEventFactory xmlEventFactory = XMLEventFactory.newInstance();
	          XMLEvent end = xmlEventFactory.createDTD("\n");
	          XMLEvent tab = xmlEventFactory.createDTD("\t");
	          //Create Start node
	          StartElement sElement = xmlEventFactory.createStartElement("", "", element);
	          eventWriter.add(tab);
	          eventWriter.add(sElement);
	          //Create Content
	          Characters characters = xmlEventFactory.createCharacters(value);
	          eventWriter.add(characters);
	          // Create End node
	          EndElement eElement = xmlEventFactory.createEndElement("", "", element);
	          eventWriter.add(eElement);
	          eventWriter.add(end);
	 
	        }
}
