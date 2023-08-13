public class test {
    public void testSignedInfoString() throws Exception {
        String signedInfoStr = "<dsig:SignedInfo xmlns:dsig=\"http:
        Document signedInfoDoc = XmlUtils.parse(signedInfoStr);
        Element signedInfo = signedInfoDoc.getRootElement();
        byte[] bytes = XmlUtils.canonicalize(signedInfo, Canonicalizable.EXCLUSIVE_CANONICAL_XML);
        try {
            String b64EncodedDigest = CryptoUtils.digest(bytes, "SHA");
            String expected = "kP5B9dvJTnb+sSLDdMkgj+UYjJM=";
            assertEquals(expected, b64EncodedDigest);
        } catch (CryptoException e) {
            throw new CryptoException(e);
        }
    }
}
