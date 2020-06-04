package com.crawler;

import java.util.ArrayList;

public class OutputImageValue {
		private String websiteURL;
		private ArrayList<String> imageURL;
		
		public OutputImageValue (String websiteURL, ArrayList<String> imageURL) {
			this.websiteURL = websiteURL;
			this.imageURL = imageURL;
			
		}
		
		public String getWebsiteURL () {
			return this.websiteURL;
		}
		
		public ArrayList<String> getImageURL () {
			return this.imageURL;
		}
		
}
