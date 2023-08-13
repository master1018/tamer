public class ASN1Oid extends ASN1Primitive {
    private static final ASN1Oid ASN1 = new ASN1Oid();
    public ASN1Oid() {
        super(TAG_OID);
    }
    public static ASN1Oid getInstance() {
        return ASN1;
    }
    public Object decode(BerInputStream in) throws IOException {
        in.readOID();
        if (in.isVerify) {
            return null;
        }
        return getDecodedObject(in);
    }
    public Object getDecodedObject(BerInputStream in) throws IOException {
        int oidElement = in.oidElement;
        int[] oid = new int[oidElement];
        for (int id = 1, i = 0; id < oid.length; id++, i++) {
            int octet = in.buffer[in.contentOffset + i];
            oidElement = octet & 0x7F;
            while ((octet & 0x80) != 0) {
                i++;
                octet = in.buffer[in.contentOffset + i];
                oidElement = oidElement << 7 | (octet & 0x7f);
            }
            oid[id] = oidElement;
        }
        if (oid[1] > 79) {
            oid[0] = 2;
            oid[1] = oid[1] - 80;
        } else {
            oid[0] = oid[1] / 40;
            oid[1] = oid[1] % 40;
        }
        return oid;
    }
    public void encodeContent(BerOutputStream out) {
        out.encodeOID();
    }
    public void setEncodingContent(BerOutputStream out) {
        int[] oid = (int[]) out.content;
        int length = 0;
        int elem = oid[0] * 40 + oid[1];
        if (elem == 0) {
            length = 1;
        } else {
            for (; elem > 0; elem = elem >> 7) {
                length++;
            }
        }
        for (int i = 2; i < oid.length; i++) {
            if (oid[i] == 0) {
                length++;
                continue;
            }
            for (elem = oid[i]; elem > 0; elem = elem >> 7) {
                length++;
            }
        }
        out.length = length;
    }
    private final static ASN1Oid STRING_OID = new ASN1Oid() {
        public Object getDecodedObject(BerInputStream in) throws IOException {
            StringBuilder buf = new StringBuilder();
            int element;
            int octet = in.buffer[in.contentOffset];
            element = octet & 0x7F;
            int index = 0;
            while ((octet & 0x80) != 0) {
                index++;
                octet = in.buffer[in.contentOffset + index];
                element = element << 7 | (octet & 0x7F);
            }
            if (element > 79) {
                buf.append('2');
                buf.append('.');
                buf.append(element - 80);
            } else {
                buf.append(element / 40);
                buf.append('.');
                buf.append(element % 40);
            }
            for (int j = 2; j < in.oidElement; j++) {
                buf.append('.');
                index++;
                octet = in.buffer[in.contentOffset + index];
                element = octet & 0x7F;
                while ((octet & 0x80) != 0) {
                    index++;
                    octet = in.buffer[in.contentOffset + index];
                    element = element << 7 | (octet & 0x7f);
                }
                buf.append(element);
            }
            return buf.toString();
        }
        public void setEncodingContent(BerOutputStream out) {
            out.content = ObjectIdentifier.toIntArray((String) out.content);
            super.setEncodingContent(out);
        }
    };
    public static ASN1Oid getInstanceForString() {
        return STRING_OID;
    }
}
