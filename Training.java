import java.beans.FeatureDescriptor;
import java.io.File;


public class Training {

	int nFeat = HaarFeature.NO_FEATURES;
	int nFaces;
	int nNFaces;
	double[][] fv_face;
	double[][] fv_Nface;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Training t = new Training();

		t.train();
	}

	void train() {
		HaarFeature.init();

		fv_face = makeFeatureVector("D:\\Dropbox\\BIK\\pro\\TrainingImages\\FACES\\");
		nFaces = fv_face.length;

		fv_Nface = makeFeatureVector("D:\\Dropbox\\BIK\\pro\\TrainingImages\\NFACES\\");
		nNFaces = fv_face.length;

		double [] w_face = new double[nFaces];
		double [] w_Nface = new double[nNFaces];
		initWeights(w_face);
		initWeights(w_Nface);

		double [] mu_p = new double[nFeat];
		double [] mu_n = new double[nFeat];
		double [] thld = new double[nFeat];
		double [][] err_face   = new double[nFeat][2];
		double [][] err_Nface  = new double[nFeat][2];
		int [] indAndParity = new int[2];
		double sum_p;
		double sum_n;

		System.out.println("Training weak");
		
		sum_p = weightedMean(w_face, fv_face, nFaces, mu_p);
		sum_n = weightedMean(w_Nface, fv_Nface, nNFaces, mu_n);
		setThreshold(mu_p,mu_n,thld);

		setError(w_face, fv_face, nFaces, thld, err_face);
		setError(w_Nface, fv_Nface, nNFaces, thld, err_Nface);

		setOptimal(indAndParity,
				w_face, err_face, sum_p,
				w_Nface, err_Nface, sum_n);

		HaarFeature.printFeature(indAndParity[0]);
	}

	void initWeights(double[] w) {
		for (int j=0;j<w.length;j++) {
			w[j] = 1;
		}
	}

	void setOptimal(int[] indAndParity,
			double[] w_face, double[][] err_face, double sum_p,
			double[] w_Nface, double[][] err_Nface, double sum_n) {

		double err;
		double minErr = Double.MAX_VALUE;
		for (int j=0;j<nFeat;j++) {
			err = (err_face[j][0]/sum_p+err_Nface[j][1]/sum_n);
			if(err<minErr) {
				minErr = err;
				indAndParity[0] = j;
				indAndParity[1] = 0;
			}
		}
		for (int j=0;j<nFeat;j++) {
			err = (err_face[j][1]/sum_p+err_Nface[j][0]/sum_n);
			if(err<minErr) {
				minErr = err;
				indAndParity[0] = j;
				indAndParity[1] = 1;
			}
		}
	}

	void setError(double[] w, double[][] f, int n, double[] thld, double[][] err) {
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

	void setThreshold(double[] mu_p, double[] mu_n, double[] thld) {
		for (int j=0;j<nFeat;j++) {
			thld[j] = (mu_p[j]+mu_n[j])/2;
		}
	}

	double weightedMean(double[] w, double[][] f, int n, double[] mu) {

		double div = 0;
		for (int i=0;i<n;i++) {
			for (int j=0;j<nFeat;j++) {
				mu[j] += w[i]*f[i][j];
			}
			div += w[i];
		}

		for (int j=0;j<nFeat;j++) {
			mu[j] /= div;
		}

		return div;
	}

	double[][] makeFeatureVector(String dir) {; 

	File folder = new File(dir);
	File[] listOfFiles = folder.listFiles();
	int nFiles = listOfFiles.length;

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
