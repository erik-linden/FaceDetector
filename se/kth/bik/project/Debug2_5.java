package se.kth.bik.project;

public class Debug2_5 {

    /**
     * Perform the sanity check on page 10 in the lab manual
     */
    public static void main(String[] args) {
        HaarFeatureComputer.init();
        int W = 19;
        int H = 19;

        for(int i = 0; i < HaarFeatureComputer.NO_FEATURES; i += 5) {
            switch(HaarFeatureComputer.FEATURE_TABLE[i]) {
            case 1:
                if(HaarFeatureComputer.FEATURE_TABLE[i+1] + HaarFeatureComputer.FEATURE_TABLE[i + 3] > W) {
                    System.out.println(i + " too wide: x=" + HaarFeatureComputer.FEATURE_TABLE[i+1] + ", w=" + HaarFeatureComputer.FEATURE_TABLE[i+3]);
                }
                if(HaarFeatureComputer.FEATURE_TABLE[i+2] + 2*HaarFeatureComputer.FEATURE_TABLE[i + 4] > H) {
                    System.out.println(i + " too high: y=" + HaarFeatureComputer.FEATURE_TABLE[i+2] + ", h=" + HaarFeatureComputer.FEATURE_TABLE[i+4]);
                }
                break;
            case 2:
                if(HaarFeatureComputer.FEATURE_TABLE[i+1] + 2*HaarFeatureComputer.FEATURE_TABLE[i + 3] > W) {
                    System.out.println(i + " too wide: x=" + HaarFeatureComputer.FEATURE_TABLE[i+1] + ", w=" + HaarFeatureComputer.FEATURE_TABLE[i+3]);
                }
                if(HaarFeatureComputer.FEATURE_TABLE[i+2] + HaarFeatureComputer.FEATURE_TABLE[i + 4] > H) {
                    System.out.println(i + " too high: y=" + HaarFeatureComputer.FEATURE_TABLE[i+2] + ", h=" + HaarFeatureComputer.FEATURE_TABLE[i+4]);
                }
                break;
            case 3:
                if(HaarFeatureComputer.FEATURE_TABLE[i+1] + 3*HaarFeatureComputer.FEATURE_TABLE[i + 3] > W) {
                    System.out.println(i + " too wide: x=" + HaarFeatureComputer.FEATURE_TABLE[i+1] + ", w=" + HaarFeatureComputer.FEATURE_TABLE[i+3]);
                }
                if(HaarFeatureComputer.FEATURE_TABLE[i+2] + HaarFeatureComputer.FEATURE_TABLE[i + 4] > H) {
                    System.out.println(i + " too high: y=" + HaarFeatureComputer.FEATURE_TABLE[i+2] + ", h=" + HaarFeatureComputer.FEATURE_TABLE[i+4]);
                }
                break;
            case 4:
                if(HaarFeatureComputer.FEATURE_TABLE[i+1] + 2*HaarFeatureComputer.FEATURE_TABLE[i + 3] > W) {
                    System.out.println(i + " too wide: x=" + HaarFeatureComputer.FEATURE_TABLE[i+1] + ", w=" + HaarFeatureComputer.FEATURE_TABLE[i+3]);
                }
                if(HaarFeatureComputer.FEATURE_TABLE[i+2] + 2*HaarFeatureComputer.FEATURE_TABLE[i + 4] > H) {
                    System.out.println(i + " too high: y=" + HaarFeatureComputer.FEATURE_TABLE[i+2] + ", h=" + HaarFeatureComputer.FEATURE_TABLE[i+4]);
                }
                break;
            }
        }
        System.out.println("Finished");
    }

}
