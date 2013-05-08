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
}
