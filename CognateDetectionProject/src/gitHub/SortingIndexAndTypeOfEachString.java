package gitHub;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.collections15.multimap.MultiHashMap;

public class SortingIndexAndTypeOfEachString {
	
	private MultiHashMap<Integer, IndexAndTypeOfEachStringMap> indexAndTypeOfEachString;
	
	public SortingIndexAndTypeOfEachString(MultiHashMap<Integer, IndexAndTypeOfEachStringMap> basicinfor){
		this.indexAndTypeOfEachString=basicinfor;
	}
	
	public List<IndexAndTypeOfEachStringMap> sort (){
		
		List<IndexAndTypeOfEachStringMap> list = new ArrayList<IndexAndTypeOfEachStringMap>(indexAndTypeOfEachString.values());
		
		Collections.sort(list, new Comparator<IndexAndTypeOfEachStringMap>(){
			@Override
			public int compare(IndexAndTypeOfEachStringMap o1, IndexAndTypeOfEachStringMap o2){		

				//compare the value of index.
				return Integer.valueOf(o1.getPosition()).compareTo(o2.getPosition());
			}			
				}				
				);	
		
		return list;
	}
}