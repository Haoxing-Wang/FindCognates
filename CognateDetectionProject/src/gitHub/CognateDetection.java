package gitHub;

import it.uniroma1.lcl.babelfy.Babelfy;
import it.uniroma1.lcl.babelfy.Babelfy.AccessType;
import it.uniroma1.lcl.babelfy.Babelfy.Matching;
import it.uniroma1.lcl.babelfy.data.Annotation;
import it.uniroma1.lcl.babelfy.data.BabelSynsetAnchor;
import it.uniroma1.lcl.babelfy.BabelfyKeyNotValidOrLimitReached;
import it.uniroma1.lcl.babelnet.BabelSense;
import it.uniroma1.lcl.babelnet.BabelSenseSource;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections15.multimap.MultiHashMap;

public class CognateDetection {

	private String English, French;
	private double Similarity, Precision, Recall;
	private MultiHashMap<Integer, IndexAndTypeOfEachStringMap> indexAndTypeOfEachString;	
	private static int trueYes=0, cognateCount=0, identifiedCognate=0;

	public CognateDetection (){		
	}

	public MultiHashMap<Integer, CandidateWithSimilarityMap> detectCognates (String line){

		indexAndTypeOfEachString = new MultiHashMap<Integer, IndexAndTypeOfEachStringMap>();
		MultiHashMap<Integer, DectedCandidatesMap> detectedCandidates = new MultiHashMap<Integer, DectedCandidatesMap>();
		MultiHashMap<Integer, CandidateWithSimilarityMap> detectedCanWithSimlarity = new MultiHashMap<Integer, CandidateWithSimilarityMap>();
		
		// this count is particular for filtering
		// the number of French senses retrieved from OMWN
		int senseCount=1;

		// split each line using space
		List<String> splitLine = Arrays.asList(line.split("[\\s+]"));

		// save each splitted string into indexAndTypeOfEachString map
		int stringStart=0, stringIndex=0;
		for (String eachword:splitLine){

			// if string contains human annotation, type 1
			if (eachword.matches("\\A(<)([^<].*?)")){

				// calculate index of each word and their types
				stringIndex=line.indexOf(eachword, stringStart);
				stringStart = eachword.length()+stringIndex;
				indexAndTypeOfEachString.put(stringIndex+1, new IndexAndTypeOfEachStringMap (eachword, stringIndex+1, 1));

				// count how many cognates annotation are there
				cognateCount++;
			}

			// if string without a human annotation, type -1
			else{
				// calculate index of each word and their types
				stringIndex=line.indexOf(eachword, stringStart);
				stringStart = eachword.length()+stringIndex;
				indexAndTypeOfEachString.put(stringIndex, new IndexAndTypeOfEachStringMap (eachword, stringIndex, -1));
			}
		}

		// start to disambiguate use babelfy
		try {

			Babelfy BFY = Babelfy.getInstance(AccessType.ONLINE);
			Annotation annotations = BFY.babelfy("_0m3pZ6ABiThDvc7liEJm5EZYxwOovKV", line, Matching.EXACT, Language.EN);

			for (BabelSynsetAnchor eachAnno: annotations.getAnnotations())
			{
				int annotationStart=0, annotationIndex=0;

				// get index of each annotation
				annotationIndex = line.indexOf(eachAnno.getAnchorText(), annotationStart);
				annotationStart= eachAnno.getAnchorText().length()+annotationIndex;

				// retrieve all the French translations of eachAnno
				List<BabelSense> senses = eachAnno.getBabelSynset().getSenses(Language.FR);

				if (senses.isEmpty()==false)
				{
					for (BabelSense eachsense : senses)
					{
						// filter the sense source, if the source is OMWN,
						// only retrieve the first French sense from this it
						// and save the annoIndex, annotation and French into the map
						if (eachsense.getSource().equals(BabelSenseSource.OMWN) )
						{
							detectedCandidates.put(annotationIndex, new DectedCandidatesMap( annotationIndex,  eachAnno.getAnchorText(), eachsense.getLemma()));
							senseCount++;
							if (senseCount==1)
								break;
						}

						// all other sense sources, save 
						// annoIndex, annotation and French into the map
						if ( eachsense.getSource().equals(BabelSenseSource.WN) || eachsense.getSource().equals(BabelSenseSource.WNTR) || eachsense.getSource().equals(BabelSenseSource.OMWIKI) || eachsense.getSource().equals(BabelSenseSource.WIKI) || eachsense.getSource().equals(BabelSenseSource.WIKITR) || eachsense.getSource().equals(BabelSenseSource.WIKIDATA) || eachsense.getSource().equals(BabelSenseSource.WIKT))
						{
							detectedCandidates.put(annotationIndex, new DectedCandidatesMap( annotationIndex, eachAnno.getAnchorText(), eachsense.getLemma() ));
						}								
					}				
				}
			}// end of finding and saving every annotation

			// assign similarity to each candidate
			SimilarityCalculation similarity = new SimilarityCalculation();
			detectedCanWithSimlarity = similarity.calculateSimilarity(detectedCandidates);		

		}
		catch (IOException | URISyntaxException | BabelfyKeyNotValidOrLimitReached e){
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return detectedCanWithSimlarity;
	}


	public void makeDecisions (BufferedReader trainer, MultiHashMap<Integer, CandidateWithSimilarityMap> detectedCanWithSimlarity) throws FileNotFoundException{
		
		// create an unlabeled file from finalmap, deciding whether or not the potential cognates are real ones
		PrintStream outputData = new PrintStream("/home/irvin/StemedOut/input.arff");
		outputData.println("@relation cognate");	
		outputData.println("@attribute sWSS numeric");
		outputData.println("@attribute levenshteinWSS numeric");
		outputData.println("@attribute nWSS numeric");
		outputData.println("@attribute dWSS numeric");
		outputData.println("@attribute lcsrWSS numeric");
		outputData.println("@attribute class {tested_negative, tested_positive}");
		outputData.println("@data");

		// create a list to save each line of the arff file
		// as a result, the order of the list is the same as the arff file
		ArrayList<CandidateWithSimilarityMap> candSimilarityVals = new ArrayList<CandidateWithSimilarityMap>();
		for (Entry<Integer, Collection<CandidateWithSimilarityMap>> centries: detectedCanWithSimlarity.entrySet())
		{
			for (CandidateWithSimilarityMap cvalues:centries.getValue())
			{	
				outputData.println(cvalues.getSoundexWss()+","+cvalues.getEdWss()+","+cvalues.getNgramWss()+","+cvalues.getDiceWss()+","+cvalues.getLcsrWss()+","+"?");
				candSimilarityVals.add(cvalues);
			}
		}
		outputData.close();

		// start to label with weka
		try {
			
			// create a new Weka instance
			Weka weka = new Weka(trainer, new BufferedReader (new FileReader("/home/irvin/StemedOut/input.arff")));
			HashMap<Integer, Integer> labeled = weka.weka();

			// analyze labeled data
			for (int i=0; i<labeled.size();i++)
			{
				Integer labeledtype = labeled.get(i);

				// if label type is 1, meaning it is a cognate and do two things
				// 1. compare with indexAndTypeOfEachString map and record trueYes & falseYes
				// 2. remove them from indexAndTypeOfEachString map and write the updated indexAndTypeOfEachString map to local file

				if (labeledtype ==1)
				{
					identifiedCognate++;
					CandidateWithSimilarityMap value = candSimilarityVals.get(i);

					// compare
					Collection<IndexAndTypeOfEachStringMap> bvalues = indexAndTypeOfEachString.getCollection(value.getPosition());
					for (IndexAndTypeOfEachStringMap each: bvalues)
					{
						System.out.println("LABELED AS 1: "+each.getPosition()+"\t"+each.getEnglish()+"\tORIGINAL LABEL: "+each.getType());
						if (each.getType()==1)
						{
							// correct labelled
							trueYes++;
						}
						if (each.getType()==-1)
						{
							// wrong labeled
							System.out.println("A cognate but wrong labeled");
						}
					}

					// remove
					if (value.getEnglish().contains(" "))
					{
						// remove the first word in compounds
						int firstIndex = value.getPosition();
						indexAndTypeOfEachString.remove(firstIndex);

						// remove the second or third word in compounds
						String[] splittedWords = value.getEnglish().split(" ");
						for (int h=1; h< splittedWords.length ;h++)
						{
							int nextindex = firstIndex + splittedWords[h-1].length() +  1;
							indexAndTypeOfEachString.remove(nextindex);
							firstIndex = nextindex;
						}
					}
					else
					{
						indexAndTypeOfEachString.remove(value.getPosition());
					}
				}

				// if label type is -1, meaning it is not a cognate, just record falseNo and falseYes
				if (labeledtype == -1)
				{						
					Collection<IndexAndTypeOfEachStringMap> bvalues = indexAndTypeOfEachString.getCollection(candSimilarityVals.get(i).getPosition());
					for (IndexAndTypeOfEachStringMap each: bvalues)
					{
						System.out.println("LABELED AS -1: "+each.getPosition()+"\t"+each.getEnglish()+"\tORIGINAL LABEL: "+each.getType());
						if (each.getType()==1)
						{
							// wrong labeled
							System.out.println("A non cognate and wrong labeled");
						}
						if (each.getType()==-1)
						{
							// correct labeled and original is non cognate
							System.out.println("A non cognate and correct labeled");
						}
					}						
				}		

			}// end of processing comparing and removing

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// return the updated indexAndTypeOfEachString
		SortingIndexAndTypeOfEachString sort = new SortingIndexAndTypeOfEachString(indexAndTypeOfEachString);
		List<IndexAndTypeOfEachStringMap> list = sort.sort();
		for (IndexAndTypeOfEachStringMap values:list){
			System.out.println("SORTED indexAndTypeOfEachString MAP: "+values.getPosition()+"\t"+values.getEnglish());
		}
	}

	public String getEnglish (int AnnotationID){

		return English;		
	}

	public String getFrench (int AnnotationID){
		return French;
	}

	public double getSimilarity (String English, String French){
		return Similarity;
	}

	public double getPrecision (int trueyes, int cognatecount){
		trueYes=trueyes;
		cognateCount=cognatecount;
		this.Precision = (double) (trueYes) / (double)(cognateCount);

		return Precision;
	}

	public double getRecall (int trueyes, int identifiedcognate){
		trueYes= trueyes;
		identifiedCognate = identifiedcognate;
		this.Recall= (double) (trueYes) / (double)(identifiedCognate);

		return Recall;
	}
}