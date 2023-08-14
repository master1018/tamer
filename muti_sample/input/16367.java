public final class BigInt {
    private byte[]      places;
    public BigInt(byte[] data) { places = data.clone(); }
    public BigInt(BigInteger i) {
        byte[]  temp = i.toByteArray();
        if ((temp[0] & 0x80) != 0)
            throw new IllegalArgumentException("negative BigInteger");
        if (temp[0] != 0)
            places = temp;
        else {
            places = new byte[temp.length - 1];
            for (int j = 1; j < temp.length; j++)
                places[j - 1] = temp[j];
        }
    }
    public BigInt(int i) {
        if (i < (1 << 8)) {
            places = new byte[1];
            places[0] = (byte) i;
        } else if (i < (1 << 16)) {
            places = new byte[2];
            places[0] = (byte) (i >> 8);
            places[1] = (byte) i;
        } else if (i < (1 << 24)) {
            places = new byte[3];
            places[0] = (byte) (i >> 16);
            places[1] = (byte) (i >> 8);
            places[2] = (byte) i;
        } else {
            places = new byte[4];
            places[0] = (byte) (i >> 24);
            places[1] = (byte) (i >> 16);
            places[2] = (byte) (i >> 8);
            places[3] = (byte) i;
        }
    }
    public int toInt() {
        if (places.length > 4)
            throw new NumberFormatException("BigInt.toLong, too big");
        int retval = 0, i = 0;
        for (; i < places.length; i++)
            retval = (retval << 8) + ((int)places[i] & 0xff);
        return retval;
    }
    public String toString() { return hexify(); }
    public BigInteger toBigInteger()
        { return new BigInteger(1, places); }
    public byte[] toByteArray() { return places.clone(); }
    private static final String digits = "0123456789abcdef";
    private String hexify() {
        if (places.length == 0)
            return "  0  ";
        StringBuffer buf = new StringBuffer(places.length * 2);
        buf.append("    ");     
        for (int i = 0; i < places.length; i++) {
            buf.append(digits.charAt((places[i] >> 4) & 0x0f));
            buf.append(digits.charAt(places[i] & 0x0f));
            if (((i + 1) % 32) == 0) {
                if ((i +  1) != places.length)
                    buf.append("\n    ");       
            } else if (((i + 1) % 4) == 0)
                buf.append(' ');                
        }
        return buf.toString();
    }
    public boolean equals(Object other) {
        if (other instanceof BigInt)
            return equals((BigInt) other);
        return false;
    }
    public boolean equals(BigInt other) {
        if (this == other)
            return true;
        byte[] otherPlaces = other.toByteArray();
        if (places.length != otherPlaces.length)
            return false;
        for (int i = 0; i < places.length; i++)
            if (places[i] != otherPlaces[i])
                return false;
        return true;
    }
    public int hashCode() {
        return hexify().hashCode();
    }
}
