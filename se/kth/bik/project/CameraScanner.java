package se.kth.bik.project;
import java.awt.FlowLayout;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.List;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

import com.googlecode.javacv.VideoInputFrameGrabber;

public class CameraScanner {

	public static void main(String[] args) {

		HaarFeature.init();

		JFrame frame = new JFrame();
		ImageIcon icon = new ImageIcon();

		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		frame.getContentPane().setLayout(new FlowLayout());
		frame.getContentPane().add(new JLabel(icon));
		frame.pack();
		frame.setVisible(true);

		final VideoInputFrameGrabber grabber = new VideoInputFrameGrabber(0);
		try {
			grabber.start();
			BufferedImage srcImage = grabber.grab().getBufferedImage();

			IntegralImage img = new IntegralImage(srcImage);
			ImageScanner imgScanner = new ImageScanner();
			imgScanner.f = new HaarFeature(img);
			imgScanner.thld_gain = 1.45;
			imgScanner.startScale = 3;
			imgScanner.scaleSkip = 1.20;

			FileInputStream saveFile = new FileInputStream("trainingData.sav");
			ObjectInputStream restore = new ObjectInputStream(saveFile);
			Object tr = restore.readObject();
			restore.close();

			imgScanner.classifier = (CascadeClassifier) tr;
			
			for(int i=0;i<100;) {
				long startTime = System.currentTimeMillis();
				srcImage = grabber.grab().getBufferedImage();
				imgScanner.f.img.setSrcImage(srcImage);
				List<Detection> list = imgScanner.scan();
				System.out.println((System.currentTimeMillis()-startTime));

				System.out.println("Found: "+list.size());
				ImageScanner.drawBoundingBoxes(srcImage, list, 1);
				frame.repaint();
				frame.setSize(srcImage.getWidth(), srcImage.getHeight());
			}

		}
		catch (Exception e) {

		}

	}

}
