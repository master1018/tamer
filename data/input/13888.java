public class JavaxURLNulls {
    public static void main(String[] args) throws Exception {
        URL foobar = new URL("https:
        HttpsURLConnection urlc =
            (HttpsURLConnection) foobar.openConnection();
        try {
            urlc.getCipherSuite();
        } catch (IllegalStateException e) {
            System.out.print("Caught proper exception: ");
            System.out.println(e.getMessage());
        }
        try {
            urlc.getLocalCertificates();
        } catch (IllegalStateException e) {
            System.out.print("Caught proper exception: ");
            System.out.println(e.getMessage());
        }
        try {
            urlc.getServerCertificates();
        } catch (IllegalStateException e) {
            System.out.print("Caught proper exception: ");
            System.out.println(e.getMessage());
        }
        try {
            urlc.setDefaultHostnameVerifier(null);
        } catch (IllegalArgumentException e) {
            System.out.print("Caught proper exception: ");
            System.out.println(e.getMessage());
        }
        try {
            urlc.setHostnameVerifier(null);
        } catch (IllegalArgumentException e) {
            System.out.print("Caught proper exception: ");
            System.out.println(e.getMessage());
        }
        try {
            urlc.setDefaultSSLSocketFactory(null);
        } catch (IllegalArgumentException e) {
            System.out.print("Caught proper exception: ");
            System.out.println(e.getMessage());
        }
        try {
            urlc.setSSLSocketFactory(null);
        } catch (IllegalArgumentException e) {
            System.out.print("Caught proper exception: ");
            System.out.println(e.getMessage());
        }
        System.out.println("TESTS PASSED");
    }
}
