/**
 * 
 */
package edu.buffalo.cse.ir.wikiindexer.indexer;

/**
 * @author Pratik
 * THis class is responsible for assigning a partition to a given term.
 * The static methods imply that all instances of this class should 
 * behave exactly the same. Given a term, irrespective of what instance
 * is called, the same partition number should be assigned to it.
 */
public class Partitioner {
	/**
	 * Method to get the total number of partitions
	 * THis is a pure design choice on how many partitions you need
	 * and also how they are assigned.
	 * @return: Total number of partitions
	 */
	public static int partno=15;
	public synchronized static int getNumPartitions() {

		return 15;
	}
	
	/**
	 * Method to fetch the partition number for the given term.
	 * The partition numbers should be assigned from 0 to N-1
	 * where N is the total number of partitions.
	 * @param term: The term to be looked up
	 * @return The assigned partition number for the given term
	 */
	public synchronized static int getPartitionNumber (String term) {

		if(term.charAt(0)=='a' || term.charAt(0)=='A')
		return 0;
		
		if(term.charAt(0)=='c' || term.charAt(0)=='C')
		return 1;
		
		if(term.charAt(0)=='m' || term.charAt(0)=='M')
		return 2;
		
		if(term.charAt(0)=='p' || term.charAt(0)=='P')
		return 3;
		
		if(term.charAt(0)=='s' || term.charAt(0)=='S')
		return 4;
		
		if(term.charAt(0)=='t' || term.charAt(0)=='T')
		return 5;
		
		if(term.charAt(0)=='b' || term.charAt(0)=='v'|| term.charAt(0)=='B' || term.charAt(0)=='V')
		return 6;
		
		if(term.charAt(0)=='d' || term.charAt(0)=='j'|| term.charAt(0)=='D'|| term.charAt(0)=='J')
		return 7;
		
		if(term.charAt(0)=='e' || term.charAt(0)=='o'|| term.charAt(0)=='E'|| term.charAt(0)=='O')
		return 8;
		
		if(term.charAt(0)=='f' || term.charAt(0)=='w'|| term.charAt(0)=='F'|| term.charAt(0)=='W')
		return 9;
		
		if(term.charAt(0)=='g' || term.charAt(0)=='i'|| term.charAt(0)=='G'|| term.charAt(0)=='I')
		return 10;
		
		if(term.charAt(0)=='h' || term.charAt(0)=='u'|| term.charAt(0)=='H'|| term.charAt(0)=='U')
		return 11;
		
		if(term.charAt(0)=='k' || term.charAt(0)=='r'|| term.charAt(0)=='K'|| term.charAt(0)=='R')
		return 12;
		
		if(term.charAt(0)=='l' || term.charAt(0)=='n'|| term.charAt(0)=='L'|| term.charAt(0)=='N')
		return 13;
		
		if(term.charAt(0)=='q' || term.charAt(0)=='x'|| term.charAt(0)=='y'|| term.charAt(0)=='z' || term.charAt(0)=='Q' || term.charAt(0)=='X'|| term.charAt(0)=='Y'|| term.charAt(0)=='Z' || term.charAt(0)=='0' || term.charAt(0)=='1'|| term.charAt(0)=='2'|| term.charAt(0)=='3'|| term.charAt(0)=='4'|| term.charAt(0)=='5'|| term.charAt(0)=='6'|| term.charAt(0)=='7'|| term.charAt(0)=='8'|| term.charAt(0)=='9')
		return 14;
		
			return 18;
	}
}
