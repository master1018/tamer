class ObjectIdentifier implements Serializable
{
    private byte[] encoding = null;
    private transient volatile String stringForm;
    private static final long serialVersionUID = 8697030238860181294L;
    private Object      components   = null;          
    private int         componentLen = -1;            
    transient private boolean   componentsCalculated = false;
    private void readObject(ObjectInputStream is)
            throws IOException, ClassNotFoundException {
        is.defaultReadObject();
        if (encoding == null) {  
            init((int[])components, componentLen);
        }
    }
    private void writeObject(ObjectOutputStream os)
            throws IOException {
        if (!componentsCalculated) {
            int[] comps = toIntArray();
            if (comps != null) {    
                components = comps;
                componentLen = comps.length;
            } else {
                components = HugeOidNotSupportedByOldJDK.theOne;
            }
            componentsCalculated = true;
        }
        os.defaultWriteObject();
    }
    static class HugeOidNotSupportedByOldJDK implements Serializable {
        private static final long serialVersionUID = 1L;
        static HugeOidNotSupportedByOldJDK theOne = new HugeOidNotSupportedByOldJDK();
    }
    public ObjectIdentifier (String oid) throws IOException
    {
        int ch = '.';
        int start = 0;
        int end = 0;
        int pos = 0;
        byte[] tmp = new byte[oid.length()];
        int first = 0, second;
        int count = 0;
        try {
            String comp = null;
            do {
                int length = 0; 
                end = oid.indexOf(ch,start);
                if (end == -1) {
                    comp = oid.substring(start);
                    length = oid.length() - start;
                } else {
                    comp = oid.substring(start,end);
                    length = end - start;
                }
                if (length > 9) {
                    BigInteger bignum = new BigInteger(comp);
                    if (count == 0) {
                        checkFirstComponent(bignum);
                        first = bignum.intValue();
                    } else {
                        if (count == 1) {
                            checkSecondComponent(first, bignum);
                            bignum = bignum.add(BigInteger.valueOf(40*first));
                        } else {
                            checkOtherComponent(count, bignum);
                        }
                        pos += pack7Oid(bignum, tmp, pos);
                    }
                } else {
                    int num = Integer.parseInt(comp);
                    if (count == 0) {
                        checkFirstComponent(num);
                        first = num;
                    } else {
                        if (count == 1) {
                            checkSecondComponent(first, num);
                            num += 40 * first;
                        } else {
                            checkOtherComponent(count, num);
                        }
                        pos += pack7Oid(num, tmp, pos);
                    }
                }
                start = end + 1;
                count++;
            } while (end != -1);
            checkCount(count);
            encoding = new byte[pos];
            System.arraycopy(tmp, 0, encoding, 0, pos);
            this.stringForm = oid;
        } catch (IOException ioe) { 
            throw ioe;
        } catch (Exception e) {
            throw new IOException("ObjectIdentifier() -- Invalid format: "
                    + e.toString(), e);
        }
    }
    public ObjectIdentifier (int values []) throws IOException
    {
        checkCount(values.length);
        checkFirstComponent(values[0]);
        checkSecondComponent(values[0], values[1]);
        for (int i=2; i<values.length; i++)
            checkOtherComponent(i, values[i]);
        init(values, values.length);
    }
    public ObjectIdentifier (DerInputStream in) throws IOException
    {
        byte    type_id;
        int     bufferEnd;
        type_id = (byte) in.getByte ();
        if (type_id != DerValue.tag_ObjectId)
            throw new IOException (
                "ObjectIdentifier() -- data isn't an object ID"
                + " (tag = " +  type_id + ")"
                );
        encoding = new byte[in.getLength()];
        in.getBytes(encoding);
        check(encoding);
    }
    ObjectIdentifier (DerInputBuffer buf) throws IOException
    {
        DerInputStream in = new DerInputStream(buf);
        encoding = new byte[in.available()];
        in.getBytes(encoding);
        check(encoding);
    }
    private void init(int[] components, int length) {
        int pos = 0;
        byte[] tmp = new byte[length*5+1];  
        if (components[1] < Integer.MAX_VALUE - components[0]*40)
            pos += pack7Oid(components[0]*40+components[1], tmp, pos);
        else {
            BigInteger big = BigInteger.valueOf(components[1]);
            big = big.add(BigInteger.valueOf(components[0]*40));
            pos += pack7Oid(big, tmp, pos);
        }
        for (int i=2; i<length; i++) {
            pos += pack7Oid(components[i], tmp, pos);
        }
        encoding = new byte[pos];
        System.arraycopy(tmp, 0, encoding, 0, pos);
    }
    public static ObjectIdentifier newInternal(int[] values) {
        try {
            return new ObjectIdentifier(values);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    void encode (DerOutputStream out) throws IOException
    {
        out.write (DerValue.tag_ObjectId, encoding);
    }
    @Deprecated
    public boolean equals(ObjectIdentifier other) {
        return equals((Object)other);
    }
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ObjectIdentifier == false) {
            return false;
        }
        ObjectIdentifier other = (ObjectIdentifier)obj;
        return Arrays.equals(encoding, other.encoding);
    }
    @Override
    public int hashCode() {
        return Arrays.hashCode(encoding);
    }
    private int[] toIntArray() {
        int length = encoding.length;
        int[] result = new int[20];
        int which = 0;
        int fromPos = 0;
        for (int i = 0; i < length; i++) {
            if ((encoding[i] & 0x80) == 0) {
                if (i - fromPos + 1 > 4) {
                    BigInteger big = new BigInteger(pack(encoding, fromPos, i-fromPos+1, 7, 8));
                    if (fromPos == 0) {
                        result[which++] = 2;
                        BigInteger second = big.subtract(BigInteger.valueOf(80));
                        if (second.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
                            return null;
                        } else {
                            result[which++] = second.intValue();
                        }
                    } else {
                        if (big.compareTo(BigInteger.valueOf(Integer.MAX_VALUE)) == 1) {
                            return null;
                        } else {
                            result[which++] = big.intValue();
                        }
                    }
                } else {
                    int retval = 0;
                    for (int j = fromPos; j <= i; j++) {
                        retval <<= 7;
                        byte tmp = encoding[j];
                        retval |= (tmp & 0x07f);
                    }
                    if (fromPos == 0) {
                        if (retval < 80) {
                            result[which++] = retval / 40;
                            result[which++] = retval % 40;
                        } else {
                            result[which++] = 2;
                            result[which++] = retval - 80;
                        }
                    } else {
                        result[which++] = retval;
                    }
                }
                fromPos = i+1;
            }
            if (which >= result.length) {
                result = Arrays.copyOf(result, which + 10);
            }
        }
        return Arrays.copyOf(result, which);
    }
    @Override
    public String toString() {
        String s = stringForm;
        if (s == null) {
            int length = encoding.length;
            StringBuffer sb = new StringBuffer(length * 4);
            int fromPos = 0;
            for (int i = 0; i < length; i++) {
                if ((encoding[i] & 0x80) == 0) {
                    if (fromPos != 0) {  
                        sb.append('.');
                    }
                    if (i - fromPos + 1 > 4) { 
                        BigInteger big = new BigInteger(pack(encoding, fromPos, i-fromPos+1, 7, 8));
                        if (fromPos == 0) {
                            sb.append("2.");
                            sb.append(big.subtract(BigInteger.valueOf(80)));
                        } else {
                            sb.append(big);
                        }
                    } else { 
                        int retval = 0;
                        for (int j = fromPos; j <= i; j++) {
                            retval <<= 7;
                            byte tmp = encoding[j];
                            retval |= (tmp & 0x07f);
                        }
                        if (fromPos == 0) {
                            if (retval < 80) {
                                sb.append(retval/40);
                                sb.append('.');
                                sb.append(retval%40);
                            } else {
                                sb.append("2.");
                                sb.append(retval - 80);
                            }
                        } else {
                            sb.append(retval);
                        }
                    }
                    fromPos = i+1;
                }
            }
            s = sb.toString();
            stringForm = s;
        }
        return s;
    }
    private static byte[] pack(byte[] in, int ioffset, int ilength, int iw, int ow) {
        assert (iw > 0 && iw <= 8): "input NUB must be between 1 and 8";
        assert (ow > 0 && ow <= 8): "output NUB must be between 1 and 8";
        if (iw == ow) {
            return in.clone();
        }
        int bits = ilength * iw;    
        byte[] out = new byte[(bits+ow-1)/ow];
        int ipos = 0;
        int opos = (bits+ow-1)/ow*ow-bits;
        while(ipos < bits) {
            int count = iw - ipos%iw;   
            if (count > ow - opos%ow) { 
                count = ow - opos%ow;   
            }
            out[opos/ow] |=                         
                (((in[ioffset+ipos/iw]+256)         
                    >> (iw-ipos%iw-count))          
                        & ((1 << (count))-1))       
                            << (ow-opos%ow-count);  
            ipos += count;  
            opos += count;  
        }
        return out;
    }
    private static int pack7Oid(byte[] in, int ioffset, int ilength, byte[] out, int ooffset) {
        byte[] pack = pack(in, ioffset, ilength, 8, 7);
        int firstNonZero = pack.length-1;   
        for (int i=pack.length-2; i>=0; i--) {
            if (pack[i] != 0) {
                firstNonZero = i;
            }
            pack[i] |= 0x80;
        }
        System.arraycopy(pack, firstNonZero, out, ooffset, pack.length-firstNonZero);
        return pack.length-firstNonZero;
    }
    private static int pack8(byte[] in, int ioffset, int ilength, byte[] out, int ooffset) {
        byte[] pack = pack(in, ioffset, ilength, 7, 8);
        int firstNonZero = pack.length-1;   
        for (int i=pack.length-2; i>=0; i--) {
            if (pack[i] != 0) {
                firstNonZero = i;
            }
        }
        System.arraycopy(pack, firstNonZero, out, ooffset, pack.length-firstNonZero);
        return pack.length-firstNonZero;
    }
    private static int pack7Oid(int input, byte[] out, int ooffset) {
        byte[] b = new byte[4];
        b[0] = (byte)(input >> 24);
        b[1] = (byte)(input >> 16);
        b[2] = (byte)(input >> 8);
        b[3] = (byte)(input);
        return pack7Oid(b, 0, 4, out, ooffset);
    }
    private static int pack7Oid(BigInteger input, byte[] out, int ooffset) {
        byte[] b = input.toByteArray();
        return pack7Oid(b, 0, b.length, out, ooffset);
    }
    private static void check(byte[] encoding) throws IOException {
        int length = encoding.length;
        if (length < 1 ||      
                (encoding[length - 1] & 0x80) != 0) {  
            throw new IOException("ObjectIdentifier() -- " +
                    "Invalid DER encoding, not ended");
        }
        for (int i=0; i<length; i++) {
            if (encoding[i] == (byte)0x80 &&
                    (i==0 || (encoding[i-1] & 0x80) == 0)) {
                throw new IOException("ObjectIdentifier() -- " +
                        "Invalid DER encoding, useless extra octet detected");
            }
        }
    }
    private static void checkCount(int count) throws IOException {
        if (count < 2) {
            throw new IOException("ObjectIdentifier() -- " +
                    "Must be at least two oid components ");
        }
    }
    private static void checkFirstComponent(int first) throws IOException {
        if (first < 0 || first > 2) {
            throw new IOException("ObjectIdentifier() -- " +
                    "First oid component is invalid ");
        }
    }
    private static void checkFirstComponent(BigInteger first) throws IOException {
        if (first.signum() == -1 ||
                first.compareTo(BigInteger.valueOf(2)) == 1) {
            throw new IOException("ObjectIdentifier() -- " +
                    "First oid component is invalid ");
        }
    }
    private static void checkSecondComponent(int first, int second) throws IOException {
        if (second < 0 || first != 2 && second > 39) {
            throw new IOException("ObjectIdentifier() -- " +
                    "Second oid component is invalid ");
        }
    }
    private static void checkSecondComponent(int first, BigInteger second) throws IOException {
        if (second.signum() == -1 ||
                first != 2 &&
                second.compareTo(BigInteger.valueOf(39)) == 1) {
            throw new IOException("ObjectIdentifier() -- " +
                    "Second oid component is invalid ");
        }
    }
    private static void checkOtherComponent(int i, int num) throws IOException {
        if (num < 0) {
            throw new IOException("ObjectIdentifier() -- " +
                    "oid component #" + (i+1) + " must be non-negative ");
        }
    }
    private static void checkOtherComponent(int i, BigInteger num) throws IOException {
        if (num.signum() == -1) {
            throw new IOException("ObjectIdentifier() -- " +
                    "oid component #" + (i+1) + " must be non-negative ");
        }
    }
}
