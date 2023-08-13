public class Test6814842 {
    static final short[] sa = new short[] { (short) 0xF1F2 };
    static final char[]  ca = new char[]  { (char) 0xF3F4  };
    static final int[]   ia = new int[]   { 0xF1F2F3F4     };
    public static void main(String[] args)
    {
        byte s2b = loadS2B(sa);
        if (s2b != (byte) 0xF2)
            throw new InternalError("loadS2B failed: " + s2b + " != " + (byte) 0xF2);
        byte s2bmask255 = loadS2Bmask255(sa);
        if (s2bmask255 != (byte) 0xF2)
            throw new InternalError("loadS2Bmask255 failed: " + s2bmask255 + " != " + (byte) 0xF2);
        byte us2b = loadUS2B(ca);
        if (us2b != (byte) 0xF4)
            throw new InternalError("loadUS2B failed: " + us2b + " != " + (byte) 0xF4);
        byte us2bmask255 = loadUS2Bmask255(ca);
        if (us2bmask255 != (byte) 0xF4)
            throw new InternalError("loadUS2Bmask255 failed: " + us2bmask255 + " != " + (byte) 0xF4);
        byte i2b = loadI2B(ia);
        if (i2b != (byte) 0xF4)
            throw new InternalError("loadI2B failed: " + i2b + " != " + (byte) 0xF4);
        byte i2bmask255 = loadI2Bmask255(ia);
        if (i2bmask255 != (byte) 0xF4)
            throw new InternalError("loadI2Bmask255 failed: " + i2bmask255 + " != " + (byte) 0xF4);
        short i2s = loadI2S(ia);
        if (i2s != (short) 0xF3F4)
            throw new InternalError("loadI2S failed: " + i2s + " != " + (short) 0xF3F4);
        short i2smask255 = loadI2Smask255(ia);
        if (i2smask255 != (short) 0xF4)
            throw new InternalError("loadI2Smask255 failed: " + i2smask255 + " != " + (short) 0xF4);
        short i2smask65535 = loadI2Smask65535(ia);
        if (i2smask65535 != (short) 0xF3F4)
            throw new InternalError("loadI2Smask65535 failed: " + i2smask65535 + " != " + (short) 0xF3F4);
        char i2us = loadI2US(ia);
        if (i2us != (char) 0xF3F4)
            throw new InternalError("loadI2US failed: " + (int) i2us + " != " + (char) 0xF3F4);
        char i2usmask255 = loadI2USmask255(ia);
        if (i2usmask255 != (char) 0xF4)
            throw new InternalError("loadI2USmask255 failed: " + (int) i2usmask255 + " != " + (char) 0xF4);
        char i2usmask65535 = loadI2USmask65535(ia);
        if (i2usmask65535 != (char) 0xF3F4)
            throw new InternalError("loadI2USmask65535 failed: " + (int) i2usmask65535 + " != " + (char) 0xF3F4);
    }
    static byte  loadS2B          (short[] sa) { return (byte)  (sa[0]         ); }
    static byte  loadS2Bmask255   (short[] sa) { return (byte)  (sa[0] & 0xFF  ); }
    static byte  loadUS2B         (char[]  ca) { return (byte)  (ca[0]         ); }
    static byte  loadUS2Bmask255  (char[]  ca) { return (byte)  (ca[0] & 0xFF  ); }
    static byte  loadI2B          (int[]   ia) { return (byte)  (ia[0]         ); }
    static byte  loadI2Bmask255   (int[]   ia) { return (byte)  (ia[0] & 0xFF  ); }
    static short loadI2S          (int[]   ia) { return (short) (ia[0]         ); }
    static short loadI2Smask255   (int[]   ia) { return (short) (ia[0] & 0xFF  ); }
    static short loadI2Smask65535 (int[]   ia) { return (short) (ia[0] & 0xFFFF); }
    static char  loadI2US         (int[]   ia) { return (char)  (ia[0]         ); }
    static char  loadI2USmask255  (int[]   ia) { return (char)  (ia[0] & 0xFF  ); }
    static char  loadI2USmask65535(int[]   ia) { return (char)  (ia[0] & 0xFFFF); }
}
