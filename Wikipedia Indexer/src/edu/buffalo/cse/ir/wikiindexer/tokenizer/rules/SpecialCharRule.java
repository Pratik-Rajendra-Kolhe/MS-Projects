package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

/**
 * An implementation of the Punctuation Remover
 *
 */

//Annotation
@RuleClass(className = RULENAMES.SPECIALCHARS)
public class SpecialCharRule implements TokenizerRule {
	public void apply(TokenStream stream) throws TokenizerException {
		try
		{
		if (stream != null) {
			String token;
			String[] token1;
			while (stream.hasNext()) { 
				token = stream.next();
				if (token != null) {
					token = token.replaceAll(" ", "");
					// [Remove \ ]
					if(token.contains("\\"))
					{
						token = token.replace("\\", "");
					}	
					// [Tokens with only special characters]
					Pattern p4 = Pattern.compile("^([\\~\\(\\)\\@\\#\\$\\%\\^\\*\\=\\&\\+\\:\\<\\>\\|\\_\\/\\]\\;\\,\"\\”\\“\\’\\‘\\{\\}]+)$");
					Matcher m4 = p4.matcher(token);
					if(m4.find())
					{
						stream.previous();
						stream.remove();
					}
					// [Tokens with Special Char in the beginning]
					Pattern p5 = Pattern.compile("^([\\~\\(\\)\\@\\#\\$\\%\\^\\*\\=\\&\\+\\:\\<\\>\\|\\_\\/\\]\\;\\,\"\\”\\“\\’\\‘\\{\\}]+)(.)*");
					Matcher m5 = p5.matcher(token);
					if(m5.find())
					{
						token = token.replaceAll("[\\~\\(\\)\\@\\#\\$\\%\\^\\*\\=\\&\\+\\:\\<\\>\\|\\_\\/\\]\\;\\,\"\\”\\“\\’\\‘\\{\\}]", "");
						stream.previous();
						stream.set(token);
						stream.next();
					}
					//[Tokens with Special Char in the end]
					Pattern p6 = Pattern.compile("([\\~\\(\\)\\@\\#\\$\\%\\^\\*\\=\\&\\+\\:\\<\\>\\|\\_\\/\\]\\;\\,\"\\”\\“\\’\\‘\\{\\}]+)$");
					Matcher m6 = p6.matcher(token);
					if(m6.find())
					{
						token = token.replaceAll("[\\~\\(\\)\\@\\#\\$\\%\\^\\*\\=\\&\\+\\:\\<\\>\\|\\_\\/\\]\\;\\,\"\\”\\“\\’\\‘\\{\\}]", "");
						stream.previous();
						stream.set(token);
						stream.next();
					}
					// [Others]
					token = token.replaceAll("[\\~\\(\\)\\@\\#\\$\\%\\^\\*\\=\\&\\+\\:\\<\\>\\|\\_\\/\\]\\;\\,\"\\”\\“\\’\\‘\\{\\}]+", " ");
					token1 = token.split("\\s");
					if(token=="")
					{	
						stream.previous();
						stream.remove();
					}
					if(token1.length != 0)
					{
						stream.previous();
						stream.set(token1);
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
