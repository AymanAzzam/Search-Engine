package com.crawler;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


import java.sql.*;

import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.sql.rowset.spi.SyncResolver;



public class Crawler {
	//data members:
	private static DBController controller;
	private final int MAX_LINKS_COUNT;
//	private HashSet<String> visitedLinks;
//	private Queue <String> toBeProcessedLinks;
//	private int visitedLinksCnt;
//	private int linksCnt;
	private java.sql.Connection mainCrawlerConnection;
	private Object crawlingMutex, DBMutex;
	private int totalCrawlingSize;
	private int currentNonCrawledSize;

	//constructor:
	public Crawler (int maxNoOfLinks, String seederFileName, DBController dbController, Object dbmutex, Object crawlmutex)
			throws ClassNotFoundException, SQLException {

		MAX_LINKS_COUNT = maxNoOfLinks;
		controller = dbController;
		mainCrawlerConnection = controller.connect();
		DBMutex = dbmutex;
		crawlingMutex = crawlmutex;
		seed(seederFileName);

		totalCrawlingSize = controller.getCrawlingSize(mainCrawlerConnection);
		currentNonCrawledSize = controller.checkNonCrawled(mainCrawlerConnection);
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

				  controller.insertCrawlingURL(mainCrawlerConnection, url);
		      }
		      reader.close();
		    } catch (FileNotFoundException e) {
		      System.out.println("An error occurred while open the seeder...");
		    }
	}




	public class Crawl extends Thread {
		private String filePath;
		private String url;
		private int ID;
		private Document webDoc;
		private java.sql.Connection crawlConnection;
		private ResultSet res;
		
		public Crawl() throws SQLException {
			crawlConnection = controller.connect();
		}
		
		//extract links:
		public void extractLinks(Document htmlDocument) {

			Elements webPagesOnHtml = htmlDocument.select("a[href]");
			
			for (Element webpPage : webPagesOnHtml) {
				String newURL = webpPage.attr("abs:href");
				
				
				synchronized (crawlingMutex) {
					
					if(totalCrawlingSize == MAX_LINKS_COUNT) {
						return;
					}
					
					boolean success = controller.insertCrawlingURL(mainCrawlerConnection, newURL);
					
					totalCrawlingSize += success?1:0;
					currentNonCrawledSize += success?1:0;
					
					if(success && currentNonCrawledSize == 1) {
						crawlingMutex.notify();
					}
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

				synchronized(crawlingMutex) {
					
					try {
						while(currentNonCrawledSize == 0) {
							try {
								crawlingMutex.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
						
						res = controller.getNonCrawledRows(crawlConnection);
						controller.markNonCrawledRows(crawlConnection);
						currentNonCrawledSize--;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}
				
				
				
				try {
					res.next();
					ID = res.getInt(1);
					url = res.getString(2);
					
					webDoc = Jsoup.parse(new URL(url).openStream(), "ASCII", url);

					filePath = saveWebPage(webDoc, ID);
					
					if(!filePath.isEmpty()) {
						synchronized (DBMutex) {
							controller.insertURL(crawlConnection, url, filePath);
							Main.currentNonIndexedSize++;
							
							if(Main.currentNonIndexedSize == 1) {
								DBMutex.notify();
							}
						}
					}
					System.out.println("Crawled: " + url);
					
					extractLinks(webDoc);
					
				} catch (IOException | SQLException e) {
					System.err.println("for '"+url+"': "+e.getMessage());
				}
			}
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////


	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InterruptedException {

		Object dbmutex = new Object();
		Object crawlmutex = new Object();
		DBController dbController = new DBController();

		Crawler myCrawler = new Crawler(30, "seeder.txt", dbController, dbmutex, crawlmutex);

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
