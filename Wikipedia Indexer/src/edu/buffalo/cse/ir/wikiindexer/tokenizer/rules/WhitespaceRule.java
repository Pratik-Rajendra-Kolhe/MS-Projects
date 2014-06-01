package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

/**
 * An implementation of the Whitespace Remover
 *
 */

//Annotation
@RuleClass(className = RULENAMES.WHITESPACE)
public class WhitespaceRule implements TokenizerRule {
	public void apply(TokenStream stream) throws TokenizerException {
		try
		{
		if (stream != null) {
			String token;
			String[] token1;
			while (stream.hasNext()) { 
				token = stream.next();
				if (token != null) {
					token = token.trim().replaceAll("\\s+", " ");
					token1 = token.split("\\s+");
					stream.previous();
					stream.set(token1);
					stream.next();
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
