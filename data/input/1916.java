public class OpenSync {
    static PrintStream log = System.err;
    public static void main(String[] args) throws Exception {
        File blah = File.createTempFile("OpenSync", null);
        blah.deleteOnExit();
        String[] badModes = { "d", "s", "rd", "rs", "rwx", "foo" };
        for (int i = 0; i < badModes.length; i++) {
            String mode = badModes[i];
            try {
                new RandomAccessFile(blah, mode);
            } catch (IllegalArgumentException x) {
                log.println("Mode \"" + mode +"\": Thrown as expected: "
                            + x.getClass().getName());
                log.println("  " + x.getMessage());
                continue;
            }
            throw new Exception("Exception not thrown for illegal mode "
                                + mode);
        }
        new RandomAccessFile(blah, "rw").close();
        new RandomAccessFile(blah, "r").close();
        String hi = "Hello, world!";
        RandomAccessFile raf = new RandomAccessFile(blah, "rws");
        raf.writeUTF(hi);
        raf.close();
        raf = new RandomAccessFile(blah, "rwd");
        if (!raf.readUTF().equals(hi))
            throw new Exception("File content mismatch");
        raf.close();
    }
}
