public class EncodingConstructor {
    public static void main(String args[]) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        String s = "xyzzy";
        int n = s.length();
        try (PrintStream ps = new PrintStream(bo, false, "UTF-8")) {
            ps.print(s);
        }
        byte[] ba = bo.toByteArray();
        if (ba.length != n)
            throw new Exception("Length mismatch: " + n + " " + ba.length);
        for (int i = 0; i < n; i++) {
            if (ba[i] != (byte)s.charAt(i))
                throw new Exception("Content mismatch: "
                                    + i + " "
                                    + Integer.toString(ba[i]) + " "
                                    + Integer.toString(s.charAt(i)));
        }
    }
}
