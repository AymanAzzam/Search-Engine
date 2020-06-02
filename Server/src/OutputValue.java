
public class OutputValue {
	String websiteName;
	String headerText;
	String summary;
	
	public OutputValue(String websiteName, String headerText, String summary) {
		this.websiteName = websiteName;
		this.headerText = headerText;
		this.summary = summary;
	}
	
	public String getWebsiteName() {
		return this.websiteName;
	}
	
	public String getHeaderText() {
		return this.headerText;
	}
	
	public String getSummary() {
		return this.summary;
	}

}
