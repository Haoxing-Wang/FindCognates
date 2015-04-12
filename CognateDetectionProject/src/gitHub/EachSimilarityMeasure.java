package gitHub;

public class EachSimilarityMeasure {
	private int index;
	private double wss;
	private String english, french;
	
	EachSimilarityMeasure(int index, double wss,String english, String french){
		this.index=index;
		this.wss=wss;
		this.english=english;
		this.french=french;
	}
	
	public int getIndex(){
		return index;
	}
	
	public double getWSS(){
		return wss;
	}
	
	public String getEnglish(){
		return english;
	}
	
	public String getFrench(){
		return french;
	}
}