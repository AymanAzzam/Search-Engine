public class OutputImageValue {
		private String websiteURL;
		private String imageURL;
		
		public OutputImageValue (String websiteURL, String imageURL) {
			this.websiteURL = websiteURL;
			this.imageURL = imageURL;
			
		}
		
		public String getWebsiteURL () {
			return this.websiteURL;
		}
		
		public String getImageURL () {
			return this.imageURL;
		}
		
}
