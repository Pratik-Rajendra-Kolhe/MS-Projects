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
@RuleClass(className = RULENAMES.APOSTROPHE)
public class ApostropheRule implements TokenizerRule {
	public void apply(TokenStream stream) throws TokenizerException {
		try
		{
		if (stream != null) {
			String token;
			String token1[];
			String [] o = {"ain't","aren't","can't","can't've","'cause","could've","couldn't","couldn't've","didn't","doesn't","don't","hadn't","hadn't've","hasn't","haven't","he'd","he'd've","he'll","he'll've","he's","how'd","how'd'y","how'll","how's","I'd","I'd've","I'll","I'll've","I'm","I've","isn't","it'd","it'd've","it'll","it'll've","it's","let's","ma'am","mayn't","might've","mightn't","mightn't've","must've","mustn't","mustn't've","needn't","needn't've","o'clock","oughtn't","oughtn't've","shan't","sha'n't","shan't've","she'd","she'd've","she'll","she'll've","she's","should've","shouldn't","shouldn't've","so've","so's","that'd","that'd've","that's","there'd","there'd've","there's","they'd","they'd've","they'll","they'll've","they're","they've","to've","wasn't","we'd","we'd've","we'll","we'll've","we're","we've","weren't","what'll","what'll've","what're","what's","what've","when's","when've","where'd","where's","where've","who'll","who'll've","who's","who've","why's","why've","will've","won't","won't've","would've","wouldn't","wouldn't've","y'all","y'all'd","y'all'd've","y'all're","y'all've","you'd","you'd've","you'll","you'll've","you're","you've","'em"};
			String [] r = {"am not","are not","cannot","cannot have","because","could have","could not","could not have","did not","does not","do not","had not","had not have","has not","have not","he would","he would have","he will","he will have","he is","how did","how do you","how will","how is","I would","I would have","I will","I will have","I am","I have","is not","it would","it would have","it will","it will have","it is","let us","madam","may not","might have","might not","might not have","must have","must not","must not have","need not","need not have","of the clock","ougth not","ought not have","shall not","shall not","shall not have","she would","she would have","she will","she will have","she is","should have","should not","should not have","so have","so is","that would","that would have","there is","that is","there would have","there is","they would","they would have","they will","they will have","they are","they have","to have","was not","we would","we would have","we will","we will have","we are","we have","were not","what will","what will have","what are","what is","what have","when is","when have","where did","where is","where have","who will","who will have","who is","who have","why is","why have","will have","will not","will not have","would have","would not","would not have","you all","you all would","you all would have","you all are","you all have","you would","you would have","you will","you will have","you are","you have","them"};
			while (stream.hasNext()) { 
				token = stream.next();
				if (token != null) {
					// [Contractions]
					for(int i=0; i <= o.length-1; i++)
					{
						if(token.compareToIgnoreCase(o[i])==0)
						{
							if(Character.isLetter(token.charAt(0)))
							{
							if(token.charAt(0) == Character.toUpperCase(token.charAt(0)))
							{
								r[i].toLowerCase();
								r[i] = Character.toString(r[i].charAt(0)).toUpperCase()+ r[i].substring(1);
							}
							}
							token = r[i];
							token1 = token.split(" ");
							stream.previous();
							stream.set(token1);
							stream.next();		
						}
					}
					// [Ending with 's]
					if(token.contains("'s"))
					{
						token = token.replace("'s", "");
						stream.previous();
						stream.set(token);
						stream.next();
					}	
					// [Others]
					Pattern p2 = Pattern.compile("([']+)");
					Matcher m2 = p2.matcher(token);
					if(m2.find())
					{
						token = token.replace(m2.group(1), "");
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
