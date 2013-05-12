package se.kth.bik.project;

import java.util.List;


public class CascadeClassifier implements java.io.Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	List<WeakClassifier> weakClassifiers;
	List<Integer> cascadeLevels;
	List<Double> cascadeThlds;

	public CascadeClassifier(List<WeakClassifier> weakClassifiers,
			List<Integer> cascadeLevels, List<Double> cascadeThlds) {
		super();
		this.weakClassifiers = weakClassifiers;
		this.cascadeLevels = cascadeLevels;
		this.cascadeThlds = cascadeThlds;
	}

    boolean classifyPatch(HaarFeature feature, double thld_gain) {
        double sumH = 0;
	double sumA = 0;
	int n = 0;
	int nLayers = cascadeLevels.size();

	for (int l=0;l<nLayers;l++) {

		while(n<cascadeLevels.get(l)) {
			WeakClassifier c = weakClassifiers.get(n);
			if(c.classify(feature)) {
				sumH += c.alpha;
			}
			sumA += c.alpha;

			n++;
		}

		double thld_adj = cascadeThlds.get(l);
		if(sumH<sumA/2*thld_adj*thld_gain) {
			return false;
		}

	}

	return true;
    }
}
