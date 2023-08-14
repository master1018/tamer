public abstract class ContentSigner {
    public abstract byte[] generateSignedData(
        ContentSignerParameters parameters, boolean omitContent,
        boolean applyTimestamp)
            throws NoSuchAlgorithmException, CertificateException, IOException;
}
