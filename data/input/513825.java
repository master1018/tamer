class CertTool {
    private static final String LOGTAG = "CertTool";
    private static final AlgorithmIdentifier MD5_WITH_RSA =
            new AlgorithmIdentifier(PKCSObjectIdentifiers.md5WithRSAEncryption);
    static final String CERT = Credentials.CERTIFICATE;
    static final String PKCS12 = Credentials.PKCS12;
    static String[] getKeyStrengthList() {
        return new String[] {"High Grade", "Medium Grade"};
    }
    static String getSignedPublicKey(Context context, int index, String challenge) {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize((index == 0) ? 2048 : 1024);
            KeyPair pair = generator.genKeyPair();
            NetscapeCertRequest request = new NetscapeCertRequest(challenge,
                    MD5_WITH_RSA, pair.getPublic());
            request.sign(pair.getPrivate());
            byte[] signed = request.toASN1Object().getDEREncoded();
            Credentials.getInstance().install(context, pair);
            return new String(Base64.encode(signed));
        } catch (Exception e) {
            Log.w(LOGTAG, e);
        }
        return null;
    }
    static void addCertificate(Context context, String type, byte[] value) {
        Credentials.getInstance().install(context, type, value);
    }
    private CertTool() {}
}
