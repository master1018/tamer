public class ClearErrorStream extends PrintStream {
   public ClearErrorStream(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
   }
    public static void main(String[] args) throws Exception {
        File f = new File(System.getProperty("test.dir", "."),
                          "print-stream.out");
        f.deleteOnExit();
        ClearErrorStream out = new ClearErrorStream(
                                new BufferedOutputStream(
                                new FileOutputStream(f)),
                                true);
        out.println("Hello World!");
        out.close();
        out.println("Writing after close");
        if (out.checkError()) {
            System.out.println("An error occured");
            out.clearError();
            if (!out.checkError()) {
                System.out.println("Error status cleared");
            } else {
                throw new Exception("Error Status unchanged");
            }
         }
         else {
             System.out.println(" No error occured");
         }
    }
}
