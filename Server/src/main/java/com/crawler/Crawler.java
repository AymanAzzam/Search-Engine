package com.crawler;


import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import crawlercommons.robots.BaseRobotRules;
import crawlercommons.robots.SimpleRobotRules;
import crawlercommons.robots.SimpleRobotRulesParser;
import crawlercommons.robots.SimpleRobotRules.RobotRulesMode;

import java.sql.*;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class Crawler {
	// data members:
	private static DBController controller;
	private final int MAX_LINKS_COUNT;
	private String USER_AGENT = "HogwartsBot";
	// private HashSet<String> visitedLinks;
	// private Queue <String> toBeProcessedLinks;
	// private int visitedLinksCnt;
	// private int linksCnt;
	private java.sql.Connection mainCrawlerConnection;
	private Object crawlingMutex, DBMutex;
	private int totalCrawlingSize;
	private int currentNonCrawledSize;

	Map <String, String> countries;
	Map<String, BaseRobotRules> robotsRules;

	

	// constructor:
	public Crawler(int maxNoOfLinks, String seederFileName, String countriesFileName, DBController dbController, Object dbmutex,
			Object crawlmutex) throws ClassNotFoundException, SQLException, MalformedURLException, IOException {

		MAX_LINKS_COUNT = maxNoOfLinks;
		robotsRules = new HashMap<String, BaseRobotRules>();
		countries = new HashMap<String, String>();
		controller = dbController;
		mainCrawlerConnection = controller.connect();
		DBMutex = dbmutex;
		crawlingMutex = crawlmutex;
		seed(seederFileName);
		fillCountries(countriesFileName);

		totalCrawlingSize = controller.getCrawlingSize(mainCrawlerConnection);
		currentNonCrawledSize = controller.checkNonCrawled(mainCrawlerConnection);
		mainCrawlerConnection.close();
		// Main.numberOfConnections++;
	}

	// seed
	public void seed(String fileName) throws MalformedURLException, IOException {

		try {

			File seeder = new File(fileName);
			Scanner reader = new Scanner(seeder);
			ArrayList<String> URLs = new ArrayList<String>();
			// read and add links
			while (reader.hasNextLine()) {
				// read the link
				String url = reader.nextLine();
				if(checkAllowedByRobots(url)) {
					URLs.add(url);	
				}

			}
			controller.insertCrawlingURLs(mainCrawlerConnection, URLs);
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred while open the seeder...");
		}
	}

	//load the countries and thier domain extension:
	public void fillCountries(String fileName) throws MalformedURLException, IOException {

		try {

			File countriesFile = new File(fileName);
			Scanner reader = new Scanner(countriesFile);
			
			while (reader.hasNextLine()){
				String [] line = reader.nextLine().split("\\s");
				int l = line.length;
				String cd = line[0];
				String cntry = line[1];
				for (int i=2; i<l; i++){
					cntry = cntry +' '+line[i];
				}
				countries.put(cd,cntry);
			}
			reader.close();
		} catch (FileNotFoundException e) {
			System.out.println("An error occurred while open the countries file...");
		}
	}

	// Allowed URL to be crawled
	public boolean checkAllowedByRobots(String url) throws MalformedURLException, IOException {
	
		BaseRobotRules rules = robotsRules.get(url);

		if (rules == null) {

			try {
				URL UR = new URL(url);
				URLConnection roboConnection = new URL(UR.getProtocol() + "://" + UR.getHost() + "/robots.txt").openConnection();
				roboConnection.setConnectTimeout(10000);
				InputStream robotContent = roboConnection.getInputStream();
				byte[] robotFile = new byte[robotContent.available()];
				robotContent.read(robotFile);

				SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
				rules = robotParser.parseContent(url, robotFile, "text/plain", USER_AGENT);

				robotsRules.put(url, rules);
			} catch (Exception e) {
				rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
			}
		}
		return rules.isAllowed(url);
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
				// System.out.println("L");
				// Validate URL
				String cleanURL = validateURL(newURL);
				if (cleanURL != null) {
					URLs.add(cleanURL);
				}

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
					// System.out.println("I");

					// try {

					// URL valid = new URL(imageLink);
					// } catch (Exception e) {

					// if (imageLink.length() > 2 && imageLink.substring(0, 2).equals("//")) {
					// imageLink = imageLink.substring(2, imageLink.length());
					// } else if (!imageLink.isEmpty() && imageLink.charAt(0) == '/') {
					// imageLink = url.concat(imageLink);
					// }
					// }

					// Validate Image
					try {

						URL realURL = new URL(imageLink);
						// clean the url when it has parameters:
						String cleanURL = new String(
								realURL.getProtocol() + "://" + realURL.getHost() + realURL.getPath());

						if (validateImageURL(cleanURL)) {
							myWriter.write(cleanURL + "\n");
						}

					} catch (IOException e) {
						// e.printStackTrace()
					}

					// myWriter.write(imageLink + "\n");
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
					// System.out.println("H");
					String headerText = header.text();
					headerText = getEnglishText(headerText);
					myWriter.write(headerText + "\n");
				}

				// plaintext
				myWriter.write("#PLAINTEXT\n");
				Elements plains = webPage.select("p");
				for (Element plain : plains) {
					// System.out.println("P");
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
				e.printStackTrace();
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


		// check if the url is valid or not:
		public String validateURL(String url) {
			try {

				URL realURL = new URL(url);
				// clean the url when it has parameters:
				String cleanURL = new String(realURL.getProtocol() + "://" + realURL.getHost() + realURL.getPath());

				new URL(cleanURL).toURI();

				if(checkAllowedByRobots(cleanURL)) {
					return cleanURL;
				}
			
			} catch (Exception e) {}
			return null;
		}

		// check if the url contains image:
		public boolean validateImageURL(String imgURL) {

			// String url = validateURL(imgURL);

			// if(url == null) {
			// return null;
			// }

			try {
				BufferedImage img = ImageIO.read(new URL(imgURL));
				if (img != null) {
					return true;
				}
			} catch (MalformedURLException e) {
			} catch (IOException e) {
			}
			return false;
		}

		// return the location of webpage:
		public String getWebPageLocation(String url) {

			try {
				URL locURL = new URL(url);
				String host = locURL.getHost();

				char[] loc = new char[3];
				host.getChars(host.length() - 3, host.length(), loc, 0);
				String location = new String(loc);
				return countries.get(location);

			} catch (MalformedURLException e) {
				return null;
			}

		}

		public void run() {

			try {
				webDoc = Jsoup.parse(new URL(url).openStream(), "ASCII", url);

				filePath = saveWebPage(webDoc, ID, url);
				if (!filePath.isEmpty()) {
					String location = getWebPageLocation(url);
					controller.insertURL(processorConnection, url, filePath, location);
					// Main.currentNonIndexedSize++;
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

			} catch (SQLException e) {
				e.printStackTrace();
			}
			Main.connectionSemaphore.release();
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////

	public static void main(String[] args)
			throws IOException, ClassNotFoundException, SQLException, InterruptedException {
		
		// robotsRules = new HashMap<String, BaseRobotRules>();
		// System.out.println(checkAllowedByRobots("https://www.geeksforgeeks.org/"));
		// System.out.println(checkAllowedByRobots("https://www.geeksforgeeks.org/content-override.php"));
	
		// String USER_AGENT = "HogwartsBot";
		// String url = "https://www.geeksforgeeks.org/wp-admins/7moda-pasta";
		// URL urlObj = new URL(url);
		// String hostId = urlObj.getProtocol() + "://" + urlObj.getHost();
		// Map<String, BaseRobotRules> robotsRules = new HashMap<String, BaseRobotRules>();
		// BaseRobotRules rules = robotsRules.get(hostId);
		// if (rules == null) {
		// 	HttpClient httpclient = HttpClientBuilder.create().build();
		// 	HttpGet httpget = new HttpGet(hostId + "/robots.txt");
		// 	BasicHttpContext context = new BasicHttpContext();
		// 	HttpResponse response = httpclient.execute(httpget, context);
		// 	if (response.getStatusLine() != null && response.getStatusLine().getStatusCode() == 404) {
		// 		rules = new SimpleRobotRules(RobotRulesMode.ALLOW_ALL);
		// 		// consume entity to deallocate connection
		// 		EntityUtils.consumeQuietly(response.getEntity());
		// 	} else {
		// 		BufferedHttpEntity entity = new BufferedHttpEntity(response.getEntity());
		// 		SimpleRobotRulesParser robotParser = new SimpleRobotRulesParser();
		// 		byte[] targetArray = new byte[entity.getContent().available()];
		// 	    entity.getContent().read(targetArray);
		// 		rules = robotParser.parseContent(hostId, targetArray,
		// 				"text/plain", USER_AGENT);
		// 	}
		// 	robotsRules.put(hostId, rules);
		// }
		// for(String r:robotsRules.keySet()) {
		// 	System.out.println(r);
		// }
		// boolean urlAllowed = rules.isAllowed(url);
		// System.out.println(rules.isAllowed("https://www.geeksforgeeks.org/"));
		// System.out.println(rules.isAllowed("https://www.geeksforgeeks.org/content-override.php"));
		// try {
		//  String url = new String ("http://localhost:8080/GetTrends?Country=Egypt");
		//  URL test = new URL(url);
		
		// // System.out.println(url);
		// // Document doc = Jsoup.parse(test.openStream(), "ASCII", url);
		// InputStream in = test.openStream();
		// System.out.println("A");
		// } catch(Exception e) {
		// 	e.printStackTrace();
		// 	System.out.println("B");
		// }
		// Elements times = doc.select("time");
		// for (Element time : times) {
			
		// 	String item = time.attr("itemprop");
		// 	String dateTime = time.attr("datetime");
		// 	String pubDate = time.attr("pubdate");
		// 	if(item == null){
		// 		System.out.println("item is null");
		// 	}
		// 	else if (item.equals("")){
		// 		System.out.println("item is empty");
		// 	}
		// 	if(dateTime == null){
		// 		System.out.println("dateTime is null");
		// 	}
		// 	else if (dateTime.equals("")){
		// 		System.out.println("dateTime is empty");
		// 	}
		// 	if(pubDate == null){
		// 		System.out.println("pubDate is null");
		// 	}
		// 	else if (pubDate.equals("")){
		// 		System.out.println("pubDate is empty");
		// 	}
		// 	System.out.println("item prob: " + item);
		// 	System.out.println("datetime: " + dateTime);
		// 	System.out.println("pubDate: " + pubDate);


		// }

		//////////////////////////////////////////////////////////////////////////////////////////////////
		// String host = test.getHost();
		// char [] loc = new char [3];
		// host.getChars(host.length()-3, host.length(), loc, 0);
		// String location = new String (loc);
		// System.out.println(location);
		// StringBuilder locationReturned = new StringBuilder(location);

		// if(location.charAt(0)=='.'){
		// 	locationReturned.deleteCharAt(0);
		// } else {
		// 	locationReturned.delete(0, 3);
		// }
		// System.out.println(locationReturned.toString());

		/////////////////////////////////////////////////////////////////////////////////////////////////
		

		// System.out.println(test.getHost());
		// System.out.println(test.getPort());
		// System.out.println(test.getPath());
		// System.out.println(test.getFile());
		// System.out.println(test.getQuery());
		// System.out.println(test.getRef());
		// System.out.println(test.getUserInfo());
		
		
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
///////////////////////////////////////////////////////////////////////
		//countries:
		Map <String,String> cs = new HashMap <String,String> ();
		File cntriesfile = new File("cntrs.txt");
		Scanner reader = new Scanner (cntriesfile);
		while (reader.hasNextLine()){
			String [] line = reader.nextLine().split("\\s");
			int l = line.length;
			String cd = line[0];
			String cntry = line[1];
			for (int i=2; i<l; i++){
				cntry = cntry +' '+line[i];
			}
			cs.put(cd,cntry);
		}
		reader.close();
		if(cs.get("ag")== null){ System.out.println("nullllll");}
		System.out.println(cs.get("ag"));
		System.out.println(cs.get("ag").length());
		System.out.println(cs.size());
	}


	// String s1="java string split method by javatpoint";  
	// String[] words=s1.split("\\s");//splits the string based on whitespace  
	// //using java foreach loop to print elements of string array  
	// for(String w:words){  
	// 	System.out.println(w);
	// 	System.out.println(w.length());  
	// 	}
	//}  

}
