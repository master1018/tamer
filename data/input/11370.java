public class PKCS12Oid {
    private static String OID_PBEWithSHAAnd40BitRC2CBC = "1.2.840.113549.1.12.1.6";
    private static String OID_PBEWithSHAAnd3KeyTripleDESCBC = "1.2.840.113549.1.12.1.3";
    public static void main(String[] argv) throws Exception {
        Cipher c = Cipher.getInstance(OID_PBEWithSHAAnd40BitRC2CBC, "SunJCE");
        c = Cipher.getInstance(OID_PBEWithSHAAnd3KeyTripleDESCBC, "SunJCE");
        System.out.println("All tests passed");
    }
}
