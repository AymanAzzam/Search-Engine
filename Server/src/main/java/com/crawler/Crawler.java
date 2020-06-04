package com.crawler;


import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.sql.*;

import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Queue;
import java.util.LinkedList;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class Crawler {
	//data members:
	private static DBController controller;
	private final int MAX_LINKS_COUNT;
	private HashSet<String> visitedLinks;
	private Queue <String> toBeProcessedLinks;
	private int visitedLinksCnt;
	private int linksCnt;
//	private java.sql.Connection crawlerConnection;
	private Object queueMutex, DBMutex;

	//constructor:
	public Crawler (int maxNoOfLinks, String seederFileName, DBController dbController, Object mutex)
			throws ClassNotFoundException, SQLException {

		visitedLinks = new HashSet <String>();
		toBeProcessedLinks = new LinkedList<String>();
		MAX_LINKS_COUNT = maxNoOfLinks;
		visitedLinksCnt = 0;
		linksCnt = 0;
		controller = dbController;
//		crawlerConnection = controller.connect();
		DBMutex = mutex;
		queueMutex = new Object();
		seed(seederFileName);

	}

	//seed
	public void seed(String fileName) {

		try {

		      File seeder = new File(fileName);
		      Scanner reader = new Scanner(seeder);

		      //read and add links
		      while (reader.hasNextLine()) {
		    	  //read the link
				  String url = reader.nextLine();
				  //if the link is not in the queue then add it
				  if(!visitedLinks.contains(url)) {
					  toBeProcessedLinks.add(url);
					  visitedLinks.add(url);
					  visitedLinksCnt++;
				  }
		      }
		      reader.close();
		    } catch (FileNotFoundException e) {
		      System.out.println("An error occurred while open the seeder...");
		    }

	}




	public class Crawl extends Thread {
		private int queueSize;
		private String filePath;
		private String url;
		private int ID;
		private Document webDoc;
		private java.sql.Connection crawlConnection;
		
		public Crawl() throws SQLException {
			crawlConnection = controller.connect();
		}
		
		//extract links:
		public void extractLinks(Document htmlDocument) {

			Elements webPagesOnHtml = htmlDocument.select("a[href]");

			for (Element webpPage : webPagesOnHtml) {
				String newURL = webpPage.attr("abs:href");
				//if the link wasnt visited yet and max count of links not reached then add it to be processed
				if(!visitedLinks.contains(newURL) && visitedLinksCnt < MAX_LINKS_COUNT) {
					visitedLinks.add(newURL);
					toBeProcessedLinks.add(newURL);
					visitedLinksCnt++;
				}
			}
		}

		//save html file contains the webpage content:
		//imgs - title - h1->h6 - plaintext -bodys
		public String saveWebPage(Document webPage, int docID) {

		    try {
				FileWriter myWriter = new FileWriter("docs/doc"+docID+".txt");

				//images::
				myWriter.write("#IMAGES\n");
				Elements images = webPage.select("img");
				for(Element image : images) {
					String imageLink = image.attr("src");
					myWriter.write(imageLink+"\n");
				}

				//title::
				myWriter.write("#TITLE\n");
				String title = webPage.title();
				title = getEnglishText(title);
				myWriter.write(title+"\n");

				//headers::
				myWriter.write("#HEADERS\n");
				Elements headers = webPage.select("h1, h2, h3, h4, h5, h6");
				for(Element header : headers) {
					String headerText = header.text();
					headerText = getEnglishText(headerText);
					myWriter.write(headerText+"\n");
				}

				//plaintext
				myWriter.write("#PLAINTEXT\n");
				Elements plains = webPage.select("p");
				for(Element plain : plains) {
					String plainText = plain.text();
					plainText = getEnglishText(plainText);
					myWriter.write(plainText+"\n");
				}

				//body
				myWriter.write("#BODY\n");
				String body = webPage.body().text();
				body = getEnglishText(body);
				myWriter.write(body);

				myWriter.close();

				return new String("docs/doc"+docID+".txt");

			} catch (IOException e) {
				// TODO Auto-generated catch block
				//e.printStackTrace();
				return new String("");
			}

		}

		//filter webpage content:
		public String getEnglishText(String text) {
		    Pattern pattern = Pattern.compile("[^a-zA-Z 0-9\n]");
		    Matcher matcher = pattern.matcher(text);
		    String englishText = matcher.replaceAll("");
		    return englishText;
		 }

		public void run() {

			while(true) {

				synchronized(queueMutex) {
					while(toBeProcessedLinks.isEmpty()) {
						try {
							//###############################################
							System.out.println("Sleep thread no. " + String.valueOf(Thread.currentThread().getId()));
							//###############################################
							queueMutex.wait();
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							//###############################################
							System.out.println("awake thread no. " + String.valueOf(Thread.currentThread().getId()));
							//###############################################
							e.printStackTrace();
						}
					}

					url = toBeProcessedLinks.poll();
					ID = ++linksCnt;
				}
				
				try {
					webDoc = Jsoup.parse(new URL(url).openStream(), "ASCII", url);
					
					synchronized (queueMutex) {
						queueSize = toBeProcessedLinks.size();
						extractLinks(webDoc);
						if(queueSize == 0 && (!toBeProcessedLinks.isEmpty())) {
							queueMutex.notifyAll();
						}
						
					}

					filePath = saveWebPage(webDoc, ID);
					if(!filePath.isEmpty()) {
						synchronized (DBMutex) {
							controller.insertURL(crawlConnection, url, filePath);
							
						}
					}
					
					
					//###############################################
					System.out.println("Done: " + url+"==> by thread no. " + String.valueOf(Thread.currentThread().getId()));
					//###############################################
				} catch (IOException e) {
					// TODO Auto-generated catch block
					System.err.println("for '"+url+"': "+e.getMessage());
				}
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////


	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InterruptedException {

		Object mutex = new Object();
		DBController dbController = new DBController();

		Crawler myCrawler = new Crawler(30, "seeder.txt", dbController, mutex);

		Crawl crawler1 = myCrawler.new Crawl();
		Crawl crawler2 = myCrawler.new Crawl();
		Crawl crawler3 = myCrawler.new Crawl();
		Crawl crawler4 = myCrawler.new Crawl();
		Crawl crawler5 = myCrawler.new Crawl();
		Crawl crawler6 = myCrawler.new Crawl();
		Crawl crawler7 = myCrawler.new Crawl();

		crawler1.start();
		crawler2.start();
		crawler3.start();
		crawler4.start();
		crawler5.start();
		crawler6.start();
		crawler7.start();
	}

}
