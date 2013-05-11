package se.kth.bik.project;

import java.io.File;
import java.io.IOException;

public class Debug2 {

    public static final double f1 = 1.7414;
    public static final double f2 = -5.5576;
    public static final double f3 = -18.0986;
    public static final double f4 = 0.3335;

    public static final int h = 5;
    public static final int w = 5;
    public static final int x = 4;
    public static final int y = 5;

    public static void main(String[] args) throws IOException {
        IntegralImage iim = IntegralImage.makeIntegralImage(new File(FileUtils.combinePath(EnvironmentConstants.PROJECT_ROOT, "TrainingImages", "FACES", "face00001.bmp")));
        HaarFeature.init();
        HaarFeature hf = new HaarFeature(iim);
        System.out.println(hf.typeI(x, y, w, h));
        System.out.println(hf.typeII(x, y, w, h));
        System.out.println(hf.typeIII(x, y, w, h));
        System.out.println(hf.typeIV(x, y, w, h));
        System.out.println("Finished");
    }

}
