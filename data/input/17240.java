class WrapToken_v2 extends MessageToken_v2 {
    byte[] confounder = null;
    private final boolean privacy;
    public WrapToken_v2(Krb5Context context,
                     byte[] tokenBytes, int tokenOffset, int tokenLen,
                     MessageProp prop)  throws GSSException {
        super(Krb5Token.WRAP_ID_v2, context,
              tokenBytes, tokenOffset, tokenLen, prop);
        this.privacy = prop.getPrivacy();
    }
    public WrapToken_v2(Krb5Context context,
                     InputStream is, MessageProp prop)
        throws GSSException {
        super(Krb5Token.WRAP_ID_v2, context, is, prop);
        this.privacy = prop.getPrivacy();
    }
    public byte[] getData() throws GSSException {
        byte[] temp = new byte[tokenDataLen];
        int len = getData(temp, 0);
        return Arrays.copyOf(temp, len);
    }
    public int getData(byte[] dataBuf, int dataBufOffset)
        throws GSSException {
        if (privacy) {
            cipherHelper.decryptData(this, tokenData, 0, tokenDataLen,
                                dataBuf, dataBufOffset, getKeyUsage());
            return tokenDataLen - CONFOUNDER_SIZE -
                TOKEN_HEADER_SIZE - cipherHelper.getChecksumLength();
        } else {
            int data_length = tokenDataLen - cipherHelper.getChecksumLength();
            System.arraycopy(tokenData, 0,
                             dataBuf, dataBufOffset,
                             data_length);
            if (!verifySign(dataBuf, dataBufOffset, data_length)) {
                throw new GSSException(GSSException.BAD_MIC, -1,
                         "Corrupt checksum in Wrap token");
            }
            return data_length;
        }
    }
    public WrapToken_v2(Krb5Context context, MessageProp prop,
                     byte[] dataBytes, int dataOffset, int dataLen)
            throws GSSException {
        super(Krb5Token.WRAP_ID_v2, context);
        confounder = Confounder.bytes(CONFOUNDER_SIZE);
        genSignAndSeqNumber(prop, dataBytes, dataOffset, dataLen);
        if (!context.getConfState())
            prop.setPrivacy(false);
        privacy = prop.getPrivacy();
        if (!privacy) {
            tokenData = new byte[dataLen + checksum.length];
            System.arraycopy(dataBytes, dataOffset, tokenData, 0, dataLen);
            System.arraycopy(checksum, 0, tokenData, dataLen, checksum.length);
        } else {
            tokenData = cipherHelper.encryptData(this, confounder, getTokenHeader(),
                dataBytes, dataOffset, dataLen, getKeyUsage());
        }
    }
    public void encode(OutputStream os) throws IOException {
        encodeHeader(os);
        os.write(tokenData);
    }
    public byte[] encode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(
                MessageToken_v2.TOKEN_HEADER_SIZE + tokenData.length);
        encode(bos);
        return bos.toByteArray();
    }
    public int encode(byte[] outToken, int offset) throws IOException {
        byte[] token = encode();
        System.arraycopy(token, 0, outToken, offset, token.length);
        return token.length;
    }
    static int getSizeLimit(int qop, boolean confReq, int maxTokenSize,
        CipherHelper ch) throws GSSException {
        return (GSSHeader.getMaxMechTokenSize(OID, maxTokenSize) -
                (TOKEN_HEADER_SIZE + ch.getChecksumLength() + CONFOUNDER_SIZE)
                - 8 );
    }
}
