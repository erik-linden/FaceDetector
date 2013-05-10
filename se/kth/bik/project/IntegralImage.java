package se.kth.bik.project;

import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class IntegralImage {

	/**
	 * The integral image source image
	 */
	private double[][] integralImage;
	/**
	 * The integral image of the squared source image
	 */
	private double[][] squareIntegralImage;
	public int width;
	public int height;

	/**
	 * For testing.
	 * 
	 * @param args
	 */
	public static void main(String [ ] args) {
		File file = new File(FileUtils.combinePath(EnvironmentConstants.PROJECT_ROOT, "TrainingImages", "FACES", "face00001.bmp"));
		IntegralImage img = makeIntegralImage(file);
		img.drawIntegralImage(10);
	}
	
	static IntegralImage makeIntegralImage(File file) {
		try {
			BufferedImage srcImage = ImageIO.read(file);
			return new IntegralImage(srcImage);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Creates an <code>IntegralImage</code> from the specified file.
	 * 
	 * @param fileName name of image to use
	 */
	public IntegralImage(BufferedImage srcImage) {		
	
			width  = srcImage.getWidth();
			height = srcImage.getHeight();
			
			integralImage 	    = new double [width+1][height+1];
			squareIntegralImage = new double [width+1][height+1];

			updateSrcImage(srcImage);
			
	}

	public void updateSrcImage(BufferedImage srcImage) {
		int[] rgb = srcImage.getRGB(0, 0, srcImage.getWidth(), srcImage.getHeight(), null, 0, srcImage.getWidth());
		
		for(int y = 0; y<srcImage.getHeight(); y++) {
			for(int x = 0; x<srcImage.getWidth(); x++) {
				
				int i = x + srcImage.getWidth()*y;
						
				int red = (rgb[i] >> 16) & 0x000000FF;
				int green = (rgb[i] >>8 ) & 0x000000FF;
				int blue = (rgb[i]) & 0x000000FF;
				
				int xp = x+1;
				int yp = y+1;
				
				// Matlab style weighting
				integralImage[xp][yp] = 
						0.2989 * red +
						0.5870 * green +
						0.1140 * blue;
				
				// Build up an array containing the squared values of the original
				// grayscale image.
				squareIntegralImage[xp][yp] = Math.pow(integralImage[xp][yp],2);
			}
		}

		// Integrate both images.
		integrateImage(integralImage);
		integrateImage(squareIntegralImage);
	}

	
	public double xy(int x, int y) {
		return integralImage[x][y];
	}
	
	public double xyS(int x, int y) {
		return squareIntegralImage[x][y];
	}

	/**
	 * Calculates the integral image by taking the cumulative
	 * sum in both directions.
	 * 
	 * @param img image array to integrate
	 */
	private void integrateImage(double[][] img) {
		
		// Sum in the x-direction
		for(int y = 1; y<height+1;y++) {
			for(int x = 1; x<width+1;x++) {
				img[x][y] += img[x-1][y];
			}
		}

		// Sum in the y-direction
		for (int y = 1; y<height+1;y++) {
		    for (int x = 1; x<width+1;x++){
		        img[x][y] += img[x][y-1];
			}
		}
	}

	/**
	 * Returns a normalized integral image, ie the integral image 
	 * that would have been generated if the original image had been
	 * normalized.
	 * 
	 * @return normalized image
	 */
	private double[] getNormalizedImg() {
		double [] normImage = new double [width*height];
		
		// We are using
		//
		// std^2 = mean(x)^2 + mean(x^2)
		//
		// to normalize the image patch.
		double mean = integralImage[width][height]/(width*height);
		double meanSqr = squareIntegralImage[width][height]/(width*height);
		double std = Math.sqrt(meanSqr-Math.pow(mean,2));

		for(int y = 0; y<height;y++) {
			for(int x = 0; x<width;x++) {
				normImage[x + width*y] = integralImage[x+1][y+1]-(x+1)*(y+1)*mean;
				normImage[x + width*y] *= 1/std;
			}
		}

		return normImage;
	}

	/**
	 * Draws the normalized integral image, scaled to [0-255].
	 *
	 * @param scaleFactor scale the displayed image
	 */
	public void drawIntegralImage(double scaleFactor) {
		// We want to display the normalized integral image.
		double [] img = getNormalizedImg();
		showImg(img, width, height, scaleFactor);
	}
	
    /**
     * Image integral.
     * 
     * @param x
     *            the x coordinate of the upper-left corner
     * @param y
     *            the y coordinate of the upper-left corner
     * @param w
     *            the width of the rectangle
     * @param h
     *            the height of the rectangle
     * @return the integral of the rectangle specified by
     *         <code>x, y, w, h</code>
     */
	public double integral(int x, int y, int w, int h) {
    	// y  D    C
    	// ^  |----|
    	// |  |    |
    	// |  |----|
    	// |  A    B
    	// | -------> x
    
    	double A = xy(x, y);
    
    	double B = xy(x+w, y);
    
    	double D = xy(x, y+h);
    
    	double C = xy(x+w, y+h);
    
    	return A+C-B-D;			
    }

    static JFrame showImg(double[] img, int width, int height, double scaleFactor) {
		// Find the smallest and largest values.
				double min = Double.MAX_VALUE;
				double max = Double.MIN_VALUE;
				for(int i = 0; i<width*height;i++) {
					if (img[i]>max) {
						max = img[i];
					}
					else if (img[i]<min) {
						min = img[i];
					}
				}

				// Scale the image so that all values are in [0-255]
				for(int i = 0; i<width*height;i++) {
					img[i] = (img[i] - min)/(max - min)*255;
				}

				// We will write our double array to buffImg, so that 
				// we can display it.
				BufferedImage buffImg = new BufferedImage(width, height, 
						BufferedImage.TYPE_BYTE_GRAY);

				// We make a writable raster, write our image array to it
				// and add the raster to buffImg.
				WritableRaster raster = (WritableRaster) buffImg.getData();
				raster.setPixels(0, 0, width, height, img);
				buffImg.setData(raster);

				JFrame frame = new JFrame();
				frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

				frame.getContentPane().setLayout(new FlowLayout());
				frame.getContentPane().add(
						new JLabel(new ImageIcon(
								buffImg.getScaledInstance(
										(int)(width*scaleFactor), (int)(height*scaleFactor), Image.SCALE_SMOOTH))));
				frame.pack();
				frame.setVisible(true);
				
				return frame;
	}

}
