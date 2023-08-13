public class StandardPasswordDigest implements PasswordDigest {
    public String digest(String schema, String nonce, String password) throws ImException {
        byte[] digestBytes;
        byte[] inputBytes;
        try {
            inputBytes = (nonce + password).getBytes("ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            throw new ImException(e);
        }
        try {
            if ("SHA".equals(schema))
                schema = "SHA-1";
            MessageDigest md = MessageDigest.getInstance(schema);
            digestBytes = md.digest(inputBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new ImException("Unsupported schema: " + schema);
        }
        return Base64.encodeToString(digestBytes, Base64.NO_WRAP);
    }
    public String[] getSupportedDigestSchema() {
        return new String[] {"MD5", "SHA"};
    }
}
