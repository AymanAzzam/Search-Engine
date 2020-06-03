import java.util.*;




public class PhraseSearch {
	
	Hashtable<String, ArrayList<Ranker.WordValue>> invertedFile = new Hashtable <String, ArrayList<Ranker.WordValue>> ();
	Hashtable<String, Ranker.WebsiteValue> linkDatabase = new Hashtable <String, Ranker.WebsiteValue> ();
	Integer totalNumberOfDocuments;
	String phrase;
	
	
	public PhraseSearch(Hashtable<String, ArrayList<Ranker.WordValue>> invertedFile, Hashtable<String, Ranker.WebsiteValue> linkDatabase,
			Integer totalNumberOfDocuments, String phrase) {
		this.invertedFile = invertedFile;
		this.linkDatabase = linkDatabase;
		this.totalNumberOfDocuments = totalNumberOfDocuments;
		this.phrase = phrase;
	}
	
	public ArrayList<Ranker.OutputValue> phraseSearch() {
		Ranker rankerObject = new Ranker(invertedFile, linkDatabase, totalNumberOfDocuments);
		ArrayList<Ranker.WebsiteTFIDFPair>rankerResult = rankerObject.helper();
		String[] words = phrase.split("\\s+");
		Integer numberOfPhraseWords = words.length;
		ArrayList<Ranker.OutputValue> phraseSearchOutput = new ArrayList<Ranker.OutputValue> ();
		
		for (int i=0; i< rankerResult.size(); i++) {
			
			if (rankerResult.get(i).getNumberOfWords()< numberOfPhraseWords)
				break;
			
			String websiteName = rankerResult.get(i).getWebsiteName();
			
			if (linkDatabase.get(websiteName).getContent().contains(phrase)) {
				String headerText = linkDatabase.get(websiteName).getHeaderText();
				String summary = linkDatabase.get(websiteName).getSummary();
				Ranker.OutputValue val = new Ranker.OutputValue(websiteName, headerText, summary);
				phraseSearchOutput.add(val);
			}
		}
		return phraseSearchOutput;
		
	}
}
