/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.wikipedia;


import java.util.regex.*;
/**
 * @author Pratik
 * This class implements Wikipedia markup processing.
 * Wikipedia markup details are presented here: http://en.wikipedia.org/wiki/Help:Wiki_markup
 * It is expected that all methods marked "todo" will be implemented by students.
 * All methods are static as the class is not expected to maintain any state.
 */
public class WikipediaParser {
	
	/**
	 * Method to parse section titles or headings.
	 * Refer: http://en.wikipedia.org/wiki/Help:Wiki_markup#Sections
	 * @param titleStr: The string to be parsed
	 * @return The parsed string with the markup removed
	 */
	public static String parseSectionTitle(String titleStr) {
		if(titleStr!=null)
		{
			titleStr = titleStr.replaceAll("\\s*=+\\s*", "");
			return titleStr;
		}
		else 
			return titleStr;
	}
	
	
	/**
	 * Method to parse list items (ordered, unordered and definition lists).
	 * Refer: http://en.wikipedia.org/wiki/Help:Wiki_markup#Lists
	 * @param itemText: The string to be parsed
	 * @return The parsed string with markup removed 
	 */
	public static String parseListItem(String itemText) {
		if(itemText!=null)
		{
			itemText = itemText.replaceAll("\\*+\\s*", "").replaceAll("\\#+\\s*", "").replaceAll("\\;+\\s*", "").replaceAll("\\:+\\s*", "");
			return itemText;
		}
		else 
			return itemText;	
	}
	
	
	/**
	 * Method to parse text formatting: bold and italics.
	 * Refer: http://en.wikipedia.org/wiki/Help:Wiki_markup#Text_formatting first point
	 * @param text: The text to be parsed
	 * @return The parsed text with the markup removed
	 */
	public static String parseTextFormatting(String text) {
		if(text!=null)
		{

		Matcher m = Pattern.compile("(?<!=)('{2,5}+[\\s]*)([^']+)([\\s]*'{2,5}+)(?!=)").matcher(text);
		while(m.find())
			text = text.replace(m.group(),m.group(2));
		return text;	
		}
		else 
			return text;
	}
	

	/**
	 * Method to parse *any* HTML style tags like: <xyz ...> </xyz>
	 * For most cases, simply removing the tags should work.
	 * @param text: The text to be parsed
	 * @return The parsed text with the markup removed.
	 */
	public static String parseTagFormatting(String text) {
		if(text!=null)
		{	
			if(text.contains("("))
				text = text.replace("(", "");
			if(text.contains(")"))
				text = text.replace(")", "");

			Pattern p1 = Pattern.compile("<ref^<*^>*\\/ref>");
			Matcher m1 = p1.matcher(text);	
			while(m1.find())
				text = text.replaceAll(m1.group(), " ");
				
			text = text.replaceAll("(&lt;)[^&]*(&gt;)","");
			text = text.replaceAll("[\\s]+<[^<>]*>[\\s]+"," ");
			text = text.replaceAll("<[^<>]*>\\s+","");
			text = text.replaceAll("\\s*<[^<>]*>","");		
			return text;
		}
		else 
			return text;
	}
	

	/**
	 * Method to parse wikipedia templates. These are *any* {{xyz}} tags
	 * For most cases, simply removing the tags should work.
	 * @param text: The text to be parsed
	 * @return The parsed text with the markup removed
	 */
	public static String parseTemplates(String text) {
		if(text!=null)
		{
		text = text.replaceAll("\\{\\{[^\\{\\}]*\\}\\}","");
		text = text.replaceAll("\\{\\{[^\\{\\}]*\\}\\}","");
		text = text.replaceAll("\\{\\{[^\\{\\}]*\\}\\}","");
		text = text.replaceAll("\\{\\{[^\\{\\}]*\\}\\}","");
		return text;
		}
		else 
			return text;
	}	
	

