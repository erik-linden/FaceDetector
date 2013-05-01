import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Vector;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;


public class ImageScanner {

	HaarFeature f;
	Vector<WeakClassifier> classifier;
	int pixelSkip = 3;
	double scaleSkip = 1.1;

	public static void main(String[] args) {
		File file = new File("D:\\Dropbox\\BIK\\pro\\TestImages\\Student4.jpg");
		try {
			BufferedImage srcImage = ImageIO.read(file);

			IntegralImage img = new IntegralImage(file);
			ImageScanner imgScanner = new ImageScanner();
			imgScanner.f = new HaarFeature(img);
			
			FileInputStream saveFile = new FileInputStream("trainingData.sav");
			ObjectInputStream restore = new ObjectInputStream(saveFile);
			Object tr = restore.readObject();
			restore.close();

			HaarFeature.init();
			imgScanner.classifier = (Vector<WeakClassifier>) tr;
			Vector<Detection> list = imgScanner.scan();

//			for (Detection c : list) {
//				System.out.println("x: "+c.x+" y: "+c.y+" w: "+c.w);
//			}
			System.out.println("Found: "+list.size());
			drawBoundingBoxes(srcImage, list, 1);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	Vector<Detection> scan() {
		Vector<Detection> list = new Vector<>();
		int nTests = 0;
		
		int w = HaarFeature.MIN_PATCH_SIDE;
		while (w<=Math.min(f.img.height, f.img.width)) {
			int x=0;
			while (x<(f.img.width-w)) {
				int y=0;
				while (y<(f.img.height-w)) {
					if (testPatch(x,y,w)) {
						list.add(new Detection(x,y,w));
					}
					nTests++;
					y = y+pixelSkip;
				}
				x = x+pixelSkip;
			}
			w = (int) Math.round(w*scaleSkip);
		}		
		System.out.println("Tested "+nTests+" locations");
		return list;
	}

	private boolean testPatch(int x, int y, int w) {

		f.setROI(x, y, w);

		double sumH = 0;
		double sumA = 0;
		for(WeakClassifier c : classifier) {
			if(c.parity*f.computeFeature(c.index) > c.parity*c.thld) {
				sumH += c.alpha;
			}
			sumA += c.alpha;
		}
		if(sumH>=sumA/2*1.4) {
			return true;
		}
		else {
			return false;
		}
	}

	static void drawBoundingBoxes(BufferedImage srcImage, Vector<Detection> list, double scaleFactor) {

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
		File outputfile = new File("img6.jpg");
	    try {
			ImageIO.write(img, "jpg", outputfile);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(
				new JLabel(new ImageIcon(
						img.getScaledInstance(
								(int)(width*scaleFactor), (int)(height*scaleFactor), Image.SCALE_SMOOTH))));
		frame.pack();
		frame.setVisible(true);
	}
}
