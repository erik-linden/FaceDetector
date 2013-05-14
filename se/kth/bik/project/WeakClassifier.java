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

	private boolean classify(double featureValue) {
	    return parity * featureValue < parity * thld;
	}

    /**
     * @param fv
     *            a vector of feature values corresponding to different
     *            features. From this vector, <code>fv[index]</code> will be
     *            used, where <code>index</code> is the value of this
     *            WeakClassifier's {@link #index} member.
     * @return <code>true</code> if the given feature value (the value at index
     *         <code>index</code> in <code>fv</code>) is classified as a face by
     *         this classifier.
     */
	public boolean classify(double[] fv) {
	    return classify(fv[index]);
	}

    /**
     * @param featureComputer
     *            the {@link HaarFeatureComputer} instance which will compute the
     *            feature value.
     * @return <code>true</code> if the feature value computed by
     *         <code>feature</code> is classified as a face by this classifier.
     */
	public boolean classify(HaarFeatureComputer featureComputer) {
	    return classify(featureComputer.computeFeature(index));
	}

	@Override
	public String toString() {
	    int ind = index * 5;
        int type = HaarFeatureComputer.FEATURE_TABLE[ind];
        int x = HaarFeatureComputer.FEATURE_TABLE[ind+1];
        int y = HaarFeatureComputer.FEATURE_TABLE[ind+2];
        int w = HaarFeatureComputer.FEATURE_TABLE[ind+3];
        int h = HaarFeatureComputer.FEATURE_TABLE[ind+4];
	    return String.format("[WeakClassifier: (t=%d, x=%d, y=%d, w=%d, h=%d), thld=%.2f, par=%d, a=%.2f]", type, x, y, w, h, thld, parity, alpha);
	}
}
