public class ReadFully {
    public static final void main(String[] args) throws Exception {
        byte[] buffer = new byte[100];
        File file = new File(System.getProperty("test.src"),
                "ReadFully.java");
        FileInputStream in = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(in);
        boolean caughtException = false;
        try {
            dis.readFully(buffer, 0, -20);
        } catch (IndexOutOfBoundsException ie) {
            caughtException = true;
        } finally {
            dis.close();
            if (!caughtException)
                throw new RuntimeException("Test failed");
        }
    }
}
