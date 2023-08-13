public class DeleteFont {
    public static void main(String args[]) throws Exception {
        String font = "A.ttf";
        String sep = System.getProperty("file.separator");
        String testSrc = System.getenv("TESTSRC");
        if (testSrc != null) {
            font = testSrc + sep + font;
        }
        System.out.println("Using font file: " + font);
        FileInputStream fis = new FileInputStream(font);
        Font f = Font.createFont(Font.TRUETYPE_FONT, fis);
        f.toString();
        f.deriveFont(Font.BOLD);
        f.canDisplay('X');
       InputStream in = new InputStream() {
            public int read() {
                throw new RuntimeException();
            }
        };
        boolean gotException = false;
        try {
           Font.createFont(java.awt.Font.TRUETYPE_FONT, in);
        } catch (IOException e) {
            gotException = true;
        }
        if (!gotException) {
            throw new RuntimeException("No expected IOException");
        }
        badRead(-2, Font.TRUETYPE_FONT);
        badRead(8193, Font.TRUETYPE_FONT);
        badRead(-2, Font.TYPE1_FONT);
        badRead(8193, Font.TYPE1_FONT);
        System.gc(); System.gc();
    }
    static void badRead(final int retval, int fontType) {
        int num = 2;
        byte[] buff = new byte[16*8192]; 
        for (int ct=0; ct<num; ++ct) {
            try {
                Font.createFont(
                    fontType,
                    new ByteArrayInputStream(buff) {
                        @Override
                        public int read(byte[] buff, int off, int len) {
                            int read = super.read(buff, off, len);
                            return read<0 ? retval : read;
                        }
                    }
                );
            } catch (Throwable exc) {
            }
        }
    }
}
