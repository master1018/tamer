public abstract class MessageDigestSpi {
    private byte[] tempArray;
    protected int engineGetDigestLength() {
        return 0;
    }
    protected abstract void engineUpdate(byte input);
    protected abstract void engineUpdate(byte[] input, int offset, int len);
    protected void engineUpdate(ByteBuffer input) {
        if (input.hasRemaining() == false) {
            return;
        }
        if (input.hasArray()) {
            byte[] b = input.array();
            int ofs = input.arrayOffset();
            int pos = input.position();
            int lim = input.limit();
            engineUpdate(b, ofs + pos, lim - pos);
            input.position(lim);
        } else {
            int len = input.remaining();
            int n = JCAUtil.getTempArraySize(len);
            if ((tempArray == null) || (n > tempArray.length)) {
                tempArray = new byte[n];
            }
            while (len > 0) {
                int chunk = Math.min(len, tempArray.length);
                input.get(tempArray, 0, chunk);
                engineUpdate(tempArray, 0, chunk);
                len -= chunk;
            }
        }
    }
    protected abstract byte[] engineDigest();
    protected int engineDigest(byte[] buf, int offset, int len)
                                                throws DigestException {
        byte[] digest = engineDigest();
        if (len < digest.length)
                throw new DigestException("partial digests not returned");
        if (buf.length - offset < digest.length)
                throw new DigestException("insufficient space in the output "
                                          + "buffer to store the digest");
        System.arraycopy(digest, 0, buf, offset, digest.length);
        return digest.length;
    }
    protected abstract void engineReset();
    public Object clone() throws CloneNotSupportedException {
        if (this instanceof Cloneable) {
            return super.clone();
        } else {
            throw new CloneNotSupportedException();
        }
    }
}
