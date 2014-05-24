package wikiparser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.NoSuchElementException;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

public class Parser
{
	public static void main(String[] args)
	{
		long t = System.currentTimeMillis();
		XMLInputFactory xif= XMLInputFactory.newFactory();
		XMLStreamReader xsr=null;
		String docText=null,timestamp=null,title=null;
		File f = new File(args[1]);
		boolean isRedirect = false;
		int inputDocCount = 0,outputDocCount = 0;
		WikipediaParser.XmlElements xmlElements = null;
		if(f.exists())
			f.delete();
		try(BufferedReader br=new BufferedReader(new InputStreamReader(new FileInputStream(new File(args[0])), "UTF-8"),16384);BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File(args[1])), "UTF-8"))) 
		{
			xsr= xif.createXMLStreamReader(br);
			
			bw.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n<docs>\n");
			while(xsr.hasNext())
			{
				try
				{
				while(!(xsr.next()==XMLStreamConstants.START_ELEMENT && xsr.getLocalName().equals("timestamp")));				
				timestamp = xsr.getElementText();			
				inputDocCount++;
				while(!(xsr.next()==XMLStreamConstants.START_ELEMENT && xsr.getLocalName().equals("text")));
				docText = xsr.getElementText();
				String leftOver=WikipediaParser.parseTextFormatting(WikipediaParser.parseTagFormatting(WikipediaParser.parseTemplates(WikipediaParser.parseListItem(docText))));
				if(leftOver.startsWith("REDIRECT"))
				{
					leftOver = leftOver.replaceFirst("REDIRECT", "");
					isRedirect = true;
				}
				else if(leftOver.startsWith("redirect"))
				{
					leftOver = leftOver.replaceFirst("redirect", "");
					isRedirect = true;
				}
				if(leftOver.length()>400 || isRedirect)
				{
					xmlElements = WikipediaParser.putLinks(leftOver, bw);					
					
					while(!(xsr.next()==XMLStreamConstants.START_ELEMENT && xsr.getLocalName().equals("title")));
					title = xsr.getElementText();
						
					if(!title.toLowerCase().startsWith("news brief"))
					{
						bw.append("<page>\n\t<timestamp>"+timestamp+"</timestamp>\n");
						bw.append("\t<categories>"+xmlElements.categories+"</categories>\n");
						bw.append("\t<text>"+xmlElements.docText+"</text>\n");
						bw.append("\t<summary>"+(xmlElements.docText.length()>300?xmlElements.docText.substring(0, 300):xmlElements.docText)+"</summary>\n");
						bw.append("\t<title>"+title+"</title>\n");
						bw.append("</page>\n\n");
						outputDocCount++;
					}					
				}
				isRedirect = false;
				}
				catch(NoSuchElementException e)
				{}
			}
			
			bw.append("</docs>");

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (NoSuchElementException e){
						System.out.println("EOF");			
		} catch (XMLStreamException e) {
			e.printStackTrace();
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		finally
		{
			try {
				if(xsr != null)
					xsr.close();
			} catch (XMLStreamException e) {
				e.printStackTrace();
			}
		}
		System.out.println(System.currentTimeMillis() - t);
		System.out.println("InputDocCouunt:"+inputDocCount);
		System.out.println("OutputDocCouunt:"+outputDocCount);
	}
}
