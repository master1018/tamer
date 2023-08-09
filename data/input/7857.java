public final class CommandAPDU implements java.io.Serializable {
    private static final long serialVersionUID = 398698301286670877L;
    private static final int MAX_APDU_SIZE = 65544;
    private byte[] apdu;
    private transient int nc;
    private transient int ne;
    private transient int dataOffset;
    public CommandAPDU(byte[] apdu) {
        this.apdu = apdu.clone();
        parse();
    }
    public CommandAPDU(byte[] apdu, int apduOffset, int apduLength) {
        checkArrayBounds(apdu, apduOffset, apduLength);
        this.apdu = new byte[apduLength];
        System.arraycopy(apdu, apduOffset, this.apdu, 0, apduLength);
        parse();
    }
    private void checkArrayBounds(byte[] b, int ofs, int len) {
        if ((ofs < 0) || (len < 0)) {
            throw new IllegalArgumentException
                ("Offset and length must not be negative");
        }
        if (b == null) {
            if ((ofs != 0) && (len != 0)) {
                throw new IllegalArgumentException
                    ("offset and length must be 0 if array is null");
            }
        } else {
            if (ofs > b.length - len) {
                throw new IllegalArgumentException
                    ("Offset plus length exceed array size");
            }
        }
    }
    public CommandAPDU(ByteBuffer apdu) {
        this.apdu = new byte[apdu.remaining()];
        apdu.get(this.apdu);
        parse();
    }
    public CommandAPDU(int cla, int ins, int p1, int p2) {
        this(cla, ins, p1, p2, null, 0, 0, 0);
    }
    public CommandAPDU(int cla, int ins, int p1, int p2, int ne) {
        this(cla, ins, p1, p2, null, 0, 0, ne);
    }
    public CommandAPDU(int cla, int ins, int p1, int p2, byte[] data) {
        this(cla, ins, p1, p2, data, 0, arrayLength(data), 0);
    }
    public CommandAPDU(int cla, int ins, int p1, int p2, byte[] data,
            int dataOffset, int dataLength) {
        this(cla, ins, p1, p2, data, dataOffset, dataLength, 0);
    }
    public CommandAPDU(int cla, int ins, int p1, int p2, byte[] data, int ne) {
        this(cla, ins, p1, p2, data, 0, arrayLength(data), ne);
    }
    private static int arrayLength(byte[] b) {
        return (b != null) ? b.length : 0;
    }
    private void parse() {
        if (apdu.length < 4) {
            throw new IllegalArgumentException("apdu must be at least 4 bytes long");
        }
        if (apdu.length == 4) {
            return;
        }
        int l1 = apdu[4] & 0xff;
        if (apdu.length == 5) {
            this.ne = (l1 == 0) ? 256 : l1;
            return;
        }
        if (l1 != 0) {
            if (apdu.length == 4 + 1 + l1) {
                this.nc = l1;
                this.dataOffset = 5;
                return;
            } else if (apdu.length == 4 + 2 + l1) {
                this.nc = l1;
                this.dataOffset = 5;
                int l2 = apdu[apdu.length - 1] & 0xff;
                this.ne = (l2 == 0) ? 256 : l2;
                return;
            } else {
                throw new IllegalArgumentException
                    ("Invalid APDU: length=" + apdu.length + ", b1=" + l1);
            }
        }
        if (apdu.length < 7) {
            throw new IllegalArgumentException
                ("Invalid APDU: length=" + apdu.length + ", b1=" + l1);
        }
        int l2 = ((apdu[5] & 0xff) << 8) | (apdu[6] & 0xff);
        if (apdu.length == 7) {
            this.ne = (l2 == 0) ? 65536 : l2;
            return;
        }
        if (l2 == 0) {
            throw new IllegalArgumentException("Invalid APDU: length="
                    + apdu.length + ", b1=" + l1 + ", b2||b3=" + l2);
        }
        if (apdu.length == 4 + 3 + l2) {
            this.nc = l2;
            this.dataOffset = 7;
            return;
        } else if (apdu.length == 4 + 5 + l2) {
            this.nc = l2;
            this.dataOffset = 7;
            int leOfs = apdu.length - 2;
            int l3 = ((apdu[leOfs] & 0xff) << 8) | (apdu[leOfs + 1] & 0xff);
            this.ne = (l3 == 0) ? 65536 : l3;
        } else {
            throw new IllegalArgumentException("Invalid APDU: length="
                    + apdu.length + ", b1=" + l1 + ", b2||b3=" + l2);
        }
    }
    public CommandAPDU(int cla, int ins, int p1, int p2, byte[] data,
            int dataOffset, int dataLength, int ne) {
        checkArrayBounds(data, dataOffset, dataLength);
        if (dataLength > 65535) {
            throw new IllegalArgumentException("dataLength is too large");
        }
        if (ne < 0) {
            throw new IllegalArgumentException("ne must not be negative");
        }
        if (ne > 65536) {
            throw new IllegalArgumentException("ne is too large");
        }
        this.ne = ne;
        this.nc = dataLength;
        if (dataLength == 0) {
            if (ne == 0) {
                this.apdu = new byte[4];
                setHeader(cla, ins, p1, p2);
            } else {
                if (ne <= 256) {
                    byte len = (ne != 256) ? (byte)ne : 0;
                    this.apdu = new byte[5];
                    setHeader(cla, ins, p1, p2);
                    this.apdu[4] = len;
                } else {
                    byte l1, l2;
                    if (ne == 65536) {
                        l1 = 0;
                        l2 = 0;
                    } else {
                        l1 = (byte)(ne >> 8);
                        l2 = (byte)ne;
                    }
                    this.apdu = new byte[7];
                    setHeader(cla, ins, p1, p2);
                    this.apdu[5] = l1;
                    this.apdu[6] = l2;
                }
            }
        } else {
            if (ne == 0) {
                if (dataLength <= 255) {
                    apdu = new byte[4 + 1 + dataLength];
                    setHeader(cla, ins, p1, p2);
                    apdu[4] = (byte)dataLength;
                    this.dataOffset = 5;
                    System.arraycopy(data, dataOffset, apdu, 5, dataLength);
                } else {
                    apdu = new byte[4 + 3 + dataLength];
                    setHeader(cla, ins, p1, p2);
                    apdu[4] = 0;
                    apdu[5] = (byte)(dataLength >> 8);
                    apdu[6] = (byte)dataLength;
                    this.dataOffset = 7;
                    System.arraycopy(data, dataOffset, apdu, 7, dataLength);
                }
            } else {
                if ((dataLength <= 255) && (ne <= 256)) {
                    apdu = new byte[4 + 2 + dataLength];
                    setHeader(cla, ins, p1, p2);
                    apdu[4] = (byte)dataLength;
                    this.dataOffset = 5;
                    System.arraycopy(data, dataOffset, apdu, 5, dataLength);
                    apdu[apdu.length - 1] = (ne != 256) ? (byte)ne : 0;
                } else {
                    apdu = new byte[4 + 5 + dataLength];
                    setHeader(cla, ins, p1, p2);
                    apdu[4] = 0;
                    apdu[5] = (byte)(dataLength >> 8);
                    apdu[6] = (byte)dataLength;
                    this.dataOffset = 7;
                    System.arraycopy(data, dataOffset, apdu, 7, dataLength);
                    if (ne != 65536) {
                        int leOfs = apdu.length - 2;
                        apdu[leOfs] = (byte)(ne >> 8);
                        apdu[leOfs + 1] = (byte)ne;
                    } 
                }
            }
        }
    }
    private void setHeader(int cla, int ins, int p1, int p2) {
        apdu[0] = (byte)cla;
        apdu[1] = (byte)ins;
        apdu[2] = (byte)p1;
        apdu[3] = (byte)p2;
    }
    public int getCLA() {
        return apdu[0] & 0xff;
    }
    public int getINS() {
        return apdu[1] & 0xff;
    }
    public int getP1() {
        return apdu[2] & 0xff;
    }
    public int getP2() {
        return apdu[3] & 0xff;
    }
    public int getNc() {
        return nc;
    }
    public byte[] getData() {
        byte[] data = new byte[nc];
        System.arraycopy(apdu, dataOffset, data, 0, nc);
        return data;
    }
    public int getNe() {
        return ne;
    }
    public byte[] getBytes() {
        return apdu.clone();
    }
    public String toString() {
        return "CommmandAPDU: " + apdu.length + " bytes, nc=" + nc + ", ne=" + ne;
    }
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj instanceof CommandAPDU == false) {
            return false;
        }
        CommandAPDU other = (CommandAPDU)obj;
        return Arrays.equals(this.apdu, other.apdu);
     }
    public int hashCode() {
        return Arrays.hashCode(apdu);
    }
    private void readObject(java.io.ObjectInputStream in)
            throws java.io.IOException, ClassNotFoundException {
        apdu = (byte[])in.readUnshared();
        parse();
    }
}
