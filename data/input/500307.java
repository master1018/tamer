public class BerOutputStream {
    public byte[] encoded;
    protected int offset;
    public int length;
    public Object content;
    public BerOutputStream() {
    }
    public final void encodeTag(int tag) {
        encoded[offset++] = (byte) tag; 
        if (length > 127) { 
            int eLen = length >> 8;
            byte numOctets = 1;
            for (; eLen > 0; eLen = eLen >> 8) {
                numOctets++;
            }
            encoded[offset] = (byte) (numOctets | 0x80);
            offset++;
            eLen = length;
            int numOffset = offset + numOctets - 1;
            for (int i = 0; i < numOctets; i++, eLen = eLen >> 8) {
                encoded[numOffset - i] = (byte) eLen; 
            }
            offset += numOctets;
        } else { 
            encoded[offset++] = (byte) length;
        }
    }
    public void encodeANY() {
        System.arraycopy(content, 0, encoded, offset, length);
        offset += length;
    }
    public void encodeBitString() {
        BitString bStr = (BitString) content;
        encoded[offset] = (byte) bStr.unusedBits;
        System.arraycopy(bStr.bytes, 0, encoded, offset + 1, length - 1);
        offset += length;
    }
    public void encodeBoolean() {
        if (((Boolean) content).booleanValue()) {
            encoded[offset] = (byte) 0xFF;
        } else {
            encoded[offset] = 0x00;
        }
        offset++;
    }
    public void encodeChoice(ASN1Choice choice) {
        throw new RuntimeException("Is not implemented yet"); 
    }
    public void encodeExplicit(ASN1Explicit explicit) {
        throw new RuntimeException("Is not implemented yet"); 
    }
    public void encodeGeneralizedTime() {
        System.arraycopy(content, 0, encoded, offset, length);
        offset += length;
    }
    public void encodeUTCTime() {
        System.arraycopy(content, 0, encoded, offset, length);
        offset += length;
    }
    public void encodeInteger() {
        System.arraycopy(content, 0, encoded, offset, length);
        offset += length;
    }
    public void encodeOctetString() {
        System.arraycopy(content, 0, encoded, offset, length);
        offset += length;
    }
    public void encodeOID() {
        int[] oid = (int[]) content;
        int oidLen = length;
        int elem;
        for (int i = oid.length - 1; i > 1; i--, oidLen--) {
            elem = oid[i];
            if (elem > 127) {
                encoded[offset + oidLen - 1] = (byte) (elem & 0x7F);
                elem = elem >> 7;
                for (; elem > 0;) {
                    oidLen--;
                    encoded[offset + oidLen - 1] = (byte) (elem | 0x80);
                    elem = elem >> 7;
                }
            } else {
                encoded[offset + oidLen - 1] = (byte) elem;
            }
        }
        elem = oid[0] * 40 + oid[1];
        if (elem > 127) {
            encoded[offset + oidLen - 1] = (byte) (elem & 0x7F);
            elem = elem >> 7;
            for (; elem > 0;) {
                oidLen--;
                encoded[offset + oidLen - 1] = (byte) (elem | 0x80);
                elem = elem >> 7;
            }
        } else {
            encoded[offset + oidLen - 1] = (byte) elem;
        }
        offset += length;
    }
    public void encodeSequence(ASN1Sequence sequence) {
        throw new RuntimeException("Is not implemented yet"); 
    }
    public void encodeSequenceOf(ASN1SequenceOf sequenceOf) {
        throw new RuntimeException("Is not implemented yet"); 
    }
    public void encodeSet(ASN1Set set) {
        throw new RuntimeException("Is not implemented yet"); 
    }
    public void encodeSetOf(ASN1SetOf setOf) {
        throw new RuntimeException("Is not implemented yet"); 
    }
    public void encodeString() {
        System.arraycopy(content, 0, encoded, offset, length);
        offset += length;
    }
    public void getChoiceLength(ASN1Choice choice) {
        throw new RuntimeException("Is not implemented yet"); 
    }
    public void getExplicitLength(ASN1Explicit sequence) {
        throw new RuntimeException("Is not implemented yet"); 
    }
    public void getSequenceLength(ASN1Sequence sequence) {
        throw new RuntimeException("Is not implemented yet"); 
    }
    public void getSequenceOfLength(ASN1SequenceOf sequence) {
        throw new RuntimeException("Is not implemented yet"); 
    }
    public void getSetLength(ASN1Set set) {
        throw new RuntimeException("Is not implemented yet"); 
    }
    public void getSetOfLength(ASN1SetOf setOf) {
        throw new RuntimeException("Is not implemented yet"); 
    }
    public int getStringLength(Object object) {
        throw new RuntimeException("Is not implemented yet"); 
    }
}
