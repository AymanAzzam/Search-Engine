package com.crawler;

public class WordValue {
	
	String websiteName;
	Integer numberOfAppearance;
	Integer numberOfPlain;
	//Integer numberOfBold;
	Integer numberOfHeader;
	
	public WordValue(String websiteName, Integer numberOfAppearance,
			Integer numberOfPlain, Integer numberOfHeader) {
		
		this.websiteName = websiteName;
		this.numberOfAppearance = numberOfAppearance;
		this.numberOfPlain = numberOfPlain;
		this.numberOfHeader = numberOfHeader;
	}
	/*
	public WordValue(String websiteName, Integer numberOfAppearance,
			Integer numberOfPlain, Integer numberOfBold, 
			Integer numberOfHeader) {
		
		this.websiteName = websiteName;
		this.numberOfAppearance = numberOfAppearance;
		this.numberOfPlain = numberOfPlain;
		//this.numberOfBold = numberOfBold;
		//this.numberOfHeader = numberOfHeader;
	}
	*/
	public String getWebsiteName() {
		return websiteName;
	}
	
	public Integer getNumberOfAppearance() {
		return numberOfAppearance;
	}
	
	public Integer getNumberOfPlain() {
		return numberOfPlain;
	}
	/*
	public Integer getNumberOfBold() {
		return numberOfBold;
	}
	*/
	
	public Integer getNumberOfHeader() {
		return numberOfHeader;
	}
	

}
