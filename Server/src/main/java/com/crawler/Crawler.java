package com.crawler;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
	private final int MAX_LINKS_COUNT;
	private HashSet<String> visitedLinks;
	Queue <String> toBeProcessedLinks;
	private int visitedLinksCnt;

	//constructor:
	public Crawler (int maxNoOfLinks) {

		visitedLinks = new HashSet <String>();
		toBeProcessedLinks = new LinkedList<String>();
		MAX_LINKS_COUNT = maxNoOfLinks;
		visitedLinksCnt = 0;

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

	//extract links:
	public void extractLinks(Document htmlDocument) {

		Elements webPagesOnHtml = htmlDocument.select("a[href]");

		for (Element webpPage : webPagesOnHtml) {
			String url = webpPage.attr("abs:href");
			//if the link wasnt visited yet and max count of links not reached then add it to be processed
			if(!visitedLinks.contains(url) && visitedLinksCnt < MAX_LINKS_COUNT) {
				visitedLinks.add(url);
				toBeProcessedLinks.add(url);
				visitedLinksCnt++;
			}
		}
	}

	//save html file contains the webpage content:
	//url - imgs - title - h1->h6
	public void saveWebPage(Document webPage, String url) {

	}

	public void crawel(String url) {

	}


	//filter webpage content:
	public static String getEnglishText(String text) {
	    Pattern pattern = Pattern.compile("[^a-zA-Z 0-9]");
	    Matcher matcher = pattern.matcher(text);
	    String englishText = matcher.replaceAll("");
	    return englishText;
	 }

























	///////////////////////////////////////////////////////////////////////////////////////
//	private HashSet<String> links;
//	private final int MAX_LINKS_CNT;
//	private int linksCnt;
//	private int failedCnt;
//	private int nonenglish;


	public static void main(String[] args) {

		Crawler myCrawler = new Crawler(10);
		myCrawler.seed("seeder.txt");


//		for(String link: myCrawler.toBeProcessedLinks) {
//			System.out.println(link);
//		}
//		String url = myCrawler.toBeProcessedLinks.remove();
//		try {
//			Document doc = Jsoup.parse(new URL(url).openStream(), "ASCII", url);
//			myCrawler.extractLinks(doc);
//			for(String link: myCrawler.toBeProcessedLinks) {
//				System.out.println(link);
//			}
//
//		} catch (IOException e ) {
//			System.err.println("for '"+url+"': "+e.getMessage());
//		}


		///////////////////////////////////////////////////////
//		Crawler myCrawler = new Crawler();
//		Queue <String> newLinks = new LinkedList <String>();
//		String URL = new String ("");
//
//
//
//		newLinks.add("https://www.facebook.com/");
//		newLinks.add("https://www.geeksforgeeks.org/");
//		while (newLinks.size()!=0) {
//			URL = newLinks.remove();
//			myCrawler.getLinks(URL,newLinks);
//		}
//		//for(String s : myCrawler.links) {
//			//System.out.println(s);
//		//}
//		System.out.println("Crawler linked: "+myCrawler.linksCnt);
//		System.out.println("Crawler failed: "+myCrawler.failedCnt);
//		System.out.println("Crawler noneng: "+myCrawler.nonenglish);
//		System.out.println("Crawler finished");
	}

//	public Crawler() {
//		// TODO Auto-generated constructor stub
//		links = new HashSet <String> ();
//		linksCnt  = 0;
//		failedCnt = 0;
//		nonenglish = 0;
//		MAX_LINKS_CNT = 30;
//	}

	//public void crawl()

//	public void getLinks(String url, Queue <String> q) {
//		if (!links.contains(url) && links.size() < MAX_LINKS_CNT) {
//			try {
//				if(links.add(url)) {
//					//linksCnt++;
//					//System.out.println(url);
//				}
//				//Connection.Response html = Jsoup.connect(url).execute();
//
//				byte [] bys = Jsoup.connect(url).execute().bodyAsBytes();
//				//Document doc =Jsoup.connect(url).get();
//				//Document doc1 = html.parse();
//				Document document = Jsoup.parse(new URL(url).openStream(), "ASCII", url);
//				String langu = new String();
//				Element taglang = document.selectFirst("html");
//				langu = taglang.attr("lang");
//
//				//System.out.println(langu);
//				//String pg = new String (document.outerHtml());
////				if(isEnglish(pg)) {
////					FileWriter myWriter = new FileWriter("doc"+linksCnt+".html");
////				    myWriter.write(document.outerHtml());
////				    myWriter.close();
////
////				}
//				if (langu.contains("en"))
//				{
//					FileOutputStream fos = new FileOutputStream("doc"+linksCnt+".html");
//					fos.write(bys);
//					fos.close();
//
//					//FileWriter myWriter = new FileWriter("doc"+linksCnt+".html");
//				    //myWriter.write(document.outerHtml());
//				    //myWriter.close();
//				    linksCnt++;
//				}else {
//					nonenglish++;
//					System.out.println("lang not english: "+nonenglish);
//				}
//
//				//System.out.println(doc1.outerHtml());
//				//System.out.println("title: "+doc1.title());
//				System.out.println(url);
//				//System.out.println(url.text());
//
//
//				Elements linksOnPage = document.select("a[href]");
//				for(Element page : linksOnPage) {
//					q.add(page.attr("abs:href"));
//				}
//			}catch (IOException e) {
//				failedCnt++;
//				System.err.println("for '"+url+"': "+e.getMessage());
//			}
//		}
//	}
//
//
//	public static boolean isEnglish(String text) {
//		int english = 0, nonEnglish =0;
//		 for (char character : text.toCharArray()) {
//		    if (Character.UnicodeBlock.of(character) != Character.UnicodeBlock.BASIC_LATIN
//		            /*|| Character.UnicodeBlock.of(character) == Character.UnicodeBlock.LATIN_1_SUPPLEMENT
//		            || Character.UnicodeBlock.of(character) == Character.UnicodeBlock.LATIN_EXTENDED_A
//		            || Character.UnicodeBlock.of(character) == Character.UnicodeBlock.GENERAL_PUNCTUATION*/) {
//		       english++;
//		    } else {nonEnglish++;}
//		 }
//		 if((double) english/text.length()>.1) {return true;}else {return false;}
//	}
//

}
