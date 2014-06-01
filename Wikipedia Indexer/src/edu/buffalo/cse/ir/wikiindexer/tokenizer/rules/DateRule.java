package edu.buffalo.cse.ir.wikiindexer.tokenizer.rules;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenStream;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.TokenizerException;
import edu.buffalo.cse.ir.wikiindexer.tokenizer.rules.TokenizerRule.RULENAMES;

/**
 * An implementation of the Dates Formatter
 *
 */

//Annotation
@RuleClass(className = RULENAMES.DATES)
public class DateRule implements TokenizerRule {
	public void apply(TokenStream stream) throws TokenizerException {
		if (stream != null) {
			String token;
			String d;
			
			//Change to Formats
			SimpleDateFormat d1 = new SimpleDateFormat("yyyyMMdd");
			SimpleDateFormat d11 = new SimpleDateFormat("1900MMdd");
			SimpleDateFormat d9 = new SimpleDateFormat("kk:mm:ss");
			SimpleDateFormat d12 = new SimpleDateFormat("HH:mm:ss");
			
			//Change from Formats
			SimpleDateFormat d4 = new SimpleDateFormat("dd MMM yyyy");
			SimpleDateFormat d5 = new SimpleDateFormat("MMMM dd, yyyy");
			SimpleDateFormat d13 = new SimpleDateFormat("MMM dd yyyy");
			SimpleDateFormat d6 = new SimpleDateFormat("yyyy GG");
			SimpleDateFormat d7 = new SimpleDateFormat("yyyy");
			SimpleDateFormat d8 = new SimpleDateFormat("hh:mmaa");
			SimpleDateFormat d10 = new SimpleDateFormat("MMM dd");
			
			while (stream.hasNext()) { 
				token = stream.next();
				if (token != null) {
					try {
						
					//[00:58:53 UTC on Sunday, 26 December 2004]
					Pattern p5 = Pattern.compile("([0-9]{1,2})([\\:])([0-9]{2})([\\:])([0-9]{2})([\\s])([A-Za-z]+)([\\s])([A-Za-z]+)([\\s])([A-Za-z,]+)([\\s])([0-9]{1,2})([\\s])((?:January)|(?:Jan)|(?:February)|(?:Feb)|(?:March)|(?:Mar)|(?:April)|(?:Apr)|(?:May)|(?:June)|(?:Jun)|(?:July)|(?:Jul)|(?:August)|(?:Aug)|(?:September)|(?:Sept)|(?:October)|(?:Oct)|(?:November)|(?:Nov)|(?:December)|(?:Dec))([\\s])([0-9]{2,4})");
					Matcher m5 = p5.matcher(token);
					while(m5.find()){
						String dn1 = d1.format(d4.parse(m5.group(13)+m5.group(14)+m5.group(15)+m5.group(16)+m5.group(17)));
						String dn2 = d12.format(d12.parse(m5.group(1)+m5.group(2)+m5.group(3)+m5.group(4)+m5.group(5)));
						d = dn1 + " " + dn2;
						token = token.replaceAll(m5.group(), d);
					}
						
					//[DayNo MonthName YearNo]
					Pattern p1 = Pattern.compile("([0-9]{1,2})([\\s])((January)|(Jan)|(February)|(Feb)|(March)|(Mar)|(April)|(Apr)|(May)|(June)|(Jun)|(July)|(Jul)|(August)|(Aug)|(September)|(Sept)|(October)|(Oct)|(November)|(Nov)|(December)|(Dec))([\\s])([0-9]{2,4})");
					Matcher m1 = p1.matcher(token);
					while(m1.find()){
						String temp = m1.group();
						if(m1.group(20)!=null)
						{
						if(m1.group(20).compareTo("Sept")==0)
						{
							temp = temp.replaceAll("Sept", "September");
						}	
						}
						d = d1.format(d4.parse(temp));
						token = token.replaceAll(m1.group(), d);
					}
					
					//[MonthName DayNo, YearNo,]
					Pattern p2 = Pattern.compile("((January)|(Jan)|(February)|(Feb)|(March)|(Mar)|(April)|(Apr)|(May)|(June)|(Jun)|(July)|(Jul)|(August)|(Aug)|(September)|(Sept)|(October)|(Oct)|(November)|(Nov)|(December)|(Dec))([\\s])([0-9]{1,2}),*([\\s])([0-9]{2,4})");
					Matcher m2 = p2.matcher(token);
					while(m2.find()){
						String temp = m2.group();
						if(m2.group(18)!=null)
						{
						if(m2.group(18).compareTo("Sept")==0)
						{
							temp = temp.replaceAll("Sept", "September");
						}	
						}
						if(m2.group().contains(","))
						{
							d = d1.format(d5.parse(temp));
							token = token.replaceAll(m2.group(), d);	
						}
						else
						{
							d = d1.format(d13.parse(temp));
							token = token.replaceAll(m2.group(), d);
						}
					}
					
					//[YearNo Era BC]
					Pattern p3 = Pattern.compile("([0-9]+)([\\s])((BC)|(bc))");
					Matcher m3 = p3.matcher(token);
					while(m3.find()){
						d = d1.format(d6.parse(m3.group()));
						token = token.replaceAll(m3.group(), "-"+d);
					}
					
					//[YearNo Era AD]
					Pattern p9 = Pattern.compile("([0-9]+)([\\s])((AD)|(ad))");
					Matcher m9 = p9.matcher(token);
					while(m9.find()){
						d = d1.format(d6.parse(m9.group()));
						token = token.replaceAll(m9.group(), d);
					}

					//[10:15 am]
					Pattern p4 = Pattern.compile("([0-9]{1,2}[\\:][0-9]{2})[\\s]*([AMamPMpm]+)");
					Matcher m4 = p4.matcher(token);
					while(m4.find()){
						d = d9.format(d8.parse(m4.group(1)+m4.group(2)));
						token = token.replaceAll(m4.group(), d);
					}
				
					//[MonthName DayNo]
					Pattern p7 = Pattern.compile("((January)|(Jan)|(February)|(Feb)|(March)|(Mar)|(April)|(Apr)|(May)|(June)|(Jun)|(July)|(Jul)|(August)|(Aug)|(September)|(Sept)|(October)|(Oct)|(November)|(Nov)|(December)|(Dec))([\\s])([0-9]{1,2})([\\s])");
					Matcher m7 = p7.matcher(token);
					while(m7.find()){
						d = d11.format(d10.parse(m7.group()));
						token = token.replaceAll(m7.group(), d+" ");
					}
					
					//[Year � Year]
					Pattern p8 = Pattern.compile("([0-9]+)[�]([0-9]+)");
					Matcher m8 = p8.matcher(token);
					while(m8.find()){
						d = d1.format(d7.parse(m8.group(1)))+"�"+d1.format(d7.parse(m8.group(1).toString().substring(0,2)+m8.group(2)));
						token = token.replaceAll(m8.group(), d);
					}
					
					//[YearNo]
					Pattern p6 = Pattern.compile("([\\s])([0-9]{4})([\\s])");
					Matcher m6 = p6.matcher(token); 
					if(m6.find()){
						d = d1.format(d7.parse(m6.group(2)));
						token = token.replaceAll(m6.group(), " "+d+" ");
						//System.out.println(token);
					}
					
					stream.previous();
					stream.set(token);
					stream.next();
				}
				
				catch (ParseException e) {					
					}
				}
			}
			stream.reset();
		}
			
	}
}

