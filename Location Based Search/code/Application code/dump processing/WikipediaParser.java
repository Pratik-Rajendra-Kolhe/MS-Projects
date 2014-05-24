/**
 * 
 */
package wikiparser;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author nikhillo
 * This class implements Wikipedia markup processing.
 * Wikipedia markup details are presented here: http://en.wikipedia.org/wiki/Help:Wiki_markup
 * It is expected that all methods marked "todo" will be implemented by students.
 * All methods are static as the class is not expected to maintain any state.
 */
public class WikipediaParser {
	
	private static final Pattern hasPipePattern = Pattern.compile("(.*?)\\[\\[\\:?(.*?\\:)?(.*?)(\\,.*?| \\(.*?\\))?\\|(.*\\|)?(.*?)\\]\\](.*)");
	private static final Pattern noPipePattern = Pattern.compile("(.*?)\\[\\[(\\:?)(.*?\\:)?(.*?)\\]\\](.*)");
	private static final Pattern externalLinkPattern = Pattern.compile("\\[http.*?\\s(.*?)\\]");
		
	
	/* TODO */
	/**
	 * Method to parse section titles or headings.
	 * Refer: http://en.wikipedia.org/wiki/Help:Wiki_markup#Sections
	 * @param titleStr: The string to be parsed
	 * @return The parsed string with the markup removed
	 * @throws FileNotFoundException 
	 */
	public static String parseSectionTitle(String titleStr)
	{		
		try
		{
			return titleStr.replaceAll("\\=\\=+\\s*(.+?)\\s*\\=\\=+", "");
		}
		catch(NullPointerException e)
		{
			return null;
		}
	}
	
	
	/* TODO */
	/**
	 * Method to parse list items (ordered, unordered and definition lists).
	 * Refer: http://en.wikipedia.org/wiki/Help:Wiki_markup#Lists
	 * @param itemText: The string to be parsed
	 * @return The parsed string with markup removed
	 */
	public static String parseListItem(String itemText) {
		
	    try
	    {
	    	return itemText.replaceAll("(?m)(^\\*+\\s*)(.+?)", "$2").replaceAll("(?m)(^#+\\s*)(.+?)", "$2").replaceAll("(?m)(^;\\s*)(.+?)", "$2").replaceAll("(?m)(^:\\s*)(.+?)", "$2");
	    }
	    catch(NullPointerException e)
	    {
	    	return null;
	    }
	}
	
	/* TODO */
	/**
	 * Method to parse text formatting: bold and italics.
	 * Refer: http://en.wikipedia.org/wiki/Help:Wiki_markup#Text_formatting first point
	 * @param text: The text to be parsed
	 * @return The parsed text with the markup removed
	 */
	public static String parseTextFormatting(String text) {
		try
		{
			return text.replaceAll("(''+\\s*)(.+?)(\\s*''+)", "$2");
		}
		catch(NullPointerException e)
		{
			return null;
		}
	}
	
	/* TODO */
	/**
	 * Method to parse *any* HTML style tags like: <xyz ...> </xyz>
	 * For most cases, simply removing the tags should work.
	 * @param text: The text to be parsed
	 * @return The parsed text with the markup removed.
	 */
	public static String parseTagFormatting(String text) {
		
		try
		{
			return text.replaceAll("(?s)<!--.+?-->", "").replaceAll("&lt;.+?&gt;", "").replaceAll("<.+?>", "").replaceAll("\\s+", " ").trim();
		}
		catch(NullPointerException e)
		{
			return null;
		}
	}
	
	/* TODO */
	/**
	 * Method to parse wikipedia templates. These are *any* {{xyz}} tags
	 * For most cases, simply removing the tags should work.
	 * @param text: The text to be parsed
	 * @return The parsed text with the markup removed
	 */
	public static String parseTemplates(String text) {
		
		try
		{
			char textArray[] = text.toCharArray();
			int len = textArray.length, start = -1, cnt = 0, end = -1;
			for(int i=0;i<len;i++)
			{
				if(textArray[i] == '{')
				{
					if(cnt == 0)
						start = i;
					cnt++;
				}
				else if(textArray[i] == '}')
				{
					if(cnt == 1)
					{
						end = i;
						cnt = 0;
					}
					else if(cnt > 1)
					{
						cnt--;
					}
				}
				if(cnt == 0 && start != end)
				{
					System.arraycopy(textArray, end+1, textArray, start, len - end - 1);
					len = len - end + start - 1;
					i = start - 1;
					start = end = -1;
					cnt = 0;					
				}
			}
			char temp[] = new char[len];
			System.arraycopy(textArray, 0, temp, 0, len);
			return new String(temp);
		}
		catch(NullPointerException e)
		{
			return null;
		}
		
	}
	
