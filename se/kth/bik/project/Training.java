package se.kth.bik.project;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;


public class Training {

    private static final double FALSE_POSITIVE_CHANCE_TARGET = 1e-3;
    private static final int MAX_NUMBER_OF_LAYERS = 20;
    private static final int MAX_NUMBER_OF_WEAK_CLASSIFIERS = 500;
    private static final int NUMBER_OF_FEATURES = HaarFeatureComputer.NO_FEATURES;
    private static final double TRUE_POSITIVE_DECREASE_TOLERANCE = 0.999;

    public static final String SAVE_FILENAME = "trainingData.sav";

    /**
     * @param args
     * @throws IOException
     */
    public static void main(String[] args) throws IOException {
        CascadeClassifier tr = Training.train();

        System.out.println("Training complete, writing to " + SAVE_FILENAME);

        FileOutputStream saveFile = new FileOutputStream(SAVE_FILENAME);
        ObjectOutputStream save = new ObjectOutputStream(saveFile);
        save.writeObject(tr);
        save.close();

        System.out.println("Write complete, nothing more to do here.");
    }

    private static CascadeClassifier train() throws IOException {

        System.out.println("Commencing training");
        System.out.println("Initializing Haar features...");
        HaarFeatureComputer.init();
        System.out.println("Haar features initialized.");

        System.out.println("Computing feature vectors for FACES...");
        double[][] fv_face = makeFeatureVector(
                FileUtils.combinePath(EnvironmentConstants.PROJECT_ROOT, "TrainingImages", "FACES"),
                10000);
        int nFaces = fv_face.length;
        System.out.println("FACES feature vectors computed.");

        System.out.println("Computing feature vectors for NFACES...");
        double[][] fv_Nface = makeFeatureVector(
                FileUtils.combinePath(EnvironmentConstants.PROJECT_ROOT, "TrainingImages", "NFACES"),
                10000);
        int nNFaces = fv_Nface.length;
        System.out.println("FACES feature vectors computed.");

        System.out.println("Commencing training of cascade classifier");

        double[] w_face = new double[nFaces];
        double[] w_Nface = new double[nNFaces];
        initWeights(w_face, 1/(2*((double)nFaces)));
        initWeights(w_Nface, 1/(2*((double)nNFaces)));

        List<WeakClassifier> classifier = new ArrayList<WeakClassifier>();
        List<Integer> cascadeLevels = new ArrayList<Integer>();
        List<Double> cascadeThlds = new ArrayList<Double>();

        JFrame frame = null;

        double f = 0.75;

        int i = 0;
        int n = 0;

        double fp = 1;
        double tp = 1;
        double thld_adj = 0.1;
        double prev_fp;
        double prev_tp;

        while (fp > FALSE_POSITIVE_CHANCE_TARGET && i < MAX_NUMBER_OF_LAYERS) {
            i++;
            prev_fp = fp;
            prev_tp = tp;
            while (fp > f * prev_fp && n < MAX_NUMBER_OF_WEAK_CLASSIFIERS) {
                n++;
                long startTime = System.currentTimeMillis();

                System.out.println("\nFeature no: "+n);

                long selectTime = System.currentTimeMillis();
                classifier.add(
                        selectAndTrainWeakClassifier(fv_face, fv_Nface, w_face,
                                w_Nface));
                System.out.println("Selection took " + (System.currentTimeMillis() - selectTime) + " ms");

                long findThldAdjStartTime = System.currentTimeMillis();

                tp = Double.MAX_VALUE;
                double thld_adj_max = 10;
                double thld_adj_min = 0;
                for(int it=0; it<25 || tp < TRUE_POSITIVE_DECREASE_TOLERANCE * prev_tp; ++it) {
                    thld_adj = (thld_adj_max + thld_adj_min)/2;
                    tp = ((double)testCascade(fv_face, classifier,
                            cascadeLevels,  cascadeThlds, thld_adj))/((double)nFaces);
                    if(tp > TRUE_POSITIVE_DECREASE_TOLERANCE * prev_tp) {
                        thld_adj_min = thld_adj;
                    } else {
                        thld_adj_max = thld_adj;
                    }
                }

                System.out.println("Took " + (System.currentTimeMillis() - findThldAdjStartTime) + " ms to find threshold adj");

                fp = testCascade(fv_Nface, classifier, cascadeLevels, cascadeThlds, thld_adj);
                fp = fp/((double)nNFaces);

                System.out.println("TPR: "+tp+" with thld adj "+thld_adj);
                System.out.println("FPR: "+fp+" with thld adj "+thld_adj);
                System.out.println("Took " + (System.currentTimeMillis()-startTime) + " ms to add weak classifier");


                if (frame != null) {
                    frame.setVisible(false);
                    frame.dispose();
                }
//                frame = HaarFeatureComputer.showClassifierImg(classifier);

            }

            if (n <= MAX_NUMBER_OF_WEAK_CLASSIFIERS) {
                cascadeLevels.add(n);
                cascadeThlds.add(thld_adj);
                System.out.println("Layer "+i+" at "+(n)+" classifiers with thld adj="+thld_adj);
            }
            if (n == MAX_NUMBER_OF_WEAK_CLASSIFIERS) {
                break;
            }
        }

        return new CascadeClassifier(classifier, cascadeLevels, cascadeThlds);
    }

