final class KerberosPreMasterSecret {
    private ProtocolVersion protocolVersion; 
    private byte preMaster[];           
    private byte encrypted[];
    KerberosPreMasterSecret(ProtocolVersion protocolVersion,
        SecureRandom generator, EncryptionKey sessionKey) throws IOException {
        if (sessionKey.getEType() ==
            EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD) {
            throw new IOException(
               "session keys with des3-cbc-hmac-sha1-kd encryption type " +
               "are not supported for TLS Kerberos cipher suites");
        }
        this.protocolVersion = protocolVersion;
        preMaster = generatePreMaster(generator, protocolVersion);
        try {
            EncryptedData eData = new EncryptedData(sessionKey, preMaster,
                KeyUsage.KU_UNKNOWN);
            encrypted = eData.getBytes();  
        } catch (KrbException e) {
            throw (SSLKeyException)new SSLKeyException
                ("Kerberos premaster secret error").initCause(e);
        }
    }
    KerberosPreMasterSecret(ProtocolVersion currentVersion,
        ProtocolVersion clientVersion,
        SecureRandom generator, HandshakeInStream input,
        EncryptionKey sessionKey) throws IOException {
         encrypted = input.getBytes16();
         if (HandshakeMessage.debug != null && Debug.isOn("handshake")) {
            if (encrypted != null) {
                Debug.println(System.out,
                     "encrypted premaster secret", encrypted);
            }
         }
        if (sessionKey.getEType() ==
            EncryptedData.ETYPE_DES3_CBC_HMAC_SHA1_KD) {
            throw new IOException(
               "session keys with des3-cbc-hmac-sha1-kd encryption type " +
               "are not supported for TLS Kerberos cipher suites");
        }
        try {
            EncryptedData data = new EncryptedData(sessionKey.getEType(),
                        null , encrypted);
            byte[] temp = data.decrypt(sessionKey, KeyUsage.KU_UNKNOWN);
            if (HandshakeMessage.debug != null && Debug.isOn("handshake")) {
                 if (encrypted != null) {
                     Debug.println(System.out,
                         "decrypted premaster secret", temp);
                 }
            }
            if (temp.length == 52 &&
                    data.getEType() == EncryptedData.ETYPE_DES_CBC_CRC) {
                if (paddingByteIs(temp, 52, (byte)4) ||
                        paddingByteIs(temp, 52, (byte)0)) {
                    temp = Arrays.copyOf(temp, 48);
                }
            } else if (temp.length == 56 &&
                    data.getEType() == EncryptedData.ETYPE_DES_CBC_MD5) {
                if (paddingByteIs(temp, 56, (byte)8)) {
                    temp = Arrays.copyOf(temp, 48);
                }
            }
            preMaster = temp;
            protocolVersion = ProtocolVersion.valueOf(preMaster[0],
                 preMaster[1]);
            if (HandshakeMessage.debug != null && Debug.isOn("handshake")) {
                 System.out.println("Kerberos PreMasterSecret version: "
                        + protocolVersion);
            }
        } catch (Exception e) {
            preMaster = null;
            protocolVersion = currentVersion;
        }
        boolean versionMismatch = (protocolVersion.v != clientVersion.v);
        if (versionMismatch && (clientVersion.v <= 0x0301)) {
            versionMismatch = (protocolVersion.v != currentVersion.v);
        }
         if ((preMaster == null) || (preMaster.length != 48)
                || versionMismatch) {
            if (HandshakeMessage.debug != null && Debug.isOn("handshake")) {
                System.out.println("Kerberos PreMasterSecret error, "
                                   + "generating random secret");
                if (preMaster != null) {
                    Debug.println(System.out, "Invalid secret", preMaster);
                }
            }
            preMaster = generatePreMaster(generator, clientVersion);
            protocolVersion = clientVersion;
        }
    }
    private static boolean paddingByteIs(byte[] data, int len, byte b) {
        for (int i=48; i<len; i++) {
            if (data[i] != b) return false;
        }
        return true;
    }
    KerberosPreMasterSecret(ProtocolVersion protocolVersion,
        SecureRandom generator) {
        this.protocolVersion = protocolVersion;
        preMaster = generatePreMaster(generator, protocolVersion);
    }
    private static byte[] generatePreMaster(SecureRandom rand,
        ProtocolVersion ver) {
        byte[] pm = new byte[48];
        rand.nextBytes(pm);
        pm[0] = ver.major;
        pm[1] = ver.minor;
        return pm;
    }
    byte[] getUnencrypted() {
        return preMaster;
    }
    byte[] getEncrypted() {
        return encrypted;
    }
}