	/* TODO */
	/**
	 * Method to parse links and URLs.
	 * Refer: http://en.wikipedia.org/wiki/Help:Wiki_markup#Links_and_URLs
	 * @param text: The text to be parsed
	 * @return An array containing two elements as follows - 
	 *  The 0th element is the parsed text as visible to the user on the page
	 *  The 1st element is the link url
	 * @throws FileNotFoundException 
	 */	
	public static String[] parseLinks(String text)
	{
		String tempStr = null, tempStr2 = null, before = null, after = null, links[] = new String[2];
		Matcher m = null;
		char temp[] = null;
		links[0] = "";
		links[1] = "";
		if(text != null && !text.equals(""))
		{
			
			//has pipe
			if((text.indexOf('|', 0) != -1) && (m = hasPipePattern.matcher(text)).find())
			{
				before = m.group(1);
				after = m.group(7);
				if(((tempStr = m.group(6)) != null) && (!tempStr.equals("")))
				{
					if((tempStr2 = m.group(2)) == null || tempStr2.equals(""))
					{
						temp = m.group(3).toCharArray();
						if(temp[0] >= 'a' && temp[0] <= 'z')
							temp[0] -= 32;
						for(int i=1;i<temp.length;i++)
							if(temp[i] == ' ')
								temp[i] = '_';
						links[1] = new String(temp);
						if((tempStr2 = m.group(4)) != null && !tempStr2.equals(""))
						{							
							temp = tempStr2.toCharArray();
							for(int i=0;i<temp.length;i++)
								if(temp[i] == ' ')
									temp[i] = '_';
							links[1] += new String(temp);
						}
					}
					links[0] = before + tempStr + after;
				}
				else
				{
					tempStr = m.group(3);
					if((tempStr2 = m.group(2)) == null || tempStr2.equals(""))
					{
						temp = tempStr.toCharArray();
						if(temp[0] >= 'a' && temp[0] <= 'z')
							temp[0] -= 32;
						for(int i=1;i<temp.length;i++)
							if(temp[i] == ' ')
								temp[i] = '_';
						links[1] = new String(temp);
						if((tempStr2 = m.group(4)) != null && !tempStr2.equals(""))
						{							
							temp = tempStr2.toCharArray();
							for(int i=0;i<temp.length;i++)
								if(temp[i] == ' ')
									temp[i] = '_';
							links[1] += new String(temp);
						}					
					}	
					else
					{
						if(tempStr.indexOf('#', 0) != -1)
							links[0] = m.group(2);
					}
					links[0] = links[0] + (before + tempStr + after);	
				}
			}
			
			//does not have pipe
			else if((text.indexOf("[[", 0) != -1) && (m = noPipePattern.matcher(text)).find())
			{
				before = m.group(1);
				after = m.group(5);
				if(after.indexOf(after, 0) != -1)
					after = after.replaceAll("\\<nowiki \\/\\>", "");
				if((tempStr = m.group(3)) != null && !tempStr.equals(""))
				{
					if(tempStr.startsWith("Category", 0))
					{
						if((tempStr2 = m.group(2)) == null || tempStr2.equals(""))		
							links[0] = before + m.group(4) + after;
						else
							links[0] = before + tempStr + m.group(4) + after;
					}
					else if(!tempStr.startsWith("File", 0) && !tempStr.startsWith("media", 0))
						links[0] = before + tempStr + m.group(4) + after;
				}
				else
				{
					tempStr = m.group(4);
					links[0] = before + tempStr + after;
					temp = tempStr.toCharArray();
					if(temp[0] >= 'a' && temp[0] <= 'z')
						temp[0] -= 32;
					for(int i=1;i<temp.length;i++)
						if(temp[i] == ' ')
							temp[i] = '_';
					links[1] = new String(temp);
				}
			}
			//external links
			else if((text.indexOf("[http", 0) != -1) && (m = externalLinkPattern.matcher(text)).find())
			{
				if((tempStr = m.group(1)) != null && !tempStr.equals(""))
					links[0] = m.group(1);
			}
			
		}//null check if
		
		return links;
	}