    private static WeakClassifier selectAndTrainWeakClassifier(double[][] fv_face,
            double[][] fv_Nface,
            double[] w_face,
            double[] w_Nface) {

        double normalizer = 1 / (Common.sum(w_face) + Common.sum(w_Nface));
        scaleWeights(w_face, normalizer);
        scaleWeights(w_Nface, normalizer);

        double[] mu_p = weightedMean(w_face, fv_face);
        double[] mu_n = weightedMean(w_Nface, fv_Nface);
        double[] thld = new double[NUMBER_OF_FEATURES];

        for (int j=0;j<NUMBER_OF_FEATURES;j++) {
            thld[j] = (mu_p[j]+mu_n[j])/2;
        }

        int bestIndex = 0;
        int bestParity = 1;
        double bestThreshold = 0;

        double minErr = Double.POSITIVE_INFINITY;
        for(int j=0; j<NUMBER_OF_FEATURES; ++j) {
            double err_minus = 0;
            double err_plus = 0;

            for(int i=0; i<fv_face.length; ++i) {
                if(fv_face[i][j] < thld[j]) {
                    // True positive - Increase error for parity -1
                    err_minus += w_face[i];
                } else {
                    // False positive - Increase error for parity +1
                    err_plus += w_face[i];
                }
                if(err_minus > minErr && err_plus > minErr) {
                    break;
                }
            }

            for(int i=0; i<fv_Nface.length; ++i) {
                if(fv_Nface[i][j] < thld[j]) {
                    // False negative - Increase error for parity +1
                    err_plus += w_Nface[i];
                } else {
                    // True negative - Increase error for parity -1
                    err_minus += w_Nface[i];
                }
                if(err_minus > minErr && err_plus > minErr) {
                    break;
                }
            }

            int p;
            double err;
            if(err_minus < err_plus) {
                err = err_minus;
                p = -1;
            } else {
                err = err_plus;
                p = 1;
            }

            if(err < minErr) {
                bestIndex = j;
                bestParity = p;
                bestThreshold = thld[bestIndex];
                minErr = err;
            }
        }

        System.out.println(String.format("Selected feature %d with error %.3f, threshold %.3f, parity %d", bestIndex, minErr, bestThreshold, bestParity));

        double alpha = updateWeights(w_face, fv_face, bestIndex, bestParity, bestThreshold, minErr, true);
        updateWeights(w_Nface, fv_Nface, bestIndex, bestParity, bestThreshold, minErr, false);

        return new WeakClassifier(bestIndex,bestThreshold,bestParity,alpha);
    }

