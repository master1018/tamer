final class PlainClient implements SaslClient {
    private boolean completed = false;
    private byte[] pw;
    private String authorizationID;
    private String authenticationID;
    private static byte SEP = 0; 
    PlainClient(String authorizationID, String authenticationID, byte[] pw)
    throws SaslException {
        if (authenticationID == null || pw == null) {
            throw new SaslException(
                "PLAIN: authorization ID and password must be specified");
        }
        this.authorizationID = authorizationID;
        this.authenticationID = authenticationID;
        this.pw = pw;  
    }
    public String getMechanismName() {
        return "PLAIN";
    }
    public boolean hasInitialResponse() {
        return true;
    }
    public void dispose() throws SaslException {
        clearPassword();
    }
    public byte[] evaluateChallenge(byte[] challengeData) throws SaslException {
        if (completed) {
            throw new IllegalStateException(
                "PLAIN authentication already completed");
        }
        completed = true;
        try {
            byte[] authz = (authorizationID != null)?
                authorizationID.getBytes("UTF8") :
                null;
            byte[] auth = authenticationID.getBytes("UTF8");
            byte[] answer = new byte[pw.length + auth.length + 2 +
                (authz == null ? 0 : authz.length)];
            int pos = 0;
            if (authz != null) {
                System.arraycopy(authz, 0, answer, 0, authz.length);
                pos = authz.length;
            }
            answer[pos++] = SEP;
            System.arraycopy(auth, 0, answer, pos, auth.length);
            pos += auth.length;
            answer[pos++] = SEP;
            System.arraycopy(pw, 0, answer, pos, pw.length);
            clearPassword();
            return answer;
        } catch (java.io.UnsupportedEncodingException e) {
            throw new SaslException("Cannot get UTF-8 encoding of ids", e);
        }
    }
    public boolean isComplete() {
        return completed;
    }
    public byte[] unwrap(byte[] incoming, int offset, int len)
        throws SaslException {
        if (completed) {
            throw new SaslException(
                "PLAIN supports neither integrity nor privacy");
        } else {
            throw new IllegalStateException("PLAIN authentication not completed");
        }
    }
    public byte[] wrap(byte[] outgoing, int offset, int len) throws SaslException {
        if (completed) {
            throw new SaslException(
                "PLAIN supports neither integrity nor privacy");
        } else {
            throw new IllegalStateException("PLAIN authentication not completed");
        }
    }
    public Object getNegotiatedProperty(String propName) {
        if (completed) {
            if (propName.equals(Sasl.QOP)) {
                return "auth";
            } else {
                return null;
            }
        } else {
            throw new IllegalStateException("PLAIN authentication not completed");
        }
    }
    private void clearPassword() {
        if (pw != null) {
            for (int i = 0; i < pw.length; i++) {
                pw[i] = (byte)0;
            }
            pw = null;
        }
    }
    protected void finalize() {
        clearPassword();
    }
}
