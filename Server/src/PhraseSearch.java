import java.util.*;

public class PhraseSearch {
	
	Hashtable<String, ArrayList<WordValue>> invertedFile = new Hashtable <String, ArrayList<WordValue>> ();
	Hashtable<String, WebsiteValue> linkDatabase = new Hashtable <String, WebsiteValue> ();
	Integer totalNumberOfDocuments;
	String phrase;
	
	
	public PhraseSearch(Hashtable<String, ArrayList<WordValue>> invertedFile, Hashtable<String, WebsiteValue> linkDatabase,
			Integer totalNumberOfDocuments, String phrase) {
		this.invertedFile = invertedFile;
		this.linkDatabase = linkDatabase;
		this.totalNumberOfDocuments = totalNumberOfDocuments;
		this.phrase = phrase;
	}
	
	public ArrayList<OutputValue> phraseSearch() {
		Ranker rankerObject = new Ranker(invertedFile, linkDatabase, totalNumberOfDocuments);
		ArrayList<WebsiteTFIDFPair>rankerResult = rankerObject.helper();
		String[] words = phrase.split("\\s+");
		Integer numberOfPhraseWords = words.length;
		ArrayList<OutputValue> phraseSearchOutput = new ArrayList<OutputValue> ();
		
		for (int i=0; i< rankerResult.size(); i++) {
			
			if (rankerResult.get(i).getNumberOfWords()< numberOfPhraseWords)
				break;
			
			String websiteName = rankerResult.get(i).getWebsiteName();
			
			if (linkDatabase.get(websiteName).getContent().contains(phrase)) {
				String headerText = linkDatabase.get(websiteName).getHeaderText();
				String summary = linkDatabase.get(websiteName).getSummary();
				OutputValue val = new OutputValue(websiteName, headerText, summary);
				phraseSearchOutput.add(val);
			}
		}
		return phraseSearchOutput;
		
	}
}
