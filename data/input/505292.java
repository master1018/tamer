public class ASN1BitString extends ASN1StringType {
    private static final ASN1BitString ASN1 = new ASN1BitString();
    public ASN1BitString() {
        super(TAG_BITSTRING);
    }
    public static ASN1BitString getInstance() {
        return ASN1;
    }
    public Object decode(BerInputStream in) throws IOException {
        in.readBitString();
        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }
    public Object getDecodedObject(BerInputStream in) throws IOException {
        byte[] bytes = new byte[in.length - 1];
        System.arraycopy(in.buffer, in.contentOffset + 1, bytes, 0,
                in.length - 1);
        return new BitString(bytes, in.buffer[in.contentOffset]);
    }
    public void encodeContent(BerOutputStream out) {
        out.encodeBitString();
    }
    public void setEncodingContent(BerOutputStream out) {
        out.length = ((BitString) out.content).bytes.length + 1;
    }
    public static class ASN1NamedBitList extends ASN1BitString {
        private static final byte[] SET_MASK = { (byte) 128, 64, 32, 16, 8, 4,
                2, 1 };
        private static final BitString emptyString = new BitString(
                new byte[] {}, 0);
        private static final int INDEFINITE_SIZE = -1;
        private final int minBits;
        private final int maxBits;
        public ASN1NamedBitList() {
            this.minBits = INDEFINITE_SIZE;
            this.maxBits = INDEFINITE_SIZE;
        }
        public ASN1NamedBitList(int minBits) {
            this.minBits = minBits;
            this.maxBits = INDEFINITE_SIZE;
        }
        public ASN1NamedBitList(int minBits, int maxBits) {
            this.minBits = minBits;
            this.maxBits = maxBits;
        }
        public Object getDecodedObject(BerInputStream in) throws IOException {
            boolean[] value = null;
            int unusedBits = in.buffer[in.contentOffset];
            int bitsNumber = (in.length - 1) * 8 - unusedBits;
            if (maxBits == INDEFINITE_SIZE) {
                if (minBits == INDEFINITE_SIZE) {
                    value = new boolean[bitsNumber];
                } else {
                    if (bitsNumber > minBits) {
                        value = new boolean[bitsNumber];
                    } else {
                        value = new boolean[minBits];
                    }
                }
            } else {
                if (bitsNumber > maxBits) {
                    throw new ASN1Exception(
                            Messages.getString("security.97")); 
                }
                value = new boolean[maxBits];
            }
            if (bitsNumber == 0) {
                return value;
            }
            int i = 1;
            int j = 0;
            byte octet = in.buffer[in.contentOffset + i];
            for (int size = in.length - 1; i < size; i++) {
                for (int k = 0; k < 8; k++, j++) {
                    value[j] = (SET_MASK[k] & octet) != 0;
                }
                i++;
                octet = in.buffer[in.contentOffset + i];
            }
            for (int k = 0; k < (8 - unusedBits); k++, j++) {
                value[j] = (SET_MASK[k] & octet) != 0;
            }
            return value;
        }
        public void setEncodingContent(BerOutputStream out) {
            boolean[] toEncode = (boolean[]) out.content;
            int index = toEncode.length - 1;
            while (index > -1 && !toEncode[index]) {
                index--;
            }
            if (index == -1) {
                out.content = emptyString;
                out.length = 1;
            } else {
                int unusedBits = 7 - index % 8;
                byte[] bytes = new byte[index / 8 + 1];
                int j = 0;
                index = bytes.length - 1;
                for (int i = 0; i < index; i++) {
                    for (int k = 0; k < 8; k++, j++) {
                        if (toEncode[j]) {
                            bytes[i] = (byte) (bytes[i] | SET_MASK[k]);
                        }
                    }
                }
                for (int k = 0; k < (8 - unusedBits); k++, j++) {
                    if (toEncode[j]) {
                        bytes[index] = (byte) (bytes[index] | SET_MASK[k]);
                    }
                }
                out.content = new BitString(bytes, unusedBits);
                out.length = bytes.length + 1;
            }
        }
    }
}
