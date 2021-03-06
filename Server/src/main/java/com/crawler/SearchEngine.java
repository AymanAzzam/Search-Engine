package com.crawler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Properties;

import javax.servlet.http.*;

import org.json.JSONArray;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class SearchEngine extends HttpServlet {

	/**
	 *
	 */
	private static final long serialVersionUID = 1L;
	final static int MAX_RESULTS = 100;

	public class TrendsProcessor extends Thread {
		String query, location;
		DBController controller;
		Connection conn;

		public TrendsProcessor(String q, String loc, DBController dbController) throws SQLException {
			query = new String(q);
			location = new String(loc);
			controller = dbController;
			conn = dbController.connect();
		}

		public void run() {
			System.out.println("START:\t" + Thread.currentThread().getId());
			Annotation annotator = new Annotation(query);

			Properties properties = new Properties();
			properties.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,entitymentions");

			StanfordCoreNLP pipeline = new StanfordCoreNLP(properties);
			pipeline.annotate(annotator);
	
			for(CoreMap sentence : annotator.get(CoreAnnotations.SentencesAnnotation.class)) {
				for(CoreMap entityMention : sentence.get(CoreAnnotations.MentionsAnnotation.class)) {
					String type = entityMention.get(CoreAnnotations.EntityTypeAnnotation.class);
					String name = entityMention.toString();

					if(type.equals("PERSON")) {
						try {
							System.out.println(type + ":\t" + name);
							controller.insertTrend(conn, name, location);
						} catch (SQLException e) {
							e.printStackTrace();
						}
					}
				}
			}
			System.out.println("Finish:\t" + Thread.currentThread().getId());
		}
	}
	
	
	public static void main(String []args) throws FileNotFoundException,Exception {
			  
		//Receiving Request called query it's type is query
		
		try {
			
			//Receiving Request called query it's type is query
			
			String query = "top rated movies" ;
			
			int type = 0;
			
			ArrayList<String> queryWords, invertedFileElement, linkFileElement;
			
			DBController dbController = new DBController();
			Connection conn = dbController.connect();
			
			queryWords = query(query);
			
			Hashtable<String, WebsiteValue> linkDatabase = new Hashtable <String, WebsiteValue> ();
			Hashtable<String, ArrayList<WordValue>> invertedFile = new Hashtable <String, ArrayList<WordValue>> ();
			
			for(int i=1; i<queryWords.size(); i++)
			{

				ArrayList<WordValue> invertedFileTempList = new ArrayList<WordValue> ();
				
				invertedFileElement = dbController.getInvertedFile(conn,queryWords.get(i));
				
				LocalDate dummyPublishedDate =  LocalDate.now();
				for(int j=0; j<invertedFileElement.size(); j+=4)
				{
					linkFileElement = dbController.getUrlFile(conn,Integer.parseInt(invertedFileElement.get(j)));
					invertedFileElement.set(j,linkFileElement.get(1));

					/*
					 ************************************ 
					 the last two argument will change for website value
					 ************************************
					 */
					
					WebsiteValue websiteValue = new WebsiteValue(Integer.parseInt(linkFileElement.get(3)), linkFileElement.get(0), linkFileElement.get(2), linkFileElement.get(5), dummyPublishedDate, Integer.parseInt(linkFileElement.get(4)));
					
					linkDatabase.putIfAbsent(linkFileElement.get(1), websiteValue);
					

					WordValue wordvalue = new WordValue(invertedFileElement.get(j), Integer.parseInt(invertedFileElement.get(j+3)), Integer.parseInt(invertedFileElement.get(j+1)), Integer.parseInt(invertedFileElement.get(j+2)));
					invertedFileTempList.add(wordvalue);
				}
				
				ArrayList<String> matchingURLs = dbController.getMatchingURLsAndTitle(conn, queryWords.get(i));

				for(int j=0 ; j<matchingURLs.size() ; j+=6) {
					String url = matchingURLs.get(j);
					WebsiteValue websiteValue = new WebsiteValue(Integer.parseInt(matchingURLs.get(j+1)), matchingURLs.get(j+2), matchingURLs.get(j+3), matchingURLs.get(j+4), dummyPublishedDate, Integer.parseInt(matchingURLs.get(j+5)));
					linkDatabase.putIfAbsent(url, websiteValue);

					boolean exists = false;

					for(WordValue w:invertedFileTempList) {
						if(w.websiteName.equals(url)) {
							exists = true;
							break;
						}
					}

					if(!exists) {
						invertedFileTempList.add(new WordValue(url, 0, 0, 0));
					}
				}


				invertedFile.put(queryWords.get(i), invertedFileTempList);
			}
			
			Integer dummyTotalNumberOfDocuments = dbController.getURLsSize(conn);
			
			// System.out.println("POP: " + Ranker.donePopularity);

			// if(!Ranker.donePopularity) {
			// 	int siz = dbController.getCrawlingSize(conn);
			// 	System.out.println(siz + " #### " + Main.MAX_LINKS_CNT);
			// 	if(siz >= Main.MAX_LINKS_CNT) {
			// 		Ranker.donePopularity = true;
					
			// 		Hashtable<String, ArrayList<String>> pointingWebsites = new Hashtable<String, ArrayList<String>>();
			// 		Hashtable<String, Integer> pointedToCount = new Hashtable<String, Integer>();
					
			// 		ArrayList<String> URLs = dbController.getAllURLs(conn);

			// 		for(String url:URLs) {
			// 			pointingWebsites.put(url, dbController.getPointedFromURLs(conn, url));
			// 			pointedToCount.put(url, dbController.getPointingToCount(conn, url));
			// 		}
			// 		System.out.println("POPULARITY##########");

			// 		Ranker.calculatePopularity(pointingWebsites, pointedToCount);
			// 	}
			// }

			Hashtable<String, Double> popularity = dbController.getPopularity(conn);

			Object result;
			
			JSONArray json;
			String location = "Egy";
			if(queryWords.get(0) == "1")
			{
				query = query.replaceAll("[^a-zA-Z0-9 ]", "");
				PhraseSearch phSearch = new PhraseSearch(invertedFile, linkDatabase, dummyTotalNumberOfDocuments,location, query);
				result = phSearch.phraseSearch(popularity);

				json = new JSONArray((ArrayList<OutputValue>)result);
			}
			else
			{
				Ranker ranker = new Ranker (invertedFile, linkDatabase, dummyTotalNumberOfDocuments, type, location, popularity);
				result = ranker.rank(conn, dbController);
				
				if(type == 0) {
					
					// ArrayList<OutputValue> tmp = ((ArrayList<OutputValue>)result);
					// for(int i=1 ; i<queryWords.size() ; ++i) {
					// 	ArrayList<OutputValue> ret = dbController.getMatchingURLs(conn, queryWords.get(i));
					// 	tmp.addAll(0, ret);
					// }
					
					// int siz = tmp.size();
					// for(int i=0 ; i<siz ; ++i) {
					// 	for(int j=i+1 ; j<siz ; ++j) {
					// 		if(tmp.get(i).getWebsiteName().equals(tmp.get(j).getWebsiteName())) {
					// 			tmp.remove(j);
					// 			--j;
					// 			--siz;
					// 		}
					// 	}
					// }
					
					json = new JSONArray((ArrayList<OutputValue>)result);
				}
				else {
					
					json = new JSONArray((ArrayList<OutputImageValue>)result);
				}
			}
			
			conn.close();
			
			for(OutputValue o:(ArrayList<OutputValue>)result) {
				// System.out.println(o.getWebsiteName() + "\n" + o.getHeaderText() + "\n" + o.getSummary());
				System.out.println(o.getWebsiteName());
			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		
	}
	
	public static ArrayList<String> query(String sentence) throws FileNotFoundException,Exception
 	{
        ArrayList<String> queryWords = new ArrayList<String>();

        /*** Checking Phrase Search ***/
    	if(sentence.startsWith("\'") && sentence.endsWith("\'"))
    		queryWords.add("1");
    	else
    		queryWords.add("0");
    	
    	/*** Converting the Sentence into words ***/
    	queryWords.addAll(Main.steaming(sentence));
    	
    	return queryWords;
 	}
	
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			
			//Receiving Request called query it's type is query
			
			String query = request.getParameter("Query") ;
			String typeString = request.getParameter("Type");
			String location = request.getParameter("Location");
			
			int type = typeString.equals("Image")?1:0;


			
			System.out.println("Search:\t\t" + typeString + "\t" + query);
			
			ArrayList<String> queryWords, invertedFileElement, linkFileElement;
			
			/********** Abo Shama Should update the following Two lines **************/ 
			DBController dbController = new DBController();
			Connection conn = dbController.connect();
			
			// Processing the Trend
			new TrendsProcessor(query, location, dbController).start();


			queryWords = query(query);
			
			Hashtable<String, WebsiteValue> linkDatabase = new Hashtable <String, WebsiteValue> ();
			Hashtable<String, ArrayList<WordValue>> invertedFile = new Hashtable <String, ArrayList<WordValue>> ();
			
			for(int i=1; i<queryWords.size(); i++)
			{
				
				ArrayList<WordValue> invertedFileTempList = new ArrayList<WordValue> ();
				
				invertedFileElement = dbController.getInvertedFile(conn,queryWords.get(i));
				
				LocalDate dummyPublishedDate =  LocalDate.now();
				for(int j=0; j<invertedFileElement.size(); j+=4)
				{
					linkFileElement = dbController.getUrlFile(conn,Integer.parseInt(invertedFileElement.get(j)));
					invertedFileElement.set(j,linkFileElement.get(1));

					/*
					 ************************************ 
					 the last two argument will change for website value
					 ************************************
					 */
					
					WebsiteValue websiteValue = new WebsiteValue(Integer.parseInt(linkFileElement.get(3)), linkFileElement.get(0), linkFileElement.get(2), linkFileElement.get(5), dummyPublishedDate, Integer.parseInt(linkFileElement.get(4)));
					
					linkDatabase.putIfAbsent(linkFileElement.get(1), websiteValue);
					

					WordValue wordvalue = new WordValue(invertedFileElement.get(j), Integer.parseInt(invertedFileElement.get(j+3)), Integer.parseInt(invertedFileElement.get(j+1)), Integer.parseInt(invertedFileElement.get(j+2)));
					invertedFileTempList.add(wordvalue);
				}
				
				ArrayList<String> matchingURLs = dbController.getMatchingURLsAndTitle(conn, queryWords.get(i));

				for(int j=0 ; j<matchingURLs.size() ; j+=6) {
					String url = matchingURLs.get(j);
					WebsiteValue websiteValue = new WebsiteValue(Integer.parseInt(matchingURLs.get(j+1)), matchingURLs.get(j+2), matchingURLs.get(j+3), matchingURLs.get(j+4), dummyPublishedDate, Integer.parseInt(matchingURLs.get(j+5)));
					linkDatabase.putIfAbsent(url, websiteValue);

					boolean exists = false;

					for(WordValue w:invertedFileTempList) {
						if(w.websiteName.equals(url)) {
							exists = true;
							break;
						}
					}

					if(!exists) {
						invertedFileTempList.add(new WordValue(url, 0, 0, 0));
					}
				}
				
				invertedFile.put(queryWords.get(i), invertedFileTempList);
			}
			
			Integer dummyTotalNumberOfDocuments = dbController.getURLsSize(conn);
			
			// System.out.println("POP: " + Ranker.donePopularity);

			// if(!Ranker.donePopularity) {
			// 	int siz = dbController.getCrawlingSize(conn);
			// 	System.out.println(siz + " #### " + Main.MAX_LINKS_CNT);
			// 	if(siz >= Main.MAX_LINKS_CNT) {
			// 		Ranker.donePopularity = true;
					
			// 		Hashtable<String, ArrayList<String>> pointingWebsites = new Hashtable<String, ArrayList<String>>();
			// 		Hashtable<String, Integer> pointedToCount = new Hashtable<String, Integer>();
					
			// 		ArrayList<String> URLs = dbController.getAllURLs(conn);

			// 		for(String url:URLs) {
			// 			pointingWebsites.put(url, dbController.getPointedFromURLs(conn, url));
			// 			pointedToCount.put(url, dbController.getPointingToCount(conn, url));
			// 		}
			// 		System.out.println("POPULARITY##########");

			// 		Ranker.calculatePopularity(pointingWebsites, pointedToCount);
			// 	}
			// }

			Hashtable<String, Double> popularity = dbController.getPopularity(conn);

			Object result;
			
		    response.setContentType("application/json");
		    response.setCharacterEncoding("UTF-8");
			JSONArray json;
			// String location = "Egy";
			if(queryWords.get(0) == "1")
			{
				query = query.replaceAll("[^a-zA-Z0-9 ]", "");
				PhraseSearch phSearch = new PhraseSearch(invertedFile, linkDatabase, dummyTotalNumberOfDocuments,location, query);
				result = phSearch.phraseSearch(popularity);

				json = new JSONArray((ArrayList<OutputValue>)result);
			}
			else
			{
				Ranker ranker = new Ranker (invertedFile, linkDatabase, dummyTotalNumberOfDocuments, type, location, popularity);
				result = ranker.rank(conn, dbController);
				
				if(type == 0) {
					
					// ArrayList<OutputValue> tmp = ((ArrayList<OutputValue>)result);
					// for(int i=1 ; i<queryWords.size() ; ++i) {
					// 	ArrayList<OutputValue> ret = dbController.getMatchingURLs(conn, queryWords.get(i));
					// 	tmp.addAll(0, ret);
					// }
					
					// int siz = tmp.size();
					// for(int i=0 ; i<siz ; ++i) {
					// 	for(int j=i+1 ; j<siz ; ++j) {
					// 		if(tmp.get(i).getWebsiteName().equals(tmp.get(j).getWebsiteName())) {
					// 			tmp.remove(j);
					// 			--j;
					// 			--siz;
					// 		}
					// 	}
					// }
					
					json = new JSONArray((ArrayList<OutputValue>)result);
				}
				else {
					
					json = new JSONArray((ArrayList<OutputImageValue>)result);
				}
			}
			
			conn.close();
			response.getWriter().print(json);
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace(response.getWriter());
		}
		response.getWriter().flush();
		
	}
	 
}
