import java.util.ArrayList;
import java.util.Arrays;

public class Main {

	 public static void main(String []args) throws FileNotFoundException,Exception {
	       		
			//Receiving Request called query it's type is query
			
			String query = "Received Request" ;
			ArrayList<String> queryWords, invertedFileElement, linkFileElement;
			DBController dbController = new DBController();
			
			queryWords = QueryProcessor.query(query);

			Hashtable<String, WebsiteValue> linkDatabase = new Hashtable <String, WebsiteValue> ();
			Hashtable<String, ArrayList<WordValue>> invertedFile = new Hashtable <String, ArrayList<WordValue>> ();
			
			for(int i=1; i<queryWords.size(); i++)
			{
				
				ArrayList<WordValue> invertedFileTempList = new ArrayList<WordValue> ();

				invertedFileElement = dbController.getInvertedFile(queryWords.get(i));
				for(int j=0; j<invertedFileElement.size(); j+=4)
				{
					linkFileElement = dbController.getUrlFile(invertedFileElement.get(j));
					invertedFileElement.set(j,linkFileElement.get(1));

					/************** Comments for Menna ********************/
					// add the following three elements in the object that you want for linkDatabase 
					// then add the object to the hashtable that you should create with the defined variables above
					// linkFileElement.get(0) is the header
					// linkFileElement.get(1) is the link
					// linkFileElement.get(2) is the content
					// linkFileElement.get(3) is the content
					WebsiteValue websiteValue = new WebsiteValue(linkFileElement.get(3), linkFileElement.get(0), linkFileElement.get(2));
					
					linkDatabase.put(linkFileElement.get(1), websiteValue);
					// add the following elements in the object that you want for invertedFile 
					// then add the object to the ArrayList<WordValue> that you should create with the defined variables above
					// invertedFileElement.get(j+1) is the URL
					// invertedFileElement.get(j+2) is the Plain Text Count
					// invertedFileElement.get(j+3) is the Header Count
					// invertedFileElement.get(j+4) is the total Count
					WordValue wordvalue = new WordValue(invertedFileElement.get(j+1), invertedFileElement.get(j+4), invertedFileElement.get(j+2));
					invertedFileTempList.add(wordvalue);
				}
				
				// And Here you shoud add this ArrayList<WordValue> to hashtable of invertedFile
				// queryWords.get(i) is the word
				invertedFile.put(queryWords.get(i), invertedFileTempList);
			}
			
			Integer dummyTotalNumberOfDocuments = 3;

			if(queryWords.get(0) == "1")
			{
				// It's a phrase Search and to get the query with alphabitcs, numbers and spaces only
				query = query.replaceAll("[^a-zA-Z0-9 ]", "");
				PhraseSearch phSearch = new PhraseSearch(invertedFile, linkDatabase, dummyTotalNumberOfDocuments, query);
				ArrayList<OutputValue> result = phSearch.phraseSearch();
	
			}
			else
			{
				// It's Normal Search
				Ranker ranker = new Ranker (invertedFile, linkDatabase, dummyTotalNumberOfDocuments);
				ArrayList<OutputValue> result = ranker.rank();
				
			}
	     }
}
