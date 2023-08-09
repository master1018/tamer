public class WriteBytesChars {
    public static void main(String args[]) throws Exception {
        String towrite;
        char[] buf = new char[80];
        byte[] b = new byte[80];
        File fn = new File("x.WriteBytesChars");
        RandomAccessFile raf = new RandomAccessFile(fn , "rw");;
        try {
            for (int i = 0; i < 80; i++) {
                buf[i] = 'a';
            }
            towrite = new String(buf);
            raf.writeBytes(towrite);
            raf.seek(0);
            raf.read(b);
            System.out.println("RandomAccessFile.writeBytes");
            if (towrite.equals(new String(b))) {
                System.err.println("Test succeeded.");
            } else {
                throw new
                    RuntimeException("RandomAccessFile.writeBytes, wrong result");
            }
            raf.seek(0);
            raf.writeChars(towrite);
            raf.seek(0);
            for (int i = 0; i < 80; i++) {
                buf[i] = raf.readChar();
            }
            System.out.println("RandomAccessFile.writeChars");
            if (towrite.equals(new String(buf))) {
                System.err.println("Test succeeded.");
            } else {
                throw new
                    RuntimeException("RandomAccessFile.writeChars, wrong result");
            }
        } finally {
            raf.close();
            fn.delete();
        }
    }
}
