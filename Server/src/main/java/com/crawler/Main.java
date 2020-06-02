package com.crawler;

import java.io.FileNotFoundException;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Hashtable;

public class Main {

	 public static void main(String []args) throws FileNotFoundException,Exception {
	       		
			//Receiving Request called query it's type is query
			
			String query = "Received Request" ;
			ArrayList<String> queryWords, invertedFileElement, linkFileElement;
			
			/********** Abo Shama Should update the following Two lines **************/ 
			DBController dbController = new DBController();
			Connection conn = dbController.connect();
			
			queryWords = QueryProcessor.query(query);

			Hashtable<String, WebsiteValue> linkDatabase = new Hashtable <String, WebsiteValue> ();
			Hashtable<String, ArrayList<WordValue>> invertedFile = new Hashtable <String, ArrayList<WordValue>> ();
			
			for(int i=1; i<queryWords.size(); i++)
			{
				
				ArrayList<WordValue> invertedFileTempList = new ArrayList<WordValue> ();

				invertedFileElement = dbController.getInvertedFile(conn,queryWords.get(i));
				for(int j=0; j<invertedFileElement.size(); j+=4)
				{
					linkFileElement = dbController.getUrlFile(conn,invertedFileElement.get(j));
					invertedFileElement.set(j,linkFileElement.get(1));

					
					WebsiteValue websiteValue = new WebsiteValue(Integer.parseInt(linkFileElement.get(3)), linkFileElement.get(0), linkFileElement.get(2));
					linkDatabase.put(linkFileElement.get(1), websiteValue);
					
					WordValue wordvalue = new WordValue(invertedFileElement.get(j+1), Integer.parseInt(invertedFileElement.get(j+4)), Integer.parseInt(invertedFileElement.get(j+2)));
					invertedFileTempList.add(wordvalue);
				}
				
				invertedFile.put(queryWords.get(i), invertedFileTempList);
			}
			
			Integer dummyTotalNumberOfDocuments = 3;

			if(queryWords.get(0) == "1")
			{
				query = query.replaceAll("[^a-zA-Z0-9 ]", "");
				PhraseSearch phSearch = new PhraseSearch(invertedFile, linkDatabase, dummyTotalNumberOfDocuments, query);
				ArrayList<OutputValue> result = phSearch.phraseSearch();
	
			}
			else
			{
				Ranker ranker = new Ranker (invertedFile, linkDatabase, dummyTotalNumberOfDocuments);
				ArrayList<OutputValue> result = ranker.rank();
			}
	     }
}
