import java.util.ArrayList;

public class TFIDFValue {
	
	Integer numberOfWords;
	ArrayList<Inner> innerArray;
	
	
	class Inner {
		String word;
		Integer indexInList;
		
		public Inner(String word, Integer indexInList) {
			this.word = word;
			this.indexInList = indexInList;
		}
	}
	
	
	public TFIDFValue (Integer numberOfWords, String word, Integer indexInList) {
		this.numberOfWords = numberOfWords;
		Inner inner = new Inner(word, indexInList);
		this.innerArray = new ArrayList<Inner>();
		this.innerArray.add(inner);
	}
	
	public void incrementNumberOfWords() {
		this.numberOfWords ++;
	}
	
	public Integer getNumberOfWords() {
		return this.numberOfWords;
	}
	
	public void addToInnerArray(String word, Integer indexInList) {
		Inner inner = new Inner(word, indexInList);
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
