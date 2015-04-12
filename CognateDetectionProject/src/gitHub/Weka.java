package gitHub;

import java.io.BufferedReader;
import java.util.HashMap;
import weka.classifiers.bayes.NaiveBayes;
import weka.classifiers.functions.LibSVM;
import weka.core.Instance;
import weka.core.Instances;
import weka.filters.Filter;
import weka.filters.supervised.instance.SMOTE;

public class Weka {
	
	private BufferedReader TrainBreader;
	private BufferedReader TestBreader;

	public Weka (BufferedReader trainbreader, BufferedReader testbreader){
		this.TrainBreader=trainbreader;
		this.TestBreader=testbreader;
	}

	public HashMap<Integer, Integer> weka () throws Exception
	{
		Instances train = new Instances (TrainBreader);
		train.setClassIndex(train.numAttributes() - 1);
		Instances test = new Instances(TestBreader);
		test.setClassIndex(test.numAttributes() - 1);

		TrainBreader.close();
		TestBreader.close();
		

		//using the SMOTE to solve the imbalanced instance in training set
		SMOTE filters = new SMOTE();
		filters.setInputFormat(train);
		filters.setPercentage(200);
		filters.setNearestNeighbors(5);
		Instances subSamplingInstances = Filter.useFilter(train, filters); 

		//train model		
		NaiveBayes nb = new NaiveBayes();
		nb.buildClassifier(subSamplingInstances);
		nb.setUseKernelEstimator(true);
		Instances labeled = new Instances(test);
		for (int i =0; i<test.numInstances(); i++)
		{
			double classlabel = nb.classifyInstance(test.instance(i));
			
			labeled.instance(i).setClassValue(classlabel);
		}

		HashMap<Integer, Integer> found = new HashMap<Integer, Integer>();
		for (int i =0; i<labeled.numInstances();i++)
		{
			Instance each = labeled.instance(i);

			double valueofeach = each.value(5);
			if(valueofeach== 0.0)
			{
				valueofeach=-1;
				found.put(i, (int)valueofeach);
			}
			if (valueofeach==1.0)
			{
				found.put(i, (int)valueofeach);
			}			
		}		
		return found;
	}
}