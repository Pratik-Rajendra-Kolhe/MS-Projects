package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

/**
 * An implementation of the LowerCase Converter
 *
 */

//Annotation
@RuleClass(className = RULENAMES.CAPITALIZATION)
public class CapitalizationRule implements TokenizerRule {
	public void apply(TokenStream stream) throws TokenizerException {
		try
		{
		if (stream != null) {
			String token;
			String token1;
			int n=0;
			while (stream.hasNext()) { 
				token = stream.next();
				if (token != null) {
					// [If all letters are CAPITAL]
					if(token == token.toUpperCase()){		
						stream.previous();
						stream.set(token);
						stream.next();
						}
					else
					{
						// [If it is the first word]
						if(n==0)
						{
							stream.previous();
							stream.set(token.toLowerCase());
							stream.next();
						}
						// [If it is not the first word and first letter is capital, Also Combine if next word has first letter capital]
						else if(token.charAt(0) == Character.toUpperCase(token.charAt(0)))
						{
							if(stream.hasNext())
							{
								token1 = stream.next();
								if(token1.charAt(0) == Character.toUpperCase(token1.charAt(0)))
								{
									stream.previous();
									stream.remove();
									stream.previous();
									stream.set(token+" "+token1);
									stream.next();
								}
								else
								{
									stream.previous();
									stream.previous();
									stream.set(token);
									stream.next();
								}
							}
						}
						// [Others]
							else
							{
								stream.previous();
								stream.set(token);
								stream.next();
							}
						}	
					
					}
				n++;
			}
			stream.reset();
		}
	}
		catch(Exception e)
		{
		}
	}
}
