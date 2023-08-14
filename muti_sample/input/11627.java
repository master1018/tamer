public class GetDefaultPort {
    public static void main(String args[]) throws Exception {
        int p;
        URL url = new URL ("http:
        if ((p=url.getDefaultPort ()) != 80)
            throw new Exception ("getDefaultPort returned wrong value: "+p);
    }
}
