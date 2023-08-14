abstract class DigestMD5Base extends AbstractSaslImpl {
    private static final String DI_CLASS_NAME = DigestIntegrity.class.getName();
    private static final String DP_CLASS_NAME = DigestPrivacy.class.getName();
    protected static final int MAX_CHALLENGE_LENGTH = 2048;
    protected static final int MAX_RESPONSE_LENGTH = 4096;
    protected static final int DEFAULT_MAXBUF = 65536;
    protected static final int DES3 = 0;
    protected static final int RC4 = 1;
    protected static final int DES = 2;
    protected static final int RC4_56 = 3;
    protected static final int RC4_40 = 4;
    protected static final String[] CIPHER_TOKENS = { "3des",
                                                      "rc4",
                                                      "des",
                                                      "rc4-56",
                                                      "rc4-40" };
    private static final String[] JCE_CIPHER_NAME = {
        "DESede/CBC/NoPadding",
        "RC4",
        "DES/CBC/NoPadding",
    };
    protected static final byte DES_3_STRENGTH = HIGH_STRENGTH;
    protected static final byte RC4_STRENGTH = HIGH_STRENGTH;
    protected static final byte DES_STRENGTH = MEDIUM_STRENGTH;
    protected static final byte RC4_56_STRENGTH = MEDIUM_STRENGTH;
    protected static final byte RC4_40_STRENGTH = LOW_STRENGTH;
    protected static final byte UNSET = (byte)0;
    protected static final byte[] CIPHER_MASKS = { DES_3_STRENGTH,
                                                   RC4_STRENGTH,
                                                   DES_STRENGTH,
                                                   RC4_56_STRENGTH,
                                                   RC4_40_STRENGTH };
    private static final String SECURITY_LAYER_MARKER =
        ":00000000000000000000000000000000";
    protected static final byte[] EMPTY_BYTE_ARRAY = new byte[0];
    protected int step;
    protected CallbackHandler cbh;
    protected SecurityCtx secCtx;
    protected byte[] H_A1; 
    protected byte[] nonce;         
    protected String negotiatedStrength;
    protected String negotiatedCipher;
    protected String negotiatedQop;
    protected String negotiatedRealm;
    protected boolean useUTF8 = false;
    protected String encoding = "8859_1";  
    protected String digestUri;
    protected String authzid;       
    protected DigestMD5Base(Map props, String className, int firstStep,
        String digestUri, CallbackHandler cbh) throws SaslException {
        super(props, className); 
        step = firstStep;
        this.digestUri = digestUri;
        this.cbh = cbh;
    }
    public String getMechanismName() {
        return "DIGEST-MD5";
    }
    public byte[] unwrap(byte[] incoming, int start, int len) throws SaslException {
        if (!completed) {
            throw new IllegalStateException(
                "DIGEST-MD5 authentication not completed");
        }
        if (secCtx == null) {
            throw new IllegalStateException(
                "Neither integrity nor privacy was negotiated");
        }
        return (secCtx.unwrap(incoming, start, len));
    }
    public byte[] wrap(byte[] outgoing, int start, int len) throws SaslException {
        if (!completed) {
            throw new IllegalStateException(
                "DIGEST-MD5 authentication not completed");
        }
        if (secCtx == null) {
            throw new IllegalStateException(
                "Neither integrity nor privacy was negotiated");
        }
        return (secCtx.wrap(outgoing, start, len));
    }
    public void dispose() throws SaslException {
        if (secCtx != null) {
            secCtx = null;
        }
    }
    public Object getNegotiatedProperty(String propName) {
        if (completed) {
            if (propName.equals(Sasl.STRENGTH)) {
                return negotiatedStrength;
            } else {
                return super.getNegotiatedProperty(propName);
            }
        } else {
            throw new IllegalStateException(
                "DIGEST-MD5 authentication not completed");
        }
    }
    private final static char pem_array[] = {
                'A','B','C','D','E','F','G','H', 
                'I','J','K','L','M','N','O','P', 
                'Q','R','S','T','U','V','W','X', 
                'Y','Z','a','b','c','d','e','f', 
                'g','h','i','j','k','l','m','n', 
                'o','p','q','r','s','t','u','v', 
                'w','x','y','z','0','1','2','3', 
                '4','5','6','7','8','9','+','/'  
    };
    private static final int RAW_NONCE_SIZE = 30;
    private static final int ENCODED_NONCE_SIZE = RAW_NONCE_SIZE*4/3;
    protected static final byte[] generateNonce() {
        Random random = new Random();
        byte[] randomData = new byte[RAW_NONCE_SIZE];
        random.nextBytes(randomData);
        byte[] nonce = new byte[ENCODED_NONCE_SIZE];
        byte a, b, c;
        int j = 0;
        for (int i = 0; i < randomData.length; i += 3) {
            a = randomData[i];
            b = randomData[i+1];
            c = randomData[i+2];
            nonce[j++] = (byte)(pem_array[(a >>> 2) & 0x3F]);
            nonce[j++] = (byte)(pem_array[((a << 4) & 0x30) + ((b >>> 4) & 0xf)]);
            nonce[j++] = (byte)(pem_array[((b << 2) & 0x3c) + ((c >>> 6) & 0x3)]);
            nonce[j++] = (byte)(pem_array[c & 0x3F]);
        }
        return nonce;
    }
    protected static void writeQuotedStringValue(ByteArrayOutputStream out,
        byte[] buf) {
        int len = buf.length;
        byte ch;
        for (int i = 0; i < len; i++) {
            ch = buf[i];
            if (needEscape((char)ch)) {
                out.write('\\');
            }
            out.write(ch);
        }
    }
    private static boolean needEscape(String str) {
        int len = str.length();
        for (int i = 0; i < len; i++) {
            if (needEscape(str.charAt(i))) {
                return true;
            }
        }
        return false;
    }
    private static boolean needEscape(char ch) {
        return ch == '"' ||  
            ch == '\\' ||    
            ch == 127 ||     
            (ch >= 0 && ch <= 31 && ch != 13 && ch != 9 && ch != 10);
    }
    protected static String quotedStringValue(String str) {
        if (needEscape(str)) {
            int len = str.length();
            char[] buf = new char[len+len];
            int j = 0;
            char ch;
            for (int i = 0; i < len; i++) {
                ch = str.charAt(i);
                if (needEscape(ch)) {
                    buf[j++] =  '\\';
                }
                buf[j++] = ch;
            }
            return new String(buf, 0, j);
        } else {
            return str;
        }
    }
    protected byte[] binaryToHex(byte[] digest) throws
    UnsupportedEncodingException {
        StringBuffer digestString = new StringBuffer();
        for (int i = 0; i < digest.length; i ++) {
            if ((digest[i] & 0x000000ff) < 0x10) {
                digestString.append("0"+
                    Integer.toHexString(digest[i] & 0x000000ff));
            } else {
                digestString.append(
                    Integer.toHexString(digest[i] & 0x000000ff));
            }
        }
        return digestString.toString().getBytes(encoding);
    }
    protected byte[] stringToByte_8859_1(String str) throws SaslException {
        char[] buffer = str.toCharArray();
        try {
            if (useUTF8) {
                for( int i = 0; i< buffer.length; i++ ) {
                    if( buffer[i] > '\u00FF' ) {
                        return str.getBytes("UTF8");
                    }
                }
            }
            return str.getBytes("8859_1");
        } catch (UnsupportedEncodingException e) {
            throw new SaslException(
                "cannot encode string in UTF8 or 8859-1 (Latin-1)", e);
        }
    }
    protected static byte[] getPlatformCiphers() {
        byte[] ciphers = new byte[CIPHER_TOKENS.length];
        for (int i = 0; i < JCE_CIPHER_NAME.length; i++) {
            try {
                Cipher.getInstance(JCE_CIPHER_NAME[i]);
                logger.log(Level.FINE, "DIGEST01:Platform supports {0}", JCE_CIPHER_NAME[i]);
                ciphers[i] |= CIPHER_MASKS[i];
            } catch (NoSuchAlgorithmException e) {
            } catch (NoSuchPaddingException e) {
            }
        }
        if (ciphers[RC4] != UNSET) {
            ciphers[RC4_56] |= CIPHER_MASKS[RC4_56];
            ciphers[RC4_40] |= CIPHER_MASKS[RC4_40];
        }
        return ciphers;
    }
    protected byte[] generateResponseValue(
        String authMethod,
        String digestUriValue,
        String qopValue,
        String usernameValue,
        String realmValue,
        char[] passwdValue,
        byte[] nonceValue,
        byte[] cNonceValue,
        int nonceCount,
        byte[] authzidValue
        ) throws NoSuchAlgorithmException,
            UnsupportedEncodingException,
            IOException {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        byte[] hexA1, hexA2;
        ByteArrayOutputStream A2, beginA1, A1, KD;
        A2 = new ByteArrayOutputStream();
        A2.write((authMethod + ":" + digestUriValue).getBytes(encoding));
        if (qopValue.equals("auth-conf") ||
            qopValue.equals("auth-int")) {
            logger.log(Level.FINE, "DIGEST04:QOP: {0}", qopValue);
            A2.write(SECURITY_LAYER_MARKER.getBytes(encoding));
        }
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "DIGEST05:A2: {0}", A2.toString());
        }
        md5.update(A2.toByteArray());
        byte[] digest = md5.digest();
        hexA2 = binaryToHex(digest);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "DIGEST06:HEX(H(A2)): {0}", new String(hexA2));
        }
        beginA1 = new ByteArrayOutputStream();
        beginA1.write(stringToByte_8859_1(usernameValue));
        beginA1.write(':');
        beginA1.write(stringToByte_8859_1(realmValue));
        beginA1.write(':');
        beginA1.write(stringToByte_8859_1(new String(passwdValue)));
        md5.update(beginA1.toByteArray());
        digest = md5.digest();
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "DIGEST07:H({0}) = {1}",
                new Object[]{beginA1.toString(), new String(binaryToHex(digest))});
        }
        A1 = new ByteArrayOutputStream();
        A1.write(digest);
        A1.write(':');
        A1.write(nonceValue);
        A1.write(':');
        A1.write(cNonceValue);
        if (authzidValue != null) {
            A1.write(':');
            A1.write(authzidValue);
        }
        md5.update(A1.toByteArray());
        digest = md5.digest();
        H_A1 = digest; 
        hexA1 = binaryToHex(digest);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "DIGEST08:H(A1) = {0}", new String(hexA1));
        }
        KD = new ByteArrayOutputStream();
        KD.write(hexA1);
        KD.write(':');
        KD.write(nonceValue);
        KD.write(':');
        KD.write(nonceCountToHex(nonceCount).getBytes(encoding));
        KD.write(':');
        KD.write(cNonceValue);
        KD.write(':');
        KD.write(qopValue.getBytes(encoding));
        KD.write(':');
        KD.write(hexA2);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "DIGEST09:KD: {0}", KD.toString());
        }
        md5.update(KD.toByteArray());
        digest = md5.digest();
        byte[] answer = binaryToHex(digest);
        if (logger.isLoggable(Level.FINE)) {
            logger.log(Level.FINE, "DIGEST10:response-value: {0}",
                new String(answer));
        }
        return (answer);
    }
    protected static String nonceCountToHex(int count) {
        String str = Integer.toHexString(count);
        StringBuffer pad = new StringBuffer();
        if (str.length() < 8) {
            for (int i = 0; i < 8-str.length(); i ++) {
                pad.append("0");
            }
        }
        return pad.toString() + str;
    }
    protected static byte[][] parseDirectives(byte[] buf,
        String[]keyTable, List<byte[]> realmChoices, int realmIndex) throws SaslException {
        byte[][] valueTable = new byte[keyTable.length][];
        ByteArrayOutputStream key = new ByteArrayOutputStream(10);
        ByteArrayOutputStream value = new ByteArrayOutputStream(10);
        boolean gettingKey = true;
        boolean gettingQuotedValue = false;
        boolean expectSeparator = false;
        byte bch;
        int i = skipLws(buf, 0);
        while (i < buf.length) {
            bch = buf[i];
            if (gettingKey) {
                if (bch == ',') {
                    if (key.size() != 0) {
                        throw new SaslException("Directive key contains a ',':" +
                            key);
                    }
                    i = skipLws(buf, i+1);
                } else if (bch == '=') {
                    if (key.size() == 0) {
                        throw new SaslException("Empty directive key");
                    }
                    gettingKey = false;      
                    i = skipLws(buf, i+1);   
                    if (i < buf.length) {
                        if (buf[i] == '"') {
                            gettingQuotedValue = true;
                            ++i; 
                        }
                    } else {
                        throw new SaslException(
                            "Valueless directive found: " + key.toString());
                    }
                } else if (isLws(bch)) {
                    i = skipLws(buf, i+1);
                    if (i < buf.length) {
                        if (buf[i] != '=') {
                            throw new SaslException("'=' expected after key: " +
                                key.toString());
                        }
                    } else {
                        throw new SaslException(
                            "'=' expected after key: " + key.toString());
                    }
                } else {
                    key.write(bch);    
                    ++i;               
                }
            } else if (gettingQuotedValue) {
                if (bch == '\\') {
                    ++i;       
                    if (i < buf.length) {
                        value.write(buf[i]);
                        ++i;   
                    } else {
                        throw new SaslException(
                            "Unmatched quote found for directive: "
                            + key.toString() + " with value: " + value.toString());
                    }
                } else if (bch == '"') {
                    ++i;  
                    gettingQuotedValue = false;
                    expectSeparator = true;
                } else {
                    value.write(bch);
                    ++i;  
                }
            } else if (isLws(bch) || bch == ',') {
                extractDirective(key.toString(), value.toByteArray(),
                    keyTable, valueTable, realmChoices, realmIndex);
                key.reset();
                value.reset();
                gettingKey = true;
                gettingQuotedValue = expectSeparator = false;
                i = skipLws(buf, i+1);   
            } else if (expectSeparator) {
                throw new SaslException(
                    "Expecting comma or linear whitespace after quoted string: \""
                        + value.toString() + "\"");
            } else {
                value.write(bch);   
                ++i;                
            }
        }
        if (gettingQuotedValue) {
            throw new SaslException(
                "Unmatched quote found for directive: " + key.toString() +
                " with value: " + value.toString());
        }
        if (key.size() > 0) {
            extractDirective(key.toString(), value.toByteArray(),
                keyTable, valueTable, realmChoices, realmIndex);
        }
        return valueTable;
    }
    private static boolean isLws(byte b) {
        switch (b) {
        case 13:   
        case 10:   
        case 32:   
        case 9:    
            return true;
        }
        return false;
    }
    private static int skipLws(byte[] buf, int start) {
        int i;
        for (i = start; i < buf.length; i++) {
            if (!isLws(buf[i])) {
                return i;
            }
        }
        return i;
    }
    private static void  extractDirective(String key, byte[] value,
        String[] keyTable, byte[][] valueTable,
        List<byte[]> realmChoices, int realmIndex) throws SaslException {
        for (int i = 0; i < keyTable.length; i++) {
            if (key.equalsIgnoreCase(keyTable[i])) {
                if (valueTable[i] == null) {
                    valueTable[i] = value;
                    if (logger.isLoggable(Level.FINE)) {
                        logger.log(Level.FINE, "DIGEST11:Directive {0} = {1}",
                            new Object[]{
                                keyTable[i],
                                new String(valueTable[i])});
                    }
                } else if (realmChoices != null && i == realmIndex) {
                    if (realmChoices.size() == 0) {
                        realmChoices.add(valueTable[i]); 
                    }
                    realmChoices.add(value);  
                } else {
                    throw new SaslException(
                        "DIGEST-MD5: peer sent more than one " +
                        key + " directive: " + new String(value));
                }
                break; 
            }
        }
     }
    class DigestIntegrity implements SecurityCtx {
        static final private String CLIENT_INT_MAGIC = "Digest session key to " +
            "client-to-server signing key magic constant";
        static final private String SVR_INT_MAGIC = "Digest session key to " +
            "server-to-client signing key magic constant";
        protected byte[] myKi;     
        protected byte[] peerKi;   
        protected int mySeqNum = 0;
        protected int peerSeqNum = 0;
        protected final byte[] messageType = new byte[2];
        protected final byte[] sequenceNum = new byte[4];
        DigestIntegrity(boolean clientMode) throws SaslException {
            try {
                generateIntegrityKeyPair(clientMode);
            } catch (UnsupportedEncodingException e) {
                throw new SaslException(
                    "DIGEST-MD5: Error encoding strings into UTF-8", e);
            } catch (IOException e) {
                throw new SaslException("DIGEST-MD5: Error accessing buffers " +
                    "required to create integrity key pairs", e);
            } catch (NoSuchAlgorithmException e) {
                throw new SaslException("DIGEST-MD5: Unsupported digest " +
                    "algorithm used to create integrity key pairs", e);
            }
            intToNetworkByteOrder(1, messageType, 0, 2);
        }
        private void generateIntegrityKeyPair(boolean clientMode)
            throws UnsupportedEncodingException, IOException,
                NoSuchAlgorithmException {
            byte[] cimagic = CLIENT_INT_MAGIC.getBytes(encoding);
            byte[] simagic = SVR_INT_MAGIC.getBytes(encoding);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] keyBuffer = new byte[H_A1.length + cimagic.length];
            System.arraycopy(H_A1, 0, keyBuffer, 0, H_A1.length);
            System.arraycopy(cimagic, 0, keyBuffer, H_A1.length, cimagic.length);
            md5.update(keyBuffer);
            byte[] Kic = md5.digest();
            System.arraycopy(simagic, 0, keyBuffer, H_A1.length, simagic.length);
            md5.update(keyBuffer);
            byte[] Kis = md5.digest();
            if (logger.isLoggable(Level.FINER)) {
                traceOutput(DI_CLASS_NAME, "generateIntegrityKeyPair",
                    "DIGEST12:Kic: ", Kic);
                traceOutput(DI_CLASS_NAME, "generateIntegrityKeyPair",
                    "DIGEST13:Kis: ", Kis);
            }
            if (clientMode) {
                myKi = Kic;
                peerKi = Kis;
            } else {
                myKi = Kis;
                peerKi = Kic;
            }
        }
        public byte[] wrap(byte[] outgoing, int start, int len)
            throws SaslException {
            if (len == 0) {
                return EMPTY_BYTE_ARRAY;
            }
            byte[] wrapped = new byte[len+10+2+4];
            System.arraycopy(outgoing, start, wrapped, 0, len);
            incrementSeqNum();
            byte[] mac = getHMAC(myKi, sequenceNum, outgoing, start, len);
            if (logger.isLoggable(Level.FINEST)) {
                traceOutput(DI_CLASS_NAME, "wrap", "DIGEST14:outgoing: ",
                    outgoing, start, len);
                traceOutput(DI_CLASS_NAME, "wrap", "DIGEST15:seqNum: ",
                    sequenceNum);
                traceOutput(DI_CLASS_NAME, "wrap", "DIGEST16:MAC: ", mac);
            }
            System.arraycopy(mac, 0, wrapped, len, 10);
            System.arraycopy(messageType, 0, wrapped, len+10, 2);
            System.arraycopy(sequenceNum, 0, wrapped, len+12, 4);
            if (logger.isLoggable(Level.FINEST)) {
                traceOutput(DI_CLASS_NAME, "wrap", "DIGEST17:wrapped: ", wrapped);
            }
            return wrapped;
        }
        public byte[] unwrap(byte[] incoming, int start, int len)
            throws SaslException {
            if (len == 0) {
                return EMPTY_BYTE_ARRAY;
            }
            byte[] mac = new byte[10];
            byte[] msg = new byte[len - 16];
            byte[] msgType = new byte[2];
            byte[] seqNum = new byte[4];
            System.arraycopy(incoming, start, msg, 0, msg.length);
            System.arraycopy(incoming, start+msg.length, mac, 0, 10);
            System.arraycopy(incoming, start+msg.length+10, msgType, 0, 2);
            System.arraycopy(incoming, start+msg.length+12, seqNum, 0, 4);
            byte[] expectedMac = getHMAC(peerKi, seqNum, msg, 0, msg.length);
            if (logger.isLoggable(Level.FINEST)) {
                traceOutput(DI_CLASS_NAME, "unwrap", "DIGEST18:incoming: ",
                    msg);
                traceOutput(DI_CLASS_NAME, "unwrap", "DIGEST19:MAC: ",
                    mac);
                traceOutput(DI_CLASS_NAME, "unwrap", "DIGEST20:messageType: ",
                    msgType);
                traceOutput(DI_CLASS_NAME, "unwrap", "DIGEST21:sequenceNum: ",
                    seqNum);
                traceOutput(DI_CLASS_NAME, "unwrap", "DIGEST22:expectedMAC: ",
                    expectedMac);
            }
            if (!Arrays.equals(mac, expectedMac)) {
                logger.log(Level.INFO, "DIGEST23:Unmatched MACs");
                return EMPTY_BYTE_ARRAY;
            }
            if (peerSeqNum != networkByteOrderToInt(seqNum, 0, 4)) {
                throw new SaslException("DIGEST-MD5: Out of order " +
                    "sequencing of messages from server. Got: " +
                    networkByteOrderToInt(seqNum, 0, 4) +
                    " Expected: " +     peerSeqNum);
            }
            if (!Arrays.equals(messageType, msgType)) {
                throw new SaslException("DIGEST-MD5: invalid message type: " +
                    networkByteOrderToInt(msgType, 0, 2));
            }
            peerSeqNum++;
            return msg;
        }
        protected byte[] getHMAC(byte[] Ki, byte[] seqnum, byte[] msg,
            int start, int len) throws SaslException {
            byte[] seqAndMsg = new byte[4+len];
            System.arraycopy(seqnum, 0, seqAndMsg, 0, 4);
            System.arraycopy(msg, start, seqAndMsg, 4, len);
            try {
                SecretKey keyKi = new SecretKeySpec(Ki, "HmacMD5");
                Mac m = Mac.getInstance("HmacMD5");
                m.init(keyKi);
                m.update(seqAndMsg);
                byte[] hMAC_MD5 = m.doFinal();
                byte macBuffer[] = new byte[10];
                System.arraycopy(hMAC_MD5, 0, macBuffer, 0, 10);
                return macBuffer;
            } catch (InvalidKeyException e) {
                throw new SaslException("DIGEST-MD5: Invalid bytes used for " +
                    "key of HMAC-MD5 hash.", e);
            } catch (NoSuchAlgorithmException e) {
                throw new SaslException("DIGEST-MD5: Error creating " +
                    "instance of MD5 digest algorithm", e);
            }
        }
        protected void incrementSeqNum() {
            intToNetworkByteOrder(mySeqNum++, sequenceNum, 0, 4);
        }
    }
    final class DigestPrivacy extends DigestIntegrity implements SecurityCtx {
        static final private String CLIENT_CONF_MAGIC =
            "Digest H(A1) to client-to-server sealing key magic constant";
        static final private String SVR_CONF_MAGIC =
            "Digest H(A1) to server-to-client sealing key magic constant";
        private Cipher encCipher;
        private Cipher decCipher;
        DigestPrivacy(boolean clientMode) throws SaslException {
            super(clientMode); 
            try {
                generatePrivacyKeyPair(clientMode);
            } catch (SaslException e) {
                throw e;
            } catch (UnsupportedEncodingException e) {
                throw new SaslException(
                    "DIGEST-MD5: Error encoding string value into UTF-8", e);
            } catch (IOException e) {
                throw new SaslException("DIGEST-MD5: Error accessing " +
                    "buffers required to generate cipher keys", e);
            } catch (NoSuchAlgorithmException e) {
                throw new SaslException("DIGEST-MD5: Error creating " +
                    "instance of required cipher or digest", e);
            }
        }
        private void generatePrivacyKeyPair(boolean clientMode)
            throws IOException, UnsupportedEncodingException,
            NoSuchAlgorithmException, SaslException {
            byte[] ccmagic = CLIENT_CONF_MAGIC.getBytes(encoding);
            byte[] scmagic = SVR_CONF_MAGIC.getBytes(encoding);
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            int n;
            if (negotiatedCipher.equals(CIPHER_TOKENS[RC4_40])) {
                n = 5;          
            } else if (negotiatedCipher.equals(CIPHER_TOKENS[RC4_56])) {
                n = 7;          
            } else { 
                n = 16;         
            }
            byte[] keyBuffer =  new byte[n + ccmagic.length];
            System.arraycopy(H_A1, 0, keyBuffer, 0, n);   
            System.arraycopy(ccmagic, 0, keyBuffer, n, ccmagic.length);
            md5.update(keyBuffer);
            byte[] Kcc = md5.digest();
            System.arraycopy(scmagic, 0, keyBuffer, n, scmagic.length);
            md5.update(keyBuffer);
            byte[] Kcs = md5.digest();
            if (logger.isLoggable(Level.FINER)) {
                traceOutput(DP_CLASS_NAME, "generatePrivacyKeyPair",
                    "DIGEST24:Kcc: ", Kcc);
                traceOutput(DP_CLASS_NAME, "generatePrivacyKeyPair",
                    "DIGEST25:Kcs: ", Kcs);
            }
            byte[] myKc;
            byte[] peerKc;
            if (clientMode) {
                myKc = Kcc;
                peerKc = Kcs;
            } else {
                myKc = Kcs;
                peerKc = Kcc;
            }
            try {
                SecretKey encKey;
                SecretKey decKey;
                if (negotiatedCipher.indexOf(CIPHER_TOKENS[RC4]) > -1) {
                    encCipher = Cipher.getInstance("RC4");
                    decCipher = Cipher.getInstance("RC4");
                    encKey = new SecretKeySpec(myKc, "RC4");
                    decKey = new SecretKeySpec(peerKc, "RC4");
                    encCipher.init(Cipher.ENCRYPT_MODE, encKey);
                    decCipher.init(Cipher.DECRYPT_MODE, decKey);
                } else if ((negotiatedCipher.equals(CIPHER_TOKENS[DES])) ||
                    (negotiatedCipher.equals(CIPHER_TOKENS[DES3]))) {
                    String cipherFullname, cipherShortname;
                    if (negotiatedCipher.equals(CIPHER_TOKENS[DES])) {
                        cipherFullname = "DES/CBC/NoPadding";
                        cipherShortname = "des";
                    } else {
                        cipherFullname = "DESede/CBC/NoPadding";
                        cipherShortname = "desede";
                    }
                    encCipher = Cipher.getInstance(cipherFullname);
                    decCipher = Cipher.getInstance(cipherFullname);
                    encKey = makeDesKeys(myKc, cipherShortname);
                    decKey = makeDesKeys(peerKc, cipherShortname);
                    IvParameterSpec encIv = new IvParameterSpec(myKc, 8, 8);
                    IvParameterSpec decIv = new IvParameterSpec(peerKc, 8, 8);
                    encCipher.init(Cipher.ENCRYPT_MODE, encKey, encIv);
                    decCipher.init(Cipher.DECRYPT_MODE, decKey, decIv);
                    if (logger.isLoggable(Level.FINER)) {
                        traceOutput(DP_CLASS_NAME, "generatePrivacyKeyPair",
                            "DIGEST26:" + negotiatedCipher + " IVcc: ",
                            encIv.getIV());
                        traceOutput(DP_CLASS_NAME, "generatePrivacyKeyPair",
                            "DIGEST27:" + negotiatedCipher + " IVcs: ",
                            decIv.getIV());
                        traceOutput(DP_CLASS_NAME, "generatePrivacyKeyPair",
                            "DIGEST28:" + negotiatedCipher + " encryption key: ",
                            encKey.getEncoded());
                        traceOutput(DP_CLASS_NAME, "generatePrivacyKeyPair",
                            "DIGEST29:" + negotiatedCipher + " decryption key: ",
                            decKey.getEncoded());
                    }
                }
            } catch (InvalidKeySpecException e) {
                throw new SaslException("DIGEST-MD5: Unsupported key " +
                    "specification used.", e);
            } catch (InvalidAlgorithmParameterException e) {
                throw new SaslException("DIGEST-MD5: Invalid cipher " +
                    "algorithem parameter used to create cipher instance", e);
            } catch (NoSuchPaddingException e) {
                throw new SaslException("DIGEST-MD5: Unsupported " +
                    "padding used for chosen cipher", e);
            } catch (InvalidKeyException e) {
                throw new SaslException("DIGEST-MD5: Invalid data " +
                    "used to initialize keys", e);
            }
        }
        public byte[] wrap(byte[] outgoing, int start, int len)
            throws SaslException {
            if (len == 0) {
                return EMPTY_BYTE_ARRAY;
            }
            incrementSeqNum();
            byte[] mac = getHMAC(myKi, sequenceNum, outgoing, start, len);
            if (logger.isLoggable(Level.FINEST)) {
                traceOutput(DP_CLASS_NAME, "wrap", "DIGEST30:Outgoing: ",
                    outgoing, start, len);
                traceOutput(DP_CLASS_NAME, "wrap", "seqNum: ",
                    sequenceNum);
                traceOutput(DP_CLASS_NAME, "wrap", "MAC: ", mac);
            }
            int bs = encCipher.getBlockSize();
            byte[] padding;
            if (bs > 1 ) {
                int pad = bs - ((len + 10) % bs); 
                padding = new byte[pad];
                for (int i=0; i < pad; i++) {
                    padding[i] = (byte)pad;
                }
            } else {
                padding = EMPTY_BYTE_ARRAY;
            }
            byte[] toBeEncrypted = new byte[len+padding.length+10];
            System.arraycopy(outgoing, start, toBeEncrypted, 0, len);
            System.arraycopy(padding, 0, toBeEncrypted, len, padding.length);
            System.arraycopy(mac, 0, toBeEncrypted, len+padding.length, 10);
            if (logger.isLoggable(Level.FINEST)) {
                traceOutput(DP_CLASS_NAME, "wrap",
                    "DIGEST31:{msg, pad, KicMAC}: ", toBeEncrypted);
            }
            byte[] cipherBlock;
            try {
                cipherBlock = encCipher.update(toBeEncrypted);
                if (cipherBlock == null) {
                    throw new IllegalBlockSizeException(""+toBeEncrypted.length);
                }
            } catch (IllegalBlockSizeException e) {
                throw new SaslException(
                    "DIGEST-MD5: Invalid block size for cipher", e);
            }
            byte[] wrapped = new byte[cipherBlock.length+2+4];
            System.arraycopy(cipherBlock, 0, wrapped, 0, cipherBlock.length);
            System.arraycopy(messageType, 0, wrapped, cipherBlock.length, 2);
            System.arraycopy(sequenceNum, 0, wrapped, cipherBlock.length+2, 4);
            if (logger.isLoggable(Level.FINEST)) {
                traceOutput(DP_CLASS_NAME, "wrap", "DIGEST32:Wrapped: ", wrapped);
            }
            return wrapped;
        }
        public byte[] unwrap(byte[] incoming, int start, int len)
            throws SaslException {
            if (len == 0) {
                return EMPTY_BYTE_ARRAY;
            }
            byte[] encryptedMsg = new byte[len - 6];
            byte[] msgType = new byte[2];
            byte[] seqNum = new byte[4];
            System.arraycopy(incoming, start,
                encryptedMsg, 0, encryptedMsg.length);
            System.arraycopy(incoming, start+encryptedMsg.length,
                msgType, 0, 2);
            System.arraycopy(incoming, start+encryptedMsg.length+2,
                seqNum, 0, 4);
            if (logger.isLoggable(Level.FINEST)) {
                logger.log(Level.FINEST,
                    "DIGEST33:Expecting sequence num: {0}",
                    new Integer(peerSeqNum));
                traceOutput(DP_CLASS_NAME, "unwrap", "DIGEST34:incoming: ",
                    encryptedMsg);
            }
            byte[] decryptedMsg;
            try {
                decryptedMsg = decCipher.update(encryptedMsg);
                if (decryptedMsg == null) {
                    throw new IllegalBlockSizeException(""+encryptedMsg.length);
                }
            } catch (IllegalBlockSizeException e) {
                throw new SaslException("DIGEST-MD5: Illegal block " +
                    "sizes used with chosen cipher", e);
            }
            byte[] msgWithPadding = new byte[decryptedMsg.length - 10];
            byte[] mac = new byte[10];
            System.arraycopy(decryptedMsg, 0,
                msgWithPadding, 0, msgWithPadding.length);
            System.arraycopy(decryptedMsg, msgWithPadding.length,
                mac, 0, 10);
            if (logger.isLoggable(Level.FINEST)) {
                traceOutput(DP_CLASS_NAME, "unwrap",
                    "DIGEST35:Unwrapped (w/padding): ", msgWithPadding);
                traceOutput(DP_CLASS_NAME, "unwrap", "DIGEST36:MAC: ", mac);
                traceOutput(DP_CLASS_NAME, "unwrap", "DIGEST37:messageType: ",
                    msgType);
                traceOutput(DP_CLASS_NAME, "unwrap", "DIGEST38:sequenceNum: ",
                    seqNum);
            }
            int msgLength = msgWithPadding.length;
            int blockSize = decCipher.getBlockSize();
            if (blockSize > 1) {
                msgLength -= (int)msgWithPadding[msgWithPadding.length - 1];
                if (msgLength < 0) {
                    if (logger.isLoggable(Level.INFO)) {
                        logger.log(Level.INFO,
                            "DIGEST39:Incorrect padding: {0}",
                            new Byte(msgWithPadding[msgWithPadding.length - 1]));
                    }
                    return EMPTY_BYTE_ARRAY;
                }
            }
            byte[] expectedMac = getHMAC(peerKi, seqNum, msgWithPadding,
                0, msgLength);
            if (logger.isLoggable(Level.FINEST)) {
                traceOutput(DP_CLASS_NAME, "unwrap", "DIGEST40:KisMAC: ",
                    expectedMac);
            }
            if (!Arrays.equals(mac, expectedMac)) {
                logger.log(Level.INFO, "DIGEST41:Unmatched MACs");
                return EMPTY_BYTE_ARRAY;
            }
            if (peerSeqNum != networkByteOrderToInt(seqNum, 0, 4)) {
                throw new SaslException("DIGEST-MD5: Out of order " +
                    "sequencing of messages from server. Got: " +
                    networkByteOrderToInt(seqNum, 0, 4) + " Expected: " +
                    peerSeqNum);
            }
            if (!Arrays.equals(messageType, msgType)) {
                throw new SaslException("DIGEST-MD5: invalid message type: " +
                    networkByteOrderToInt(msgType, 0, 2));
            }
            peerSeqNum++;
            if (msgLength == msgWithPadding.length) {
                return msgWithPadding; 
            } else {
                byte[] clearMsg = new byte[msgLength];
                System.arraycopy(msgWithPadding, 0, clearMsg, 0, msgLength);
                return clearMsg;
            }
        }
    }
    private static final BigInteger MASK = new BigInteger("7f", 16);
    private static void setParityBit(byte[] key) {
        for (int i = 0; i < key.length; i++) {
            int b = key[i] & 0xfe;
            b |= (Integer.bitCount(b) & 1) ^ 1;
            key[i] = (byte) b;
        }
    }
    private static byte[] addDesParity(byte[] input, int offset, int len) {
        if (len != 7)
            throw new IllegalArgumentException(
                "Invalid length of DES Key Value:" + len);
        byte[] raw = new byte[7];
        System.arraycopy(input, offset, raw, 0, len);
        byte[] result = new byte[8];
        BigInteger in = new BigInteger(raw);
        for (int i=result.length-1; i>=0; i--) {
            result[i] = in.and(MASK).toByteArray()[0];
            result[i] <<= 1;         
            in = in.shiftRight(7);
        }
        setParityBit(result);
        return result;
    }
    private static SecretKey makeDesKeys(byte[] input, String desStrength)
        throws NoSuchAlgorithmException, InvalidKeyException,
            InvalidKeySpecException {
        byte[] subkey1 = addDesParity(input, 0, 7);
        KeySpec spec = null;
        SecretKeyFactory desFactory =
            SecretKeyFactory.getInstance(desStrength);
        if (desStrength.equals("des")) {
            spec = new DESKeySpec(subkey1, 0);
            if (logger.isLoggable(Level.FINEST)) {
                traceOutput(DP_CLASS_NAME, "makeDesKeys",
                    "DIGEST42:DES key input: ", input);
                traceOutput(DP_CLASS_NAME, "makeDesKeys",
                    "DIGEST43:DES key parity-adjusted: ", subkey1);
                traceOutput(DP_CLASS_NAME, "makeDesKeys",
                    "DIGEST44:DES key material: ", ((DESKeySpec)spec).getKey());
                logger.log(Level.FINEST, "DIGEST45: is parity-adjusted? {0}",
                    Boolean.valueOf(DESKeySpec.isParityAdjusted(subkey1, 0)));
            }
        } else if (desStrength.equals("desede")) {
            byte[] subkey2 = addDesParity(input, 7, 7);
            byte[] ede = new byte[subkey1.length*2+subkey2.length];
            System.arraycopy(subkey1, 0, ede, 0, subkey1.length);
            System.arraycopy(subkey2, 0, ede, subkey1.length, subkey2.length);
            System.arraycopy(subkey1, 0, ede, subkey1.length+subkey2.length,
                subkey1.length);
            spec = new DESedeKeySpec(ede, 0);
            if (logger.isLoggable(Level.FINEST)) {
                traceOutput(DP_CLASS_NAME, "makeDesKeys",
                    "DIGEST46:3DES key input: ", input);
                traceOutput(DP_CLASS_NAME, "makeDesKeys",
                    "DIGEST47:3DES key ede: ", ede);
                traceOutput(DP_CLASS_NAME, "makeDesKeys",
                    "DIGEST48:3DES key material: ",
                    ((DESedeKeySpec)spec).getKey());
                logger.log(Level.FINEST, "DIGEST49: is parity-adjusted? ",
                    Boolean.valueOf(DESedeKeySpec.isParityAdjusted(ede, 0)));
            }
        } else {
            throw new IllegalArgumentException("Invalid DES strength:" +
                desStrength);
        }
        return desFactory.generateSecret(spec);
    }
}
