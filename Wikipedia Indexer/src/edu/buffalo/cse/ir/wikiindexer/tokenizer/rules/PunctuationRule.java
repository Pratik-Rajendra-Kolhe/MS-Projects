package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

/**
 * An implementation of the Punctuation Remover
 *
 */
//Annotation
@RuleClass(className = RULENAMES.PUNCTUATION)
public class PunctuationRule implements TokenizerRule {
	public void apply(TokenStream stream) throws TokenizerException {
		try
		{
		if (stream != null) {
			String token;
			while (stream.hasNext()) { 
				token = stream.next();
				if (token != null) {
					// More Punctuation can be added "#$%&()*+,-/:;<=>@[\]^_`{|}~
					token = token.replaceAll("(\\w+)([\\.?!]*)(\\s|$)", "$1$3");
					stream.previous();
					stream.set(token);
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
