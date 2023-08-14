public final class ResponseAPDU implements java.io.Serializable {
    private static final long serialVersionUID = 6962744978375594225L;
    private byte[] apdu;
    public ResponseAPDU(byte[] apdu) {
        apdu = apdu.clone();
        check(apdu);
        this.apdu = apdu;
    }
    private static void check(byte[] apdu) {
        if (apdu.length < 2) {
            throw new IllegalArgumentException("apdu must be at least 2 bytes long");
        }
    }
    public int getNr() {
        return apdu.length - 2;
    }
    public byte[] getData() {
        byte[] data = new byte[apdu.length - 2];
        System.arraycopy(apdu, 0, data, 0, data.length);
        return data;
    }
    public int getSW1() {
        return apdu[apdu.length - 2] & 0xff;
    }
    public int getSW2() {
        return apdu[apdu.length - 1] & 0xff;
    }
    public int getSW() {
        return (getSW1() << 8) | getSW2();
    }
    public byte[] getBytes() {
        return apdu.clone();
    }
    public String toString() {
        return "ResponseAPDU: " + apdu.length + " bytes, SW="
            + Integer.toHexString(getSW());
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof ResponseAPDU == false) {
            return false;
        }
        ResponseAPDU other = (ResponseAPDU)obj;
        return Arrays.equals(this.apdu, other.apdu);
    }
    public int hashCode() {
        return Arrays.hashCode(apdu);
    }
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        apdu = (byte[])in.readUnshared();
        check(apdu);
    }
}
