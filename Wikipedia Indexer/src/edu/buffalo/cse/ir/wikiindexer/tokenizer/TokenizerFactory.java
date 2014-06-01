/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.tokenizer;

import java.util.Properties;

import edu.buffalo.cse.ir.wikiindexer.indexer.INDEXFIELD;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.*;

/**
 * Factory class to instantiate a Tokenizer instance
 * The expectation is that you need to decide which rules to apply for which field
 * Thus, given a field type, initialize the applicable rules and create the tokenizer
 *
 */
public class TokenizerFactory {
	//private instance, we just want one factory
	private static TokenizerFactory factory;

	//properties file, if you want to read soemthing for the tokenizers
	private static Properties props;

	/**
	 * Private constructor, singleton
	 */
	private TokenizerFactory() {
		//TODO: Implement this method
	}

	/**
	 * MEthod to get an instance of the factory class
	 * @return The factory instance
	 */
	public static TokenizerFactory getInstance(Properties idxProps) {
		if (factory == null) {
			factory = new TokenizerFactory();
			props = idxProps;
		}

		return factory;
	}

	/**
	 * Method to get a fully initialized tokenizer for a given field type
	 * @param field: The field for which to instantiate tokenizer
	 * @return The fully initialized tokenizer
	 * @throws TokenizerException 
	 */
	public Tokenizer getTokenizer(INDEXFIELD field) throws TokenizerException {
		/*
		 * For example, for field F1 I want to apply rules R1, R3 and R5
		 * For F2, the rules are R1, R2, R3, R4 and R5 both in order
		 * So the pseudo-code will be like:
		 * if (field == F1)
		 * 		return new Tokenizer(new R1(), new R3(), new R5())
		 * else if (field == F2)
		 * 		return new TOkenizer(new R1(), new R2(), new R3(), new R4(), new R5())
		 * ... etc
		 */
		DateRule date = new DateRule();
		WhitespaceRule white = new WhitespaceRule();
		PunctuationRule punct = new PunctuationRule();
		HyphenRule hyph = new HyphenRule();
		NumberRule num = new NumberRule();
		AccentRule acct = new AccentRule();
		ApostropheRule apos = new ApostropheRule(); 
		SpecialCharRule spch = new SpecialCharRule();
		CapitalizationRule caps = new CapitalizationRule();
		StopWordsRule stop = new StopWordsRule();
		EnglishStemmer stem = new EnglishStemmer();
			
		if(field == INDEXFIELD.TERM)
			return new Tokenizer(date,white,spch,punct,caps,apos,acct,hyph,num,stop,stem);
		
		if(field == INDEXFIELD.AUTHOR)
			return new Tokenizer(acct,apos,punct);
		
		if(field == INDEXFIELD.LINK)
			return new Tokenizer(punct,hyph,acct);
		
		if(field == INDEXFIELD.CATEGORY)
			return new Tokenizer(caps,apos,hyph,acct,punct);
		return null;
	}
}
