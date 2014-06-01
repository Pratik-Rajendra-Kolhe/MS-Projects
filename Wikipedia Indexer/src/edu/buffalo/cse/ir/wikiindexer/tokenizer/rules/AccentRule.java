package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.text.Normalizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

/**
 * An implementation of the Accent Remover
 *
 */

//Annotation
@RuleClass(className = RULENAMES.ACCENTS)
public class AccentRule implements TokenizerRule {
	public void apply(TokenStream stream) throws TokenizerException {
		try
		{
		if (stream != null) {
			String token;
			int n=0;
			while (stream.hasNext()) { 
				token = stream.next();
				if (token != null) 
				{
					token = Normalizer.normalize(token, Normalizer.Form.NFD);
					
					// [All Accents]
					Pattern p1 = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
					Matcher m1 = p1.matcher(token);
					if(m1.find())
					{
						token = token.replaceAll("[\\p{InCombiningDiacriticalMarks}+]", "");
						//token = token.replaceAll("[^\\p{IsCyrillic}\\x00-\\x7F]+", "");
						
					// [If all letters CAPITAL]
					if(token == token.toUpperCase())
					{		
						
					}	
					// [First word of the String]
					else if(n==0)
					{
						
					}
					// [Not the first Word but first character is capital]
					else if(token.charAt(0) == Character.toUpperCase(token.charAt(0)))
					{
						token = token.toLowerCase();
						Character.toUpperCase(token.charAt(0));	
					}
					// [Others]
					else
					{
						token = token.toLowerCase();		
					}
					
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
