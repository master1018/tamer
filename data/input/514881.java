public class ObexSession {
    protected Authenticator mAuthenticator;
    protected byte[] mChallengeDigest;
    public boolean handleAuthChall(HeaderSet header) throws IOException {
        if (mAuthenticator == null) {
            return false;
        }
        byte[] challenge = ObexHelper.getTagValue((byte)0x00, header.mAuthChall);
        byte[] option = ObexHelper.getTagValue((byte)0x01, header.mAuthChall);
        byte[] description = ObexHelper.getTagValue((byte)0x02, header.mAuthChall);
        String realm = null;
        if (description != null) {
            byte[] realmString = new byte[description.length - 1];
            System.arraycopy(description, 1, realmString, 0, realmString.length);
            switch (description[0] & 0xFF) {
                case ObexHelper.OBEX_AUTH_REALM_CHARSET_ASCII:
                case ObexHelper.OBEX_AUTH_REALM_CHARSET_ISO_8859_1:
                    try {
                        realm = new String(realmString, "ISO8859_1");
                    } catch (Exception e) {
                        throw new IOException("Unsupported Encoding Scheme");
                    }
                    break;
                case ObexHelper.OBEX_AUTH_REALM_CHARSET_UNICODE:
                    realm = ObexHelper.convertToUnicode(realmString, false);
                    break;
                default:
                    throw new IOException("Unsupported Encoding Scheme");
            }
        }
        boolean isUserIDRequired = false;
        boolean isFullAccess = true;
        if (option != null) {
            if ((option[0] & 0x01) != 0) {
                isUserIDRequired = true;
            }
            if ((option[0] & 0x02) != 0) {
                isFullAccess = false;
            }
        }
        PasswordAuthentication result = null;
        header.mAuthChall = null;
        try {
            result = mAuthenticator
                    .onAuthenticationChallenge(realm, isUserIDRequired, isFullAccess);
        } catch (Exception e) {
            return false;
        }
        if (result == null) {
            return false;
        }
        byte[] password = result.getPassword();
        if (password == null) {
            return false;
        }
        byte[] userName = result.getUserName();
        if (userName != null) {
            header.mAuthResp = new byte[38 + userName.length];
            header.mAuthResp[36] = (byte)0x01;
            header.mAuthResp[37] = (byte)userName.length;
            System.arraycopy(userName, 0, header.mAuthResp, 38, userName.length);
        } else {
            header.mAuthResp = new byte[36];
        }
        byte[] digest = new byte[challenge.length + password.length + 1];
        System.arraycopy(challenge, 0, digest, 0, challenge.length);
        digest[challenge.length] = (byte)0x3A;
        System.arraycopy(password, 0, digest, challenge.length + 1, password.length);
        header.mAuthResp[0] = (byte)0x00;
        header.mAuthResp[1] = (byte)0x10;
        System.arraycopy(ObexHelper.computeMd5Hash(digest), 0, header.mAuthResp, 2, 16);
        header.mAuthResp[18] = (byte)0x02;
        header.mAuthResp[19] = (byte)0x10;
        System.arraycopy(challenge, 0, header.mAuthResp, 20, 16);
        return true;
    }
    public boolean handleAuthResp(byte[] authResp) {
        if (mAuthenticator == null) {
            return false;
        }
        byte[] correctPassword = mAuthenticator.onAuthenticationResponse(ObexHelper.getTagValue(
                (byte)0x01, authResp));
        if (correctPassword == null) {
            return false;
        }
        byte[] temp = new byte[correctPassword.length + 16];
        System.arraycopy(mChallengeDigest, 0, temp, 0, 16);
        System.arraycopy(correctPassword, 0, temp, 16, correctPassword.length);
        byte[] correctResponse = ObexHelper.computeMd5Hash(temp);
        byte[] actualResponse = ObexHelper.getTagValue((byte)0x00, authResp);
        for (int i = 0; i < 16; i++) {
            if (correctResponse[i] != actualResponse[i]) {
                return false;
            }
        }
        return true;
    }
}
