
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;
import opennlp.tools.stemmer.PorterStemmer;

public class QueryProcessor {

	 public static void main(String []args) throws FileNotFoundException {
	        System.out.println("Query Processor started\n");
	        
	        /*** Declare Variables ***/
	        ArrayList<String> stopWords = new ArrayList<String>();
	        String sentence ="\"how to install nodejs\"" ;
	        boolean phraseSearch = false;
	        
	        /*** Checking Phrase Search ***/
	    	if(sentence.charAt(0) == '"' && sentence.charAt(sentence.length()-1) == '"')
	    		 phraseSearch = true;
	    	
	    	/*** For Testing Purpose ***/
	    	System.out.println("Checking Phrase Search = "+phraseSearch+"\n");
	        
	        /*** Removing the Speacial Charachters ***/
	    	sentence = sentence.replaceAll("[^a-zA-Z0-9 ]", "");
	    	
	    	/*** For Testing Purpose ***/
	    	System.out.println("After Removing the Speacial Charachters \n"+sentence+"\n");
	        
	    	/*** Converting the Sentence into words ***/
	    	ArrayList<String> queryWords = new ArrayList<String>(Arrays.asList(sentence.split(" ")));
	    	
	    	/*** For Testing Purpose ***/
	        System.out.println("After Converting the Sentence into words: ");
	        for(String word: queryWords)
	            System.out.println(word);
	        
	        /*** Deleting the Stop Words ***/
	        for(String word : readStopWords("stopwords.txt"))	queryWords.remove(word);
	        
	        /*** For Testing Purpose ***/
	        System.out.println("\nAfter Deleting the Stop Words: ");
	        for(String word: queryWords)
	            System.out.println(word);
	        
	        /*** Steaming ***/
	        PorterStemmer porterStemmer = new PorterStemmer();
	        for(int i =0; i < queryWords.size(); i++)
	        	queryWords.set(i, porterStemmer.stem(queryWords.get(i)));
	        
	        /*** For Testing Purpose ***/
	        System.out.println("\nAfter Steaming: ");
	        for(String word: queryWords)
	            System.out.println(word);
	        
	     }
	     
	     private static ArrayList<String> readStopWords(String directory) throws FileNotFoundException
	     {
	         ArrayList<String> stopWords = new ArrayList<String>();
	         
	         File file = new File(directory);
	         Scanner scanner = new Scanner(file);
	         while (scanner.hasNextLine()) { stopWords.add(scanner.nextLine());  }
	         scanner.close();
	         
	         return stopWords;
	     }
}
