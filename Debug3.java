import java.io.File;

public class Debug3 {

    public static final double[] fs = new double[]{27.7878, 21.7913, 20.3766,
            18.6791, 5.6434, -3.2352, -6.0722, 26.3625, 24.3620, 23.8478,
            28.9380, 28.5235, 31.8582, 22.2150, 15.9880, 17.2862, 24.6416,
            -29.9445, -18.0152, -19.6835, 35.1893, 29.3764, 40.6121, 38.8674,
            34.0833, 28.5498, 19.7362, 10.3651, 24.4421, 23.0340, 21.9528,
            18.3220, 12.8172, 14.4014, 16.3618, 14.4152, 15.5174, 25.4580,
            23.9536, 26.9705, 0.2260, -2.3264, -3.3979, -5.7781, 10.1402,
            9.4117, 11.7508, 13.7718, 20.1937, 27.6311, 24.9082, 20.6660,
            24.1601, 13.9094, 12.7980, 18.1956, 11.6968, 11.3063, 26.9185,
            23.1693, 13.0915, 9.8345, 23.2170, 26.7069, 26.3302, 27.5901,
            33.3023, 24.6419, 36.5972, 33.4521, 29.4684, 35.1761, 8.5318,
            8.0412, 10.8335, 5.9787, 7.6856, 21.7274, 20.6373, 17.3528,
            21.6099, 19.4173, 24.8462, 25.3161, 24.9228, 27.3892, 27.8775,
            2.9902, 3.6893, 19.0800, 22.6530, 22.6835, 19.9704, 34.3626,
            34.2481, 38.9012, 38.0277, 29.0981, 31.5134, 34.1506};

    public static final int type = 2;
    public static final int x = 7;
    public static final int y = 7;
    public static final int w = 5;
    public static final int h = 5;

    public static void main(String[] args) {

        int nFiles = 100;
        double fs[] = new double[nFiles];

        int ind = -1;
        HaarFeature.init();

        // Find the index of the feature stated above
        for(int i = 0; i < HaarFeature.NO_FEATURES; ++i) {
            if(HaarFeature.FEATURE_TABLE[i * 5] == type
                    && HaarFeature.FEATURE_TABLE[i * 5 + 1] == x
                    && HaarFeature.FEATURE_TABLE[i * 5 + 2] == y
                    && HaarFeature.FEATURE_TABLE[i * 5 + 3] == w
                    && HaarFeature.FEATURE_TABLE[i * 5 + 4] == h) {
                ind = i;
                break;
            }
        }

        for(int fileNo = 0; fileNo < nFiles; fileNo++) {
            File file =
                    new File(FileUtils.combinePath(
                            EnvironmentConstants.PROJECT_ROOT,
                            "TrainingImages", "FACES",
                            String.format("face%05d.bmp", fileNo + 1)));
            IntegralImage img = new IntegralImage(file);
            HaarFeature fet = new HaarFeature(img);

            fs[fileNo] = fet.computeFeature(ind);
        }

        double epsilon = 1E-1;
        for(int i = 0; i < 100; ++i) {
            if(Math.abs(fs[i] + Debug3.fs[i]) > epsilon) {
                System.out.println("Index " + i + " difference exceeds "
                        + epsilon + ": " + Debug3.fs[i] + " " + fs[i]);
            }
        }

        System.out.println("Finished");

    }

}
