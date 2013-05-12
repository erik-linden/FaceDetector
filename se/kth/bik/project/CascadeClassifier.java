package se.kth.bik.project;

import java.util.Iterator;
import java.util.List;

public class CascadeClassifier implements java.io.Serializable {

    /**
	 * 
	 */
    private static final long serialVersionUID = 1L;

    private WeakClassifier[][] layers;
    private Double[] thldAdjustments;

    public CascadeClassifier(List<WeakClassifier> weakClassifiers,
            List<Integer> cascadeLevels,
            List<Double> cascadeThlds) {

        layers = new WeakClassifier[cascadeLevels.size()][];

        Iterator<WeakClassifier> classifierIterator =
                weakClassifiers.iterator();
        Iterator<Integer> layerIterator = cascadeLevels.iterator();

        int prevLayerEnd = 0;
        int i = 0;
        for(int layer = 0; layerIterator.hasNext(); ++layer) {
            int layerEnd = layerIterator.next();
            layers[layer] = new WeakClassifier[layerEnd - prevLayerEnd];

            for(; i < layerEnd; ++i) {
                layers[layer][i - prevLayerEnd] = classifierIterator.next();
            }

            prevLayerEnd = layerEnd;
        }

        thldAdjustments = cascadeThlds.toArray(new Double[0]);
    }

    public boolean classifyPatch(HaarFeatureComputer featureComputer, double thld_gain) {
        double sumH = 0;
        double sumA = 0;

        for(int i = 0; i < layers.length; ++i) {
            for(WeakClassifier c : layers[i]) {
                if(c.classify(featureComputer)) {
                    sumH += c.alpha;
                }
                sumA += c.alpha;
            }

            if(sumH < sumA / 2 * thldAdjustments[i] * thld_gain) {
                return false;
            }
        }

        return true;
    }

}
