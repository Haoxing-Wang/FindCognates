package gitHub;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import org.apache.commons.collections15.multimap.MultiHashMap;

public class MainMethod {
	
	public static void main (String args[]){		
		
		String Line= "the munster <captain went off midway through the <second half of saturday's 13-10 defeat by <england at twickenham with a hamstring strain";

		CognateDetection dec = new CognateDetection();
		MultiHashMap<Integer, CandidateWithSimilarityMap> detectedCanWithSimlarity = dec.detectCognates(Line);
		
		try {
			
			BufferedReader trainbreader = new BufferedReader (new FileReader("/home/irvin/IT60/Data after disambiguating_training model_v2/3rd model/Learning_from_all.arff"));
			dec.makeDecisions(trainbreader, detectedCanWithSimlarity);
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}
}