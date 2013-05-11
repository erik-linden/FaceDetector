package se.kth.bik.project;

public class Common {
    
    public static final boolean DEBUG = false;
    
    public static void debugPrint(String msg) {
        if(DEBUG) {
            System.out.println(msg);
        }
    }

}
