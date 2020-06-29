package com.crawler;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.concurrent.Semaphore;

import com.crawler.Crawler.Crawl;
import com.crawler.Indexer.Producer;

import opennlp.tools.stemmer.PorterStemmer;

public class Main {

	static ArrayList<String> stopWords = new ArrayList<String>();
	final static int INDEXER_CNT = 10;
	final static int CRAWLER_CNT = 10;
	final static int MAX_LINKS_CNT = 10000;
	final static int MAX_CONNECTIONS = 130;
	
	final static boolean DEBUG_MODE = true;


	public static ArrayList<Producer> prodList;
	public static ArrayList<Crawl> crawlList;
	
	// public static int currentNonIndexedSize;
	public static int numberOfConnections;
	
	public static Object DBMutex, crawlingMutex;
	public static DBController controller;
	public static Semaphore connectionSemaphore;
	
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
        DBMutex = new Object();

        // Create Crawling Mutex
		crawlingMutex = new Object();
		

        
        connectionSemaphore = new Semaphore(MAX_CONNECTIONS);

        // Create DB Controller
		controller = new DBController();
        
        // Creating Tables in Database
		Connection connect = controller.connect();
		// if(DEBUG_MODE) {
		// 	controller.drop(connect);		// For Testing Purpose
		// 	File[] fi = f.listFiles();
		// 	for(File ff:fi) {
		// 		ff.delete();
		// 	}
		// 	f.delete();
		// 	f.mkdirs();
		// }
        // controller.build(connect);
        
        // currentNonIndexedSize = controller.checkNonIndexed(connect);

        connect.close();
        
		// Create Crawler Instance
		// Crawler crawler = new Crawler(MAX_LINKS_CNT, "seeder.txt", controller, DBMutex, crawlingMutex);
		

		prodList = new ArrayList<Producer>();
		// crawlList = new ArrayList<Crawl>();
		
		// for(int i=0 ; i<CRAWLER_CNT ; ++i) {
		// 	crawlList.add(crawler.new Crawl());
		// 	connectionSemaphore.acquire();
		// 	crawlList.get(i).start();
		// }

		// for(int i=0 ; i<CRAWLER_CNT ; ++i) {
		// 	crawlList.get(i).join();
		// 	connectionSemaphore.release();
		// }
		// while(connectionSemaphore.availablePermits() != MAX_CONNECTIONS);

        // Create Indexer Instance
		Indexer indexer = new Indexer(controller, DBMutex);
		
		for(int i=0 ; i<INDEXER_CNT ; ++i) {
			prodList.add(indexer.new Producer());
			connectionSemaphore.acquire();
			prodList.get(i).start();
		}

		connect = controller.connect();
		
		Hashtable<String, ArrayList<String>> pointingWebsites = new Hashtable<String, ArrayList<String>>();
		Hashtable<String, Integer> pointedToCount = new Hashtable<String, Integer>();

		controller.removeDefected(connect);
		
		ArrayList<String> URLs = controller.getAllURLs(connect);

		for(String url:URLs) {
			pointingWebsites.put(url, controller.getPointedFromURLs(connect, url));
			pointedToCount.put(url, controller.getPointingToCount(connect, url));
		}

		connect.close();


		
//		while(System.in.read()>-1)
//		{
//			synchronized (DBMutex) {
//				DBMutex.notifyAll();
//			}
//		}
		
		
		for(int i=0 ; i<INDEXER_CNT ; ++i) {
			prodList.get(i).join();
			connectionSemaphore.release();
		}

		while(connectionSemaphore.availablePermits() != MAX_CONNECTIONS);
		
		Hashtable<String, Double> popularity = Ranker.calculatePopularity(pointingWebsites, pointedToCount);
		
		connect = controller.connect();
		controller.insertPopularity(connect, popularity);
		connect.close();
		System.out.println("DONE!");
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
