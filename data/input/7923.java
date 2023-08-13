final class ExternalClient implements SaslClient {
    private byte[] username;
    private boolean completed = false;
    ExternalClient(String authorizationID) throws SaslException {
        if (authorizationID != null) {
            try {
                username = authorizationID.getBytes("UTF8");
            } catch (java.io.UnsupportedEncodingException e) {
                throw new SaslException("Cannot convert " + authorizationID +
                    " into UTF-8", e);
            }
        } else {
            username = new byte[0];
        }
    }
    public String getMechanismName() {
        return "EXTERNAL";
    }
    public boolean hasInitialResponse() {
        return true;
    }
    public void dispose() throws SaslException {
    }
    public byte[] evaluateChallenge(byte[] challengeData)
        throws SaslException {
        if (completed) {
            throw new IllegalStateException(
                "EXTERNAL authentication already completed");
        }
        completed = true;
        return username;
    }
    public boolean isComplete() {
        return completed;
    }
    public byte[] unwrap(byte[] incoming, int offset, int len)
        throws SaslException {
        if (completed) {
            throw new SaslException("EXTERNAL has no supported QOP");
        } else {
            throw new IllegalStateException(
                "EXTERNAL authentication Not completed");
        }
    }
    public byte[] wrap(byte[] outgoing, int offset, int len)
        throws SaslException {
        if (completed) {
            throw new SaslException("EXTERNAL has no supported QOP");
        } else {
            throw new IllegalStateException(
                "EXTERNAL authentication not completed");
        }
    }
    public Object getNegotiatedProperty(String propName) {
        if (completed) {
            return null;
        } else {
            throw new IllegalStateException(
                "EXTERNAL authentication not completed");
        }
    }
}
