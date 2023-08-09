class WrapToken extends MessageToken {
    static final int CONFOUNDER_SIZE = 8;
    static final byte[][] pads = {
        null, 
        {0x01},
        {0x02, 0x02},
        {0x03, 0x03, 0x03},
        {0x04, 0x04, 0x04, 0x04},
        {0x05, 0x05, 0x05, 0x05, 0x05},
        {0x06, 0x06, 0x06, 0x06, 0x06, 0x06},
        {0x07, 0x07, 0x07, 0x07, 0x07, 0x07, 0x07},
        {0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08, 0x08}
    };
    private boolean readTokenFromInputStream = true;
    private InputStream is = null;
    private byte[] tokenBytes = null;
    private int tokenOffset = 0;
    private int tokenLen = 0;
    private byte[] dataBytes = null;
    private int dataOffset = 0;
    private int dataLen = 0;
    private int dataSize = 0;
    byte[] confounder = null;
    byte[] padding = null;
    private boolean privacy = false;
    public WrapToken(Krb5Context context,
                     byte[] tokenBytes, int tokenOffset, int tokenLen,
                     MessageProp prop)  throws GSSException {
        super(Krb5Token.WRAP_ID, context,
              tokenBytes, tokenOffset, tokenLen, prop);
        this.readTokenFromInputStream = false;
        this.tokenBytes = tokenBytes;
        this.tokenOffset = tokenOffset;
        this.tokenLen = tokenLen;
        this.privacy = prop.getPrivacy();
        dataSize =
            getGSSHeader().getMechTokenLength() - getKrb5TokenSize();
    }
    public WrapToken(Krb5Context context,
                     InputStream is, MessageProp prop)
        throws GSSException {
        super(Krb5Token.WRAP_ID, context, is, prop);
        this.is = is;
        this.privacy = prop.getPrivacy();
        dataSize =
            getGSSHeader().getMechTokenLength() - getTokenSize();
    }
    public byte[] getData() throws GSSException {
        byte[] temp = new byte[dataSize];
        getData(temp, 0);
        byte[] retVal = new byte[dataSize - confounder.length -
                                padding.length];
        System.arraycopy(temp, 0, retVal, 0, retVal.length);
        return retVal;
    }
    public int getData(byte[] dataBuf, int dataBufOffset)
        throws GSSException {
        if (readTokenFromInputStream)
            getDataFromStream(dataBuf, dataBufOffset);
        else
            getDataFromBuffer(dataBuf, dataBufOffset);
        return (dataSize - confounder.length - padding.length);
    }
    private void getDataFromBuffer(byte[] dataBuf, int dataBufOffset)
        throws GSSException {
        GSSHeader gssHeader = getGSSHeader();
        int dataPos = tokenOffset +
            gssHeader.getLength() + getTokenSize();
        if (dataPos + dataSize > tokenOffset + tokenLen)
            throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                                   "Insufficient data in "
                                   + getTokenName(getTokenId()));
        confounder = new byte[CONFOUNDER_SIZE];
        if (privacy) {
            cipherHelper.decryptData(this,
                tokenBytes, dataPos, dataSize, dataBuf, dataBufOffset);
        } else {
            System.arraycopy(tokenBytes, dataPos,
                             confounder, 0, CONFOUNDER_SIZE);
            int padSize = tokenBytes[dataPos + dataSize - 1];
            if (padSize < 0)
                padSize = 0;
            if (padSize > 8)
                padSize %= 8;
            padding = pads[padSize];
            System.arraycopy(tokenBytes, dataPos + CONFOUNDER_SIZE,
                             dataBuf, dataBufOffset, dataSize -
                             CONFOUNDER_SIZE - padSize);
        }
        if (!verifySignAndSeqNumber(confounder,
                                    dataBuf, dataBufOffset,
                                    dataSize - CONFOUNDER_SIZE
                                    - padding.length,
                                    padding))
            throw new GSSException(GSSException.BAD_MIC, -1,
                         "Corrupt checksum or sequence number in Wrap token");
    }
    private void getDataFromStream(byte[] dataBuf, int dataBufOffset)
        throws GSSException {
        GSSHeader gssHeader = getGSSHeader();
        confounder = new byte[CONFOUNDER_SIZE];
        try {
            if (privacy) {
                cipherHelper.decryptData(this, is, dataSize,
                    dataBuf, dataBufOffset);
            } else {
                readFully(is, confounder);
                if (cipherHelper.isArcFour()) {
                    padding = pads[1];
                    readFully(is, dataBuf, dataBufOffset, dataSize-CONFOUNDER_SIZE-1);
                } else {
                    int numBlocks = (dataSize - CONFOUNDER_SIZE)/8 - 1;
                    int offset = dataBufOffset;
                    for (int i = 0; i < numBlocks; i++) {
                        readFully(is, dataBuf, offset, 8);
                        offset += 8;
                    }
                    byte[] finalBlock = new byte[8];
                    readFully(is, finalBlock);
                    int padSize = finalBlock[7];
                    padding = pads[padSize];
                    System.arraycopy(finalBlock, 0, dataBuf, offset,
                                     finalBlock.length - padSize);
                }
            }
        } catch (IOException e) {
            throw new GSSException(GSSException.DEFECTIVE_TOKEN, -1,
                                   getTokenName(getTokenId())
                                   + ": " + e.getMessage());
        }
        if (!verifySignAndSeqNumber(confounder,
                                    dataBuf, dataBufOffset,
                                    dataSize - CONFOUNDER_SIZE
                                    - padding.length,
                                    padding))
            throw new GSSException(GSSException.BAD_MIC, -1,
                         "Corrupt checksum or sequence number in Wrap token");
    }
    private byte[] getPadding(int len) {
        int padSize = 0;
        if (cipherHelper.isArcFour()) {
            padSize = 1;
        } else {
            padSize = len % 8;
            padSize = 8 - padSize;
        }
        return pads[padSize];
    }
    public WrapToken(Krb5Context context, MessageProp prop,
                     byte[] dataBytes, int dataOffset, int dataLen)
        throws GSSException {
        super(Krb5Token.WRAP_ID, context);
        confounder = Confounder.bytes(CONFOUNDER_SIZE);
        padding = getPadding(dataLen);
        dataSize = confounder.length + dataLen + padding.length;
        this.dataBytes = dataBytes;
        this.dataOffset = dataOffset;
        this.dataLen = dataLen;
        genSignAndSeqNumber(prop,
                            confounder,
                            dataBytes, dataOffset, dataLen,
                            padding);
        if (!context.getConfState())
            prop.setPrivacy(false);
        privacy = prop.getPrivacy();
    }
    public void encode(OutputStream os) throws IOException, GSSException {
        super.encode(os);
        if (!privacy) {
            os.write(confounder);
            os.write(dataBytes, dataOffset, dataLen);
            os.write(padding);
        } else {
            cipherHelper.encryptData(this, confounder,
                dataBytes, dataOffset, dataLen, padding, os);
        }
    }
    public byte[] encode() throws IOException, GSSException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(dataSize + 50);
        encode(bos);
        return bos.toByteArray();
    }
    public int encode(byte[] outToken, int offset)
        throws IOException, GSSException  {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        super.encode(bos);
        byte[] header = bos.toByteArray();
        System.arraycopy(header, 0, outToken, offset, header.length);
        offset += header.length;
        if (!privacy) {
            System.arraycopy(confounder, 0, outToken, offset,
                             confounder.length);
            offset += confounder.length;
            System.arraycopy(dataBytes, dataOffset, outToken, offset,
                             dataLen);
            offset += dataLen;
            System.arraycopy(padding, 0, outToken, offset, padding.length);
        } else {
            cipherHelper.encryptData(this, confounder, dataBytes,
                dataOffset, dataLen, padding, outToken, offset);
        }
        return (header.length + confounder.length + dataLen + padding.length);
    }
    protected int getKrb5TokenSize() throws GSSException {
        return (getTokenSize() + dataSize);
    }
    protected int getSealAlg(boolean conf, int qop) throws GSSException {
        if (!conf) {
            return SEAL_ALG_NONE;
        }
        return cipherHelper.getSealAlg();
    }
    static int getSizeLimit(int qop, boolean confReq, int maxTokenSize,
        CipherHelper ch) throws GSSException {
        return (GSSHeader.getMaxMechTokenSize(OID, maxTokenSize) -
                (getTokenSize(ch) + CONFOUNDER_SIZE) - 8); 
    }
}
