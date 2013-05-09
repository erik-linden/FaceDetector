package se.kth.bik.project;

public class Debug2_5 {

    /**
     * Perform the sanity check on page 10 in the lab manual
     */
    public static void main(String[] args) {
        HaarFeature.init();
        int W = 19;
        int H = 19;

        for(int i = 0; i < HaarFeature.NO_FEATURES; i += 5) {
            switch(HaarFeature.FEATURE_TABLE[i]) {
            case 1:
                if(HaarFeature.FEATURE_TABLE[i+1] + HaarFeature.FEATURE_TABLE[i + 3] > W) {
                    System.out.println(i + " too wide: x=" + HaarFeature.FEATURE_TABLE[i+1] + ", w=" + HaarFeature.FEATURE_TABLE[i+3]);
                }
                if(HaarFeature.FEATURE_TABLE[i+2] + 2*HaarFeature.FEATURE_TABLE[i + 4] > H) {
                    System.out.println(i + " too high: y=" + HaarFeature.FEATURE_TABLE[i+2] + ", h=" + HaarFeature.FEATURE_TABLE[i+4]);
                }
                break;
            case 2:
                if(HaarFeature.FEATURE_TABLE[i+1] + 2*HaarFeature.FEATURE_TABLE[i + 3] > W) {
                    System.out.println(i + " too wide: x=" + HaarFeature.FEATURE_TABLE[i+1] + ", w=" + HaarFeature.FEATURE_TABLE[i+3]);
                }
                if(HaarFeature.FEATURE_TABLE[i+2] + HaarFeature.FEATURE_TABLE[i + 4] > H) {
                    System.out.println(i + " too high: y=" + HaarFeature.FEATURE_TABLE[i+2] + ", h=" + HaarFeature.FEATURE_TABLE[i+4]);
                }
                break;
            case 3:
                if(HaarFeature.FEATURE_TABLE[i+1] + 3*HaarFeature.FEATURE_TABLE[i + 3] > W) {
                    System.out.println(i + " too wide: x=" + HaarFeature.FEATURE_TABLE[i+1] + ", w=" + HaarFeature.FEATURE_TABLE[i+3]);
                }
                if(HaarFeature.FEATURE_TABLE[i+2] + HaarFeature.FEATURE_TABLE[i + 4] > H) {
                    System.out.println(i + " too high: y=" + HaarFeature.FEATURE_TABLE[i+2] + ", h=" + HaarFeature.FEATURE_TABLE[i+4]);
                }
                break;
            case 4:
                if(HaarFeature.FEATURE_TABLE[i+1] + 2*HaarFeature.FEATURE_TABLE[i + 3] > W) {
                    System.out.println(i + " too wide: x=" + HaarFeature.FEATURE_TABLE[i+1] + ", w=" + HaarFeature.FEATURE_TABLE[i+3]);
                }
                if(HaarFeature.FEATURE_TABLE[i+2] + 2*HaarFeature.FEATURE_TABLE[i + 4] > H) {
                    System.out.println(i + " too high: y=" + HaarFeature.FEATURE_TABLE[i+2] + ", h=" + HaarFeature.FEATURE_TABLE[i+4]);
                }
                break;
            }
        }
        System.out.println("Finished");
    }

}
