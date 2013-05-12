package se.kth.bik.project;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFrame;

public class HaarFeatureComputer {

	public static final int MIN_PATCH_SIDE = 19; //Smallest block size
	public static final int NO_FEATURES = 53130;
	public static int[] FEATURE_TABLE = new int[NO_FEATURES*5];

	public IntegralImage img;
	private int origin_x = 0;
	private int origin_y = 0;
	private double patch_scale;
	private double patch_mean;
	private double patch_std;

	/**
	 * For testing.
	 * 
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		HaarFeatureComputer.init();

		File file = new File(FileUtils.combinePath(EnvironmentConstants.PROJECT_ROOT, "TrainingImages","FACES","face00001.bmp"));
		IntegralImage img = IntegralImage.makeIntegralImage(file);
		HaarFeatureComputer fet = new HaarFeatureComputer(img);

		for(int i=0;i<NO_FEATURES;i++) {
			System.out.println(fet.computeFeature(i)*100);
		}


	}

	/**
	 * Creates a HaarFeature object from an
	 * <code>IntegralImage</code>. The Region
	 * Of Interest is set to the largest possible
	 * square patch.
	 * 
	 * @param newImg
	 */
	public HaarFeatureComputer(IntegralImage newImg) {
		img = newImg;
		setROI(0, 0, Math.min(img.getWidth(), img.getHeight()));
	}

	/**
	 * Sets a new ROI and calculates the mean 
	 * and std for that region.
	 * 
	 * @param x ROI x-origin
	 * @param y ROI x-origin
	 * @param w ROI side length
	 */
	public void setROI(int x, int y, int w) {
		origin_x = x;
		origin_y = y;		
		patch_scale = ((double)w)/((double)MIN_PATCH_SIDE);

		// Here we use:
		// std^2 = mean(x^2) - mean(x)^2
		patch_mean = img.integral(x,y,w,w);
		patch_mean /= ((double)(w*w));

		double meanSqr = img.integralOfSquare(x,y,w,w);
		meanSqr /= ((double)(w*w));

		if (meanSqr<=0) {
			patch_std = 1;
		}
		else {
			patch_std = Math.sqrt(meanSqr-Math.pow(patch_mean,2));
		}
	}

	/**
	 * Calculates the value of the feature
	 * enumerated by <code>ind</code>.
	 * 
	 * @param ind feature number
	 * @return	feature value
	 */
	public double computeFeature(int ind) {
		ind *= 5;
		int type = FEATURE_TABLE[ind];
		int x = FEATURE_TABLE[ind+1];
		int y = FEATURE_TABLE[ind+2];
		int w = FEATURE_TABLE[ind+3];
		int h = FEATURE_TABLE[ind+4];

		return computeFeature(type, x, y, w, h);
	}

    public double computeFeature(int type, int x, int y, int w, int h) {
        // Scale the feature to fit the current patch.
		x = (int) Math.round(origin_x + x*patch_scale);
		y = (int) Math.round(origin_y + y*patch_scale);
		w = (int) Math.round(w*patch_scale);
		h = (int) Math.round(h*patch_scale);

		switch (type) {
		case 1: 
			return typeI(x, y, w, h);
		case 2: 
			return typeII(x, y, w, h);
		case 3: 
			return typeIII(x, y, w, h);
		case 4:
			return typeIV(x, y, w, h);
		default:
			System.err.println(String.format("Tried to use feature type: %d x=%d y=%d w=%d h=%d", type, x, y, w, h));
			return Double.NaN;
		}
    }

