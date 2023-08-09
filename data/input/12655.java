public class NullMacSpi {
    public static void main(String[] args) throws Exception {
        MyMac mm = new MyMac(null, null, null);
        if (mm.getProvider() == null) {
            System.out.println("Test Passed");
        }
    }
}
class MyMac extends Mac {
    public MyMac(MacSpi macSpi, Provider provider,String algorithm) {
        super(macSpi, provider, algorithm);
    }
}
