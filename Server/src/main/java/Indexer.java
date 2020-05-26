
import java.io.*;
//import java.nio.charset.StandardCharsets;
import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;

public class Indexer {

	
	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InterruptedException {
		System.out.println("Indexer");
//		String Test = "Hello everybody, this is a test for the stemmer from the Indexer!";
//		ArrayList<String> List = QueryProcessor.steaming(Test);
//		for(String s:List)	System.out.println(s);
//		Document document = Jsoup.connect("https://www.facebook.com/").get();
//		System.out.println(document.head().text());
//		
//		OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream("test.txt"), StandardCharsets.UTF_8);
//	    writer.write(document.text());
//	    writer.close();
		
//		BufferedReader reader = new BufferedReader(new FileReader("crawler_format.txt"));
//		String s;
//		while((s = reader.readLine()) != null)
//		{
//			if(s.equals(""));
//		}
//		reader.close();
//		System.out.println("Done!");
		
//		Map<String, ArrayList<Integer>> test = new HashMap<String, ArrayList<Integer> >();
//		System.out.println(test.get("test"));
//		test.put("test", test.get("test")+1);
//		System.out.println(test.get("test"));
		
		Object mutex = new Object();
		DBController con = new DBController();
		Indexer index = new Indexer(con, mutex);
		
		Producer prod = index.new Producer();
		Processor proc = index.new Processor();
		Processor proc2 = index.new Processor();
		Processor proc3 = index.new Processor();
//		Processor proc4 = index.new Processor();
//		Processor proc5 = index.new Processor();
//		Processor proc6 = index.new Processor();
		Publisher pub = index.new Publisher();
		Publisher pub2 = index.new Publisher();
		Publisher pub3 = index.new Publisher();
		Publisher pub4 = index.new Publisher();
		Publisher pub5 = index.new Publisher();
		Publisher pub6 = index.new Publisher();
		
		prod.start();
		proc.start();
		proc2.start();
		proc3.start();
//		proc4.start();
//		proc5.start();
//		proc6.start();
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
		
//		prod.join();
//		proc.join();
//		proc2.join();
//		proc3.join();
//		pub.join();
//		pub2.join();
//		pub3.join();
	}
	
	private static DBController controller;
	
	private static Queue<URLRecord> URLQueue;
	private static Queue<WebsiteData> WebsiteQueue;
	private static Object InMutex, OutMutex, DBMutex;
	
	private Connection indexerConnection;
	
	public Indexer(DBController control, Object mutex) throws ClassNotFoundException, SQLException {
		controller = control;
		DBMutex = mutex;
		URLQueue = new LinkedList<URLRecord>();
		WebsiteQueue = new LinkedList<WebsiteData>();
		InMutex = new Object();
		OutMutex = new Object();
		indexerConnection = controller.connect();
	}
	
	
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
	
	public static class WordRecord {
		String word;
		int word_count;
		int plain_count;
		int header_count;
		
		public WordRecord(String w, int wc, int pc, int hc) {
			word = w;
			word_count = wc;
			plain_count = pc;
			header_count = hc;
		}
	}
	
	public static class WebsiteData {
		ArrayList<WordRecord> word_stats;
		ArrayList<String> images_url;
		int URLID;
		int total_words;
		String title;
		String summary;
		
		public WebsiteData(int ID) {
			word_stats = new ArrayList<WordRecord>();
			images_url = new ArrayList<String>();
			URLID = ID;
			total_words=0;
			summary = "";
		}
	}
	
	public class Producer extends Thread
	{
		private ResultSet res;
		private int siz;
		
//		public Producer() throws SQLException {
//			System.out.println("Producer Connecting...");
//			conn = controller.connect();
//		}
		