	/**
	 * Type I feature:
	 * 
	 * 	<w->
	 *  ---- h
	 *  ++++ h
	 *
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public double typeI(int x, int y, int w, int h) {

		double sumU = img.integral(x,y,w,h);
		double sumD = img.integral(x,y+h,w,h);

		return (sumD-sumU)/patch_std;
	}

	/**
	 * Type II feature:
	 * 
	 *  <w-><w->
	 *  ++++---- ^
	 *  ++++---- h
	 *  ++++---- v
	 *
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public double typeII(int x, int y, int w, int h) {

		double sumL = img.integral(x,y,w,h);
		double sumR = img.integral(x+w,y,w,h);

		return (sumL-sumR)/patch_std;
	}

	/**
	 * Type III feature:
	 * 
	 *	<w-><w-><w->
	 *  ++++----++++ ^
	 *  ++++----++++ h
	 *  ++++----++++ v
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public double typeIII(int x, int y, int w, int h) {

		double sumL = img.integral(x,y,w,h);
		double sumC = img.integral(x+w,y,w,h);
		double sumR = img.integral(x+2*w,y,w,h);

		// This is the only feature where we
		// have to account for the mean, since
		// there are more (+) than (-).
		return (sumL-sumC+sumR-patch_mean*w*h)/patch_std;
	}

	/**
	 * Type IV feature:
	 * 
	 * 	<w-><w->
	 *  ++++---- ^
	 *  ++++---- h
	 *  ++++---- v
	 *  ----++++ ^
	 *  ----++++ h
	 *  ----++++ v
	 * 
	 * @param x
	 * @param y
	 * @param w
	 * @param h
	 * @return
	 */
	public double typeIV(int x, int y, int w, int h) {

		double sumLU = img.integral(x,y,w,h);
		double sumRU = img.integral(x+w,y,w,h);
		double sumLD = img.integral(x,y+h,w,h);
		double sumRD = img.integral(x+w,y+h,w,h);

		return (-sumLD+sumRD+sumLU-sumRU)/patch_std;
	}

	/**
	 * Creates an enumeration of all possible
	 * features. Must be called on the <code>HaarFeature</code>-class
	 * before any feature value calculations can be made.
	 * 
	 */
	public static void init() {
		int i = 0;
		//Type 1
		for(int w=1; w<=MIN_PATCH_SIDE; w++) {
			for(int h=1; h<=MIN_PATCH_SIDE/2; h++) {
				for(int x=0; x<=MIN_PATCH_SIDE-w; x++) {
					for(int y=0; y<=MIN_PATCH_SIDE-2*h; y++) {
						FEATURE_TABLE[i] = 1;
						FEATURE_TABLE[i+1] = x;
						FEATURE_TABLE[i+2] = y;
						FEATURE_TABLE[i+3] = w;
						FEATURE_TABLE[i+4] = h;
						i += 5;
					}
				}
			}
		}

		//Type 2
		for(int w=1; w<=MIN_PATCH_SIDE/2; w++) {
			for(int h=1; h<=MIN_PATCH_SIDE; h++) {
				for(int x=0; x<=MIN_PATCH_SIDE-2*w; x++) {
					for(int y=0; y<=MIN_PATCH_SIDE-h; y++) {
						FEATURE_TABLE[i] = 2;
						FEATURE_TABLE[i+1] = x;
						FEATURE_TABLE[i+2] = y;
						FEATURE_TABLE[i+3] = w;
						FEATURE_TABLE[i+4] = h;
						i += 5;
					}
				}
			}
		}

		//Type 3
		for(int w=1; w<=MIN_PATCH_SIDE/3; w++) {
			for(int h=1; h<=MIN_PATCH_SIDE; h++) {
				for(int x=0; x<=MIN_PATCH_SIDE-3*w; x++) {
					for(int y=0; y<=MIN_PATCH_SIDE-h; y++) {
						FEATURE_TABLE[i] = 3;
						FEATURE_TABLE[i+1] = x;
						FEATURE_TABLE[i+2] = y;
						FEATURE_TABLE[i+3] = w;
						FEATURE_TABLE[i+4] = h;
						i += 5;
					}
				}
			}
		}

		//Type 4
		for(int w=1; w<=MIN_PATCH_SIDE/2; w++) {
			for(int h=1; h<=MIN_PATCH_SIDE/2; h++) {
				for(int x=0; x<=MIN_PATCH_SIDE-2*w; x++) {
					for(int y=0; y<=MIN_PATCH_SIDE-2*h; y++) {
						FEATURE_TABLE[i] = 4;
						FEATURE_TABLE[i+1] = x;
						FEATURE_TABLE[i+2] = y;
						FEATURE_TABLE[i+3] = w;
						FEATURE_TABLE[i+4] = h;
						i += 5;
					}
				}
			}
		}
	}

