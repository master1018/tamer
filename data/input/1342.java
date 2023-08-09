public class HandlerEquals {
    public static void main (String args[]) throws Exception {
        int errorCnt = 0 ;
        URL url1 = new URL("ftp:
        URL url2 = new URL("ftp:
        if (url1.equals(url2)) {
            throw new RuntimeException("URLStreamHandler.equals failure.");
        } else {
            System.out.println("Success.");
        }
    }
}
