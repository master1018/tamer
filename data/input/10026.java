public class EOL {
    public static void main(String[] args) throws IOException {
        Reader sr = new StringReader("one\rtwo\r\nthree\nfour\r");
        BufferedReader br = new BufferedReader(sr);
        for (int i = 0;; i++) {
            String l = br.readLine();
            if (l == null) {
                if (i != 4)
                    throw new RuntimeException("Expected 4 lines, got " + i);
                break;
            }
            System.err.println(i + ": " + l);
        }
    }
}
