package gitHub;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.collections15.multimap.MultiHashMap;

import uk.ac.shef.wit.simmetrics.similaritymetrics.AbstractStringMetric;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Levenshtein;
import uk.ac.shef.wit.simmetrics.similaritymetrics.Soundex;

public class SimilarityCalculation {

	private MultiHashMap<Integer, CandidateWithSimilarityMap> detectedCanWithSimlarity;
	
	public SimilarityCalculation (){}

	public MultiHashMap<Integer, CandidateWithSimilarityMap> calculateSimilarity (MultiHashMap<Integer, DectedCandidatesMap> detectedCandidate) throws IOException{
		
		detectedCanWithSimlarity = new MultiHashMap<Integer, CandidateWithSimilarityMap>();
		
		// create 5 different similarity measures objects
		AbstractStringMetric soundexMetric = new Soundex();
		AbstractStringMetric edMetric = new Levenshtein ();
		Ngram ngram = new Ngram(2);
		DICE dice = new DICE();
		LCSR lcsr = new LCSR();

		// make the porterstemmer as a static method?
		PorterStemmer stem= new PorterStemmer();

		for (Entry<Integer, Collection<DectedCandidatesMap>> entries:detectedCandidate.entrySet())
		{
			for (DectedCandidatesMap values:entries.getValue())
			{
				double soundexWss = soundexMetric.getUnNormalisedSimilarity(stem.cal(values.getEnglish()),stem.cal(values.getFrench()));
				double edWss = edMetric.getSimilarity(stem.cal(values.getEnglish()),stem.cal(values.getFrench()));
				double ngramWss = ngram.getDistance(stem.cal(values.getEnglish()),stem.cal(values.getFrench()));				
				double diceWss = dice.score(stem.cal(values.getEnglish()),stem.cal(values.getFrench()));			
				double lcsrWss= lcsr.getDistance(stem.cal(values.getEnglish()),stem.cal(values.getFrench()));

				detectedCanWithSimlarity.put(entries.getKey(), new CandidateWithSimilarityMap (entries.getKey(), values.getEnglish(), values.getFrench(), soundexWss, edWss, ngramWss, diceWss, lcsrWss) );
			}
		}

		MultiHashMap<Integer, EachSimilarityMeasure> sortedAndBiggestCandidate=sortSimilarity(detectedCanWithSimlarity);

		// create a new finalmap and list
		MultiHashMap<Integer, CandidateWithSimilarityMap> finalCandidateWithSimilarity = new MultiHashMap<Integer, CandidateWithSimilarityMap>();
		List<EachSimilarityMeasure> list = new ArrayList<EachSimilarityMeasure>();
		
		for (Integer index: sortedAndBiggestCandidate.keySet()){

			Collection<EachSimilarityMeasure> value = sortedAndBiggestCandidate.get(index);
			list.addAll(value);

			EachSimilarityMeasure biggestSoundexValue = list.get(0);
			EachSimilarityMeasure biggestEdValue = list.get(1);
			EachSimilarityMeasure biggestBiValue = list.get(2);
			EachSimilarityMeasure biggestDiceValue = list.get(3);
			EachSimilarityMeasure biggestLcsrValue = list.get(4);

			finalCandidateWithSimilarity.put(index, new CandidateWithSimilarityMap(index, biggestSoundexValue.getEnglish(), biggestSoundexValue.getFrench(), biggestSoundexValue.getWSS(), biggestEdValue.getWSS(), biggestBiValue.getWSS(), biggestDiceValue.getWSS(), biggestLcsrValue.getWSS() ) );
			list.clear();		
		}
		return finalCandidateWithSimilarity;
	}

