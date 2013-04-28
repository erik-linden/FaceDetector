import java.io.File;

import javax.swing.JFrame;


public class Training {

	static int nFeat = HaarFeature.NO_FEATURES;
	static int nFaces;
	static int nNFaces;
	static double[][] fv_face;
	static double[][] fv_Nface;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Training.train();
	}

	static void train() {
		HaarFeature.init();
		int nIter = 200;

		fv_face = makeFeatureVector(
				"D:\\Dropbox\\BIK\\pro\\TrainingImages\\FACES\\",
				10000);
		nFaces = fv_face.length;

		fv_Nface = makeFeatureVector(
				"D:\\Dropbox\\BIK\\pro\\TrainingImages\\NFACES\\",
				10000);
		nNFaces = fv_Nface.length;

		double[] w_face = new double[nFaces];
		double[] w_Nface = new double[nNFaces];
		initWeights(w_face,1/(2*((double)nFaces)));
		initWeights(w_Nface,1/(2*((double)nNFaces)));

		double[] mu_p = new double[nFeat];
		double[] mu_n = new double[nFeat];
		double[] thld = new double[nFeat];
		int[] p = new int[nFeat];
		double[] err_face   = new double[nFeat];
		double[] err_Nface  = new double[nFeat];
		double sum_p;
		double sum_n;
		TrainingResult[] tr = new TrainingResult[nIter];
		JFrame frame = null;

		for (int n=0;n<nIter;n++) {

			long startTime = System.currentTimeMillis();

			sum_p = weightedMean(w_face, fv_face, nFaces, mu_p);
			sum_n = weightedMean(w_Nface, fv_Nface, nNFaces, mu_n);
			setThreshold(mu_p,mu_n,thld,p);

			setError(w_face, fv_face, nFaces, thld, p, err_face, 1);
			setError(w_Nface, fv_Nface, nNFaces, thld, p, err_Nface, 0);

			tr[n] = getOptimal(w_face, err_face, sum_p,
					w_Nface, err_Nface, sum_n);
			tr[n].thld = thld[tr[n].ind];
			tr[n].par  = p[tr[n].ind];

			updateWeights(w_face, fv_face, nFaces, tr[n], 1);
			updateWeights(w_Nface, fv_Nface, nNFaces, tr[n], 0);

			if (frame != null) {
				frame.setVisible(false);
				frame.dispose();
			}
//			frame = HaarFeature.showFeatureImg(tr[n].ind);
			frame = HaarFeature.showClassifierImg(tr);
			System.out.println("\nFeature no: "+n);
			System.out.println("True positives: "+testClassifier(fv_face, nFaces, tr)*100/nFaces+"%");
			System.out.println("False positives: "+testClassifier(fv_Nface, nNFaces, tr)*100/nNFaces+"%");
			System.out.println((System.currentTimeMillis()-startTime)/1000);
		}
	}

	static int testClassifier(double[][] f, int n, TrainingResult[] tr) {

		int sumDetection = 0;
		// Loop over files
		for (int i=0;i<n;i++) {

			double sumH = 0;
			double sumA = 0;
			for(int j=0;j<tr.length;j++) {
				if(tr[j] == null) {
					break;
				}
				if(f[i][tr[j].ind]*(-2*tr[j].par+1) > tr[j].thld*(-2*tr[j].par+1)) {
					sumH += tr[j].alpha;
				}
				sumA += tr[j].alpha;
			}
			if(sumH>=sumA/2) {
				sumDetection++;
			}
		}
		return sumDetection;
	}

	static void updateWeights(double[] w, double[][] f, int n, TrainingResult tr, int pos) {

		double beta = tr.err/(1-tr.err);
		tr.alpha = Math.log(1/beta);

		for (int i=0;i<n;i++) {
			if (tr.par == (pos+1)%2 && f[i][tr.ind]>tr.thld) {
				w[i] *= beta;
			}
			else if (tr.par == (pos)%2 && f[i][tr.ind]<tr.thld) {
				w[i] *= beta;
			}
		}
	}

	static void initWeights(double[] w, double value) {
		for (int j=0;j<w.length;j++) {
			w[j] = value;
		}
	}

	static TrainingResult getOptimal(
			double[] w_face, double[] err_face, double sum_p,
			double[] w_Nface, double[] err_Nface, double sum_n) {

		TrainingResult tr = new TrainingResult();
		double minErr = Double.MAX_VALUE;
		double err;

		for (int j=0;j<nFeat;j++) {
			err = (err_face[j]+err_Nface[j]);
			if(err<minErr) {
				minErr = err;
				tr.ind = j;
			}
		}

		tr.err = minErr/(sum_p+sum_n);
		return tr;
	}

	static void setError(double[] w, double[][] f, int n, double[] thld, int[] p, double[] err, int pos) {

		for (int j=0;j<nFeat;j++) {

			err[j] = 0;

			for (int i=0;i<n;i++) {

				if (p[j] == (pos+1)%2 && f[i][j]<thld[j]) {
					err[j] += w[i];
				}
				else if (p[j] == (pos)%2 && f[i][j]>thld[j]) {
					err[j] += w[i];
				}	
			}
		}	
	}

	/*
	 * Sets the array of thresholds thld such that 
	 * the threshold lies between the mean of the positive
	 * and negative examples.
	 */
	static void setThreshold(double[] mu_p, double[] mu_n, double[] thld, int[] p) {
		for (int j=0;j<nFeat;j++) {
			thld[j] = (mu_p[j]+mu_n[j])/2;
			if(mu_p[j]>mu_n[j]) {
				p[j] = 0;
			}
			else {
				p[j] = 1;
			}
		}
	}

	/*
	 * Computes the mean along the first dimension
	 * of f, weighted by w. n is the length
	 * of the first dimension of f.
	 * The result is placed in mu.
	 * The function returns the sum of the 
	 * weights.
	 */
	static double weightedMean(double[] w, double[][] f, int n, double[] mu) {

		// Set everything to zero
		double sum = 0;
		for (int j=0;j<nFeat;j++) {
			mu[j] = 0;
		}

		// Loop over files
		for (int i=0;i<n;i++) {

			// Loop over features
			for (int j=0;j<nFeat;j++) {
				mu[j] += w[i]*f[i][j];
			}

			sum += w[i];
		}

		// Normalization
		for (int j=0;j<nFeat;j++) {
			mu[j] /= sum;
		}

		return sum;
	}

	/*
	 * Returns an nFiles-by-nFeat array of features
	 * for all files in the specified dir.
	 */
	static double[][] makeFeatureVector(String dir, int nMax) {; 

	File folder = new File(dir);
	File[] listOfFiles = folder.listFiles();
	int nFiles = Math.min(listOfFiles.length,nMax);

	double[][] fv = new double[nFiles][nFeat];

	for(int fileNo=0;fileNo<nFiles;fileNo++) {
		IntegralImage img = new IntegralImage(listOfFiles[fileNo]);
		HaarFeature fet = new HaarFeature(img);

		for(int ind=0;ind<nFeat;ind++) {
			fv[fileNo][ind] = fet.computeFeature(ind);
		}
	}

	return fv;
	}

}
