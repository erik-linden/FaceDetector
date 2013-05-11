package se.kth.bik.project;

public class WeakClassifier implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public final int index;
	public final double thld;
	public final int parity;
	public final double alpha;
	
	public WeakClassifier(int index, double thld, int parity, double alpha) {
		this.index  = index;
		this.thld = thld;
		this.parity = parity;
		this.alpha = alpha;
	}

	public boolean classify(double[] fv) {
	    return parity * fv[index] > parity * thld;
	}

	public boolean classify(HaarFeature feature) {
	    return parity * feature.computeFeature(index) > parity * thld;
	}
}