		public void run()
		{
			System.out.println("Producer Ready...");
			while(true) {
				synchronized (DBMutex) {
					
					try {
						// Insert URL from the crawler initilize the word count with -1
						while(controller.getMinURLWordCount(indexerConnection)!=-1) {
//							System.out.println(controller.getMinURLWordCount());
							
							System.out.println("Producer Wait!");
							try {
								DBMutex.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						System.out.println("Producer Awaken!");
						res = controller.getNonIndexedRows(indexerConnection);
						controller.markNonIndexedRows(indexerConnection);
//						System.out.println(lowerbound_ID);
//						lowerbound_ID = controller.getMaxURLID();
//						System.out.println(lowerbound_ID);
						
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				synchronized(InMutex) {
					siz = URLQueue.size();
					
					try {

						while(res.next()) {
							URLQueue.add(new URLRecord(res.getInt(1), res.getString(2), res.getString(4)));
						}
					} catch (SQLException e) {
						System.out.println("Failed");
						e.printStackTrace();
					}
					if(siz==0) {
						InMutex.notifyAll();
					}
				}

				System.out.println(URLQueue.size());
				
			}
		}
	}
	
	public class Processor extends Thread {
			
		private URLRecord url_rec;
		private WebsiteData website_rec;
		private int siz;
		String line;
		ArrayList<String> words;
		
		public WebsiteData process(URLRecord r) throws IOException {
			WebsiteData ret = new WebsiteData(r.ID);
			Map<String, Integer> header_freq = new HashMap<String, Integer>();
			Map<String, Integer> plain_freq = new HashMap<String, Integer>();
			Map<String, Integer> body_freq = new HashMap<String, Integer>();
			
			BufferedReader reader = new BufferedReader(new FileReader(r.filePath));
			
			int current=0;
			
			while((line = reader.readLine()) != null) {
				
				if(line.equals("#IMAGES"))	current=1;
				else if(line.equals("#TITLE"))	current=2;
				else if(line.equals("#HEADERS"))	current=3;
				else if(line.equals("#PLAINTEXT"))	current=4;
				else if(line.equals("#BODY"))	current=5;
				else if(current==1) {
					ret.images_url.add(line);
				}
				else if(current==2) {
					// Prepare Title
					ret.title=line;
				}
				else {
					// Stemming the line
					words = QueryProcessor.steaming(line);
					
					for(String s:words) {
						header_freq.putIfAbsent(s, 0);
						plain_freq.putIfAbsent(s, 0);
						body_freq.putIfAbsent(s, 0);
						
						if(current==3) {
							int c=header_freq.get(s);
							header_freq.put(s, c+1);
						}
						else if(current==4) {
							int c=plain_freq.get(s);
							plain_freq.put(s, c+1);
						}
						else if(current==5) {
							int c=body_freq.get(s);
							body_freq.put(s, c+1);
						}
					}
					
					if(current==5) {
						// Append to Summary
						if(line.length()+ret.summary.length()<500) {
							ret.summary += line;
							ret.summary += " ";
						}
						else if(ret.summary.length()<500) {
							ret.summary+=line.substring(0,Integer.min(line.length(), 500-ret.summary.length()));
						}
						
						//Count total words in document
						ret.total_words += words.size();
					}
					
				}
			}
			reader.close();
			
			for(String w:header_freq.keySet()) {
				ret.word_stats.add(new WordRecord(w, body_freq.get(w), plain_freq.get(w), header_freq.get(w)));
			}
			return ret;
		}
		
		public void run() {

			System.out.println("Processor "+ Thread.currentThread().getName() +" Ready...");
			while(true) {
				
				synchronized(InMutex) {
					while(URLQueue.size()==0) {
						
						try {
							System.out.println("Processor Wait!");
							InMutex.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						
					}
					System.out.println("Processor "+ Thread.currentThread().getName() +" Awaken!");
					url_rec = URLQueue.poll();
				}
				
				try {
					website_rec = process(url_rec);
					
//					System.out.println("Processor:");
//					System.out.println("URL: " + website_rec.URLID);
//					System.out.println("Img: " + website_rec.images_url.size());
//					System.out.println("Wrd: " + website_rec.word_stats.size());
//					System.out.println("Obj: " + website_rec);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				synchronized (OutMutex) {
					siz=WebsiteQueue.size();
					WebsiteQueue.add(website_rec);
					System.out.println("Inc: " + WebsiteQueue.size());
					if(siz==0) {
						OutMutex.notifyAll();
					}
				}
				
			}
		}
	}
	
	public class Publisher extends Thread {
		
		private WebsiteData website_rec;
		private Connection publisherConnection;
		public Publisher() throws SQLException {
			System.out.println("Publisher Connecting...");
			publisherConnection = controller.connect();
		}
		
		public void run() {

			System.out.println("Publisher Ready...");
			long cur = System.currentTimeMillis();
			while(true) {
				synchronized (OutMutex) {
					while(WebsiteQueue.size()==0) {

						cur = System.currentTimeMillis()-cur;
						System.out.println(cur);
						cur = System.currentTimeMillis();
						
						try {
//							System.out.println("Publisher "+ Thread.currentThread().getName() +" Wait!");
							OutMutex.wait();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					}
					
//					System.out.println("Publisher "+ Thread.currentThread().getName() +" Awaken!");
					website_rec = WebsiteQueue.poll();
//					System.out.println("Dec: " + WebsiteQueue.size());
				}
//				System.out.println("Publisher");
//				System.out.println("URL: " + website_rec.URLID);
//				System.out.println("Img: " + website_rec.images_url.size());
//				System.out.println("Wrd: " + website_rec.word_stats.size());
//				System.out.println("Obj: " + website_rec);
				
//				System.out.println("======================== " + Thread.currentThread().getName());
				controller.updateURL(publisherConnection, website_rec.URLID, website_rec.total_words, website_rec.title, website_rec.summary);
				
				for(WordRecord w:website_rec.word_stats) {
					controller.insertWord(publisherConnection, w.word, website_rec.URLID, 
							w.plain_count, w.header_count, w.word_count);
				}
				
				for(String img:website_rec.images_url) {
					controller.insertImage(publisherConnection, website_rec.URLID, img);
				}
//				System.out.println("######################## " + Thread.currentThread().getName());
			}
		}
	}

}
