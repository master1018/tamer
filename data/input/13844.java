public class NullLock {
    public static void main(String argv[]) throws Exception {
        if (!testBufferedReader())
            throw new Exception("Buffered Reader constructor: "
                                + "Null argument must throw an exception");
        if (!testBufferedWriter())
            throw new Exception("BufferedWriter constructor: "
                                + " Null arg must throw an exception");
    }
    static boolean testBufferedReader(){
        try {
            InputStreamReader isr = null;
            BufferedReader br = new BufferedReader(isr);
        } catch(NullPointerException e) {
            return true;
        }
        return false;
    }
    static boolean testBufferedWriter(){
        try {
            OutputStreamWriter isr = null;
            BufferedWriter br = new BufferedWriter(isr);
        } catch(NullPointerException e) {
            return true;
        }
        return false;
    }
}
