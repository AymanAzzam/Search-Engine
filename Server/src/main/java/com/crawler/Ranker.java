package com.crawler;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;


public class Ranker {
	
	Hashtable<String, ArrayList<WordValue>> invertedFile = new Hashtable <String, ArrayList<WordValue>> ();
	Hashtable<String, WebsiteValue> linkDatabase = new Hashtable <String, WebsiteValue> ();
	Hashtable<String, Double> popularity = new Hashtable<String, Double>();
	// static boolean donePopularity = false;
	Integer totalNumberOfDocuments;
	Integer normalOrImage;
	String userLocation;

	public Ranker(Hashtable<String, ArrayList<WordValue>> invertedFile, Hashtable<String, WebsiteValue> linkDatabase,
			Integer totalNumberOfDocuments, Integer normalOrImage, String userLocation, Hashtable<String, Double> popularity) {
		this.invertedFile = invertedFile;
		this.linkDatabase = linkDatabase;
		this.totalNumberOfDocuments = totalNumberOfDocuments;
		this.normalOrImage = normalOrImage;
		this.userLocation = userLocation;
		this.popularity = popularity;
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
				Double bodyweight = 0.001 * invertedFile.get(key).get(i).getNumberOfAppearance();
				Double plainWeight = 0.003 * invertedFile.get(key).get(i).getNumberOfPlain();
				Double headerWeight = 0.009 * invertedFile.get(key).get(i).getNumberOfHeader();
				Double totalWeight = bodyweight + plainWeight + headerWeight;
				if (preTFIDFTable.containsKey(websiteName)) {
					preTFIDFTable.get(websiteName).addToInnerArray(key, totalWeight,i);
					preTFIDFTable.get(websiteName).incrementNumberOfWords();
				}
				else {
					TFIDFValue tfidfValue = new TFIDFValue (1, key,totalWeight, i);
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
			Double tf = 0.0;
			Double idf = 0.0;
			Double titleWeight = 0.0;
			Integer URLMatchingWeight = 0;
			// Integer index = 0;
			String word;


			for (int i=0; i< preTFIDFTable.get(key).getInnerArraySize(); i++) {
				word = preTFIDFTable.get(key).getWordString(i);
				// index = preTFIDFTable.get(key).getWordIndex(i);
				//tf = invertedFile.get(word).get(index).getNumberOfAppearance();
				tf = preTFIDFTable.get(key).getWordsWeight(i);
				idf = mapIDF.get(word);
				counter = counter + tf*idf;

				// System.out.println(tf + " " + idf);
			}

			for(String w:invertedFile.keySet()) {
				
				if(key.toLowerCase().contains(w)) {
					URLMatchingWeight += 1;
				}

				if(linkDatabase.get(key).getHeaderText().toLowerCase().contains(w)) {
					titleWeight += idf;		// Check if IDF is appropriate
				}
			}
			
			// add popularity to tf-idf
			counter += popularity.get(key);
			
			// add frequency weight to tf-idf
			counter += 0.005 * linkDatabase.get(key).getFrequency();
			
			// add title match weight to tf-idf
			counter += titleWeight;
			
			// add url match weight to tf-idf
			counter += URLMatchingWeight;

			// add location weight to tf-idf
			String websiteLocation = linkDatabase.get(key).getLocation();
			if (userLocation == websiteLocation){
				counter += 0.05;
			}


			// add publishedDate weight to tf-idf
			//Date publishedDate = linkDatabase.get(key).getPublishedDate();
			/// date format yyyy/mm/dd as string
			LocalDate publishedDate = linkDatabase.get(key).getPublishedDate();
			if (publishedDate != null){
				counter += calculateDateWeight(publishedDate); 
			}
			
			WebsiteTFIDFPair pair = new WebsiteTFIDFPair(key, counter, preTFIDFTable.get(key).getNumeberOfWords());
			websiteTFIDFList.add(pair);		
		}
		
		Collections.sort(websiteTFIDFList); //sort descendingly -- override CompareTo
		
		// for(WebsiteTFIDFPair e:websiteTFIDFList) {
		// 	System.out.println(e.TFIDFValue + "\t" + e.websiteName);
			
		// }


		return websiteTFIDFList;		
	}

	private Double calculateDateWeight(LocalDate publishedDate) {
		Double weight = 0.003;
		LocalDate currentDate = LocalDate.now(); // Create a date object
		long noOfDaysBetween = ChronoUnit.DAYS.between(publishedDate, currentDate);
		if (noOfDaysBetween == 0){
			weight *= 1;
		}
		else{
			weight *= 1/noOfDaysBetween;
		}
		
		return weight;
	}
	
	public static Hashtable<String, Double> calculatePopularity(Hashtable<String, ArrayList<String>> pointingWebsites,
				Hashtable<String, Integer> pointedToCount) {

		Hashtable<String, Double> previousPopularityTable = new Hashtable<String, Double>();
		Hashtable<String, Double> popularity = new Hashtable<String, Double>();
	
		
		Enumeration<String> enumeration = pointingWebsites.keys();
		Integer totalNumberOfWebsites = pointingWebsites.size();
		
		while(enumeration.hasMoreElements()) { // initiating popularity for all websites
			String websiteName = enumeration.nextElement(); // key 
			popularity.put(websiteName, 1.0/totalNumberOfWebsites);
		}
		previousPopularityTable = (Hashtable<String, Double>) popularity.clone();
		
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
				popularity.put(websiteName, temp);	
				
				// System.out.println(websiteName + " " + temp);			
			}
			previousPopularityTable = (Hashtable<String, Double>) popularity.clone();
		}
		return popularity;
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

