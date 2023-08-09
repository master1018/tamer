abstract class MessageToken_v2 extends Krb5Token {
    protected static final int TOKEN_HEADER_SIZE = 16;
    private static final int TOKEN_ID_POS = 0;
    private static final int TOKEN_FLAG_POS = 2;
    private static final int TOKEN_EC_POS = 4;
    private static final int TOKEN_RRC_POS = 6;
    protected static final int CONFOUNDER_SIZE = 16;
    static final int KG_USAGE_ACCEPTOR_SEAL = 22;
    static final int KG_USAGE_ACCEPTOR_SIGN = 23;
    static final int KG_USAGE_INITIATOR_SEAL = 24;
    static final int KG_USAGE_INITIATOR_SIGN = 25;
    private static final int FLAG_SENDER_IS_ACCEPTOR = 1;
    private static final int FLAG_WRAP_CONFIDENTIAL  = 2;
    private static final int FLAG_ACCEPTOR_SUBKEY    = 4;
    private static final int FILLER = 0xff;
    private MessageTokenHeader tokenHeader = null;
    private int tokenId = 0;
    private int seqNumber;
    protected byte[] tokenData; 
    protected int tokenDataLen;
    private int key_usage = 0;
    private int ec = 0;
    private int rrc = 0;
    byte[] checksum = null;
    private boolean confState = true;
    private boolean initiator = true;
    CipherHelper cipherHelper = null;
    MessageToken_v2(int tokenId, Krb5Context context,
                 byte[] tokenBytes, int tokenOffset, int tokenLen,
                 MessageProp prop) throws GSSException {
        this(tokenId, context,
             new ByteArrayInputStream(tokenBytes, tokenOffset, tokenLen),
             prop);
    }
    MessageToken_v2(int tokenId, Krb5Context context, InputStream is,
                 MessageProp prop) throws GSSException {
        init(tokenId, context);
        try {
            if (!confState) {
                prop.setPrivacy(false);
            }
            tokenHeader = new MessageTokenHeader(is, prop, tokenId);
            if (tokenId == Krb5Token.WRAP_ID_v2) {
                key_usage = (!initiator ? KG_USAGE_INITIATOR_SEAL
                                : KG_USAGE_ACCEPTOR_SEAL);
            } else if (tokenId == Krb5Token.MIC_ID_v2) {
                key_usage = (!initiator ? KG_USAGE_INITIATOR_SIGN
                                : KG_USAGE_ACCEPTOR_SIGN);
            }
            int minSize = 0;    
            if (tokenId == Krb5Token.WRAP_ID_v2 && prop.getPrivacy()) {
                minSize = CONFOUNDER_SIZE +
                        TOKEN_HEADER_SIZE + cipherHelper.getChecksumLength();
            } else {
                minSize = cipherHelper.getChecksumLength();
            }
            if (tokenId == Krb5Token.MIC_ID_v2) {
                tokenDataLen = minSize;
                tokenData = new byte[minSize];
                readFully(is, tokenData);
            } else {
                tokenDataLen = is.available();
                if (tokenDataLen >= minSize) {  
                    tokenData = new byte[tokenDataLen];
                    readFully(is, tokenData);
                } else {
                    byte[] tmp = new byte[minSize];
                    readFully(is, tmp);
                    int more = is.available();
                    tokenDataLen = minSize + more;
                    tokenData = Arrays.copyOf(tmp, tokenDataLen);
                    readFully(is, tokenData, minSize, more);
                }
            }
            if (tokenId == Krb5Token.WRAP_ID_v2) {
                rotate();
            }
            if (tokenId == Krb5Token.MIC_ID_v2 ||
                    (tokenId == Krb5Token.WRAP_ID_v2 && !prop.getPrivacy())) {
                int chkLen = cipherHelper.getChecksumLength();
                checksum = new byte[chkLen];
                System.arraycopy(tokenData, tokenDataLen-chkLen,
                        checksum, 0, chkLen);
                if (tokenId == Krb5Token.WRAP_ID_v2 && !prop.getPrivacy()) {
                    if (chkLen != ec) {
                        throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                            getTokenName(tokenId) + ":" + "EC incorrect!");
                    }
                }
            }
        } catch (IOException e) {
            throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                getTokenName(tokenId) + ":" + e.getMessage());
        }
    }
    public final int getTokenId() {
        return tokenId;
    }
    public final int getKeyUsage() {
        return key_usage;
    }
    public final boolean getConfState() {
        return confState;
    }
    public void genSignAndSeqNumber(MessageProp prop,
                                    byte[] data, int offset, int len)
        throws GSSException {
        int qop = prop.getQOP();
        if (qop != 0) {
            qop = 0;
            prop.setQOP(qop);
        }
        if (!confState) {
            prop.setPrivacy(false);
        }
        tokenHeader = new MessageTokenHeader(tokenId,
                                prop.getPrivacy(), true);
        if (tokenId == Krb5Token.WRAP_ID_v2) {
            key_usage = (initiator ? KG_USAGE_INITIATOR_SEAL
                                : KG_USAGE_ACCEPTOR_SEAL);
        } else if (tokenId == Krb5Token.MIC_ID_v2) {
            key_usage = (initiator ? KG_USAGE_INITIATOR_SIGN
                                : KG_USAGE_ACCEPTOR_SIGN);
        }
        if ((tokenId == MIC_ID_v2) ||
            (!prop.getPrivacy() && (tokenId == WRAP_ID_v2))) {
           checksum = getChecksum(data, offset, len);
        }
        if (!prop.getPrivacy() && (tokenId == WRAP_ID_v2)) {
            byte[] tok_header = tokenHeader.getBytes();
            tok_header[4] = (byte) (checksum.length >>> 8);
            tok_header[5] = (byte) (checksum.length);
        }
    }
    public final boolean verifySign(byte[] data, int offset, int len)
        throws GSSException {
        byte[] myChecksum = getChecksum(data, offset, len);
        if (MessageDigest.isEqual(checksum, myChecksum)) {
            return true;
        }
        return false;
    }
    private void rotate() {
        if (rrc % tokenDataLen != 0) {
           rrc = rrc % tokenDataLen;
           byte[] newBytes = new byte[tokenDataLen];
           System.arraycopy(tokenData, rrc, newBytes, 0, tokenDataLen-rrc);
           System.arraycopy(tokenData, 0, newBytes, tokenDataLen-rrc, rrc);
           tokenData = newBytes;
        }
    }
    public final int getSequenceNumber() {
        return seqNumber;
    }
    byte[] getChecksum(byte[] data, int offset, int len)
        throws GSSException {
        byte[] tokenHeaderBytes = tokenHeader.getBytes();
        int conf_flag = tokenHeaderBytes[TOKEN_FLAG_POS] &
                                FLAG_WRAP_CONFIDENTIAL;
        if ((conf_flag == 0) && (tokenId == WRAP_ID_v2)) {
            tokenHeaderBytes[4] = 0;
            tokenHeaderBytes[5] = 0;
        }
        return cipherHelper.calculateChecksum(tokenHeaderBytes, data,
                                                offset, len, key_usage);
    }
    MessageToken_v2(int tokenId, Krb5Context context) throws GSSException {
        init(tokenId, context);
        this.seqNumber = context.incrementMySequenceNumber();
    }
    private void init(int tokenId, Krb5Context context) throws GSSException {
        this.tokenId = tokenId;
        this.confState = context.getConfState();
        this.initiator = context.isInitiator();
        this.cipherHelper = context.getCipherHelper(null);
    }
    protected void encodeHeader(OutputStream os) throws IOException {
        tokenHeader.encode(os);
    }
    public abstract void encode(OutputStream os) throws IOException;
    protected final byte[] getTokenHeader() {
        return (tokenHeader.getBytes());
    }
    class MessageTokenHeader {
         private int tokenId;
         private byte[] bytes = new byte[TOKEN_HEADER_SIZE];
         public MessageTokenHeader(int tokenId, boolean conf,
                boolean have_acceptor_subkey) throws GSSException {
            this.tokenId = tokenId;
            bytes[0] = (byte) (tokenId >>> 8);
            bytes[1] = (byte) (tokenId);
            int flags = 0;
            flags = (initiator ? 0 : FLAG_SENDER_IS_ACCEPTOR) |
                     ((conf && tokenId != MIC_ID_v2) ?
                                FLAG_WRAP_CONFIDENTIAL : 0) |
                     (have_acceptor_subkey ? FLAG_ACCEPTOR_SUBKEY : 0);
            bytes[2] = (byte) flags;
            bytes[3] = (byte) FILLER;
            if (tokenId == WRAP_ID_v2) {
                bytes[4] = (byte) 0;
                bytes[5] = (byte) 0;
                bytes[6] = (byte) 0;
                bytes[7] = (byte) 0;
            } else if (tokenId == MIC_ID_v2) {
                for (int i = 4; i < 8; i++) {
                    bytes[i] = (byte) FILLER;
                }
            }
            writeBigEndian(seqNumber, bytes, 12);
        }
        public MessageTokenHeader(InputStream is, MessageProp prop, int tokId)
            throws IOException, GSSException {
            readFully(is, bytes, 0, TOKEN_HEADER_SIZE);
            tokenId = readInt(bytes, TOKEN_ID_POS);
            if (tokenId != tokId) {
                throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                    getTokenName(tokenId) + ":" + "Defective Token ID!");
            }
            int acceptor_flag = (initiator ? FLAG_SENDER_IS_ACCEPTOR : 0);
            int flag = bytes[TOKEN_FLAG_POS] & FLAG_SENDER_IS_ACCEPTOR;
            if (flag != acceptor_flag) {
                throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                        getTokenName(tokenId) + ":" + "Acceptor Flag Error!");
            }
            int conf_flag = bytes[TOKEN_FLAG_POS] & FLAG_WRAP_CONFIDENTIAL;
            if ((conf_flag == FLAG_WRAP_CONFIDENTIAL) &&
                (tokenId == WRAP_ID_v2)) {
                prop.setPrivacy(true);
            } else {
                prop.setPrivacy(false);
            }
            if (tokenId == WRAP_ID_v2) {
                if ((bytes[3] & 0xff) != FILLER) {
                    throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                        getTokenName(tokenId) + ":" + "Defective Token Filler!");
                }
                ec = readBigEndian(bytes, TOKEN_EC_POS, 2);
                rrc = readBigEndian(bytes, TOKEN_RRC_POS, 2);
            } else if (tokenId == MIC_ID_v2) {
                for (int i = 3; i < 8; i++) {
                    if ((bytes[i] & 0xff) != FILLER) {
                        throw new GSSException(GSSException.DEFECTIVE_TOKEN,
                                -1, getTokenName(tokenId) + ":" +
                                "Defective Token Filler!");
                    }
                }
            }
            prop.setQOP(0);
            seqNumber = readBigEndian(bytes, 0, 8);
        }
        public final void encode(OutputStream os) throws IOException {
            os.write(bytes);
        }
        public final int getTokenId() {
            return tokenId;
        }
        public final byte[] getBytes() {
            return bytes;
        }
    } 
}
