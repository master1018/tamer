public class UNCTest {
    public static void main(String args[]) throws Exception {
        URL url = new URL( args[0] );
        URLConnection conn = url.openConnection();
        conn.setRequestProperty( "User-Agent", "Java" );
    }
}
