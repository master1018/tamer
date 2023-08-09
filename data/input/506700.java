abstract class RFC1522Codec {
    protected String encodeText(final String text, final String charset)
     throws EncoderException, UnsupportedEncodingException  
    {
        if (text == null) {
            return null;
        }
        StringBuffer buffer = new StringBuffer();
        buffer.append("=?"); 
        buffer.append(charset); 
        buffer.append('?'); 
        buffer.append(getEncoding()); 
        buffer.append('?');
        byte [] rawdata = doEncoding(text.getBytes(charset)); 
        buffer.append(new String(rawdata, StringEncodings.US_ASCII));
        buffer.append("?="); 
        return buffer.toString();
    }
    protected String decodeText(final String text)
     throws DecoderException, UnsupportedEncodingException  
    {
        if (text == null) {
            return null;
        }
        if ((!text.startsWith("=?")) || (!text.endsWith("?="))) {
            throw new DecoderException("RFC 1522 violation: malformed encoded content");
        }
        int termnator = text.length() - 2;
        int from = 2;
        int to = text.indexOf("?", from);
        if ((to == -1) || (to == termnator)) {
            throw new DecoderException("RFC 1522 violation: charset token not found");
        }
        String charset = text.substring(from, to);
        if (charset.equals("")) {
            throw new DecoderException("RFC 1522 violation: charset not specified");
        }
        from = to + 1;
        to = text.indexOf("?", from);
        if ((to == -1) || (to == termnator)) {
            throw new DecoderException("RFC 1522 violation: encoding token not found");
        }
        String encoding = text.substring(from, to);
        if (!getEncoding().equalsIgnoreCase(encoding)) {
            throw new DecoderException("This codec cannot decode " + 
                encoding + " encoded content");
        }
        from = to + 1;
        to = text.indexOf("?", from);
        byte[] data = text.substring(from, to).getBytes(StringEncodings.US_ASCII);
        data = doDecoding(data); 
        return new String(data, charset);
    }
    protected abstract String getEncoding();
    protected abstract byte[] doEncoding(byte[] bytes) throws EncoderException;
    protected abstract byte[] doDecoding(byte[] bytes) throws DecoderException;
}
