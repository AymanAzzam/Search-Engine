import java.util.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Enumeration;


public class Ranker {
	
	Hashtable<String, ArrayList<WordValue>> invertedFile = new Hashtable <String, ArrayList<WordValue>> ();
	Hashtable<String, WebsiteValue> linkDatabase = new Hashtable <String, WebsiteValue> ();
	Integer totalNumberOfDocuments;
	
	public Ranker(Hashtable<String, ArrayList<WordValue>> invertedFile, Hashtable<String, WebsiteValue> linkDatabase,
			Integer totalNumberOfDocuments) {
		this.invertedFile = invertedFile;
		this.linkDatabase = linkDatabase;
		this.totalNumberOfDocuments = totalNumberOfDocuments;
	}
	
	// to be private
	public Map<String,Double> calculateIDF() {
		
		Map<String,Double> mapIDF = new HashMap<String, Double>(); // create a map to store the word/IDF values
		Enumeration<String> enumeration = invertedFile.keys(); // to iterate on the invertedFile
		
		while(enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();
			Integer listSize = invertedFile.get(key).size();
			Double idf = Math.log10(totalNumberOfDocuments/listSize);
			mapIDF.put(key, idf);
		}
	
		return mapIDF;		
	}
	
	// to be private
	public void filterSpam() {
		
		//spam if number of appearance of ward > 0.5 the number of words in the document
		Enumeration<String> enumeration = invertedFile.keys(); // to iterate on the invertedFile
		
		while(enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();
			for (int i=0; i<invertedFile.get(key).size(); i++) { //iterate on each ArrayList per word 				
				Integer numberOfAppearance = invertedFile.get(key).get(i).getNumberOfAppearance();
				String websiteName = invertedFile.get(key).get(i).getWebsiteName();
				Integer numberOfWordsInDoc = linkDatabase.get(websiteName).getTotalNumberOfWords();
				if (numberOfAppearance >= 0.5*numberOfWordsInDoc ) {
					invertedFile.get(key).remove(i);
					i--;
				}
			}		
		}
	}
	
	
	public Hashtable<String,TFIDFValue> preTFIDF() {
		
		Hashtable<String,TFIDFValue> preTFIDFTable = new Hashtable<String,TFIDFValue> ();
		Enumeration<String> enumeration = invertedFile.keys(); // to iterate on the invertedFile
		
		while(enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();
			for(int i =0; i< invertedFile.get(key).size(); i++) {
				String websiteName = invertedFile.get(key).get(i).getWebsiteName();
				if (preTFIDFTable.containsKey(websiteName)) {
					preTFIDFTable.get(websiteName).incrementNumberOfWords();
					preTFIDFTable.get(websiteName).addToInnerArray(key, i);
				}
				else {
					TFIDFValue tfidfValue = new TFIDFValue (1, key, i);
					preTFIDFTable.put(websiteName, tfidfValue);
				}
			}
			
		}
		return preTFIDFTable;
		
	}
	
	
	public ArrayList<WebsiteTFIDFPair> formSortedTFIDFList (Map<String,Double> mapIDF,
			Hashtable<String,TFIDFValue> preTFIDFTable) {
		
		ArrayList<WebsiteTFIDFPair>  websiteTFIDFList = new ArrayList<WebsiteTFIDFPair>  ();
		Enumeration<String> enumeration = preTFIDFTable.keys(); // to iterate on the preTFIDFTable
		
		while (enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();
			Double counter = 0.0;
			Integer tf = 0;
			Double idf = 0.0;
			Integer index = 0;
			String word;
			for (int i=0; i< preTFIDFTable.get(key).getInnerArraySize(); i++) {
				word = preTFIDFTable.get(key).getWordString(i);
				index = preTFIDFTable.get(key).getWordIndex(i);
				tf = invertedFile.get(word).get(index).getNumberOfAppearance();
				idf = mapIDF.get(word);
				counter = counter + tf*idf;
			}
			WebsiteTFIDFPair pair = new WebsiteTFIDFPair(key, counter, preTFIDFTable.get(key).getNumberOfWords());
			websiteTFIDFList.add(pair);		
		}
		
		Collections.sort(websiteTFIDFList); //sort descendingly -- override CompareTo
		return websiteTFIDFList;		
	}
	
	public ArrayList<WebsiteTFIDFPair> helper(){
		
		Map<String,Double> mapIDF = calculateIDF(); //has word-IDF pair values
		filterSpam(); //remove spam websites
		Hashtable<String,TFIDFValue> preTFIDFTable = preTFIDF();
		ArrayList<WebsiteTFIDFPair> sortedTFIDFList = formSortedTFIDFList(mapIDF,preTFIDFTable);
		return sortedTFIDFList;
	}
	
	public ArrayList<OutputValue> rankerOutput(ArrayList<WebsiteTFIDFPair> sortedTFIDFList){
		
		ArrayList<OutputValue>  outputArray = new ArrayList<OutputValue> ();
		for(int i=0; i< sortedTFIDFList.size(); i++) {
			String websiteName = sortedTFIDFList.get(i).getWebsiteName();
			String headerText = linkDatabase.get(websiteName).getHeaderText();
			String summary = linkDatabase.get(websiteName).getSummary();
			OutputValue val = new OutputValue(websiteName, headerText, summary);
			outputArray.add(val);
		}
		return outputArray;		
	}
	
	public ArrayList<OutputValue> rank() {
		ArrayList<WebsiteTFIDFPair> helperOutput = helper();
		return rankerOutput(helperOutput);
	}

}
