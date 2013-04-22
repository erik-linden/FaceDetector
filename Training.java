import java.io.File;


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

	static class TrainingResult {
		int ind;
		int par;
		double err;
		double thld;
	}

	static void train() {
		HaarFeature.init();

		fv_face = makeFeatureVector(
				"D:\\Dropbox\\BIK\\pro\\TrainingImages\\FACES\\",
				10000);
		nFaces = fv_face.length;

		fv_Nface = makeFeatureVector(
				"D:\\Dropbox\\BIK\\pro\\TrainingImages\\NFACES\\",
				10000);
		nNFaces = fv_face.length;

		double [] w_face = new double[nFaces];
		double [] w_Nface = new double[nNFaces];
		initWeights(w_face,1/(2*(double)nFaces));
		initWeights(w_Nface,1/(2*(double)nNFaces));

		double [] mu_p = new double[nFeat];
		double [] mu_n = new double[nFeat];
		double [] thld = new double[nFeat];
		double [][] err_face   = new double[nFeat][2];
		double [][] err_Nface  = new double[nFeat][2];
		double sum_p;
		double sum_n;
		TrainingResult tr;

		for (int n=0;n<10;n++) {
			long startTime = System.currentTimeMillis();

			sum_p = weightedMean(w_face, fv_face, nFaces, mu_p);
			sum_n = weightedMean(w_Nface, fv_Nface, nNFaces, mu_n);
			setThreshold(mu_p,mu_n,thld);

			setError(w_face, fv_face, nFaces, thld, err_face);
			setError(w_Nface, fv_Nface, nNFaces, thld, err_Nface);

			tr = getOptimal(w_face, err_face, sum_p,
					w_Nface, err_Nface, sum_n);
			tr.thld = thld[tr.ind];

			HaarFeature.printFeature(tr.ind);
			System.out.println((System.currentTimeMillis()-startTime)/1000);

			updateWeights(w_face, fv_face, nFaces, tr, 0);
			updateWeights(w_Nface, fv_Nface, nNFaces, tr, 1);
		}
	}

	static void updateWeights(double[] w, double[][] f, int n, TrainingResult tr, int inv) {

		double beta = tr.err/(1-tr.err);

		if((tr.par+inv)%2==0) {
			for (int i=0;i<n;i++) {
				if (f[i][tr.ind]<tr.thld) {
					w[i] *= beta;	
				}
			}
		}
		else {
			for (int i=0;i<n;i++) {
				if (f[i][tr.ind]>tr.thld) {
					w[i] *= beta;	
				}
			}
		}
	}

	static void initWeights(double[] w, double value) {
		for (int j=0;j<w.length;j++) {
			w[j] = value;
		}
	}

	static TrainingResult getOptimal(
			double[] w_face, double[][] err_face, double sum_p,
			double[] w_Nface, double[][] err_Nface, double sum_n) {

		TrainingResult tr = new TrainingResult();
		double minErr = Double.MAX_VALUE;
		double err;

		for (int j=0;j<nFeat;j++) {
			err = (err_face[j][0]+err_Nface[j][1]);
			if(err<minErr) {
				minErr = err;
				tr.ind = j;
				tr.par = 0;
			}
		}

		for (int j=0;j<nFeat;j++) {
			err = (err_face[j][1]+err_Nface[j][0]);
			if(err<minErr) {
				minErr = err;
				tr.ind = j;
				tr.par = 1;
			}
		}

		tr.err = minErr/(sum_p+sum_n);
		return tr;
	}

	static void setError(double[] w, double[][] f, int n, double[] thld, double[][] err) {

		for (int j=0;j<nFeat;j++) {

			err[j][0] = 0;
			err[j][1] = 0;

			for (int i=0;i<n;i++) {

				if (f[i][j]>thld[j]) {
					err[j][0] += w[i];
				}
				else {
					err[j][1] += w[i];
				}	
			}
		}	
	}

	/*
	 * Sets the array of thresholds thld such that 
	 * the threshold lies between the mean of the positive
	 * and negative examples.
	 */
	static void setThreshold(double[] mu_p, double[] mu_n, double[] thld) {
		for (int j=0;j<nFeat;j++) {
			thld[j] = (mu_p[j]+mu_n[j])/2;
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
