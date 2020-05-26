
import java.io.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;


public class Indexer {

	
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InterruptedException {
		System.out.println("Indexer");
		
		Object mutex = new Object();
		DBController con = new DBController();
		Indexer index = new Indexer(con, mutex);
		
		Connection mainConnection = con.connect();
		con.indexerTest(mainConnection);
		
		Producer prod = index.new Producer();
		Producer prod2 = index.new Producer();
		Producer prod3 = index.new Producer();
		Producer prod4 = index.new Producer();
		Producer prod5 = index.new Producer();
		Producer prod6 = index.new Producer();

		Processor proc = index.new Processor();
		Processor proc2 = index.new Processor();
		Processor proc3 = index.new Processor();
		Processor proc4 = index.new Processor();
		Processor proc5 = index.new Processor();
		Processor proc6 = index.new Processor();

		Publisher pub = index.new Publisher();
		Publisher pub2 = index.new Publisher();
		Publisher pub3 = index.new Publisher();
		Publisher pub4 = index.new Publisher();
		Publisher pub5 = index.new Publisher();
		Publisher pub6 = index.new Publisher();
		
		prod.start();
		prod2.start();
		prod3.start();
		prod4.start();
		prod5.start();
		prod6.start();
		
		proc.start();
		proc2.start();
		proc3.start();
		proc4.start();
		proc5.start();
		proc6.start();
		
		pub.start();
		pub2.start();
		pub3.start();
		pub4.start();
		pub5.start();
		pub6.start();
		
		while(System.in.read()>-1)
		{
			synchronized (mutex) {
				mutex.notifyAll();
			}
		}
		
		prod.join();
		prod2.join();
		prod3.join();
		prod4.join();
		prod5.join();
		prod6.join();
		
		proc.join();
		proc2.join();
		proc3.join();
		proc4.join();
		proc5.join();
		proc6.join();
		
		pub.join();
		pub2.join();
		pub3.join();
		pub4.join();
		pub5.join();
		pub6.join();
	}
	
	private static DBController controller;
	
	private Queue<URLRecord> URLQueue;
	private Queue<DocumentData> DocumentQueue;
	private Object InMutex, OutMutex, DBMutex;
	private Connection indexerConnection;
	
	public Indexer(DBController control, Object mutex) throws ClassNotFoundException, SQLException {
		controller = control;
		DBMutex = mutex;
		URLQueue = new LinkedList<URLRecord>();
		DocumentQueue = new LinkedList<DocumentData>();
		InMutex = new Object();
		OutMutex = new Object();
		indexerConnection = controller.connect();
	}
	
	// Needed data of the URL record
	public static class URLRecord {
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
	public static class WordRecord {
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
	public static class DocumentData {
		ArrayList<WordRecord> wordStats;
		ArrayList<String> imagesURL;
		String URL;
		int URLID;
		int totalWords;
		String title;
		String summary;
		
		public DocumentData(int ID, String url) {
			wordStats = new ArrayList<WordRecord>();
			imagesURL = new ArrayList<String>();
			URLID = ID;
			URL = url;
			totalWords=0;
			summary = "";
		}
	}
	
	public class Producer extends Thread
	{
		private ResultSet res;
		private int siz;
		
		public void run()
		{
			while(true) {
				// Database Use Mutex lock
				synchronized (DBMutex) {
					
					try {
						// Inserting URL from the crawler initializes the word count with -1
						// Check if -1 is existing
						while(controller.getMinURLWordCount(indexerConnection)!=-1) {
							
							// Wait untill a notification of insertion
							try {
								DBMutex.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						// Get & Mark available row(s)
						res = controller.getNonIndexedRows(indexerConnection);
						controller.markNonIndexedRows(indexerConnection);
						
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				synchronized(InMutex) {
					// Get the size before the insertion
					siz = URLQueue.size();
					
					try {
						// Extract the data from the ResultSet and inserting it into a URL queue
						while(res.next()) {
							URLQueue.add(new URLRecord(res.getInt(1), res.getString(2), res.getString(4)));
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					
					// If before insertion siz=0, this means some "Processor" threads are waiting, so notify them
					if(siz==0) {
						InMutex.notifyAll();
					}
				}

			}
		}
	}
	
	public class Processor extends Thread {
			
		private URLRecord URLInstance;
		private DocumentData documentInstance;
		private int siz;
		String line;
		ArrayList<String> words;
		
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
						// Prepare Summary
						if(line.length()+ret.summary.length()<500) {
							ret.summary += line;
							// Separate every lines with spaces not "\r"
							ret.summary += " ";
						}
						else if(ret.summary.length()<500) {
							// Add the needed number of characters
							ret.summary+=line.substring(0,Integer.min(line.length(), 500-ret.summary.length()));
						}
						
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

			while(true) {
				
				synchronized(InMutex) {
					
					while(URLQueue.size()==0) {
						// If no URLRecord exists, wait untill notification
						try {
							InMutex.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					}
					// Get the first of the queue
					URLInstance = URLQueue.poll();
				}
				
				try {
					// Process the document and make the required statistics
					documentInstance = process(URLInstance);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				synchronized (OutMutex) {
					// Size before Inserting a DocumentData
					siz=DocumentQueue.size();
					// Enqueue a document instance
					DocumentQueue.add(documentInstance);
					// If the size was 0, notify the waiting threads
					if(siz==0) {
						OutMutex.notifyAll();
					}
				}
				
			}
		}
	}
	
	public class Publisher extends Thread {
		
		private DocumentData documentInstance;
		private Connection publisherConnection;
		
		public Publisher() throws SQLException {
			// Establish a connection for each thread separately
			publisherConnection = controller.connect();
		}
		
		public void run() {

			while(true) {
				synchronized (OutMutex) {
					
					while(DocumentQueue.size()==0) {
						// Wait if no DocumentData is available
						try {
							OutMutex.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					// Extract a document instance from the queue
					documentInstance = DocumentQueue.poll();
				}
				// Update the title, summary, words_count of a URL
				controller.updateURL(publisherConnection, documentInstance.URLID, documentInstance.totalWords, documentInstance.title, documentInstance.summary);
				
				// Insert words statistics related to a URL
				for(WordRecord w:documentInstance.wordStats) {
					controller.insertWord(publisherConnection, w.word, documentInstance.URLID, 
							w.plainCount, w.headerCount, w.wordCount);
				}
				
				// Insert images URLs related to a URL
				for(String img:documentInstance.imagesURL) {
					controller.insertImage(publisherConnection, documentInstance.URLID, img);
				}
				
				System.out.println("Finished: " + documentInstance.URL);
			}
		}
	}

}
