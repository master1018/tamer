class MicToken extends MessageToken {
  public MicToken(Krb5Context context,
                  byte[] tokenBytes, int tokenOffset, int tokenLen,
                  MessageProp prop)  throws GSSException {
        super(Krb5Token.MIC_ID, context,
          tokenBytes, tokenOffset, tokenLen, prop);
  }
  public MicToken(Krb5Context context,
                   InputStream is, MessageProp prop)
    throws GSSException {
    super(Krb5Token.MIC_ID, context, is, prop);
  }
  public void verify(byte[] data, int offset, int len) throws GSSException {
        if (!verifySignAndSeqNumber(null, data, offset, len, null))
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
  public MicToken(Krb5Context context, MessageProp prop,
                  byte[] data, int pos, int len)
        throws GSSException {
        super(Krb5Token.MIC_ID, context);
        if (prop == null) prop = new MessageProp(0, false);
        genSignAndSeqNumber(prop, null, data, pos, len, null);
  }
  public MicToken(Krb5Context context, MessageProp prop,
                  InputStream data)
        throws GSSException, IOException {
        super(Krb5Token.MIC_ID, context);
        byte[] dataBytes = new byte[data.available()];
        data.read(dataBytes);
        if (prop == null) prop = new MessageProp(0, false);
        genSignAndSeqNumber(prop, null, dataBytes, 0, dataBytes.length, null);
  }
  protected int getSealAlg(boolean confRequested, int qop) {
        return (SEAL_ALG_NONE);
  }
  public int encode(byte[] outToken, int offset)
      throws IOException, GSSException {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      super.encode(bos);
      byte[] token = bos.toByteArray();
      System.arraycopy(token, 0, outToken, offset, token.length);
      return token.length;
  }
  public byte[] encode() throws IOException, GSSException{
    ByteArrayOutputStream bos = new ByteArrayOutputStream(50);
    encode(bos);
    return bos.toByteArray();
  }
}
