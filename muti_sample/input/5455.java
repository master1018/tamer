final class CramMD5Client extends CramMD5Base implements SaslClient {
    private String username;
    CramMD5Client(String authID, byte[] pw) throws SaslException {
        if (authID == null || pw == null) {
            throw new SaslException(
                "CRAM-MD5: authentication ID and password must be specified");
        }
        username = authID;
        this.pw = pw;  
    }
    public boolean hasInitialResponse() {
        return false;
    }
    public byte[] evaluateChallenge(byte[] challengeData)
        throws SaslException {
        if (completed) {
            throw new IllegalStateException(
                "CRAM-MD5 authentication already completed");
        }
        if (aborted) {
            throw new IllegalStateException(
                "CRAM-MD5 authentication previously aborted due to error");
        }
        try {
            if (logger.isLoggable(Level.FINE)) {
                logger.log(Level.FINE, "CRAMCLNT01:Received challenge: {0}",
                    new String(challengeData, "UTF8"));
            }
            String digest = HMAC_MD5(pw, challengeData);
            clearPassword();
            String resp = username + " " + digest;
            logger.log(Level.FINE, "CRAMCLNT02:Sending response: {0}", resp);
            completed = true;
            return resp.getBytes("UTF8");
        } catch (java.security.NoSuchAlgorithmException e) {
            aborted = true;
            throw new SaslException("MD5 algorithm not available on platform", e);
        } catch (java.io.UnsupportedEncodingException e) {
            aborted = true;
            throw new SaslException("UTF8 not available on platform", e);
        }
    }
}
