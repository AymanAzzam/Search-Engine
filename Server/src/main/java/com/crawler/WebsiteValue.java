package com.crawler;
import java.time.LocalDate;

public class WebsiteValue {
	
	Integer totalNumberOfWords;
	String headerText;
	String content;
	String location;
	LocalDate publishedDate;
	Integer frequency;

	
	public WebsiteValue(Integer totalNumberOfWords, String headerText, String content,String location, LocalDate publishedDate
		, Integer frequency) {
		this.totalNumberOfWords = totalNumberOfWords; 
		this.headerText = headerText;
		this.content = content;
		this.location = location;
		this.publishedDate = publishedDate;
		this.frequency = frequency;
	}
	
	public Integer getTotalNumberOfWords() {
		return totalNumberOfWords;
	}
	
	public String getHeaderText() {
		return headerText;
	}
	
	public String getContent() {
		return content;
	}

	public String getLocation(){
		return location;
	}

	public LocalDate getPublishedDate(){
		return publishedDate;
	}

	public Integer getFrequency() {
		return frequency;
	}
	
	public String getSummary() {
		
		String summary = "";
		String [] arr = content.split("\\s+"); 
		for(int i=0; i<Integer.min(50,arr.length); i++){
			summary = summary + ((summary.length()!=0)?" ":"") + arr[i] ;         
       }
		return summary;
	}

}