    private static int testCascade(double[][] fv_face, List<WeakClassifier> weakClassifiers,
            List<Integer> cascadeLevels, List<Double> cascadeThlds, double thld_adj) {

        int sumDetection = 0;
        for (int i=0;i<fv_face.length;i++) {
            double sumH = 0;
            double sumA = 0;
            int s = 0;
            int nLayers = cascadeLevels.size();
            boolean rejected = false;

            for (int l=0;l<nLayers+1;l++) {

                int nSteps;
                if (l == nLayers) {
                    nSteps = weakClassifiers.size();
                }
                else {
                    nSteps = cascadeLevels.get(l);
                }

                while(s<nSteps) {
                    WeakClassifier c = weakClassifiers.get(s);
                    if(c.classify(fv_face[i])) {
                        sumH += c.alpha;
                    }
                    sumA += c.alpha;

                    s++;
                }

                double thld_adj_l;
                if (l == nLayers) {
                    thld_adj_l = thld_adj;
                }
                else {
                    thld_adj_l = cascadeThlds.get(l);
                }

                if(sumH<sumA/2*thld_adj_l) {
                    rejected = true;
                    break;
                }

            }

            if(!rejected) {
                sumDetection++;
            }

        }
        return sumDetection;
    }

    /*
     * Lowers the weight on correctly classified training data.
     */
    private static double updateWeights(double[] w, double[][] fv_face,
            int index, int par, double thld, double err, boolean pos) {

        double beta = err/(1-err);
        double alpha = Math.log(1/beta);

        // Loop over files.
        for (int i=0;i<fv_face.length;i++) {
            if (
                    (pos && par*fv_face[i][index] <  par*thld)    // True positive
                || (!pos && par*fv_face[i][index] >= par*thld)    // True negative
                ) {
                w[i] *= beta;
            }
        }
        return alpha;
    }

    /*
     *
     */
    private static void scaleWeights(double[] w, double value) {
        for (int j=0;j<w.length;j++) {
            w[j] *= value;
        }
    }

    /*
     * Sets all weights to some starting value.
     */
    private static void initWeights(double[] w, double value) {
        for (int j=0;j<w.length;j++) {
            w[j] = value;
        }
    }

    private static double[] weightedMean(double[] w, double[][] fv_face) {

        double[] mu = new double[NUMBER_OF_FEATURES];

        // Set everything to zero
        for (int j=0;j<NUMBER_OF_FEATURES;j++) {
            mu[j] = 0;
        }

        double w_sum = 0;

        // Loop over files
        for (int i=0;i<fv_face.length;i++) {

            // Loop over features
            for (int j=0;j<NUMBER_OF_FEATURES;j++) {
                mu[j] += w[i]*fv_face[i][j];
            }

            w_sum += w[i];
        }

        // Normalize
        for (int j=0;j<NUMBER_OF_FEATURES;j++) {
            mu[j] /= w_sum;
        }

        return mu;
    }

    /*
     * Returns an nFiles-by-nFeat array of features for at most nMax files in
     * the specified dir.
     */
    private static double[][] makeFeatureVector(String dir, int nMax) throws IOException {

        File folder = new File(dir);
        File[] listOfFiles = folder.listFiles();

        int nFiles = Math.min(listOfFiles.length, nMax);
        double[][] fv = new double[nFiles][NUMBER_OF_FEATURES];

        for(int fileNo = 0; fileNo < nFiles; fileNo++) {
            IntegralImage img = IntegralImage.makeIntegralImage(listOfFiles[fileNo]);
            HaarFeatureComputer hfc = new HaarFeatureComputer(img);

            for(int ind = 0; ind < NUMBER_OF_FEATURES; ind++) {
                fv[fileNo][ind] = hfc.computeFeature(ind);
            }
        }

        return fv;
    }

}
