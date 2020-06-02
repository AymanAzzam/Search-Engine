import java.util.ArrayList;
import java.util.Arrays;

public class Main {

	 public static void main(String []args) throws FileNotFoundException,Exception {
	       		
			//Receiving Request called query it's type is query
			
			String query = "Received Request" ;
			ArrayList<String> queryWords, invertedFileElement, linkFileElement;
			DBController dbController = new DBController();
			
			queryWords = QueryProcessor.query(query);
			
			for(int i=1; i<queryWords.size(); i++)
			{
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

					// add the following elements in the object that you want for invertedFile 
					// then add the object to the ArrayList<WordValue> that you should create with the defined variables above
					// invertedFileElement.get(j+1) is the URL
					// invertedFileElement.get(j+2) is the Plain Text Count
					// invertedFileElement.get(j+3) is the Header Count
					// invertedFileElement.get(j+1) is the total Count
				}
				
				// And Here you shoud add this ArrayList<WordValue> to hashtable of invertedFile
				// queryWords.get(i) is the word
			}

			if(queryWords.get(0) == "1")
			{
				// It's a phrase Search and to get the query with alphabitcs, numbers and spaces only
				// query = query.replaceAll("[^a-zA-Z0-9 ]", "");
			}
			else
			{
				// It's Normal Search
			}
	     }
}
