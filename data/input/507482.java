public final class ClassDumper
        extends BaseDumper {
    public static void dump(byte[] bytes, PrintStream out,
                            String filePath, Args args) {
        ClassDumper cd =
            new ClassDumper(bytes, out, filePath, args);
        cd.dump();
    }
    private ClassDumper(byte[] bytes, PrintStream out,
                        String filePath, Args args) {
        super(bytes, out, filePath, args);
    }
    public void dump() {
        byte[] bytes = getBytes();
        ByteArray ba = new ByteArray(bytes);
        DirectClassFile cf =
            new DirectClassFile(ba, getFilePath(), getStrictParse());
        cf.setAttributeFactory(StdAttributeFactory.THE_ONE);
        cf.setObserver(this);
        cf.getMagic(); 
        int at = getAt();
        if (at != bytes.length) {
            parsed(ba, at, bytes.length - at, "<extra data at end of file>");
        }
    }
}
