public class SnmpTools implements SnmpDefinitions {
    static public String binary2ascii(byte[] data, int length)
    {
        if(data == null) return null;
        final int size = (length * 2) + 2;
        byte[] asciiData = new byte[size];
        asciiData[0] = (byte) '0';
        asciiData[1] = (byte) 'x';
        for (int i=0; i < length; i++) {
            int j = i*2;
            int v = (data[i] & 0xf0);
            v = v >> 4;
            if (v < 10)
                asciiData[j+2] = (byte) ('0' + v);
            else
                asciiData[j+2] = (byte) ('A' + (v - 10));
            v = ((data[i] & 0xf));
            if (v < 10)
                asciiData[j+1+2] = (byte) ('0' + v);
            else
                asciiData[j+1+2] = (byte) ('A' + (v - 10));
        }
        return new String(asciiData);
    }
    static public String binary2ascii(byte[] data)
    {
        return binary2ascii(data, data.length);
    }
    static public byte[] ascii2binary(String str) {
        if(str == null) return null;
        String val = str.substring(2);
        int size = val.length();
        byte []buf = new byte[size/2];
        byte []p = val.getBytes();
        for(int i = 0; i < (size / 2); i++)
        {
            int j = i * 2;
            byte v = 0;
            if (p[j] >= '0' && p[j] <= '9') {
                v = (byte) ((p[j] - '0') << 4);
            }
            else if (p[j] >= 'a' && p[j] <= 'f') {
                v = (byte) ((p[j] - 'a' + 10) << 4);
            }
            else if (p[j] >= 'A' && p[j] <= 'F') {
                v = (byte) ((p[j] - 'A' + 10) << 4);
            }
            else
                throw new Error("BAD format :" + str);
            if (p[j+1] >= '0' && p[j+1] <= '9') {
                v += (p[j+1] - '0');
            }
            else if (p[j+1] >= 'a' && p[j+1] <= 'f') {
                v += (p[j+1] - 'a' + 10);
            }
            else if (p[j+1] >= 'A' && p[j+1] <= 'F') {
                v += (p[j+1] - 'A' + 10);
            }
            else
                throw new Error("BAD format :" + str);
            buf[i] = v;
        }
        return buf;
    }
}
