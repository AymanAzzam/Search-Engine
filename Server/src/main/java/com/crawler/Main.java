package com.crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import com.crawler.Indexer.Producer;

import opennlp.tools.stemmer.PorterStemmer;

public class Main {

	static ArrayList<String> stopWords = new ArrayList<String>();
	final static int INDEXER_CNT = 10;
	final static int CRAWLER_CNT = 10;

	
	public static void main(String[] args) throws ClassNotFoundException, SQLException, InterruptedException, IOException {

		
        /*** Reading the Stop Words ***/
        File file = new File("stopwords.txt");
        file.createNewFile();
        Scanner scanner = new Scanner(file);
        while (scanner.hasNextLine()) { stopWords.add(scanner.nextLine());  }
        scanner.close();
        
        File f = new File("docs");
        f.mkdirs();
        
        // Create DB Mutex
        Object mutex = new Object();
		
        // Create DB Controller
        DBController controller = new DBController();
        
        // Create Indexer Instance
		Indexer indexer = new Indexer(controller, mutex);
		
//		// Create Crawler Instance
//		Crawler crawler = new Crawler(controller, mutex);
		

		/*
		 * Create Crawler threads here and start them
		 */
		
		ArrayList<Producer> prodList = new ArrayList<Producer>();
		
		for(int i=0 ; i<INDEXER_CNT ; ++i) {
			prodList.add(indexer.new Producer());
			prodList.get(i).start();
		}
		
//		while(System.in.read()>-1)
//		{
//			synchronized (mutex) {
//				mutex.notifyAll();
//			}
//		}
		

		/*
		 * Wait for Crawler threads to join here
		 */
		
		for(int i=0 ; i<INDEXER_CNT ; ++i) {
			prodList.get(i).join();
		}
		
	}

	
	
	public static ArrayList<String> steaming(String sentence) throws FileNotFoundException
 	{
 		/*** Declare Variables ***/
 		ArrayList<String> stopWords = new ArrayList<String>();
    	 
 		/*** Removing the Special Characters ***/
    	sentence = sentence.replaceAll("[^a-zA-Z0-9 ]", "");
    	
    	/*** Converting the Sentence into words ***/
    	ArrayList<String> queryWords = new ArrayList<String>(Arrays.asList(sentence.split(" ")));
    	
        /*** Deleting the Stop Words ***/
        for(String word : stopWords)	while(queryWords.remove(word));
        
        /*** Steaming ***/ 
        PorterStemmer porterStemmer = new PorterStemmer();
        for(int i =0; i < queryWords.size(); i++)	queryWords.set(i, porterStemmer.stem(queryWords.get(i)).toLowerCase());
        
        return queryWords;	 
 	}
	

}
