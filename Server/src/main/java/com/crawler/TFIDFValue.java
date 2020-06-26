package com.crawler;

import java.util.ArrayList;

public class TFIDFValue {
	
	
	Integer numberOfWords;
	ArrayList<Inner> innerArray;
	
	
	class Inner {
		String word;
		Double wordWeight;
		Integer indexInList;
		
		public Inner(String word,Double wordWeight, Integer indexInList) {
			this.word = word;
			this.wordWeight = wordWeight;
			this.indexInList = indexInList;
		}
	}
	
	
	public TFIDFValue (Integer numberOfWords, String word, Double wordsWeight, Integer indexInList) {
		
		this.numberOfWords = numberOfWords;
		Inner inner = new Inner(word, wordsWeight, indexInList);
		this.innerArray = new ArrayList<Inner>();
		this.innerArray.add(inner);
	}
	/*
	public void addToWordsWeight(Double weight) {
		this.wordsWeight += weight;
	}
*/
	public void incrementNumberOfWords () {
		this.numberOfWords ++;
	}
	

	public Double getWordsWeight(Integer index) {
		return innerArray.get(index).wordWeight;
	}
	

	public Integer getNumeberOfWords() {
		return this.numberOfWords;
	}
	
	public void addToInnerArray(String word, Double wordWeight, Integer indexInList) {
		Inner inner = new Inner(word, wordWeight, indexInList);
		this.innerArray.add(inner);
	}
	
	public int getInnerArraySize() {
		return innerArray.size();
	}
	
	public String getWordString (Integer index) {
		return innerArray.get(index).word;
	}
	
	public Integer getWordIndex (Integer index) {
		return innerArray.get(index).indexInList;
	}
		
	
}
