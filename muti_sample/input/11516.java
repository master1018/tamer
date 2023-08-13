public class DelayDefaultContextLoading {
    public static void main(String[] args) throws Exception {
        Date date1 = new Date();
        HttpsURLConnection.getDefaultHostnameVerifier();
        Date date2 = new Date();
        long delta = (date2.getTime() - date1.getTime()) / 1000;
        if (delta > 5) {
            throw new Exception("FAILED:  HttpsURLConnection took " + delta +
                " seconds to load");
        }
        System.out.println("PASSED:  HttpsURLConnection took " + delta +
            " seconds to load");
    }
}
