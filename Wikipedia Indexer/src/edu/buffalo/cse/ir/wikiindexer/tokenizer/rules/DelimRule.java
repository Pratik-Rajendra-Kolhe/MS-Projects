package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

/**
 * An implementation of the Delimiter
 *
 */

//Annotation
//Replace \\s by Delimiter
@RuleClass(className = RULENAMES.DELIM)
public class DelimRule implements TokenizerRule {
	public void apply(TokenStream stream) throws TokenizerException {
		if (stream != null) {
			String token;
			String[] token1;
			while (stream.hasNext()) { 
				token = stream.next();
				if (token != null) {
					// [Delimiter before word]
					Pattern p1 = Pattern.compile("^([\\s]+)");
					Matcher m1 = p1.matcher(token);
					if(m1.find())
					{
						token = token.replace(m1.group(1), "");
					}
					// [Delimiter after word]
					Pattern p2 = Pattern.compile("([\\s]+)$");
					Matcher m2 = p2.matcher(token);
					if(m2.find())
					{
						token = token.replace(m1.group(1), "");
					}
					token1 = token.split("\\s+");
					stream.previous();
					stream.set(token1);
					stream.next();
				}
			}
			stream.reset();
		}
	}
}