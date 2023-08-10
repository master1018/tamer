public class TestLog {
    private String name;
    private ByteArrayOutputStream outputStream;
    private PrintStream printStream;
    private String terminator;
    public TestLog(String name, String terminator) {
        this.name = name;
        this.terminator = terminator;
        openLog();
    }
    public void writeLine(String string) {
        if (printStream != null) {
            printStream.println(string + terminator);
            try {
                RandomAccessFile raf = new RandomAccessFile(name, "rw");
                raf.seek(raf.length());
                raf.write(outputStream.toByteArray());
                raf.close();
                outputStream.reset();
            } catch (Exception e) {
                System.out.println("Exception writing to " + name + ".writeLine():" + e);
            }
        }
    }
    public void enablePrivileges() {
        return;
    }
    public void closeLog() {
        if (printStream != null) {
            printStream.close();
        }
    }
    public void openLog() {
        enablePrivileges();
        this.outputStream = new ByteArrayOutputStream();
        this.printStream = new PrintStream(this.outputStream);
    }
    public String toString() {
        return this.name;
    }
}
