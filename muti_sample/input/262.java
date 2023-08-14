public class Fix5070632 {
    public static void main(String[] args) throws Exception {
        Security.setProperty("ssl.SocketFactory.provider", "foo.NonExistant");
        SSLSocketFactory fac = (SSLSocketFactory)SSLSocketFactory.getDefault();
        try {
            fac.createSocket();
        } catch(SocketException se) {
            System.out.println("Throw SocketException");
            se.printStackTrace();
            return;
        }
        throw new Exception("should throw SocketException");
    }
}