	/**
	 * Method to parse links and URLs.
	 * Refer: http://en.wikipedia.org/wiki/Help:Wiki_markup#Links_and_URLs
	 * @param text: The text to be parsed
	 * @return An array containing two elements as follows - 
	 *  The 0th element is the parsed text as visible to the user on the page
	 *  The 1st element is the link url
	 */
	public static String[] parseLinks(String text) {
		String []parsedLink=new String[2];
		parsedLink[0]="";
		parsedLink[1]="";
		

	
		if(text=="" || text==null)
			return parsedLink;
		if(text.contains("<nowiki />"))
		{
			text = text.replaceAll("<nowiki />", "");
		}
		//Pattern1 [[abc]]-----	
		Pattern p1=Pattern.compile("(\\[\\[)([0-9a-zA-Z\\-_\\s'.\\(\\)]+)(\\]\\])");
		Matcher m1=p1.matcher(text);
		while(m1.find())			
		{
			String visibleText=m1.group().substring(2, m1.group().length()-2);
			String link=visibleText.replaceAll("\\s","_");
			parsedLink[0]=text.replaceAll("(\\[\\[)([0-9a-zA-Z\\-_\\s'.\\(\\)]+)(\\]\\])", visibleText);
			char[] stringArray = link.toCharArray();
			stringArray[0] = Character.toUpperCase(stringArray[0]);
			link= new String(stringArray);
			parsedLink[1]=link;
			return parsedLink;
		}
	
		//Pattern2 [[abc|xyz]]		
		Pattern p2=Pattern.compile("\\[\\[([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\s*\\|([0-9a-zA-Z\\-_\\s.'\\(\\)])+\\]\\]");
		Matcher m2=p2.matcher(text);		
        while(m2.find())			
		{
			String visibleText=m2.group().substring(2, m2.group().length()-2).split("\\|")[1];
			String link=m2.group().substring(2, m2.group().length()-2).split("\\|")[0].replaceAll("\\s","_");
			parsedLink[0]=text.replaceAll("\\[\\[([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\s*\\|([0-9a-zA-Z\\-_\\s.'\\(\\)])+\\]\\]", visibleText);
			char[] stringArray = link.toCharArray();
			stringArray[0] = Character.toUpperCase(stringArray[0]);
			link= new String(stringArray);
			parsedLink[1]=link;
			return parsedLink;
		}
			
        //Pattern3 [[abc xyz|]      
    	Pattern p3=Pattern.compile("\\[\\[([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\(([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\)\\s*\\|\\]\\]");
		Matcher m3=p3.matcher(text);		
        while(m3.find())			
		{
			String visibleText=m3.group().substring(2, m3.group().length()-3).split("\\s")[0];
			String link=visibleText+"_"+m3.group().substring(2, m3.group().length()-3).split("\\s")[1].replaceAll("\\s","_");
			parsedLink[0]=text.replaceAll("\\[\\[([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\(([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\)\\s*\\|\\]\\]",visibleText);
			char[] stringArray = link.toCharArray();
			stringArray[0] = Character.toUpperCase(stringArray[0]);
			link= new String(stringArray);
			parsedLink[1]=link;
			return parsedLink;
		}
		
        //Pattern 7 [[abc,xyz|pqr]]
        Pattern p7=Pattern.compile("\\[\\[([0-9a-zA-Z\\-_\\s'.\\(\\)])+,([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\|([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\]\\]");
		Matcher m7=p7.matcher(text);		
        while(m7.find())	
		{
			String visibleText=m7.group().substring(2, m7.group().length()-2).split("\\|")[1];
			String link=m7.group().substring(2, m7.group().length()-2).split("\\|")[0].replaceAll("\\s","_");
			parsedLink[0]=text.replaceAll("\\[\\[([0-9a-zA-Z\\-_\\s'.\\(\\)])+,([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\|([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\]\\]", visibleText);
			char[] stringArray = link.toCharArray();
			stringArray[0] = Character.toUpperCase(stringArray[0]);
			link= new String(stringArray);
			parsedLink[1]=link;
			return parsedLink;
		}
		
        //Pattern 4 [[abc, xyz|]]     
		Pattern p4=Pattern.compile("\\[\\[([0-9a-zA-Z\\-_\\s'.\\(\\)])+,([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\|\\]\\]");
		Matcher m4=p4.matcher(text);	
	    while(m4.find())	
	    {
	    	String visibleText=m4.group().substring(2, m4.group().length()-3).split(",")[0];
	    	String link=m4.group().substring(2, m4.group().length()-3).replaceAll("\\s","_");
	    	parsedLink[0]=text.replaceAll("\\[\\[([0-9a-zA-Z\\-_\\s'.\\(\\)])+,([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\|\\]\\]", visibleText);
	    	char[] stringArray = link.toCharArray();
			stringArray[0] = Character.toUpperCase(stringArray[0]);
			link= new String(stringArray);
			parsedLink[1]=link;
			return parsedLink;
		}

	    //Pattern 5 [[Wikipedia:abc|]]	        
	    Pattern p5=Pattern.compile("\\[\\[\\s*Wikipedia\\s*:([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\|\\]\\]");
	    Matcher m5=p5.matcher(text); 			
	    while(m5.find())
	    {	
	    	String visibleText=m5.group().substring(2, m5.group().length()-3).split(":")[1];
	        if(visibleText.contains("("))
	        {
	        	visibleText=visibleText.split("\\(")[0];
	        	visibleText=visibleText.trim();
	        }
			parsedLink[0]=text.replaceAll("\\[\\[\\s*Wikipedia\\s*:([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\|\\]\\]", visibleText);
			parsedLink[1]="";
			return parsedLink;
	    }
	   
	    //Pattern 6 [[Category:abc]]	        
	    Pattern p6=Pattern.compile("\\[\\[Category:([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\s*\\]\\]");
		Matcher m6=p6.matcher(text);  			
		while(m6.find())    				
		{
			String visibleText=m6.group().substring(2, m6.group().length()-2).split(":")[1];
			parsedLink[0]=text.replaceAll("\\[\\[Category:([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\s*\\]\\]", visibleText);
			parsedLink[1]="";
			return parsedLink;
		}
		
		//Pattern 8 [[:Category:abc]]	        
		Pattern p8=Pattern.compile("\\[\\[:Category:([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\s*\\]\\]");
		Matcher m8=p8.matcher(text);   			
		while(m8.find())
		{
			String visibleText=m8.group().substring(3, m8.group().length()-2);
			parsedLink[0]=text.replaceAll("\\[\\[:Category:([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\s*\\]\\]", visibleText);
			parsedLink[1]="";
			return parsedLink;
		}
				    		
    	//Pattern 9 [[:Category:abc|]]	        
		Pattern p9=Pattern.compile("\\[\\[:Category:([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\s*\\|\\]\\]");
		Matcher m9=p9.matcher(text);  			
		while(m9.find())			
		{
			String visibleText=m9.group().substring(3, m9.group().length()-3).split(":")[1];
			String link="";
			parsedLink[0]=text.replaceAll("\\[\\[:Category:([0-9a-zA-Z\\-_\\s'.\\(\\)])+\\s*\\|\\]\\]", visibleText);
			parsedLink[1]=link;
			return parsedLink;
		}
		
		//Pattern 10[[Wiktionary:abc]]
		Pattern p10=Pattern.compile("\\[\\[Wiktionary:([0-9a-zA-Z\\-_\\s'.:\\(\\)])+\\s*\\|*\\]\\]");
		Matcher m10=p10.matcher(text);	    			
		while(m10.find())	    				
		{    			   
			//System.out.println("IN");
			String visibleText=m10.group().substring(2, m10.group().length()-2);
			if(visibleText.contains("|"))
				{
					visibleText=m10.group().substring(2, m10.group().length()-3);
				}
			String tempText=visibleText.replaceAll("Wiktionary:", "");
			if(tempText.contains(":"))
				visibleText=visibleText.replaceAll("Wiktionary:", "");
			parsedLink[0]=text.replaceAll("\\[\\[Wiktionary:([0-9a-zA-Z\\-_\\s'.:\\(\\)])+\\s*\\|*\\]\\]", visibleText);
			parsedLink[1]="";
			return parsedLink;
		}
				      
		//Pattern 11[[abc:xyz:pqr|]]
		Pattern p11=Pattern.compile("\\[\\[[^(\\[\\])]+\\]\\]");
		Matcher m11=p11.matcher(text);
		while(m11.find())    				
		{					    	    			    	    
			String visibleText=m11.group().substring(2, m11.group().length()-2);	
			if(visibleText.contains("|"))
			{
				visibleText=visibleText.split("\\|")[visibleText.split("\\|").length-1];
			}
			else if(visibleText.contains(":") && visibleText.split(":")[0].length()==2)
			{		
			}
			else 
				visibleText="";
			

			parsedLink[0]=visibleText;
			parsedLink[1]="";
			return parsedLink;
		}
		
		//Pattern 12[external links]
		Pattern p12=Pattern.compile("\\[[^(\\[\\])]+\\]");
		Matcher m12=p12.matcher(text);
		while(m12.find())
		{
			String visibleText=m12.group().substring(2, m12.group().length()-1);
			visibleText=visibleText.trim();
			if(visibleText.split("\\s").length==2)
			{
				visibleText=visibleText.split("\\s")[1];
			}
			else
				visibleText="";
			
		parsedLink[0]=text.replaceAll("\\[[^(\\[\\])]+\\]", visibleText);
		parsedLink[1]="";
		return parsedLink;
		}
		
		return parsedLink;		
	}

	
	public static WikipediaDocument parseWiki(String text,WikipediaDocument wikidoc){
		int i=0;	
		int j=0;
		
		text=text.replaceAll("\\s+"," ");
		text=text.replaceAll("<", " <");
		text=text.replaceAll(">", "> ");
		text=text.replaceAll("&gt;", "&gt; ");
		text=text.replaceAll("&lt;", " &gt;");
		text=text.replaceAll("\\s+", " ");
		//-----Links and Categories parsing and LangLinks---Start				
		Pattern p=Pattern.compile("\\[\\[[^\\[\\]]+\\]\\]");
		Matcher m=p.matcher(text);
		while(m.find())
		{
			String[] parsedLink=parseLinks(m.group());	
			if(m.group().contains("Category"))
			{
				wikidoc.addCategory(parsedLink[0]);
				text=text.replace(m.group()," ");
			}
			else if(parsedLink[0].contains(":") && parsedLink[0].split(":")[0].length()==2)
			{
				wikidoc.addLangLink(parsedLink[0].split(":")[0],parsedLink[0].split(":")[0]);
				text=text.replace(m.group()," ");
			}
			else
			{	
				wikidoc.addLink(parsedLink[1]);
				text=text.replace(m.group(),parsedLink[0]);
			}	
		}
		
		Pattern p2=Pattern.compile("\\[[^\\[\\]]+\\]");
		Matcher m2=p2.matcher(text);
		while(m2.find())
		{
	       	String[] parsedLink=parseLinks(m2.group());	
	       	if(parsedLink[0].length()!=0)
	       	{
	       		wikidoc.addLink(parsedLink[0]);
	       		text=text.replace(m2.group()," ");
	       	}
	       	else
	       		text=text.replace(m2.group()," ");
		}	
        //-------Links and categories parsing---end
		
		//------- Section title and text parsing---Start
		text=parseTemplates(text);
		text=parseTextFormatting(text);
		text=parseListItem(text);
		text=parseTagFormatting(text);
	
		String[] sectexts = text.split("(?<!=)(={2,6}+[\\s]*)([^=]+)([\\s]*={2,6}+)(?!=)");
		String[] sectitles=new String[sectexts.length];
		
		Pattern p1=Pattern.compile("(?<!=)(={2,6}+[\\s]*)([^=]+)([\\s]*={2,6}+)(?!=)");
		Matcher m1=p1.matcher(text);		
		i=0;
		while(m1.find()){  
			sectitles[i]=parseSectionTitle(m1.group());		
			i++;
		}
		
		if(sectitles[sectitles.length-1]==null)
		{
			wikidoc.addSection("Default", sectexts[0]);
			for(i=0,j=1;i<=sectitles.length-1 && j<=sectitles.length-1;i++,j++)
			{
				wikidoc.addSection(sectitles[i], sectexts[j]);
			}
		}
		//-----Section title and text parsing---end	

		return wikidoc;		
	}
}
