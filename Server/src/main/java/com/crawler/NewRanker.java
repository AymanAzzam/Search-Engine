package com.crawler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;


public class Ranker {
	
	Hashtable<String, ArrayList<WordValue>> invertedFile = new Hashtable <String, ArrayList<WordValue>> ();
	Hashtable<String, WebsiteValue> linkDatabase = new Hashtable <String, WebsiteValue> ();
	Hashtable<String, ArrayList<String>> pointingWebsites = new Hashtable<String, ArrayList<String>> ();
	Hashtable<String, Integer> pointedToCount = new Hashtable<String, Integer> ();
	Integer totalNumberOfDocuments;
	Integer normalOrImage;
	
	public Ranker(Hashtable<String, ArrayList<WordValue>> invertedFile, Hashtable<String, WebsiteValue> linkDatabase,
			Hashtable<String, ArrayList<String>> pointingWebsites , Hashtable<String, Integer> pointedToCount,
			Integer totalNumberOfDocuments, Integer normalOrImage) {
		this.invertedFile = invertedFile;
		this.linkDatabase = linkDatabase;
		this.pointingWebsites = pointingWebsites;
		this.pointedToCount = pointedToCount;
		this.totalNumberOfDocuments = totalNumberOfDocuments;
		this.normalOrImage = normalOrImage;
	}
	
	// to be private
	public Map<String,Double> calculateIDF() {
		
		Map<String,Double> mapIDF = new HashMap<String, Double>(); // create a map to store the word/IDF values
		Enumeration<String> enumeration = invertedFile.keys(); // to iterate on the invertedFile
		
		while(enumeration.hasMoreElements()) {
			String key = enumeration.nextElement();
			Integer listSize = invertedFile.get(key).size();
			Double idf;
			if (listSize == 0)
				idf = 0.0;
			else 
				idf = Math.log10(totalNumberOfDocuments/listSize);
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
			Hashtable<String,TFIDFValue> preTFIDFTable, Hashtable<String, Double> popularity) {
		
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
			counter += popularity.get(key); // add popularity to tf-idf
			WebsiteTFIDFPair pair = new WebsiteTFIDFPair(key, counter, preTFIDFTable.get(key).getNumberOfWords());
			websiteTFIDFList.add(pair);		
		}
		
		Collections.sort(websiteTFIDFList); //sort descendingly -- override CompareTo
		return websiteTFIDFList;		
	}
	
	public Hashtable<String, Double> calculatePopularity() {
		Hashtable<String, Double> popularityTable = new Hashtable<String, Double>();
		Hashtable<String, Double> previousPopularityTable = new Hashtable<String, Double>();
	
		
		Enumeration<String> enumeration = pointingWebsites.keys();
		Integer totalNumberOfWebsites = pointingWebsites.size();
		
		while(enumeration.hasMoreElements()) { // initiating popularity for all websites
			String websiteName = enumeration.nextElement(); // key 
			popularityTable.put(websiteName, 1.0/totalNumberOfWebsites);
		}
		previousPopularityTable = (Hashtable<String, Double>) popularityTable.clone();
		
		Integer i=0;
		enumeration = pointingWebsites.keys();
		while(i<100) {
			i = i+1;
			while(enumeration.hasMoreElements()) {
				String websiteName = enumeration.nextElement(); // key 
				Double temp =0.0;
				for (int j=0; j<pointingWebsites.get(websiteName).size() ; j++ ) {
					String web = pointingWebsites.get(websiteName).get(j);
					temp += previousPopularityTable.get(web)/pointedToCount.get(web);
					
				}
				popularityTable.put(websiteName, temp);				
			}
			previousPopularityTable = (Hashtable<String, Double>) popularityTable.clone();
		}
		
		return popularityTable;
	}
	
	public ArrayList<WebsiteTFIDFPair> helper(){
		
		Map<String,Double> mapIDF = calculateIDF(); //has word-IDF pair values
		filterSpam(); //remove spam websites
		Hashtable<String,TFIDFValue> preTFIDFTable = preTFIDF();
		Hashtable<String, Double> popularity = calculatePopularity();
		ArrayList<WebsiteTFIDFPair> sortedTFIDFList = formSortedTFIDFList(mapIDF,preTFIDFTable, popularity);
		return sortedTFIDFList;
	}
	
	public ArrayList<OutputValue> rankerOutput(ArrayList<WebsiteTFIDFPair> sortedTFIDFList){
		
		ArrayList<OutputValue>  outputArray = new ArrayList<OutputValue> ();
		for(int i=0; i< Integer.min(SearchEngine.MAX_RESULTS,sortedTFIDFList.size()); i++) {
			String websiteName = sortedTFIDFList.get(i).getWebsiteName();
			String headerText = linkDatabase.get(websiteName).getHeaderText();
			String summary = linkDatabase.get(websiteName).getSummary();
			OutputValue val = new OutputValue(websiteName, headerText, summary);
			outputArray.add(val);
		}
		return outputArray;		
	}

	public ArrayList<OutputImageValue> rankerImageOutput(ArrayList<WebsiteTFIDFPair> sortedTFIDFList, Connection conn, DBController controller){
		ArrayList<OutputImageValue> outputImageArray = new ArrayList<OutputImageValue>();
		
		try {
			conn = controller.connect();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		
		for (int i=0; i< Integer.min(SearchEngine.MAX_RESULTS,sortedTFIDFList.size()); i++) {
			String websiteURL = sortedTFIDFList.get(i).getWebsiteName();
			ArrayList<String> imageURL = controller.getImagesURLs(conn, websiteURL);
			
			if(imageURL.isEmpty()) {
				continue;
			}
			
			OutputImageValue outputImageValue = new OutputImageValue(websiteURL, imageURL);
			outputImageArray.add(outputImageValue);
		}
		
		return outputImageArray;
	}
	
	public Object rank(Connection conn, DBController control) {
		ArrayList<WebsiteTFIDFPair> helperOutput = helper();
		if (normalOrImage == 0 ) //normal search 
			return rankerOutput(helperOutput);
		else
			return rankerImageOutput(helperOutput, conn, control);
	}


}

