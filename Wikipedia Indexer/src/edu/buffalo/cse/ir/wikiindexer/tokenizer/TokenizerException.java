/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.tokenizer;

/**
 * Wrapper class for all tokenization related exceptions.
 */
public class TokenizerException extends Exception {
	/**
	 * Generated serial version id
	 */
	private static final long serialVersionUID = 7788969087023365107L;
	
	public TokenizerException(String msg) {
		super(msg);
	}
}
