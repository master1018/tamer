public class ReadLine {
    public static void main(String args[]) throws Exception {
        File fn = new File("x.ReadLine");
        RandomAccessFile raf = new RandomAccessFile(fn,"rw");
        try {
            String line;
            int ctr = 1;
            String expected;
            raf.writeBytes
                ("ln1\rln2\r\nln3\nln4\rln5\r\nln6\n\rln8\r\rln10\n\nln12\r\r\nln14");
            raf.seek(0);
            while ((line=raf.readLine()) != null) {
                if ((ctr == 7) || (ctr == 9) ||
                    (ctr == 11) || (ctr == 13)) {
                     expected = "";
                } else {
                    expected = "ln" + ctr;
                }
                if (!line.equals(expected)) {
                    throw new Exception("Expected \"" + expected + "\"" +
                                        ", read \"" + line + "\"");
                }
                ctr++;
            }
        } finally {
            raf.close();
        }
        System.err.println("Successfully completed test!");
    }
}
