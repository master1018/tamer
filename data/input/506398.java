public class BerInputStream {
    protected InputStream in;
    protected byte[] buffer;
    protected int offset = 0;
    private static final int BUF_INCREASE_SIZE = 1024 * 16;
    protected static final int INDEFINIT_LENGTH = -1;
    public BerInputStream(byte[] encoded) throws IOException {
        this(encoded, 0, encoded.length);
    }
    public BerInputStream(byte[] encoded, int offset, int expectedLength)
            throws IOException {
        this.buffer = encoded;
        this.offset = offset;
        next();
        if (length != INDEFINIT_LENGTH
                && (offset + expectedLength) != (this.offset + this.length)) {
            throw new ASN1Exception(Messages.getString("security.111")); 
        }
    }
    public BerInputStream(InputStream in) throws IOException {
        this(in, BUF_INCREASE_SIZE);
    }
    public BerInputStream(InputStream in, int initialSize) throws IOException {
        this.in = in;
        buffer = new byte[initialSize];
        next();
        if (length != INDEFINIT_LENGTH) {
            if (buffer.length < (length + offset)) {
                byte[] newBuffer = new byte[length + offset];
                System.arraycopy(buffer, 0, newBuffer, 0, offset);
                buffer = newBuffer;
            }
        } else {
            isIndefinedLength = true;
            throw new ASN1Exception(Messages.getString("security.112")); 
        }
    }
    public final void reset(byte[] encoded) throws IOException {
        buffer = encoded;
        next();
    }
    public int tag;
    protected int length;
    public Object content;
    protected int tagOffset;
    protected int contentOffset;
    public int next() throws IOException {
        tagOffset = offset;
        tag = read();
        length = read();
        if (length != 0x80) { 
            if ((length & 0x80) != 0) { 
                int numOctets = length & 0x7F;
                if (numOctets > 5) {
                    throw new ASN1Exception(Messages.getString("security.113", 
                            tagOffset)); 
                }
                length = read();
                for (int i = 1; i < numOctets; i++) {
                    int ch = read();
                    length = (length << 8) + ch;
                }
                if (length > 0xFFFFFF) {
                    throw new ASN1Exception(Messages.getString("security.113", 
                            tagOffset)); 
                }
            }
        } else { 
            length = INDEFINIT_LENGTH;
        }
        contentOffset = offset;
        return tag;
    }
    public static int getLength(byte[] encoding) {
        int length = encoding[1] & 0xFF;
        int numOctets = 0;
        if ((length & 0x80) != 0) { 
            numOctets = length & 0x7F;
            length = encoding[2] & 0xFF;
            for (int i = 3; i < numOctets + 2; i++) {
                length = (length << 8) + (encoding[i] & 0xFF);
            }
        }
        return 1 + 1 + numOctets + length;
    }
    public void readBitString() throws IOException {
        if (tag == ASN1Constants.TAG_BITSTRING) {
            if (length == 0) {
                throw new ASN1Exception(
                        Messages.getString("security.114", tagOffset)); 
            }
            readContent();
            if (buffer[contentOffset] > 7) {
                throw new ASN1Exception(Messages.getString("security.115", 
                        contentOffset));
            }
            if (length == 1 && buffer[contentOffset] != 0) {
                throw new ASN1Exception(Messages.getString("security.116", 
                        contentOffset));
            }
        } else if (tag == ASN1Constants.TAG_C_BITSTRING) {
            throw new ASN1Exception(Messages.getString("security.117")); 
        } else {
            throw new ASN1Exception(
                    Messages.getString("security.118", tagOffset, 
                            Integer.toHexString(tag)));
        }
    }
    public void readEnumerated() throws IOException {
        if (tag != ASN1Constants.TAG_ENUM) {
            throw new ASN1Exception(
                    Messages.getString("security.119", tagOffset, 
                            Integer.toHexString(tag)));
        }
        if (length == 0) {
            throw new ASN1Exception(Messages.getString("security.11A", tagOffset));
        }
        readContent();
        if (length > 1) {
            int bits = buffer[contentOffset] & 0xFF;
            if (buffer[contentOffset + 1] < 0) {
                bits += 0x100;
            }
            if (bits == 0 || bits == 0x1FF) {
                throw new ASN1Exception(Messages.getString("security.11B", contentOffset)); 
            }
        }
    }
    public void readBoolean() throws IOException {
        if (tag != ASN1Constants.TAG_BOOLEAN) {
            throw new ASN1Exception(Messages.getString("security.11C", 
                    tagOffset, Integer.toHexString(tag)));
        }
        if (length != 1) {
            throw new ASN1Exception(Messages.getString("security.11D", tagOffset));
        }
        readContent();
    }
    public int choiceIndex;
    public int[] times;
    public void readGeneralizedTime() throws IOException {
        if (tag == ASN1Constants.TAG_GENERALIZEDTIME) {
            readContent();
            if (buffer[offset - 1] != 'Z') {
                throw new ASN1Exception(Messages.getString("security.11E")); 
            }
            if (length != 15 && (length < 17 || length > 19)) 
            {
                throw new ASN1Exception(Messages.getString("security.11F", 
                                contentOffset));
            }
            if (length > 16) {
                byte char14 = buffer[contentOffset + 14];
                if (char14 != '.' && char14 != ',') {
                    throw new ASN1Exception(
                            Messages.getString("security.11F", 
                                    contentOffset));
                }
            }
            if (times == null) {
                times = new int[7];
            }
            times[0] = strToInt(contentOffset, 4); 
            times[1] = strToInt(contentOffset + 4, 2); 
            times[2] = strToInt(contentOffset + 6, 2); 
            times[3] = strToInt(contentOffset + 8, 2); 
            times[4] = strToInt(contentOffset + 10, 2); 
            times[5] = strToInt(contentOffset + 12, 2); 
            if (length > 16) {
                times[6] = strToInt(contentOffset + 15, length - 16);
                if (length == 17) {
                    times[6] = times[6] * 100;
                } else if (length == 18) {
                    times[6] = times[6] * 10;
                }
            }
        } else if (tag == ASN1Constants.TAG_C_GENERALIZEDTIME) {
            throw new ASN1Exception(Messages.getString("security.120")); 
        } else {
            throw new ASN1Exception(Messages.getString("security.121", 
                            tagOffset, Integer.toHexString(tag)));
        }
    }
    public void readUTCTime() throws IOException {
        if (tag == ASN1Constants.TAG_UTCTIME) {
            switch (length) {
            case ASN1UTCTime.UTC_HM:
            case ASN1UTCTime.UTC_HMS:
                break;
            case ASN1UTCTime.UTC_LOCAL_HM:
            case ASN1UTCTime.UTC_LOCAL_HMS:
                throw new ASN1Exception(Messages.getString("security.122")); 
            default:
                throw new ASN1Exception(Messages.getString("security.123", 
                                tagOffset));
            }
            readContent();
            if (buffer[offset - 1] != 'Z') {
                throw new ASN1Exception("ASN.1 UTCTime wrongly encoded at [" 
                        + contentOffset + ']');
            }
            if (times == null) {
                times = new int[7];
            }
            times[0] = strToInt(contentOffset, 2); 
            if (times[0] > 49) {
                times[0] += 1900;
            } else {
                times[0] += 2000;
            }
            times[1] = strToInt(contentOffset + 2, 2); 
            times[2] = strToInt(contentOffset + 4, 2); 
            times[3] = strToInt(contentOffset + 6, 2); 
            times[4] = strToInt(contentOffset + 8, 2); 
            if (length == ASN1UTCTime.UTC_HMS) {
                times[5] = strToInt(contentOffset + 10, 2); 
            }
        } else if (tag == ASN1Constants.TAG_C_UTCTIME) {
            throw new ASN1Exception(Messages.getString("security.124")); 
        } else {
            throw new ASN1Exception(Messages.getString("security.125", 
                    tagOffset, Integer.toHexString(tag)));
        }
    }
    private int strToInt(int off, int count) throws ASN1Exception {
        int c;
        int result = 0;
        for (int i = off, end = off + count; i < end; i++) {
            c = buffer[i] - 48;
            if (c < 0 || c > 9) {
                throw new ASN1Exception(Messages.getString("security.126")); 
            }
            result = result * 10 + c;
        }
        return result;
    }
    public void readInteger() throws IOException {
        if (tag != ASN1Constants.TAG_INTEGER) {
            throw new ASN1Exception(Messages.getString("security.127", 
                    tagOffset, Integer.toHexString(tag)));
        }
        if (length < 1) {
            throw new ASN1Exception(Messages.getString("security.128", 
                    tagOffset)); 
        }
        readContent();
        if (length > 1) {
            byte firstByte = buffer[offset - length];
            byte secondByte = (byte) (buffer[offset - length + 1] & 0x80);
            if (firstByte == 0 && secondByte == 0 || firstByte == (byte) 0xFF
                    && secondByte == (byte) 0x80) {
                throw new ASN1Exception(Messages.getString("security.129", 
                                (offset - length)));
            }
        }
    }
    public void readOctetString() throws IOException {
        if (tag == ASN1Constants.TAG_OCTETSTRING) {
            readContent();
        } else if (tag == ASN1Constants.TAG_C_OCTETSTRING) {
            throw new ASN1Exception(Messages.getString("security.12A")); 
        } else {
            throw new ASN1Exception(
                    Messages.getString("security.12B", tagOffset, 
                            Integer.toHexString(tag)));
        }
    }
    public int oidElement;
    public void readOID() throws IOException {
        if (tag != ASN1Constants.TAG_OID) {
            throw new ASN1Exception(Messages.getString("security.12C", 
                    tagOffset, Integer.toHexString(tag)));
        }
        if (length < 1) {
            throw new ASN1Exception(Messages.getString("security.12D", tagOffset)); 
        }
        readContent();
        if ((buffer[offset - 1] & 0x80) != 0) {
            throw new ASN1Exception(Messages.getString("security.12E", (offset - 1))); 
        }
        oidElement = 1;
        for (int i = 0; i < length; i++, ++oidElement) {
            while ((buffer[contentOffset + i] & 0x80) == 0x80) {
                i++;
            }
        }
    }
    public void readSequence(ASN1Sequence sequence) throws IOException {
        if (tag != ASN1Constants.TAG_C_SEQUENCE) {
            throw new ASN1Exception(
                    Messages.getString("security.12F", tagOffset, 
                            Integer.toHexString(tag)));
        }
        int begOffset = offset;
        int endOffset = begOffset + length;
        ASN1Type[] type = sequence.type;
        int i = 0;
        if (isVerify) {
            for (; (offset < endOffset) && (i < type.length); i++) {
                next();
                while (!type[i].checkTag(tag)) {
                    if (!sequence.OPTIONAL[i] || (i == type.length - 1)) {
                        throw new ASN1Exception(Messages.getString("security.130", 
                                        tagOffset));
                    }
                    i++;
                }
                type[i].decode(this);
            }
            for (; i < type.length; i++) {
                if (!sequence.OPTIONAL[i]) {
                    throw new ASN1Exception(Messages.getString("security.131", 
                            tagOffset));
                }
            }
        } else {
            int seqTagOffset = tagOffset; 
            Object[] values = new Object[type.length];
            for (; (offset < endOffset) && (i < type.length); i++) {
                next();
                while (!type[i].checkTag(tag)) {
                    if (!sequence.OPTIONAL[i] || (i == type.length - 1)) {
                        throw new ASN1Exception(Messages.getString("security.132", 
                                        tagOffset));
                    }
                    if (sequence.DEFAULT[i] != null) {
                        values[i] = sequence.DEFAULT[i];
                    }
                    i++;
                }
                values[i] = type[i].decode(this);
            }
            for (; i < type.length; i++) {
                if (!sequence.OPTIONAL[i]) {
                    throw new ASN1Exception(Messages.getString("security.133", 
                            tagOffset));
                }
                if (sequence.DEFAULT[i] != null) {
                    values[i] = sequence.DEFAULT[i];
                }
            }
            content = values;
            tagOffset = seqTagOffset; 
        }
        if (offset != endOffset) {
            throw new ASN1Exception(Messages.getString("security.134", begOffset)); 
        }
    }
    public void readSequenceOf(ASN1SequenceOf sequenceOf) throws IOException {
        if (tag != ASN1Constants.TAG_C_SEQUENCEOF) {
            throw new ASN1Exception(Messages.getString("security.135", tagOffset, 
                            Integer.toHexString(tag)));
        }
        decodeValueCollection(sequenceOf);
    }
    public void readSet(ASN1Set set) throws IOException {
        if (tag != ASN1Constants.TAG_C_SET) {
            throw new ASN1Exception(Messages.getString("security.136", 
                    tagOffset, Integer.toHexString(tag)));
        }
        throw new ASN1Exception(Messages.getString("security.137")); 
    }
    public void readSetOf(ASN1SetOf setOf) throws IOException {
        if (tag != ASN1Constants.TAG_C_SETOF) {
            throw new ASN1Exception(Messages.getString("security.138", 
                    tagOffset, Integer.toHexString(tag)));
        }
        decodeValueCollection(setOf);
    }
    private final void decodeValueCollection(ASN1ValueCollection collection)
            throws IOException {
        int begOffset = offset;
        int endOffset = begOffset + length;
        ASN1Type type = collection.type;
        if (isVerify) {
            while (endOffset > offset) {
                next();
                type.decode(this);
            }
        } else {
            int seqTagOffset = tagOffset; 
            ArrayList values = new ArrayList();
            while (endOffset > offset) {
                next();
                values.add(type.decode(this));
            }
            content = values;
            tagOffset = seqTagOffset; 
        }
        if (offset != endOffset) {
            throw new ASN1Exception(Messages.getString("security.134", begOffset)); 
        }
    }
    public void readString(ASN1StringType type) throws IOException {
        if (tag == type.id) {
            readContent();
        } else if (tag == type.constrId) {
            throw new ASN1Exception(Messages.getString("security.139")); 
        } else {
            throw new ASN1Exception(
                    Messages.getString("security.13A", tagOffset, 
                            Integer.toHexString(tag)));
        }
    }
    public byte[] getEncoded() {
        byte[] encoded = new byte[offset - tagOffset];
        System.arraycopy(buffer, tagOffset, encoded, 0, encoded.length);
        return encoded;
    }
    public final byte[] getBuffer() {
        return buffer;
    }
    public final int getLength() {
        return length;
    }
    public final int getOffset() {
        return offset;
    }
    public final int getEndOffset() {
        return offset + length;
    }
    public final int getTagOffset() {
        return tagOffset;
    }
    public final int getContentOffset() {
        return contentOffset;
    }
    protected boolean isVerify;
    public final void setVerify() {
        isVerify = true;
    }
    protected boolean isIndefinedLength;
    protected int read() throws IOException {
        if (offset == buffer.length) {
            throw new ASN1Exception(Messages.getString("security.13B")); 
        }
        if (in == null) {
            return buffer[offset++] & 0xFF;
        } else {
            int octet = in.read();
            if (octet == -1) {
                throw new ASN1Exception(Messages.getString("security.13B")); 
            }
            buffer[offset++] = (byte) octet;
            return octet;
        }
    }
    public void readContent() throws IOException {
        if (offset + length > buffer.length) {
            throw new ASN1Exception(Messages.getString("security.13B")); 
        }
        if (in == null) {
            offset += length;
        } else {
            int bytesRead = in.read(buffer, offset, length);
            if (bytesRead != length) {
                int c = bytesRead;
                do {
                    if (c < 1 || bytesRead > length) {
                        throw new ASN1Exception(Messages
                                .getString("security.13C")); 
                    }
                    c = in.read(buffer, offset + bytesRead, length - bytesRead);
                    bytesRead += c;
                } while (bytesRead != length);
            }
            offset += length;
        }
    }
    public void compactBuffer() {
        if (offset != buffer.length) {
            byte[] newBuffer = new byte[offset];
            System.arraycopy(buffer, 0, newBuffer, 0, offset);
            buffer = newBuffer;
        }
    }
    private Object[][] pool;
    public void put(Object key, Object entry) {
        if (pool == null) {
            pool = new Object[2][10];
        }
        int i = 0;
        for (; i < pool[0].length && pool[0][i] != null; i++) {
            if (pool[0][i] == key) {
                pool[1][i] = entry;
                return;
            }
        }
        if (i == pool[0].length) {
            Object[][] newPool = new Object[pool[0].length * 2][2];
            System.arraycopy(pool[0], 0, newPool[0], 0, pool[0].length);
            System.arraycopy(pool[1], 0, newPool[1], 0, pool[0].length);
            pool = newPool;
        } else {
            pool[0][i] = key;
            pool[1][i] = entry;
        }
    }
    public Object get(Object key) {
        if (pool == null) {
            return null;
        }
        for (int i = 0; i < pool[0].length; i++) {
            if (pool[0][i] == key) {
                return pool[1][i];
            }
        }
        return null;
    }
}
