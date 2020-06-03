package com.crawler;

import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class Indexer {

	
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InterruptedException {
		System.out.println("Indexer");
		
		Object mutex = new Object();
		DBController controller = new DBController();
		Indexer index = new Indexer(controller, mutex);
		
		Connection mainConnection = controller.connect();
		controller.indexerTest(mainConnection);
		
		ArrayList<Producer> prodList = new ArrayList<Producer>();
		
		for(int i=0 ; i<10 ; ++i) {
			prodList.add(index.new Producer());
			prodList.get(i).start();
		}
		
		while(System.in.read()>-1)
		{
			synchronized (mutex) {
				mutex.notifyAll();
			}
		}
				
	}
	
	private DBController controller;
	
	private Object DBMutex;
	
	public Indexer(DBController control, Object mutex) throws ClassNotFoundException, SQLException {
		controller = control;
		DBMutex = mutex;
	}
	
	// Needed data of the URL record
	public class URLRecord {
		int ID;
		String Name;
		String filePath;
		
		public URLRecord(int id, String name, String file) {
			ID=id;
			Name=name;
			filePath=file;
		}
	}
	
	// Needed data of the word record
	public class WordRecord {
		String word;
		int wordCount;
		int plainCount;
		int headerCount;
		
		public WordRecord(String w, int wc, int pc, int hc) {
			word = w;
			wordCount = wc;
			plainCount = pc;
			headerCount = hc;
		}
	}
	
	// Needed data and statistics of the Document
	public class DocumentData {
		ArrayList<WordRecord> wordStats;
		ArrayList<String> imagesURL;
		String URL;
		int URLID;
		int totalWords;
		String title;
		String content;
		
		public DocumentData(int ID, String url) {
			wordStats = new ArrayList<WordRecord>();
			imagesURL = new ArrayList<String>();
			URLID = ID;
			URL = url;
			totalWords=0;
			content = "";
		}
	}
	
	// Extract URLs from the Database to be indexed
	public class Producer extends Thread
	{
		private ResultSet res;
		private Connection producerConnection;
		
		public Producer() throws SQLException {
			// Establish a connection for each thread separately
			producerConnection = controller.connect();
		}
		
		public void run()
		{
			while(true) {
				// Database Use Mutex lock
				synchronized (DBMutex) {
					
					try {
						// Inserting URL from the crawler initializes is_indexed with FALSE
						// Check if [is_indexed = false] is existing
						while(controller.checkNonIndexed(producerConnection)==0) {
							
							// Wait untill a notification of insertion
							try {
								DBMutex.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						// Get & Mark available row(s)
						res = controller.getNonIndexedRows(producerConnection);
						controller.markNonIndexedRows(producerConnection);
						
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				try {
					// Extract the data from the ResultSet and inserting it into a URL queue
					while(res.next()) {
						new Processor(new URLRecord(res.getInt(1), res.getString(2), res.getString(4))).start();
					}
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	// Processing the URLs Documents to make statistics
	public class Processor extends Thread {
			
		private URLRecord URLInstance;
		private DocumentData documentInstance;
		private Connection processorConnection;
		String line;
		ArrayList<String> words;
		
		public Processor(URLRecord rec) throws SQLException
		{
			// Establish a connection for each thread separately
			processorConnection = controller.connect();
			
			URLInstance = rec;
			System.out.println("Processing: " + rec.Name);
		}
		
		public DocumentData process(URLRecord r) throws IOException {
			
			DocumentData ret = new DocumentData(r.ID, r.Name);
			
			// Maps to count word frequencies
			Map<String, Integer> header_freq = new HashMap<String, Integer>();
			Map<String, Integer> plain_freq = new HashMap<String, Integer>();
			Map<String, Integer> body_freq = new HashMap<String, Integer>();
			
			// File Reader
			BufferedReader reader = new BufferedReader(new FileReader(r.filePath));
			
			int current=0;
			
			while((line = reader.readLine()) != null) {
				
				if(line.equals("#IMAGES"))	current=1;
				else if(line.equals("#TITLE"))	current=2;
				else if(line.equals("#HEADERS"))	current=3;
				else if(line.equals("#PLAINTEXT"))	current=4;
				else if(line.equals("#BODY"))	current=5;
				else if(current==1) {
					// Add image URL
					ret.imagesURL.add(line);
				}
				else if(current==2) {
					// add Title
					ret.title=line;
				}
				else {
					// Stemming the line
					words = QueryProcessor.steaming(line);
					
					for(String s:words) {
						
						if(s.equals(""))	continue;
						
						// If word isn't existing, add it with frequency=0
						header_freq.putIfAbsent(s, 0);
						plain_freq.putIfAbsent(s, 0);
						body_freq.putIfAbsent(s, 0);
						
						if(current==3) {
							// Header Section
							int c=header_freq.get(s);
							header_freq.put(s, c+1);
						}
						else if(current==4) {
							// Plaintext Section
							int c=plain_freq.get(s);
							plain_freq.put(s, c+1);
						}
						else if(current==5) {
							// Body Section
							int c=body_freq.get(s);
							body_freq.put(s, c+1);
						}
					}
					
					if(current==5) {
						// Prepare Content
						ret.content += line;
						// Separate every lines with spaces not "\r"
						ret.content += " ";
						
						//Count total words in the document
						ret.totalWords += words.size();
					}
					
				}
			}
			// Close the File reader connection
			reader.close();
			
			for(String w:header_freq.keySet()) {
				// Add a word statistics
				ret.wordStats.add(new WordRecord(w, body_freq.get(w), plain_freq.get(w), header_freq.get(w)));
			}
			return ret;
		}
		
		public void run() {
			try {
				// Process the document and make the required statistics
				documentInstance = process(URLInstance);
				
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			// Update the title, content, words_count of a URL
			controller.updateURL(processorConnection, documentInstance.URLID, documentInstance.totalWords, documentInstance.title, documentInstance.content);
			
			// Insert words statistics related to a URL
			for(WordRecord w:documentInstance.wordStats) {
				controller.insertWord(processorConnection, w.word, documentInstance.URLID, 
						w.plainCount, w.headerCount, w.wordCount);
			}
			
			// Insert images URLs related to a URL
			for(String img:documentInstance.imagesURL) {
				controller.insertImage(processorConnection, documentInstance.URLID, img);
			}
			
			System.out.println("Finished: " + documentInstance.URL);
		}
	}	

}
