public class SHA1_MessageDigestImpl extends MessageDigestSpi 
                                    implements Cloneable, SHA1_Data {
    private  int buffer[];       
    private byte oneByte[];      
    private int messageLength;   
    public SHA1_MessageDigestImpl() {
        buffer  = new int[BYTES_OFFSET +6];
        oneByte = new byte[1];
        engineReset();
    }
    private void processDigest(byte[] digest, int offset) {
        int i, j;         
        int lastWord;     
        long nBits = messageLength <<3 ;  
        engineUpdate( (byte) 0x80 );      
        i = 0;                     
        lastWord = (buffer[BYTES_OFFSET] + 3)>>2 ;  
        if ( buffer[BYTES_OFFSET] != 0 ) {
            if ( lastWord < 15 ) {
                i = lastWord;
            } else {
                if ( lastWord == 15 ) {
                    buffer[15] = 0;       
                }
                SHA1Impl.computeHash(buffer);
                i = 0;
            }
        }
        Arrays.fill(buffer, i, 14, 0);
        buffer[14] = (int)( nBits >>>32 );
        buffer[15] = (int)( nBits & 0xFFFFFFFF );
        SHA1Impl.computeHash(buffer);
        j = offset;
        for ( i = HASH_OFFSET; i < HASH_OFFSET +5; i++ ) {
            int k = buffer[i];
            digest[j  ] = (byte) ( k >>>24 );   
            digest[j+1] = (byte) ( k >>>16 );   
            digest[j+2] = (byte) ( k >>> 8 );   
            digest[j+3] = (byte) ( k       );   
            j += 4;
        }
        engineReset();
    }
    public Object clone() throws CloneNotSupportedException {
        SHA1_MessageDigestImpl cloneObj = (SHA1_MessageDigestImpl) super.clone();
        cloneObj.buffer  = ( int[])buffer.clone();
        cloneObj.oneByte = (byte[])oneByte.clone();
        return cloneObj;
    }
    protected byte[] engineDigest() {
        byte[] hash = new byte[DIGEST_LENGTH];
        processDigest(hash, 0);
        return hash;
    }
    protected int engineDigest(byte[] buf, int offset, int len) throws DigestException { 
        if ( buf == null ) {
            throw new IllegalArgumentException(Messages.getString("security.162"));  
        }
        if ( offset > buf.length || len > buf.length || (len + offset) > buf.length ) {
            throw new IllegalArgumentException(
               Messages.getString("security.163")); 
        }
        if ( len < DIGEST_LENGTH ) {
            throw new DigestException(Messages.getString("security.164")); 
        }
        if ( offset < 0 ) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("security.165", offset)); 
        }
        processDigest(buf, offset);
        return DIGEST_LENGTH;
    }
    protected int engineGetDigestLength() { 
        return DIGEST_LENGTH; 
    }
    protected void engineReset() {
        messageLength = 0;
        buffer[BYTES_OFFSET] = 0;
        buffer[HASH_OFFSET   ] = H0;
        buffer[HASH_OFFSET +1] = H1;
        buffer[HASH_OFFSET +2] = H2;
        buffer[HASH_OFFSET +3] = H3;
        buffer[HASH_OFFSET +4] = H4;
    }
    protected void engineUpdate(byte input) {
        oneByte[0] = input;
        SHA1Impl.updateHash( buffer, oneByte, 0, 0 );
        messageLength++;
    }
    protected void engineUpdate(byte[] input, int offset, int len) {
        if ( input == null ) {
            throw new IllegalArgumentException(Messages.getString("security.166"));  
        }
        if ( len <= 0 ) {
            return;
        }
        if ( offset < 0 ) {
            throw new ArrayIndexOutOfBoundsException(Messages.getString("security.165", offset)); 
        }
        if ( offset > input.length || len > input.length || (len + offset) > input.length ) {
            throw new IllegalArgumentException(
               Messages.getString("security.167")); 
        }
        SHA1Impl.updateHash(buffer, input, offset, offset + len -1 );
        messageLength += len;
    }
}