	class XmlElements
	{
		String categories,docText;
	}
	
	public static String removeCrap(String docText)
	{
		return docText.replaceAll("&deg;", " ").replaceAll("&nbsp;", " ").replaceAll("&mdash;", " ").replaceAll("&", " ").replaceAll(" < ", " ").replaceAll("<3", " ").replaceAll("< ", " ").replaceAll(" > ", " ").replaceAll(" >", " ").replaceAll("<table cellpadding.*?font class=\"not_sinopse\">Marily dos Santos", " ").replaceAll("<C", " ").replaceAll("Red = 300.*?<50 and grey = n/a.", " ").replaceAll("__NOTOC__", " ").replaceAll("__noTOC__", " ").replaceAll("__TOC__", " ").replaceAll("__NOINDEX__", " ").replaceAll("__NOEDITSECTION__", " ").replaceAll("_", " ");
	}
	
	public static XmlElements putLinks(String docText, BufferedWriter bw)
	{
		docText = parseSectionTitle(docText);
		docText = removeCrap(docText);
		XmlElements xmlElements = new WikipediaParser().new XmlElements();
		xmlElements.categories = "";
		
		char temp[] = null, textArray[] = docText.toCharArray();
		int start = 0, end = 0, len = textArray.length;
		String tempStrArr[] = null;
		boolean isIndexable = true;
		try
		{			
			for(int i=0;i<len;i++)
			{
				if(textArray[i] == '[')
				{
					if(i>0 && textArray[i-1] == '[')
					{
						if((textArray[i+1] == 'C' && textArray[i+2] == 'a' && textArray[i+3] == 't' && textArray[i+4] == 'e' && textArray[i+5] == 'g' && textArray[i+6] == 'o' && textArray[i+7] == 'r' && textArray[i+8] == 'y' && textArray[i+9] == ':') || (textArray[i+3] == ':'))
							isIndexable = false;
						start = i - 2;
						while(textArray[i+=2] != ']');
						if(textArray[i-1] == ']')
							end = i;
						else
							end = i + 1;
					}
					else if(textArray[i+1] == '[')
					{
						if((textArray[i+2] == 'C' && textArray[i+3] == 'a' && textArray[i+4] == 't' && textArray[i+5] == 'e' && textArray[i+6] == 'g' && textArray[i+7] == 'o' && textArray[i+8] == 'r' && textArray[i+9] == 'y' && textArray[i+10] == ':') || (textArray[i+4] == ':'))
							isIndexable = false;
						start = i - 1;
						while(textArray[i+=2] != ']');
						if(textArray[i-1] == ']')
							end = i;
						else
							end = i + 1;
					}
					else
					{
						start = i - 1;
						while(textArray[++i] != ']');
						end = i;
					}
					
					tempStrArr = parseLinks(new String(textArray, start + 1, end - start));
					if(isIndexable && !tempStrArr[0].equals(""))
					{
						temp = tempStrArr[0].toCharArray();
						System.arraycopy(temp, 0, textArray, end - temp.length + 1, temp.length);
						System.arraycopy(textArray, end - temp.length + 1, textArray, start + 1, len - end + temp.length - 1);
						len = start + len - end + temp.length;
					}
					else
					{
						try
						{
							if(textArray[start+5] != ':' && textArray[start+2] == '[')
								xmlElements.categories += tempStrArr[0]+", ";
						}
						catch(IndexOutOfBoundsException e){e.printStackTrace();}
						System.arraycopy(textArray, end + 1, textArray, start + 1, len - end - 1);
						len = start + len - end;
						isIndexable = true;
					}
					i = start;
				}
			}
		}
		catch(NullPointerException e)
		{
			e.printStackTrace();
		}
		catch(IndexOutOfBoundsException e){e.printStackTrace();}
		xmlElements.docText = new String(textArray, 0, len);
		return xmlElements;				
	}
		
}
