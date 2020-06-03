package com.crawler;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;
import javax.servlet.http.*;

import org.json.JSONArray;


public class SearchEngine extends HttpServlet{
	
	public static void main(String []args) throws FileNotFoundException,Exception {
       		
		//Receiving Request called query it's type is query
		
		String query = "Test Query" ;
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
				
				WordValue wordvalue = new WordValue(invertedFileElement.get(j), Integer.parseInt(invertedFileElement.get(j+3)), Integer.parseInt(invertedFileElement.get(j+1)));
				invertedFileTempList.add(wordvalue);
			}
			
			invertedFile.put(queryWords.get(i), invertedFileTempList);
		}
		
		Integer dummyTotalNumberOfDocuments = dbController.getURLsSize(conn);
		
		
		ArrayList<OutputValue> result;
		if(queryWords.get(0) == "1")
		{
			query = query.replaceAll("[^a-zA-Z0-9 ]", "");
			PhraseSearch phSearch = new PhraseSearch(invertedFile, linkDatabase, dummyTotalNumberOfDocuments, query);
			result = phSearch.phraseSearch();
		
		}
		else
		{
			Ranker ranker = new Ranker (invertedFile, linkDatabase, dummyTotalNumberOfDocuments);
			result = ranker.rank();
		}
		
		for(OutputValue o:result) {
			System.out.println(o.getWebsiteName() + "\n" + o.getHeaderText() + "\n" + o.getSummary());
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
    	
    	/*** For Testing Purpose ***/
//        System.out.println("\nAfter Query: ");
//        for(String word: queryWords)	System.out.println(word);
    	
    	return queryWords;
 	}
 
	public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
		try {
			
			//Receiving Request called query it's type is query
			
			String query = request.getParameter("Query") ;
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
					
					WordValue wordvalue = new WordValue(invertedFileElement.get(j), Integer.parseInt(invertedFileElement.get(j+3)), Integer.parseInt(invertedFileElement.get(j+1)));
					invertedFileTempList.add(wordvalue);
				}
				
				invertedFile.put(queryWords.get(i), invertedFileTempList);
			}
			
			Integer dummyTotalNumberOfDocuments = dbController.getURLsSize(conn);
			
			
			ArrayList<OutputValue> result;
			if(queryWords.get(0) == "1")
			{
				query = query.replaceAll("[^a-zA-Z0-9 ]", "");
				PhraseSearch phSearch = new PhraseSearch(invertedFile, linkDatabase, dummyTotalNumberOfDocuments, query);
				result = phSearch.phraseSearch();
				
			}
			else
			{
				Ranker ranker = new Ranker (invertedFile, linkDatabase, dummyTotalNumberOfDocuments);
				result = ranker.rank();
			}

		    response.setContentType("application/json");
		    response.setCharacterEncoding("UTF-8");


			JSONArray json = new JSONArray(result);
			response.getWriter().print(json);
//			for(OutputValue o:result) {
//				response.getWriter().println(o.getWebsiteName() + "\n" + o.getHeaderText() + "\n" + o.getSummary());
//			}
			
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace(response.getWriter());
		}
		response.getWriter().flush();
		
	}
	 
}
