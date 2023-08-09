class MicToken_v2 extends MessageToken_v2 {
    public MicToken_v2(Krb5Context context,
                  byte[] tokenBytes, int tokenOffset, int tokenLen,
                  MessageProp prop)  throws GSSException {
        super(Krb5Token.MIC_ID_v2, context,
                tokenBytes, tokenOffset, tokenLen, prop);
    }
    public MicToken_v2(Krb5Context context, InputStream is, MessageProp prop)
            throws GSSException {
        super(Krb5Token.MIC_ID_v2, context, is, prop);
    }
    public void verify(byte[] data, int offset, int len) throws GSSException {
        if (!verifySign(data, offset, len))
            throw new GSSException(GSSException.BAD_MIC, -1,
                         "Corrupt checksum or sequence number in MIC token");
    }
    public void verify(InputStream data) throws GSSException {
        byte[] dataBytes = null;
        try {
            dataBytes = new byte[data.available()];
            data.read(dataBytes);
        } catch (IOException e) {
            throw new GSSException(GSSException.BAD_MIC, -1,
                "Corrupt checksum or sequence number in MIC token");
        }
        verify(dataBytes, 0, dataBytes.length);
    }
    public MicToken_v2(Krb5Context context, MessageProp prop,
                  byte[] data, int pos, int len)
            throws GSSException {
        super(Krb5Token.MIC_ID_v2, context);
        if (prop == null) prop = new MessageProp(0, false);
        genSignAndSeqNumber(prop, data, pos, len);
    }
    public MicToken_v2(Krb5Context context, MessageProp prop, InputStream data)
            throws GSSException, IOException {
        super(Krb5Token.MIC_ID_v2, context);
        byte[] dataBytes = new byte[data.available()];
        data.read(dataBytes);
        if (prop == null) prop = new MessageProp(0, false);
        genSignAndSeqNumber(prop, dataBytes, 0, dataBytes.length);
    }
    public byte[] encode() throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream(50);
        encode(bos);
        return bos.toByteArray();
    }
    public int encode(byte[] outToken, int offset) throws IOException {
        byte[] token = encode();
        System.arraycopy(token, 0, outToken, offset, token.length);
        return token.length;
    }
    public void encode(OutputStream os) throws IOException {
        encodeHeader(os);
        os.write(checksum);
    }
}
