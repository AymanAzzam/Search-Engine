
public class WebsiteTFIDFPair implements Comparable<WebsiteTFIDFPair> {
	String websiteName;
	Double TFIDFValue;
	Integer numberOfWords;
	
	public WebsiteTFIDFPair(String websiteName, Double TFIDFValue, Integer numberOfWords) {
		this.websiteName = websiteName;
		this.TFIDFValue = TFIDFValue;
		this.numberOfWords = numberOfWords;
	}
	
	public Double getTFIDFValue() {
		return this.TFIDFValue;
	}
	
	public String getWebsiteName() {
		return this.websiteName;
	}
	
	public Integer getNumberOfWords() {
		return numberOfWords;
	}
	
	public int compareTo(WebsiteTFIDFPair websiteTFIDFPair) {
		// for descending order
		return websiteTFIDFPair.getTFIDFValue().compareTo(this.TFIDFValue);
	}
	
	
	
}
