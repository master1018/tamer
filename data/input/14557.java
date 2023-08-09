public class MalformedSurrogates {
    public static void main(String[] args) throws Exception {
        String fe = System.getProperty("file.encoding");
        if (  fe.equalsIgnoreCase("UTF8")
              || fe.equalsIgnoreCase("UTF-8")
              || fe.equalsIgnoreCase("UTF_8"))
            return;
        System.out.println("Testing string conversion...");
        String t = "abc\uD800\uDB00efgh";
        String t2 = t.substring(2);
        byte[] b = t2.getBytes();
        System.err.println(b.length);
        for (int i = 0; i < b.length; i++)
            System.err.println("[" + i + "]" + "=" + (char) b[i]
                               + "=" + (int) b[i]);
        if (b.length != 7) {
            throw new Exception("Bad string conversion for bad surrogate");
        }
        String t3 = "abc\uD800\uDC00efgh";
        byte[] b2 = t3.getBytes();
        System.out.println(b2.length);
        for(int i = 0; i < b2.length; i++)
            System.err.println("[" + i + "]" + "=" + (char) b2[i]);
        if (b2.length != 8) {
            throw new Exception("Bad string conversion for good surrogate");
        }
        OutputStream os = new ByteArrayOutputStream();
        OutputStreamWriter osw = new OutputStreamWriter(os);
        System.out.println("Testing flush....");
        osw.flush();
        String s = "abc\uD800"; 
        char[] c = s.toCharArray();
        osw.write(s, 0, 4);
        osw.flush();
        System.out.println("Testing convert...");
        for (int k = 1; k < 65535 ; k++) {
            osw.write("Char[" + k + "]=\"" + ((char) k) + "\"");
        }
    }
}
