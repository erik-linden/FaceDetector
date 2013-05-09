package se.kth.bik.project;

public class WeakClassifier implements java.io.Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	int index = -1;
	double thld = 0;
	int parity = 1;
	double alpha = 0;
	
	public WeakClassifier(int index, double thld, int parity, double alpha) {
		this.index  = index;
		this.thld = thld;
		this.parity = parity;
		this.alpha = alpha;
	}
}
