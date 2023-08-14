public abstract class MessageDigestSpi {
    protected int engineGetDigestLength() {
        return 0;
    }
    protected abstract void engineUpdate(byte input);
    protected abstract void engineUpdate(byte[] input, int offset, int len);
    protected void engineUpdate(ByteBuffer input) {
        if (!input.hasRemaining()) {
            return;
        }
        byte[] tmp;
        if (input.hasArray()) {
            tmp = input.array();
            int offset = input.arrayOffset();
            int position = input.position();
            int limit = input.limit();
            engineUpdate(tmp, offset+position, limit - position);
            input.position(limit);
        } else {
            tmp = new byte[input.limit() - input.position()];
            input.get(tmp);
            engineUpdate(tmp, 0, tmp.length);
        }    
    }
    protected abstract byte[] engineDigest();
    protected int engineDigest(byte[] buf, int offset, int len)
                    throws DigestException {
        if (len < engineGetDigestLength()) {
            engineReset();
            throw new DigestException(Messages.getString("security.1B"));  
        }
        if (offset < 0) {
            engineReset();
            throw new DigestException(Messages.getString("security.1C")); 
        }
        if (offset + len > buf.length) {
            engineReset();
            throw new DigestException(Messages.getString("security.1D")); 
        }
        byte tmp[] = engineDigest();
        if (len < tmp.length) {
            throw new DigestException(Messages.getString("security.1B")); 
        }
        System.arraycopy(tmp, 0, buf, offset, tmp.length);
        return tmp.length;            
    }
    protected abstract void engineReset();
    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
