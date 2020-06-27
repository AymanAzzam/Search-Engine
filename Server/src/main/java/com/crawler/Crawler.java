package com.crawler;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.sql.*;

import java.io.FileWriter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {
	// data members:
	private static DBController controller;
	private final int MAX_LINKS_COUNT;
	// private HashSet<String> visitedLinks;
	// private Queue <String> toBeProcessedLinks;
	// private int visitedLinksCnt;
	// private int linksCnt;
	private java.sql.Connection mainCrawlerConnection;
	private Object crawlingMutex, DBMutex;
	private int totalCrawlingSize;
	private int currentNonCrawledSize;

	// constructor:
	public Crawler(int maxNoOfLinks, String seederFileName, DBController dbController, Object dbmutex,
			Object crawlmutex) throws ClassNotFoundException, SQLException {

		MAX_LINKS_COUNT = maxNoOfLinks;
		controller = dbController;
		mainCrawlerConnection = controller.connect();
		DBMutex = dbmutex;
		crawlingMutex = crawlmutex;
		seed(seederFileName);

		totalCrawlingSize = controller.getCrawlingSize(mainCrawlerConnection);
		currentNonCrawledSize = controller.checkNonCrawled(mainCrawlerConnection);
		Main.numberOfConnections++;
	}

	// seed
	public void seed(String fileName) {

		try {

			File seeder = new File(fileName);
			Scanner reader = new Scanner(seeder);
			ArrayList<String> URLs = new ArrayList<String>();
			// read and add links
			while (reader.hasNextLine()) {
				// read the link
				URLs.add(reader.nextLine());

			}
			controller.insertCrawlingURLs(mainCrawlerConnection, URLs);
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred while open the seeder...");
		}
	}

	public class Crawl extends Thread {
		private java.sql.Connection crawlConnection;
		private ResultSet res;

		public Crawl() throws SQLException, InterruptedException {
			Main.connectionSemaphore.acquire();
			crawlConnection = controller.connect();
		}

		public void run() {

			while (true) {

				synchronized (crawlingMutex) {

					try {
						while (currentNonCrawledSize == 0) {
							if (totalCrawlingSize >= MAX_LINKS_COUNT) {
								crawlingMutex.notify();

								crawlConnection.close();
								Main.connectionSemaphore.release();

								return;
							}

							try {
								crawlingMutex.wait();
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}

						int limit = (currentNonCrawledSize + Main.CRAWLER_CNT - 1) / Main.CRAWLER_CNT;

						res = controller.getNonCrawledRows(crawlConnection, limit);
						int cnt = controller.markNonCrawledRows(crawlConnection, limit);
						currentNonCrawledSize -= cnt;
					} catch (SQLException e) {
						e.printStackTrace();
					}
				}

				try {
					while (res.next()) {

						try {
							Main.connectionSemaphore.acquire();

							System.out.println("Acquire: " + Main.connectionSemaphore.availablePermits());
							new Processor(res.getInt(1), res.getString(2)).start();

						} catch (InterruptedException e) {
							e.printStackTrace();
						}

					}

				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public class Processor extends Thread {
		private String filePath;
		private String url;
		private int ID;
		private Document webDoc;
		private Connection processorConnection;

		public Processor(int id, String URL) throws SQLException {
			ID = id;
			url = URL;
			processorConnection = controller.connect();
		}

		// extract links:
		public void extractLinks(Document htmlDocument, String url) {

			Elements webPagesOnHtml = htmlDocument.select("a[href]");

			ArrayList<String> URLs = new ArrayList<String>();

			for (Element webpPage : webPagesOnHtml) {
				String newURL = webpPage.attr("abs:href");

				/**
				 * TODO: Validate URL Here
				 */
				URLs.add(newURL);

			}

			synchronized (crawlingMutex) {

				if (totalCrawlingSize >= MAX_LINKS_COUNT) {
					return;
				}

				while (URLs.size() + totalCrawlingSize > MAX_LINKS_COUNT)
					URLs.remove(URLs.size() - 1);

				int added = controller.insertCrawlingURLs(processorConnection, URLs);
				controller.insertRefs(processorConnection, url, URLs);

				totalCrawlingSize += added;
				currentNonCrawledSize += added;

				if (added > 0 && currentNonCrawledSize == added) {
					crawlingMutex.notifyAll();
				}
			}
		}

		// save html file contains the webpage content:
		// imgs - title - h1->h6 - plaintext -bodys
		public String saveWebPage(Document webPage, int docID, String url) {

			try {
				FileWriter myWriter = new FileWriter("docs/doc" + docID + ".txt");

				// images::
				myWriter.write("#IMAGES\n");
				Elements images = webPage.select("img");
				for (Element image : images) {
					String imageLink = image.attr("src");

					try {

						URL valid = new URL(imageLink);
					} catch (Exception e) {

						if (imageLink.length() > 2 && imageLink.substring(0, 2).equals("//")) {
							imageLink = imageLink.substring(2, imageLink.length());
						} else if (!imageLink.isEmpty() && imageLink.charAt(0) == '/') {
							imageLink = url.concat(imageLink);
						}
					}

					myWriter.write(imageLink + "\n");
				}

				// title::
				myWriter.write("#TITLE\n");
				String title = webPage.title();
				title = getEnglishText(title);
				myWriter.write(title + "\n");

				// headers::
				myWriter.write("#HEADERS\n");
				Elements headers = webPage.select("h1, h2, h3, h4, h5, h6");
				for (Element header : headers) {
					String headerText = header.text();
					headerText = getEnglishText(headerText);
					myWriter.write(headerText + "\n");
				}

				// plaintext
				myWriter.write("#PLAINTEXT\n");
				Elements plains = webPage.select("p");
				for (Element plain : plains) {
					String plainText = plain.text();
					plainText = getEnglishText(plainText);
					myWriter.write(plainText + "\n");
				}

				// body
				myWriter.write("#BODY\n");
				String body = webPage.body().text();
				// TODO: Check Null here
				body = getEnglishText(body);
				myWriter.write(body);

				myWriter.close();

				return new String("docs/doc" + docID + ".txt");

			} catch (IOException e) {
				// e.printStackTrace();
				return new String("");
			}

		}

		// filter webpage content:
		public String getEnglishText(String text) {
			Pattern pattern = Pattern.compile("[^a-zA-Z 0-9\n]");
			Matcher matcher = pattern.matcher(text);
			String englishText = matcher.replaceAll("");
			return englishText;
		}

		public void run() {

			try {
				webDoc = Jsoup.parse(new URL(url).openStream(), "ASCII", url);

				filePath = saveWebPage(webDoc, ID, url);
				if (!filePath.isEmpty()) {
					controller.insertURL(processorConnection, url, filePath);
					Main.currentNonIndexedSize++;
				}

				System.out.println("Crawled: " + url);
				if (totalCrawlingSize < MAX_LINKS_COUNT) {
					extractLinks(webDoc, url);
				}

			} catch (IOException e) {
				System.err.println("for '" + url + "': " + e.getMessage());
				// e.printStackTrace();
			}

			try {

				processorConnection.close();
		        System.out.println("Release: " + Main.connectionSemaphore.availablePermits());
				Main.connectionSemaphore.release();

			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	
	///////////////////////////////////////////////////////////////////////////////////////


	public static void main(String[] args) throws IOException, ClassNotFoundException, SQLException, InterruptedException {

		URL test = new URL("https://en.wikipedia.org/wiki/File:Black_Moshannon_State_Park_(Revisited).jpg");
		
		System.out.println(test.getHost());
		System.out.println(test.getPort());
		System.out.println(test.getPath());
		System.out.println(test.getFile());
		System.out.println(test.getQuery());
		System.out.println(test.getRef());
		System.out.println(test.getUserInfo());
		
		
//		Object dbmutex = new Object();
//		Object crawlmutex = new Object();
//		DBController dbController = new DBController();
//
//		Crawler myCrawler = new Crawler(30, "seeder.txt", dbController, dbmutex, crawlmutex);
//
//		Crawl crawler1 = myCrawler.new Crawl();
//		Crawl crawler2 = myCrawler.new Crawl();
//		Crawl crawler3 = myCrawler.new Crawl();
//		Crawl crawler4 = myCrawler.new Crawl();
//		Crawl crawler5 = myCrawler.new Crawl();
//		Crawl crawler6 = myCrawler.new Crawl();
//		Crawl crawler7 = myCrawler.new Crawl();
//
//		crawler1.start();
//		crawler2.start();
//		crawler3.start();
//		crawler4.start();
//		crawler5.start();
//		crawler6.start();
//		crawler7.start();
	}

}
