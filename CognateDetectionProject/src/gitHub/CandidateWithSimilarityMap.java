package gitHub;

public class CandidateWithSimilarityMap {

	private int position;
	private double soundexWss;
	private double edWss;
	private double ngramWss;
	private double diceWss;
	private double lcsrWss;
	private String english, french;

	public CandidateWithSimilarityMap( int position, String english, String french, double soundexWss, double edWss, double ngramWss, double diceWss, double lcsrWss){
		this.position=position;
		this.english=english;
		this.french=french;
		this.soundexWss=soundexWss;
		this.edWss=edWss;
		this.ngramWss=ngramWss;
		this.diceWss=diceWss;
		this.lcsrWss=lcsrWss;
	}

	public int getPosition(){
		return position;
	}

	public String getEnglish(){
		return english;
	}

	public String getFrench(){
		return french;
	}

	public double getSoundexWss(){
		return soundexWss;
	}

	public double getEdWss(){
		return edWss;
	}

	public double getNgramWss(){
		return ngramWss;
	}

	public double getDiceWss(){
		return diceWss;
	}

	public double getLcsrWss(){
		return lcsrWss;
	}
}