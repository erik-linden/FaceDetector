package se.kth.bik.project;

import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class ImageScanner {

	HaarFeatureComputer featureComputer;
	CascadeClassifier classifier;
	double pixelSkip = 1.0;
	double scaleSkip = 1.10;
	double thld_gain = 1.3;
	double startScale = 3;

	public static void main(String[] args) throws IOException, ClassNotFoundException {
		File folder = new File(FileUtils.combinePath(EnvironmentConstants.PROJECT_ROOT, "TestImages"));
		
		FileInputStream saveFile = new FileInputStream("trainingData.sav");
		ObjectInputStream restore = new ObjectInputStream(saveFile);
		Object tr = restore.readObject();
		restore.close();
		
		ImageScanner imgScanner = new ImageScanner();
		imgScanner.classifier = (CascadeClassifier) tr;
		
		HaarFeatureComputer.init();

		if(true) {
		    List<BufferedImage> images = new ArrayList<BufferedImage>(folder.listFiles().length);
		    for(File file : folder.listFiles()) {
		        images.add(ImageIO.read(file));
		    }
		    long millis = imgScanner.testPerformance(images);
		    System.out.println(String.format("Processed %d images in %d ms", images.size(), millis));
		} else {
		    File file = folder.listFiles()[7];

		    imgScanner.featureComputer = new HaarFeatureComputer(IntegralImage.makeIntegralImage(file));

		    long startTime = System.currentTimeMillis();
		    List<Detection> list = imgScanner.scan();
		    System.out.println((System.currentTimeMillis()-startTime));

		    //			for (Detection c : list) {
		    //				System.out.println("x: "+c.x+" y: "+c.y+" w: "+c.w);
		    //			}
		    System.out.println("Found: "+list.size());

		    JFrame frame = new JFrame();
		    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		    frame.getContentPane().setLayout(new FlowLayout());
		    frame.getContentPane().add(new JLabel(drawBoundingBoxes(ImageIO.read(file), list, 1)));
		    frame.pack();
		    frame.setVisible(true);
		}
		
	}

    public long testPerformance(Iterable<BufferedImage> images) {
        long startTime = System.currentTimeMillis();
        for(BufferedImage image : images) {
            featureComputer = new HaarFeatureComputer(new IntegralImage(image));
            scan();
        }
        return System.currentTimeMillis() - startTime;
    }

	List<Detection> scan() {
		List<Detection> list = new LinkedList<Detection>();
		int nTests = 0;
		double scale = startScale;

		int w = (int) Math.round(((double)HaarFeatureComputer.MIN_PATCH_SIDE)*scale);
		while (w<=Math.min(featureComputer.img.getHeight(), featureComputer.img.getWidth())) {
			int x=0;
			while (x<(featureComputer.img.getWidth()-w)) {
				int y=0;
				while (y<(featureComputer.img.getHeight()-w)) {
                    featureComputer.setROI(x, y, w);
					if (classifier.classifyPatch(featureComputer, thld_gain)) {
						list.add(new Detection(x,y,w));
					}
					nTests++;
					y = (int) (y+Math.round(pixelSkip*scale));
				}
				x = (int) (x+Math.round(pixelSkip*scale));
			}
			scale *= scaleSkip;
			w = (int) Math.round(((double)HaarFeatureComputer.MIN_PATCH_SIDE)*scale);
		}
		Common.debugPrint("Tested "+nTests+" locations");
		return list;
	}

    static ImageIcon drawBoundingBoxes(BufferedImage srcImage, List<Detection> list, double scaleFactor) {

		int width = srcImage.getWidth();
		int height = srcImage.getHeight();
		Color color = new Color(255, 0, 0);
		int rgb = color.getRGB();  

		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		img.getGraphics().drawImage(srcImage, 0, 0, null);

		for (Detection d : list) {
			for (int x=d.x;x<d.x+d.w;x++) {
				img.setRGB(x, d.y, rgb);
			}
			for (int x=d.x;x<d.x+d.w;x++) {
				img.setRGB(x, d.y+d.w, rgb);
			}
			for (int y=d.y;y<d.y+d.w;y++) {
				img.setRGB(d.x, y, rgb);
			}
			for (int y=d.y;y<d.y+d.w;y++) {
				img.setRGB(d.x+d.w, y, rgb);
			}
		}
		
		return new ImageIcon(img.getScaledInstance((int) (width * scaleFactor),
				(int) (height * scaleFactor), Image.SCALE_SMOOTH));

	}
}
