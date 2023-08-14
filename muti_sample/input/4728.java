public class WinMaxPath {
    public static void main(String[] args) throws Exception {
        String osName = System.getProperty("os.name");
        if (!osName.startsWith("Windows")) {
            return;
        }
        try {
            char[] as = new char[65000];
            java.util.Arrays.fill(as, 'a');
            FileOutputStream out = new FileOutputStream(new String(as));
            out.close();
        } catch (FileNotFoundException x) {
        }
    }
}
