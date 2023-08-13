public class GrowAfterEOF {
    public static void main(String[] args) throws Exception {
        File input = new File(".", "TestEOFInput.txt");
        RandomAccessFile rf = new RandomAccessFile(input, "rw");
        try {
            BufferedReader r = new BufferedReader
                (new InputStreamReader(new FileInputStream(input)));
            try {
                rf.writeBytes("a line");
                while (r.readLine() != null);
                rf.seek(rf.length());
                rf.writeBytes("new line");
                boolean readMore = false;
                while (r.readLine() != null) {
                    readMore = true;
                }
                if (!readMore) {
                    input.delete();
                    throw new Exception("Failed test: unable to read!");
                } else {
                    input.delete();
                }
            } finally {
                r.close();
            }
        } finally {
            rf.close();
        }
    }
}
