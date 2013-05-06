import java.util.Vector;


public class CascadeClassifier implements java.io.Serializable  {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	Vector<WeakClassifier> weakClassifiers;
	Vector<Integer> cascadeLevels;
	Vector<Double> cascadeThlds;

	public CascadeClassifier(Vector<WeakClassifier> weakClassifiers,
			Vector<Integer> cascadeLevels, Vector<Double> cascadeThlds) {
		super();
		this.weakClassifiers = weakClassifiers;
		this.cascadeLevels = cascadeLevels;
		this.cascadeThlds = cascadeThlds;
	}
}