	private static double[] getFeatureImg(int ind) {

	    ind *= 5;
	    int type = FEATURE_TABLE[ind];
	    int xf = FEATURE_TABLE[ind+1];
	    int yf = FEATURE_TABLE[ind+2];
	    int w = FEATURE_TABLE[ind+3];
	    int h = FEATURE_TABLE[ind+4];

		return getFeatureImg(type, xf, yf, w, h);
	}

    private static double[] getFeatureImg(int type, int xf, int yf, int w, int h) {
        int width = MIN_PATCH_SIDE;
		double[] img = new double[width*width];

		switch (type) {
		case 1:
			for(int y=0;y<MIN_PATCH_SIDE;y++) {
				for(int x=0;x<MIN_PATCH_SIDE;x++) {
					if ((x>=xf && x<xf+w) && (y>=yf && y<yf+h))
						img[x + width*y] = -1;
					else if ((x>=xf && x<xf+w) && (y>=yf+h && y<yf+2*h))
						img[x + width*y] = +1;
				}
			}
			break;

		case 2:
			for(int y=0;y<MIN_PATCH_SIDE;y++) {
				for(int x=0;x<MIN_PATCH_SIDE;x++) {
					if ((x>=xf && x<xf+w) && (y>=yf && y<yf+h))
						img[x + width*y] = +1;
					else if ((x>=xf+w && x<xf+2*w) && (y>=yf && y<yf+h))
						img[x + width*y] = -1;
				}
			}
			break;

		case 3:
			for(int y=0;y<MIN_PATCH_SIDE;y++) {
				for(int x=0;x<MIN_PATCH_SIDE;x++) {
					if ((x>=xf && x<xf+w) && (y>=yf && y<yf+h))
						img[x + width*y] = +1;
					else if ((x>=xf+w && x<xf+2*w) && (y>=yf && y<yf+h))
						img[x + width*y] = -1;
					else if ((x>=xf+2*w && x<xf+3*w) && (y>=yf && y<yf+h))
						img[x + width*y] = +1;
				}
			}
			break;

		case 4:
			for(int y=0;y<MIN_PATCH_SIDE;y++) {
				for(int x=0;x<MIN_PATCH_SIDE;x++) {
					if ((x>=xf && x<xf+w) && (y>=yf && y<yf+h))
						img[x + width*y] = +1;
					else if ((x>=xf+w && x<xf+2*w) && (y>=yf && y<yf+h))
						img[x + width*y] = -1;
					else if ((x>=xf && x<xf+w) && (y>=yf+h && y<yf+2*h))
						img[x + width*y] = -1;
					else if ((x>=xf+w && x<xf+2*w) && (y>=yf+h && y<yf+2*h))
						img[x + width*y] = +1;
				}
			}
			break;

		default:
			break;
		}

		return img;
    }

	public static JFrame showClassifierImg(List<WeakClassifier> classifier) {

		double[] imgTemp = getFeatureImg(classifier.get(0).index);
		int length = imgTemp.length;
		double[] img = new double[length];
		for(WeakClassifier c: classifier) {
			imgTemp = getFeatureImg(c.index);

			for(int i=0; i<length; i++) {
				img[i] += c.parity*imgTemp[i]*c.alpha;
			}
		}

		return IntegralImage.showImg(img, MIN_PATCH_SIDE, MIN_PATCH_SIDE, 10);
	}
}