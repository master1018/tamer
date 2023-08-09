public class TruncateArray {
    public static void main(String args[]) throws Exception {
        try {
            TrustManager tms [] = new TrustManager [] {
                new MyTM(), new MyTM(), new MyTM() };
            KeyManager kms [] = new KeyManager [] {
                new MyKM(), new MyKM(), new MyKM() };
            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(kms, tms, null);
            KeyManagerFactory kmf = KeyManagerFactory.getInstance("SUNX509");
            KeyManager[] km = kmf.getKeyManagers();
            TrustManagerFactory tmf =
                TrustManagerFactory.getInstance("SUNX509");
            TrustManager[] tm = tmf.getTrustManagers();
        } catch (ClassCastException e) {
            throw e;
        } catch (Throwable e) {
            System.out.println("Caught something else");
            e.printStackTrace();
        }
    }
    static class MyTM implements TrustManager {
    }
    static class MyKM implements KeyManager {
    }
}
