package gitHub;


public class IndexAndTypeOfEachStringMap {

	private String english;
	private int position;
	private int type;

	public IndexAndTypeOfEachStringMap(String english, int position, int type){

		this.english = english;
		this.position = position;
		this.type = type;		
	}
	
	public String getEnglish(){
		return english;
	}
	
	public Integer getPosition(){
		return position;
	}
	
	public Integer getType(){
		return type;
	}
}