package com.crawler;

public class WebsiteValue {
	
	Integer totalNumberOfWords;
	String headerText;
	String content;
	
	public WebsiteValue(Integer totalNumberOfWords, String headerText, String content) {
		this.totalNumberOfWords = totalNumberOfWords;
		this.headerText = headerText;
		this.content = content;
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
	
	public String getSummary() {
		
		String summary = "";
		String [] arr = content.split("\\s+"); 
		for(int i=0; i<50; i++){
			summary = summary + " " + arr[i] ;         
       }
		return summary;
	}

}
