package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

/**
 * An implementation of the NUMBER Remover
 *
 */

//Annotation
@RuleClass(className = RULENAMES.NUMBERS)
public class NumberRule implements TokenizerRule {
	public void apply(TokenStream stream) throws TokenizerException {
		try
		{
		if (stream != null) {
			String token;
			while (stream.hasNext()) { 
				token = stream.next();
				if (token != null) {	
					Pattern p3 = Pattern.compile("([0-9]{1,2})([\\:])([0-9]{2})([\\:])([0-9]{2})");
					Matcher m3 = p3.matcher(token);
						if(m3.find())
						{
							if(token.contains(" "))
							{
								token=token.replace(" ", "");
							}
							stream.previous();
							stream.set(token);
							stream.next();
						}
						else
						{
						Pattern p2 = Pattern.compile("[0-9]{8}");
						Matcher m2 = p2.matcher(token);
							if(m2.find())
							{
								if(token.contains(" "))
								{
									token=token.replace(" ", "");
								}
								stream.previous();
								stream.set(token);
								stream.next();
							}
							else
							{
								Pattern p1 = Pattern.compile("^[0-9]+[\\/,\\.]*");
								Matcher m1 = p1.matcher(token);
								if(m1.find())
								{	
									token = token.replaceAll("[0-9,\\.]+", "");
									if(token.length()==0)
									{
										stream.previous();
										stream.remove();
									}
									else
									{
										stream.previous();
										stream.set(token);
										stream.next();
									}	
								}
							}
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
