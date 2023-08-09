final class CramMD5Server extends CramMD5Base implements SaslServer {
    private String fqdn;
    private byte[] challengeData = null;
    private String authzid;
    private CallbackHandler cbh;
    CramMD5Server(String protocol, String serverFqdn, Map props,
        CallbackHandler cbh) throws SaslException {
        if (serverFqdn == null) {
            throw new SaslException(
                "CRAM-MD5: fully qualified server name must be specified");
        }
        fqdn = serverFqdn;
        this.cbh = cbh;
    }
    public byte[] evaluateResponse(byte[] responseData)
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
            if (challengeData == null) {
                if (responseData.length != 0) {
                    aborted = true;
                    throw new SaslException(
                        "CRAM-MD5 does not expect any initial response");
                }
                Random random = new Random();
                long rand = random.nextLong();
                long timestamp = System.currentTimeMillis();
                StringBuffer buf = new StringBuffer();
                buf.append('<');
                buf.append(rand);
                buf.append('.');
                buf.append(timestamp);
                buf.append('@');
                buf.append(fqdn);
                buf.append('>');
                String challengeStr = buf.toString();
                logger.log(Level.FINE,
                    "CRAMSRV01:Generated challenge: {0}", challengeStr);
                challengeData = challengeStr.getBytes("UTF8");
                return challengeData.clone();
            } else {
                if(logger.isLoggable(Level.FINE)) {
                    logger.log(Level.FINE,
                        "CRAMSRV02:Received response: {0}",
                        new String(responseData, "UTF8"));
                }
                int ulen = 0;
                for (int i = 0; i < responseData.length; i++) {
                    if (responseData[i] == ' ') {
                        ulen = i;
                        break;
                    }
                }
                if (ulen == 0) {
                    aborted = true;
                    throw new SaslException(
                        "CRAM-MD5: Invalid response; space missing");
                }
                String username = new String(responseData, 0, ulen, "UTF8");
                logger.log(Level.FINE,
                    "CRAMSRV03:Extracted username: {0}", username);
                NameCallback ncb =
                    new NameCallback("CRAM-MD5 authentication ID: ", username);
                PasswordCallback pcb =
                    new PasswordCallback("CRAM-MD5 password: ", false);
                cbh.handle(new Callback[]{ncb,pcb});
                char pwChars[] = pcb.getPassword();
                if (pwChars == null || pwChars.length == 0) {
                    aborted = true;
                    throw new SaslException(
                        "CRAM-MD5: username not found: " + username);
                }
                pcb.clearPassword();
                String pwStr = new String(pwChars);
                for (int i = 0; i < pwChars.length; i++) {
                    pwChars[i] = 0;
                }
                pw = pwStr.getBytes("UTF8");
                String digest = HMAC_MD5(pw, challengeData);
                logger.log(Level.FINE,
                    "CRAMSRV04:Expecting digest: {0}", digest);
                clearPassword();
                byte [] expectedDigest = digest.getBytes("UTF8");
                int digestLen = responseData.length - ulen - 1;
                if (expectedDigest.length != digestLen) {
                    aborted = true;
                    throw new SaslException("Invalid response");
                }
                int j = 0;
                for (int i = ulen + 1; i < responseData.length ; i++) {
                    if (expectedDigest[j++] != responseData[i]) {
                        aborted = true;
                        throw new SaslException("Invalid response");
                    }
                }
                AuthorizeCallback acb = new AuthorizeCallback(username, username);
                cbh.handle(new Callback[]{acb});
                if (acb.isAuthorized()) {
                    authzid = acb.getAuthorizedID();
                } else {
                    aborted = true;
                    throw new SaslException(
                        "CRAM-MD5: user not authorized: " + username);
                }
                logger.log(Level.FINE,
                    "CRAMSRV05:Authorization id: {0}", authzid);
                completed = true;
                return null;
            }
        } catch (UnsupportedEncodingException e) {
            aborted = true;
            throw new SaslException("UTF8 not available on platform", e);
        } catch (NoSuchAlgorithmException e) {
            aborted = true;
            throw new SaslException("MD5 algorithm not available on platform", e);
        } catch (UnsupportedCallbackException e) {
            aborted = true;
            throw new SaslException("CRAM-MD5 authentication failed", e);
        } catch (SaslException e) {
            throw e; 
        } catch (IOException e) {
            aborted = true;
            throw new SaslException("CRAM-MD5 authentication failed", e);
        }
    }
    public String getAuthorizationID() {
        if (completed) {
            return authzid;
        } else {
            throw new IllegalStateException(
                "CRAM-MD5 authentication not completed");
        }
    }
}
