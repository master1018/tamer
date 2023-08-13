public class LotsOfOutput {
    static final String CAT = "/usr/bin/cat";
    public static void main(String[] args) throws Exception{
        if (File.separatorChar == '\\' ||                
                                !new File(CAT).exists()) 
            return;
        Process p = Runtime.getRuntime().exec(CAT + " /dev/zero");
        long initMemory = Runtime.getRuntime().totalMemory();
        for (int i=1; i< 10; i++) {
            Thread.sleep(100);
            if (Runtime.getRuntime().totalMemory() > initMemory + 1000000)
                throw new Exception("Process consumes memory.");
        }
    }
}
