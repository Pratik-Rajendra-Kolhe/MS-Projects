package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

/**
 * An implementation of the Hyphen Remover
 *
 */

//Annotation
@RuleClass(className = RULENAMES.HYPHEN)
public class HyphenRule implements TokenizerRule {
	public void apply(TokenStream stream) throws TokenizerException {
		try
		{
		if (stream != null) {
			String token;
			String[] token1;
			String s = null;
			while (stream.hasNext()) { 
				token = stream.next();	
				if(token != null){
					if(token.contains("-"))
					{
						token = token.replaceAll("[-]+", "-");
						// --C
						Pattern p1 = Pattern.compile("^([-]+)(\\w)");
						Matcher m1 = p1.matcher(token);
						if(m1.find())
						{
							token = token.replace(m1.group(),m1.group(2));
						}
						// C--
						Pattern p2 = Pattern.compile("(\\w)([-]+)$");
						Matcher m2 = p2.matcher(token);
						if(m2.find())
						{
							token = token.replace(m2.group(),m2.group(1));
						}
						//ABC - ABC
						Pattern p3 = Pattern.compile("^\\s*-+\\s*$");
						Matcher m3 = p3.matcher(token);
						if(m3.find())
						{
						stream.previous();
						stream.remove();
						}
						// [123-ABC ABC-123 123-123]
						else
						{
							token1 = token.split("\\-+");
							s = token.replaceAll("-+", " ");
						
							for(int i=0;i<=token1.length-1;i++)
							{
								Pattern p = Pattern.compile("[0-9]+");
								Matcher m = p.matcher(token1[i]);
								if(m.find())
								{
									s = token;
								}
							}
							stream.previous();
							stream.set(s);
							stream.next();
						}
					}
					else
					{
						stream.previous();
						stream.set(token);
						stream.next();
					}
				}
			}
			stream.reset();
		}
	}
		catch(Exception e)
		{
		}
	}
}
