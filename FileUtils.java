import java.io.File;

public class FileUtils {

    /**
     * Generate system-independent path strings.
     * 
     * @param names
     *            the sequence of names in the path. For example, the Windows
     *            path <code>C:\Windows\system32</code> consists of the names
     *            <code>["C:", "Windows", "system32"]</code>, and the Unix path
     *            <code>"theme/img/header.png"</code> consists of the names
     *            <code>["theme", "img", "header.png"</code>.
     * @return a String consisting of the given names, in order, separated by
     *         the value of {@link File#separator}.
     */
    public static String combinePath(String... names) {
        StringBuilder result = new StringBuilder();
        if(names.length > 0) {
            result.append(names[0]);
        }
        for(int i = 1; i < names.length; ++i) {
            result.append(File.separator);
            result.append(names[i]);
        }
        return result.toString();
    }
    
    public static void main(String[] args) {
        System.out.println(combinePath("theme", "img", "header.png"));
    }

}
