public class getResponseCode {
    public static void main(String[] args) throws Exception {
        try {
            MyHttpURLConnectionImpl myCon = new MyHttpURLConnectionImpl(null);
            int responseCode = myCon.getResponseCode();
            if (responseCode == -1) {
                throw new RuntimeException("java.net.HttpURLConnection "
                                           +"should provide implementation "
                                           +"for getResponseCode()");
            }
        } catch (java.net.UnknownServiceException e) {
            System.out.println("PASS");
        }
    }
}
class MyHttpURLConnectionImpl extends java.net.HttpURLConnection {
    MyHttpURLConnectionImpl(URL url) {
        super(url);
    }
    public boolean usingProxy(){
        return true;
    }
    public void connect(){
    }
    public void disconnect(){
    }
}
