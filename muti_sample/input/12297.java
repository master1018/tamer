public class TelnetTest {
    public static void main(String[] args) throws Exception {
        ByteArrayOutputStream bo = new ByteArrayOutputStream();
        TelnetOutputStream out = new TelnetOutputStream(bo, false);
        out.setStickyCRLF(true);
        out.write("Hello world!\r\nGoodbye World\r ".getBytes());
        out.flush();
        byte[] b = bo.toByteArray();
        if (b.length != 30 ||
            b[12] != '\r' || b[13] != '\n' ||
            b[27] != '\r' || b[28] != 0)
            throw new RuntimeException("Wrong output for TelnetOutputStream!");
    }
}
