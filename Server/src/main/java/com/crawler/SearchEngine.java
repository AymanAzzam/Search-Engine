package com.crawler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.servlet.http.*;
import javax.sound.midi.ControllerEventListener;

import org.json.JSONArray;

public class SearchEngine extends HttpServlet{
	
	final static int MAX_RESULTS = 100;
	
	
	public static void main(String []args) throws FileNotFoundException,Exception {
			  
		
		//Receiving Request called query it's type is query
		
		try {
			
			//Receiving Request called query it's type is query
			
			String query = "youtube" ;
			
			int type = 0;
			
			ArrayList<String> queryWords, invertedFileElement, linkFileElement;
			
			/********** Abo Shama Should update the following Two lines **************/ 
			DBController dbController = new DBController();
			Connection conn = dbController.connect();
			
			queryWords = query(query);
			
			Hashtable<String, WebsiteValue> linkDatabase = new Hashtable <String, WebsiteValue> ();
			Hashtable<String, ArrayList<WordValue>> invertedFile = new Hashtable <String, ArrayList<WordValue>> ();
			
			for(int i=1; i<queryWords.size(); i++)
			{
				
				ArrayList<WordValue> invertedFileTempList = new ArrayList<WordValue> ();
				
				invertedFileElement = dbController.getInvertedFile(conn,queryWords.get(i));
				
				for(int j=0; j<invertedFileElement.size(); j+=4)
				{
					linkFileElement = dbController.getUrlFile(conn,Integer.parseInt(invertedFileElement.get(j)));
					invertedFileElement.set(j,linkFileElement.get(1));
					
					WebsiteValue websiteValue = new WebsiteValue(Integer.parseInt(linkFileElement.get(3)), linkFileElement.get(0), linkFileElement.get(2));
					linkDatabase.put(linkFileElement.get(1), websiteValue);
					
					WordValue wordvalue = new WordValue(invertedFileElement.get(j), Integer.parseInt(invertedFileElement.get(j+3)), Integer.parseInt(invertedFileElement.get(j+1)), Integer.parseInt(invertedFileElement.get(j+2)));
					invertedFileTempList.add(wordvalue);
				}
				
				invertedFile.put(queryWords.get(i), invertedFileTempList);
			}
			
			Integer dummyTotalNumberOfDocuments = dbController.getURLsSize(conn);
			

			if(!Ranker.donePopularity) {
				int siz = dbController.getCrawlingSize(conn);
				if(siz == Main.MAX_LINKS_CNT) {
					Ranker.donePopularity = true;
					
					Hashtable<String, ArrayList<String>> pointingWebsites = new Hashtable<String, ArrayList<String>>();
					Hashtable<String, Integer> pointedToCount = new Hashtable<String, Integer>();
					
					ArrayList<String> URLs = dbController.getAllURLs(conn);

					for(String url:URLs) {
						pointingWebsites.put(url, dbController.getPointedFromURLs(conn, url));
						pointedToCount.put(url, dbController.getPointingToCount(conn, url));
					}
					
					Ranker.calculatePopularity(pointingWebsites, pointedToCount);
				}
			}

			
			
			Object result;
			
			
			
			JSONArray json;
			String dummyLocation = "Egypt";
			if(queryWords.get(0) == "1")
			{
				query = query.replaceAll("[^a-zA-Z0-9 ]", "");
				
				PhraseSearch phSearch = new PhraseSearch(invertedFile, linkDatabase, dummyTotalNumberOfDocuments,dummyLocation, query);
				result = phSearch.phraseSearch();
				
				json = new JSONArray((ArrayList<OutputValue>)result);
			}
			else
			{
				Ranker ranker = new Ranker (invertedFile, linkDatabase, dummyTotalNumberOfDocuments, type, dummyLocation);
				result = ranker.rank(conn, dbController);
				
				if(type == 0) {
					
					ArrayList<OutputValue> tmp = ((ArrayList<OutputValue>)result);
					for(int i=1 ; i<queryWords.size() ; ++i) {
						ArrayList<OutputValue> ret = dbController.getMatchingURLs(conn, queryWords.get(i));
						tmp.addAll(0, ret);
					}

					int siz = tmp.size();
					for(int i=0 ; i<siz ; ++i) {
						for(int j=i+1 ; j<siz ; ++j) {
							if(tmp.get(i).getWebsiteName().equals(tmp.get(j).getWebsiteName())) {
								// System.out.println(tmp.get(i).getWebsiteName() + " " + tmp.get(j).getWebsiteName());
								tmp.remove(j);
								--j;
								--siz;
							}
						}
					}

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
    	if(sentence.charAt(0) == '"' && sentence.charAt(sentence.length()-1) == '"')
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
			
			int type = typeString.equals("Image")?1:0;
			
			ArrayList<String> queryWords, invertedFileElement, linkFileElement;
			
			/********** Abo Shama Should update the following Two lines **************/ 
			DBController dbController = new DBController();
			Connection conn = dbController.connect();
			
			queryWords = query(query);
			
			Hashtable<String, WebsiteValue> linkDatabase = new Hashtable <String, WebsiteValue> ();
			Hashtable<String, ArrayList<WordValue>> invertedFile = new Hashtable <String, ArrayList<WordValue>> ();
			
			for(int i=1; i<queryWords.size(); i++)
			{
				
				ArrayList<WordValue> invertedFileTempList = new ArrayList<WordValue> ();
				
				invertedFileElement = dbController.getInvertedFile(conn,queryWords.get(i));
				
				for(int j=0; j<invertedFileElement.size(); j+=4)
				{
					linkFileElement = dbController.getUrlFile(conn,Integer.parseInt(invertedFileElement.get(j)));
					invertedFileElement.set(j,linkFileElement.get(1));
					
					WebsiteValue websiteValue = new WebsiteValue(Integer.parseInt(linkFileElement.get(3)), linkFileElement.get(0), linkFileElement.get(2));
					linkDatabase.put(linkFileElement.get(1), websiteValue);
					
					WordValue wordvalue = new WordValue(invertedFileElement.get(j), Integer.parseInt(invertedFileElement.get(j+3)), Integer.parseInt(invertedFileElement.get(j+1)), Integer.parseInt(invertedFileElement.get(j+2)));
					invertedFileTempList.add(wordvalue);
				}
				
				invertedFile.put(queryWords.get(i), invertedFileTempList);
			}
			
			Integer dummyTotalNumberOfDocuments = dbController.getURLsSize(conn);
			

			if(!Ranker.donePopularity) {
				int siz = dbController.getCrawlingSize(conn);
				if(siz == Main.MAX_LINKS_CNT) {
					Ranker.donePopularity = true;
					
					Hashtable<String, ArrayList<String>> pointingWebsites = new Hashtable<String, ArrayList<String>>();
					Hashtable<String, Integer> pointedToCount = new Hashtable<String, Integer>();
					
					ArrayList<String> URLs = dbController.getAllURLs(conn);

					for(String url:URLs) {
						pointingWebsites.put(url, dbController.getPointedFromURLs(conn, url));
						pointedToCount.put(url, dbController.getPointingToCount(conn, url));
					}

					Ranker.calculatePopularity(pointingWebsites, pointedToCount);
				}
			}

			
			
			Object result;
			
			
			
		    response.setContentType("application/json");
		    response.setCharacterEncoding("UTF-8");
			JSONArray json;
			String dummyLocation = "Egy";
			if(queryWords.get(0) == "1")
			{
				query = query.replaceAll("[^a-zA-Z0-9 ]", "");
				PhraseSearch phSearch = new PhraseSearch(invertedFile, linkDatabase, dummyTotalNumberOfDocuments,dummyLocation, query);
				result = phSearch.phraseSearch();

				json = new JSONArray((ArrayList<OutputValue>)result);
			}
			else
			{
				Ranker ranker = new Ranker (invertedFile, linkDatabase, dummyTotalNumberOfDocuments, type, dummyLocation);
				result = ranker.rank(conn, dbController);
				
				if(type == 0) {
					
					ArrayList<OutputValue> tmp = ((ArrayList<OutputValue>)result);
					for(int i=1 ; i<queryWords.size() ; ++i) {
						ArrayList<OutputValue> ret = dbController.getMatchingURLs(conn, queryWords.get(i));
						tmp.addAll(0, ret);
					}
					
					int siz = tmp.size();
					for(int i=0 ; i<siz ; ++i) {
						for(int j=i+1 ; j<siz ; ++j) {
							if(tmp.get(i).getWebsiteName().equals(tmp.get(j).getWebsiteName())) {
								// System.out.println(tmp.get(i).getWebsiteName() + " " + tmp.get(j).getWebsiteName());
								tmp.remove(j);
								--j;
								--siz;
							}
						}
					}
					
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
