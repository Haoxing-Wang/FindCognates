package gitHub;

public class DectedCandidatesMap {

	private String english, french;
	private int index;

	public DectedCandidatesMap(int index, String english, String french){
		this.english=english;
		this.french=french;
		this.index=index;
	}
	
	public int getIndex(){
		return index;
	}

	public String getEnglish(){
		return english;
	}

	public String getFrench(){
		return french;
	}
}