	private static MultiHashMap<Integer, EachSimilarityMeasure> sortSimilarity (MultiHashMap<Integer, CandidateWithSimilarityMap> detectedCanWithSimlarity) throws IOException
	{
		MultiHashMap<Integer, EachSimilarityMeasure> map = new MultiHashMap<Integer, EachSimilarityMeasure>();
		
		MultiHashMap<Integer, EachSimilarityMeasure> soundexMap = new MultiHashMap<Integer, EachSimilarityMeasure>();
		MultiHashMap<Integer, EachSimilarityMeasure> edMap = new MultiHashMap<Integer, EachSimilarityMeasure>();
		MultiHashMap<Integer, EachSimilarityMeasure> biMap = new MultiHashMap<Integer, EachSimilarityMeasure>();
		MultiHashMap<Integer, EachSimilarityMeasure> diceMap = new MultiHashMap<Integer, EachSimilarityMeasure>();
		MultiHashMap<Integer, EachSimilarityMeasure> lcsrMap = new MultiHashMap<Integer, EachSimilarityMeasure>();
		
		List<EachSimilarityMeasure> soundexMeasure = new ArrayList<EachSimilarityMeasure>();
		List<EachSimilarityMeasure> edMeasure = new ArrayList<EachSimilarityMeasure>();
		List<EachSimilarityMeasure> biMeasure = new ArrayList<EachSimilarityMeasure>();
		List<EachSimilarityMeasure> diceMeasure = new ArrayList<EachSimilarityMeasure>();
		List<EachSimilarityMeasure> lcsrMeasure = new ArrayList<EachSimilarityMeasure>();

		for (Entry<Integer, Collection<CandidateWithSimilarityMap>> centries: detectedCanWithSimlarity.entrySet())
		{
			for (CandidateWithSimilarityMap cvalues:centries.getValue())
			{
				soundexMeasure.add(new EachSimilarityMeasure(cvalues.getPosition(), cvalues.getSoundexWss(), cvalues.getEnglish(), cvalues.getFrench() ));
				edMeasure.add(new EachSimilarityMeasure(cvalues.getPosition(), cvalues.getEdWss(), cvalues.getEnglish(), cvalues.getFrench() )); 
				biMeasure.add(new EachSimilarityMeasure(cvalues.getPosition(), cvalues.getNgramWss(), cvalues.getEnglish(), cvalues.getFrench() )); 
				diceMeasure.add(new EachSimilarityMeasure(cvalues.getPosition(), cvalues.getDiceWss(), cvalues.getEnglish(), cvalues.getFrench() )); 
				lcsrMeasure.add(new EachSimilarityMeasure(cvalues.getPosition(), cvalues.getLcsrWss() , cvalues.getEnglish(), cvalues.getFrench() )); 
			}
		}

		
		// sort all soundex values
		Collections.sort(soundexMeasure, new Comparator<EachSimilarityMeasure>(){
			@Override
			public int compare(EachSimilarityMeasure o1, EachSimilarityMeasure o2) {	

				//compare the value of overall, ss and wss.
				return Double.valueOf(o2.getWSS()).compareTo(o1.getWSS());
			}			
				}		
				);

		// get the biggest soundex value
		for (EachSimilarityMeasure value:soundexMeasure){
			
			soundexMap.put(value.getIndex(), new EachSimilarityMeasure(value.getIndex(), value.getWSS(), value.getEnglish(), value.getFrench() ) );
		}	
		soundexMap=getBiggestSimilarity(soundexMap);
		
		// save contents from the biggest soundexMap to map
		for (Entry<Integer, Collection<EachSimilarityMeasure>> entries:soundexMap.entrySet()){
			for (EachSimilarityMeasure eachvalue:entries.getValue()){
				
				map.put(entries.getKey(), new EachSimilarityMeasure (entries.getKey(), eachvalue.getWSS(), eachvalue.getEnglish(), eachvalue.getFrench()));
			}
		}

		// sort all ed values
		Collections.sort(edMeasure, new Comparator<EachSimilarityMeasure>(){
			@Override
			public int compare(EachSimilarityMeasure o1, EachSimilarityMeasure o2) {		

				//compare the value of overall, ss and wss.
				return Double.valueOf(o2.getWSS()).compareTo(o1.getWSS());
			}			
				}		
				);

		// get the biggest ed value
		for (EachSimilarityMeasure value:edMeasure){
			edMap.put(value.getIndex(), new EachSimilarityMeasure(value.getIndex(), value.getWSS(), value.getEnglish(), value.getFrench() ) );
		}		
		edMap=getBiggestSimilarity(edMap);
		
		// save contents from the biggest edMap to map
		for (Entry<Integer, Collection<EachSimilarityMeasure>> entries:edMap.entrySet()){
			for (EachSimilarityMeasure eachvalue:entries.getValue()){
				map.put(entries.getKey(), new EachSimilarityMeasure (entries.getKey(), eachvalue.getWSS(), eachvalue.getEnglish(), eachvalue.getFrench() ) );
			}
		}

		// sort all bi values
		Collections.sort(biMeasure, new Comparator<EachSimilarityMeasure>(){
			@Override
			public int compare(EachSimilarityMeasure o1, EachSimilarityMeasure o2) {		

				//compare the value of overall, ss and wss.
				return Double.valueOf(o2.getWSS()).compareTo(o1.getWSS());
			}			
				}		
				);

		// get the biggest bi value
		for (EachSimilarityMeasure value:biMeasure){
			biMap.put(value.getIndex(), new EachSimilarityMeasure(value.getIndex(), value.getWSS(), value.getEnglish(), value.getFrench() ) );
		}		
		biMap=getBiggestSimilarity(biMap);
		
		// save contents from the biggest lcsrMap to map
		for (Entry<Integer, Collection<EachSimilarityMeasure>> entries:biMap.entrySet()){
			for (EachSimilarityMeasure eachvalue:entries.getValue()){
				map.put(entries.getKey(), new EachSimilarityMeasure (entries.getKey(), eachvalue.getWSS(), eachvalue.getEnglish(), eachvalue.getFrench() ) );
			}
		}

		// sort all dice values
		Collections.sort(diceMeasure, new Comparator<EachSimilarityMeasure>(){
			@Override
			public int compare(EachSimilarityMeasure o1, EachSimilarityMeasure o2){		

				//compare the value of overall, ss and wss.
				return Double.valueOf(o2.getWSS()).compareTo(o1.getWSS());
			}			
				}		
				);

		// get the biggest dice value
		for (EachSimilarityMeasure value:diceMeasure){
			diceMap.put(value.getIndex(), new EachSimilarityMeasure(value.getIndex(), value.getWSS(), value.getEnglish(), value.getFrench() ) );
		}		
		diceMap=getBiggestSimilarity(diceMap);
		
		// save contents from the biggest diceMap to map
		for (Entry<Integer, Collection<EachSimilarityMeasure>> entries:diceMap.entrySet()){
			for (EachSimilarityMeasure eachvalue:entries.getValue()){
				map.put(entries.getKey(), new EachSimilarityMeasure (entries.getKey(), eachvalue.getWSS(), eachvalue.getEnglish(), eachvalue.getFrench() ) );
			}
		}

		// sort all lcsr values
		Collections.sort(lcsrMeasure, new Comparator<EachSimilarityMeasure>(){
			@Override
			public int compare(EachSimilarityMeasure o1, EachSimilarityMeasure o2) {		
				//compare the value of overall, ss and wss.
				return Double.valueOf(o2.getWSS()).compareTo(o1.getWSS());
			}			
				}		
				);

		// get the biggest lcsr value
		for (EachSimilarityMeasure value:lcsrMeasure){
			lcsrMap.put(value.getIndex(), new EachSimilarityMeasure(value.getIndex(), value.getWSS(), value.getEnglish(), value.getFrench() ) );
		}		
		lcsrMap=getBiggestSimilarity(lcsrMap);
		
		// save contents from the biggest lcsrMap to map
		for (Entry<Integer, Collection<EachSimilarityMeasure>> entries:lcsrMap.entrySet()){
			for (EachSimilarityMeasure eachvalue:entries.getValue()){
				map.put(entries.getKey(), new EachSimilarityMeasure (entries.getKey(), eachvalue.getWSS(), eachvalue.getEnglish(), eachvalue.getFrench() ) );
			}
		}
		return map;
	}

	private static MultiHashMap<Integer, EachSimilarityMeasure> getBiggestSimilarity (MultiHashMap<Integer, EachSimilarityMeasure> eachSimilarityMap) throws IOException
	{
		// create a new biggestCandidate map
		MultiHashMap<Integer, EachSimilarityMeasure> biggestCandidate = new MultiHashMap<Integer, EachSimilarityMeasure>();

		int count=0;
		for (Integer keys:eachSimilarityMap.keySet())
		{
			Collection<EachSimilarityMeasure> values = eachSimilarityMap.getCollection(keys);
			Iterator<EachSimilarityMeasure> iterator = values.iterator();
			
			// only retrieve the first value of each collection
			while (iterator.hasNext())
			{
				EachSimilarityMeasure value = iterator.next();
				biggestCandidate.put(keys, value);
				count++;
				if (count==1)
				{
					count=0;
					break;
				}					
			}
		}			
		return biggestCandidate;
	}
}