public class LotsOfDestroys {
    static final int RUNS = 400;
    static final String ECHO = "/usr/bin/echo";
    public static void main(String[] args) throws Exception {
        if (File.separatorChar == '\\' ||                
                                !new File(ECHO).exists()) 
            return;
        for (int i = 0; i<= RUNS; i++) {
            Process process = Runtime.getRuntime().exec(ECHO + " x");
            process.destroy();
        }
    }
}
