import java.io.File;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import opennlp.tools.stemmer.PorterStemmer;
import java.sql.*; 

public class QueryProcessor {

	 public static void main(String []args) throws FileNotFoundException,Exception {
	        System.out.println("Query Processor started\n");
	        
	        /*** Declare Variables ***/
	        String sentence ="\"how to install nodejs on linux\"" ;
	        
	    	/*** Converting the Sentence into words ***/
	    	ArrayList<String> queryWords = query(sentence);
	    	
	     }
	 
	 	public static ArrayList<String> query(String sentence) throws FileNotFoundException,Exception
	 	{
	        ArrayList<String> queryWords = new ArrayList<String>();

	        /*** Checking Phrase Search ***/
	    	if(sentence.charAt(0) == '"' && sentence.charAt(sentence.length()-1) == '"')
	    		queryWords.add("1");
	    	else
	    		queryWords.add("0");
	    	
	    	/*** Converting the Sentence into words ***/
	    	queryWords.addAll(steaming(sentence));
	    	
	    	/*** For Testing Purpose ***/
	        System.out.println("\nAfter Query: ");
	        for(String word: queryWords)	System.out.println(word);
	    	
	    	return queryWords;
	 	}
	     
	 	public static ArrayList<String> steaming(String sentence) throws FileNotFoundException
	 	{
	 		/*** Declare Variables ***/
	 		ArrayList<String> stopWords = new ArrayList<String>();
	    	 
	 		/*** Removing the Speacial Charachters ***/
	    	sentence = sentence.replaceAll("[^a-zA-Z0-9 ]", "");
	    	
	    	/*** Converting the Sentence into words ***/
	    	ArrayList<String> queryWords = new ArrayList<String>(Arrays.asList(sentence.split(" ")));
	    	
	        /*** Reading the Stop Words ***/
	        File file = new File("stopwords.txt");
	        Scanner scanner = new Scanner(file);
	        while (scanner.hasNextLine()) { stopWords.add(scanner.nextLine());  }
	        scanner.close();
	        
	        /*** Deleting the Stop Words ***/
	        for(String word : stopWords)	while(queryWords.remove(word));
	        
	        /*** Steaming ***/ 
	        PorterStemmer porterStemmer = new PorterStemmer();
	        for(int i =0; i < queryWords.size(); i++)	queryWords.set(i, porterStemmer.stem(queryWords.get(i)).toLowerCase());
	        
	        return queryWords;	 
	 	}
	 	
	 	private static ArrayList<String> testSteaming(String sentence) throws FileNotFoundException
	 	{
	 		/*** Declare Variables ***/
	 		ArrayList<String> stopWords = new ArrayList<String>();
	    	 
	 		/*** Removing the Speacial Charachters ***/
	    	sentence = sentence.replaceAll("[^a-zA-Z0-9 ]", "");
	    	
	    	/*** For Testing Purpose ***/
	    	System.out.println("After Removing the Speacial Charachters \n"+sentence+"\n");
	        
	    	/*** Converting the Sentence into words ***/
	    	ArrayList<String> queryWords = new ArrayList<String>(Arrays.asList(sentence.split(" ")));
	    	
	    	/*** For Testing Purpose ***/
	        System.out.println("After Converting the Sentence into words: ");
	        for(String word: queryWords)	System.out.println(word);
	        
	        /*** Reading the Stop Words ***/
	        File file = new File("stopwords.txt");
	        Scanner scanner = new Scanner(file);
	        while (scanner.hasNextLine()) { stopWords.add(scanner.nextLine());  }
	        scanner.close();
	        
	        /*** Deleting the Stop Words ***/
	        for(String word : stopWords)	queryWords.remove(word);
	        
	        /*** For Testing Purpose ***/
	        System.out.println("\nAfter Deleting the Stop Words: ");
	        for(String word: queryWords)	System.out.println(word);
	        
	        /*** Steaming ***/ 
	        PorterStemmer porterStemmer = new PorterStemmer();
	        for(int i =0; i < queryWords.size(); i++)	queryWords.set(i, porterStemmer.stem(queryWords.get(i)));
	        
	        /*** For Testing Purpose ***/
	        System.out.println("\nAfter Steaming: ");
	        for(String word: queryWords)	System.out.println(word);
	        
	        return queryWords;	 
	 	}
}
