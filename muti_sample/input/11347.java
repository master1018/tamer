public class bug6768387 {
    private static void createGui() {
        JTable table = new JTable();
        OutputStream os;
        ObjectOutputStream out;
        try {
            os = new ByteArrayOutputStream();
            out = new ObjectOutputStream(os);
            out.writeObject(table);
            out.close();
        }
        catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
    public static void main(String[] args) throws Exception {
        SwingUtilities.invokeAndWait(new Runnable() {
            public void run() {
                bug6768387.createGui();
            }
        });
    }
}
