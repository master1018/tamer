public class RmpTools {
    public static void writeValue(OutputStream stream, int value, int length) throws IOException {
        int i;
        for (i = 0; i < length; i++) {
            stream.write(value & 0xFF);
            value >>= 8;
        }
    }
    public static void writeValue(OutputStream stream, long value, int length) throws IOException {
        int i;
        for (i = 0; i < length; i++) {
            stream.write((int) (value & 0xFF));
            value >>= 8;
        }
    }
    public static void writeFixedString(OutputStream stream, String str, int length) throws IOException {
        int i;
        int value;
        for (i = 0; i < length; i++) {
            if (i < str.length()) value = str.charAt(i); else value = 0;
            stream.write(value);
        }
    }
    public static void writeDouble(OutputStream os, double value) throws IOException {
        ByteArrayOutputStream bo;
        DataOutputStream dos;
        byte[] b;
        byte help;
        bo = new ByteArrayOutputStream();
        dos = new DataOutputStream(bo);
        dos.writeDouble(value);
        dos.close();
        b = bo.toByteArray();
        for (int i = 0; i < 4; i++) {
            help = b[i];
            b[i] = b[7 - i];
            b[7 - i] = help;
        }
        os.write(b);
    }
    public static String buildImageName(String name) {
        int index;
        index = name.indexOf('.');
        if (index != -1) name = name.substring(0, index);
        if (name.length() > 8) name = name.substring(0, 8);
        return name.toLowerCase().trim();
    }
    public static String buildTileName(String basename, int index) {
        String indexstr;
        indexstr = String.valueOf(index);
        if (indexstr.length() + basename.length() > 8) basename = basename.substring(0, 8 - indexstr.length());
        return basename.trim() + indexstr;
    }
}
