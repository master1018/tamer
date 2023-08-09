public class ClearErrorWriter extends PrintWriter {
    public ClearErrorWriter(Writer w, boolean autoFlush) {
        super(w, autoFlush);
    }
    public static void main(String[] args) throws Exception {
        File f = new File(System.getProperty("test.dir", "."),
                          "print-writer.out");
        f.deleteOnExit();
        ClearErrorWriter out = new ClearErrorWriter(new BufferedWriter(
                                new FileWriter(f)),
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
