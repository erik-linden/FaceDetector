package se.kth.bik.project;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class CascadeClassifier implements java.io.Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private List<List<WeakClassifier>> layers;
    private List<Double> thldAdjustments;

    public CascadeClassifier(List<WeakClassifier> weakClassifiers,
            List<Integer> cascadeLevels,
            List<Double> cascadeThlds) {

        layers = new LinkedList<List<WeakClassifier>>();

        Iterator<WeakClassifier> classifierIterator =
                weakClassifiers.iterator();
        Iterator<Integer> layerIterator = cascadeLevels.iterator();

        for(int i=0; layerIterator.hasNext(); ) {
            int layerEnd = layerIterator.next();

            List<WeakClassifier> layer = new LinkedList<WeakClassifier>();

            for(; i < layerEnd; ++i) {
                layer.add(classifierIterator.next());
            }
        }

        thldAdjustments = cascadeThlds;
    }

    public boolean classifyPatch(HaarFeatureComputer featureComputer, double thld_gain) {
        double sumH = 0;
        double sumA = 0;

        Iterator<List<WeakClassifier>> layerIterator = layers.iterator();
        Iterator<Double> thldIterator = thldAdjustments.iterator();

        while(layerIterator.hasNext()) {
            for(WeakClassifier c : layerIterator.next()) {
                if(c.classify(featureComputer)) {
                    sumH += c.alpha;
                }
                sumA += c.alpha;
            }

            if(sumH < sumA / 2 * thldIterator.next() * thld_gain) {
                return false;
            }
        }

        return true;
    }

}
