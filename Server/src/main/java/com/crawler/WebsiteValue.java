package com.crawler;
import java.util.Date;

public class WebsiteValue {
	
	Integer totalNumberOfWords;
	String headerText;
	String content;
	String location;
	Date publishedDate;
	
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

	public String getLocation(){
		return location;
	}

	public Date getPublishedDate(){
		return publishedDate;
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
