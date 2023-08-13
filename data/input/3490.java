class N2AFilter extends FilterWriter {
    public N2AFilter(Writer out) { super(out); }
    public void write(char b) throws IOException {
        char[] buf = new char[1];
        buf[0] = b;
        write(buf, 0, 1);
    }
    public void write(char[] buf, int off, int len) throws IOException {
        String lineBreak = System.getProperty("line.separator");
        for (int i = 0; i < len; i++) {
            if ((buf[i] > '\u007f')) {
                out.write('\\');
                out.write('u');
                String hex =
                    Integer.toHexString(buf[i]);
                StringBuffer hex4 = new StringBuffer(hex);
                hex4.reverse();
                int length = 4 - hex4.length();
                for (int j = 0; j < length; j++) {
                    hex4.append('0');
                }
                for (int j = 0; j < 4; j++) {
                    out.write(hex4.charAt(3 - j));
                }
            } else
                out.write(buf[i]);
        }
    }
}
