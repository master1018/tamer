public class WeakCrypto {
    public static void main(String[] args) throws Exception {
        System.setProperty("java.security.krb5.conf",
                System.getProperty("test.src", ".") +
                File.separator +
                "weakcrypto.conf");
        int[] etypes = EType.getBuiltInDefaults();
        for (int i=0, length = etypes.length; i<length; i++) {
            if (etypes[i] == EncryptedData.ETYPE_DES_CBC_CRC ||
                    etypes[i] == EncryptedData.ETYPE_DES_CBC_MD4 ||
                    etypes[i] == EncryptedData.ETYPE_DES_CBC_MD5) {
                throw new Exception("DES should not appear");
            }
        }
    }
}
