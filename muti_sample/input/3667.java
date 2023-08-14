abstract class MessageToken extends Krb5Token {
    private static final int TOKEN_NO_CKSUM_SIZE = 16;
    private static final int FILLER = 0xffff;
    static final int SGN_ALG_DES_MAC_MD5 = 0x0000;
    static final int SGN_ALG_DES_MAC     = 0x0200;
    static final int SGN_ALG_HMAC_SHA1_DES3_KD = 0x0400;
    static final int SEAL_ALG_NONE    = 0xffff;
    static final int SEAL_ALG_DES = 0x0000;
    static final int SEAL_ALG_DES3_KD = 0x0200;
    static final int SEAL_ALG_ARCFOUR_HMAC = 0x1000;
    static final int SGN_ALG_HMAC_MD5_ARCFOUR = 0x1100;
    private static final int TOKEN_ID_POS = 0;
    private static final int SIGN_ALG_POS = 2;
    private static final int SEAL_ALG_POS = 4;
    private int seqNumber;
    private boolean confState = true;
    private boolean initiator = true;
    private int tokenId = 0;
    private GSSHeader gssHeader = null;
    private MessageTokenHeader tokenHeader = null;
    private byte[] checksum = null;
    private byte[] encSeqNumber = null;
    private byte[] seqNumberData = null;
    CipherHelper cipherHelper = null;
    MessageToken(int tokenId, Krb5Context context,
                 byte[] tokenBytes, int tokenOffset, int tokenLen,
                 MessageProp prop) throws GSSException {
        this(tokenId, context,
             new ByteArrayInputStream(tokenBytes, tokenOffset, tokenLen),
             prop);
    }
    MessageToken(int tokenId, Krb5Context context, InputStream is,
                 MessageProp prop) throws GSSException {
        init(tokenId, context);
        try {
            gssHeader = new GSSHeader(is);
            if (!gssHeader.getOid().equals(OID)) {
                throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                                       getTokenName(tokenId));
            }
            if (!confState) {
                prop.setPrivacy(false);
            }
            tokenHeader = new MessageTokenHeader(is, prop);
            encSeqNumber = new byte[8];
            readFully(is, encSeqNumber);
            checksum = new byte[cipherHelper.getChecksumLength()];
            readFully(is, checksum);
        } catch (IOException e) {
            throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                getTokenName(tokenId) + ":" + e.getMessage());
        }
    }
    public final GSSHeader getGSSHeader() {
        return gssHeader;
    }
    public final int getTokenId() {
        return tokenId;
    }
    public final byte[] getEncSeqNumber() {
        return encSeqNumber;
    }
    public final byte[] getChecksum() {
        return checksum;
    }
    public final boolean getConfState() {
        return confState;
    }
    public void genSignAndSeqNumber(MessageProp prop,
                                    byte[] optionalHeader,
                                    byte[] data, int offset, int len,
                                    byte[] optionalTrailer)
        throws GSSException {
        int qop = prop.getQOP();
        if (qop != 0) {
            qop = 0;
            prop.setQOP(qop);
        }
        if (!confState) {
            prop.setPrivacy(false);
        }
        tokenHeader =
            new MessageTokenHeader(tokenId, prop.getPrivacy(), qop);
        checksum =
            getChecksum(optionalHeader, data, offset, len, optionalTrailer);
        seqNumberData = new byte[8];
        if (cipherHelper.isArcFour()) {
            writeBigEndian(seqNumber, seqNumberData);
        } else {
            writeLittleEndian(seqNumber, seqNumberData);
        }
        if (!initiator) {
            seqNumberData[4] = (byte)0xff;
            seqNumberData[5] = (byte)0xff;
            seqNumberData[6] = (byte)0xff;
            seqNumberData[7] = (byte)0xff;
        }
        encSeqNumber = cipherHelper.encryptSeq(checksum, seqNumberData, 0, 8);
    }
    public final boolean verifySignAndSeqNumber(byte[] optionalHeader,
                                        byte[] data, int offset, int len,
                                        byte[] optionalTrailer)
        throws GSSException {
        byte[] myChecksum =
            getChecksum(optionalHeader, data, offset, len, optionalTrailer);
        if (MessageDigest.isEqual(checksum, myChecksum)) {
            seqNumberData = cipherHelper.decryptSeq(
                checksum, encSeqNumber, 0, 8);
            byte directionByte = 0;
            if (initiator)
                directionByte = (byte) 0xff; 
            if ((seqNumberData[4] == directionByte) &&
                  (seqNumberData[5] == directionByte) &&
                  (seqNumberData[6] == directionByte) &&
                  (seqNumberData[7] == directionByte))
                return true;
        }
        return false;
    }
    public final int getSequenceNumber() {
        int sequenceNum = 0;
        if (cipherHelper.isArcFour()) {
            sequenceNum = readBigEndian(seqNumberData, 0, 4);
        } else {
            sequenceNum = readLittleEndian(seqNumberData, 0, 4);
        }
        return sequenceNum;
    }
    private byte[] getChecksum(byte[] optionalHeader,
                               byte[] data, int offset, int len,
                               byte[] optionalTrailer)
        throws GSSException {
        byte[] tokenHeaderBytes = tokenHeader.getBytes();
        byte[] existingHeader = optionalHeader;
        byte[] checksumDataHeader = tokenHeaderBytes;
        if (existingHeader != null) {
            checksumDataHeader = new byte[tokenHeaderBytes.length +
                                         existingHeader.length];
            System.arraycopy(tokenHeaderBytes, 0,
                             checksumDataHeader, 0, tokenHeaderBytes.length);
            System.arraycopy(existingHeader, 0,
                             checksumDataHeader, tokenHeaderBytes.length,
                             existingHeader.length);
        }
        return cipherHelper.calculateChecksum(tokenHeader.getSignAlg(),
             checksumDataHeader, optionalTrailer, data, offset, len, tokenId);
    }
    MessageToken(int tokenId, Krb5Context context) throws GSSException {
        init(tokenId, context);
        this.seqNumber = context.incrementMySequenceNumber();
    }
    private void init(int tokenId, Krb5Context context) throws GSSException {
        this.tokenId = tokenId;
        this.confState = context.getConfState();
        this.initiator = context.isInitiator();
        this.cipherHelper = context.getCipherHelper(null);
    }
    public void encode(OutputStream os) throws IOException, GSSException {
        gssHeader = new GSSHeader(OID, getKrb5TokenSize());
        gssHeader.encode(os);
        tokenHeader.encode(os);
        os.write(encSeqNumber);
        os.write(checksum);
    }
    protected int getKrb5TokenSize() throws GSSException {
        return getTokenSize();
    }
    protected final int getTokenSize() throws GSSException {
        return TOKEN_NO_CKSUM_SIZE + cipherHelper.getChecksumLength();
    }
    protected static final int getTokenSize(CipherHelper ch)
        throws GSSException {
         return TOKEN_NO_CKSUM_SIZE + ch.getChecksumLength();
    }
    protected abstract int getSealAlg(boolean confRequested, int qop)
        throws GSSException;
    class MessageTokenHeader {
         private int tokenId;
         private int signAlg;
         private int sealAlg;
         private byte[] bytes = new byte[8];
        public MessageTokenHeader(int tokenId, boolean conf, int qop)
         throws GSSException {
            this.tokenId = tokenId;
            signAlg = MessageToken.this.getSgnAlg(qop);
            sealAlg = MessageToken.this.getSealAlg(conf, qop);
            bytes[0] = (byte) (tokenId >>> 8);
            bytes[1] = (byte) (tokenId);
            bytes[2] = (byte) (signAlg >>> 8);
            bytes[3] = (byte) (signAlg);
            bytes[4] = (byte) (sealAlg >>> 8);
            bytes[5] = (byte) (sealAlg);
            bytes[6] = (byte) (MessageToken.FILLER >>> 8);
            bytes[7] = (byte) (MessageToken.FILLER);
        }
        public MessageTokenHeader(InputStream is, MessageProp prop)
            throws IOException {
            readFully(is, bytes);
            tokenId = readInt(bytes, TOKEN_ID_POS);
            signAlg = readInt(bytes, SIGN_ALG_POS);
            sealAlg = readInt(bytes, SEAL_ALG_POS);
            int temp = readInt(bytes, SEAL_ALG_POS + 2);
            switch (sealAlg) {
            case SEAL_ALG_DES:
            case SEAL_ALG_DES3_KD:
            case SEAL_ALG_ARCFOUR_HMAC:
                prop.setPrivacy(true);
                break;
            default:
                prop.setPrivacy(false);
            }
            prop.setQOP(0);  
        }
        public final void encode(OutputStream os) throws IOException {
            os.write(bytes);
        }
        public final int getTokenId() {
            return tokenId;
        }
        public final int getSignAlg() {
            return signAlg;
        }
        public final int getSealAlg() {
            return sealAlg;
        }
        public final byte[] getBytes() {
            return bytes;
        }
    } 
    protected int getSgnAlg(int qop) throws GSSException {
         return cipherHelper.getSgnAlg();
    }
}
