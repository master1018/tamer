public class BigMark {
    public static void main(String[] args) throws IOException {
        String line;
        int i = 0;
        String dir = System.getProperty("test.src", ".");
        BufferedReader br
            = new BufferedReader(new FileReader(new File(dir, "BigMark.java")), 100);
        br.mark(200);
        line = br.readLine();
        System.err.println(i + ": " + line);
        i++;
        try {
            br.mark(Integer.MAX_VALUE);
            line = br.readLine();
        } catch (OutOfMemoryError x) {
            x.printStackTrace();
            throw x;
        }
        System.out.println("OutOfMemoryError not thrown as expected");
    }
}
